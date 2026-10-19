package com.marquesdev.clinica.service;

import com.marquesdev.clinica.dto.AppointmentRequestDto;
import com.marquesdev.clinica.dto.AppointmentResponseDto;
import com.marquesdev.clinica.dto.AppointmentUpdateRequestDto;
import com.marquesdev.clinica.entity.Appointment;
import com.marquesdev.clinica.entity.Client;
import com.marquesdev.clinica.entity.Doctor;
import com.marquesdev.clinica.entity.SpecialtyConfig;
import com.marquesdev.clinica.enums.AppointmentStatus;
import com.marquesdev.clinica.enums.PaymentStatus;
import com.marquesdev.clinica.exception.EntityNotFoundException;
import com.marquesdev.clinica.exception.ScheduleConflictException;
import com.marquesdev.clinica.mapper.AppointmentMapper;
import com.marquesdev.clinica.repository.AppointmentRepository;
import com.marquesdev.clinica.repository.ClientRepository;
import com.marquesdev.clinica.repository.DoctorRepository;
import com.marquesdev.clinica.repository.SpecialtyConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final ClientRepository clientRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyConfigRepository specialtyConfigRepository;

    @Transactional
    public AppointmentResponseDto createAppointment(
            String email,
            AppointmentRequestDto appointmentRequestDto){

        Client client = clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        SpecialtyConfig specialtyConfig = specialtyConfigRepository.findBySpecialty(appointmentRequestDto.specialty())
                .orElseThrow(() -> new EntityNotFoundException("Specialty not found"));

        List<Doctor> availableDoctors = doctorRepository.findBySpecialtyAndActiveTrue(appointmentRequestDto.specialty());

        if (availableDoctors.isEmpty()){
            throw new EntityNotFoundException("No active doctors available for this specialty");
        }

        Doctor availableDoctor = findAvailableDoctor(availableDoctors, appointmentRequestDto.scheduledAt(), specialtyConfig.getDurationMinutes())
                .orElseThrow(() -> new ScheduleConflictException("No doctors available for the selected time"));

        Appointment appointment = appointmentMapper.toAppointment(appointmentRequestDto);
        appointment.assignPatient(client);
        appointment.assignDoctor(availableDoctor);
        appointment.setConsultationFee(specialtyConfig.getConsultationFee());
        appointment.setDurationMinutes(specialtyConfig.getDurationMinutes());
        appointment.setPaymentStatus(PaymentStatus.PENDING);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setAppointmentCode(
                generateAppointmentCode(appointmentRequestDto.scheduledAt())
        );
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return appointmentMapper.toDto(savedAppointment);
    }


    @Transactional
    public AppointmentResponseDto updateAppointment(String email,UUID id, AppointmentUpdateRequestDto appointmentUpdateRequestDto){
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        if (!appointment.getPatient().getUser().getEmail().equals(email)){
            throw new EntityNotFoundException("You can only update your own appointments");
        }

        appointmentMapper.updateAppointment(appointmentUpdateRequestDto, appointment);

        if (appointmentUpdateRequestDto.status() == AppointmentStatus.CANCELLED){
            appointment.setPaymentStatus(PaymentStatus.CANCELLED);
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return appointmentMapper.toDto(savedAppointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDto> findAll(String email,Pageable pageable){
        Page<Appointment> appointments = appointmentRepository.findByPatient_User_Email(email,pageable);
        return appointments.map(appointmentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDto findByCode(String code){
        Appointment appointment = appointmentRepository.findByAppointmentCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));
        return appointmentMapper.toDto(appointment);
    }


    private boolean hasScheduleConflict(
            UUID doctorId,
            LocalDateTime newStart,
            Integer newDurationMinutes
    ){
      LocalDateTime newEnd = newStart.plusMinutes(newDurationMinutes);

      return appointmentRepository
              .findPotentialConflicts(doctorId, newEnd, AppointmentStatus.CANCELLED)
              .stream()
              .anyMatch(existing ->
                      existing.getScheduledAt()
                              .plusMinutes(existing.getDurationMinutes())
                              .isAfter(newStart)
              );

    }


    private String generateAppointmentCode(LocalDateTime scheduletAt){
       String date = scheduletAt.format(DateTimeFormatter.BASIC_ISO_DATE);
       String randomSuffix = UUID.randomUUID()
               .toString()
               .replace("-", "")
               .substring(0,8)
               .toLowerCase(Locale.ROOT);
       return date + "-" + randomSuffix;
    }

    private Optional<Doctor> findAvailableDoctor(List<Doctor> doctors, LocalDateTime scheduledAt, Integer durationMinutes){
        return doctors.stream()
                .filter(doctor -> !hasScheduleConflict(doctor.getId(), scheduledAt, durationMinutes))
                .findFirst();
    }



}
