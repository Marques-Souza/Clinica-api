package com.marquesdev.clinica.service;

import com.marquesdev.clinica.dto.AppointmentAvailabilityResponseDto;
import com.marquesdev.clinica.dto.AppointmentRequestDto;
import com.marquesdev.clinica.dto.AppointmentResponseDto;
import com.marquesdev.clinica.dto.AppointmentUpdateRequestDto;
import com.marquesdev.clinica.entity.Appointment;
import com.marquesdev.clinica.entity.Client;
import com.marquesdev.clinica.entity.Doctor;
import com.marquesdev.clinica.entity.SpecialtyConfig;
import com.marquesdev.clinica.enums.AppointmentStatus;
import com.marquesdev.clinica.enums.PaymentStatus;
import com.marquesdev.clinica.enums.Specialty;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final LocalTime MORNING_OPEN = LocalTime.of(8, 0);
    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_OPEN = LocalTime.of(13, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);

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

        Integer durationMinutes = specialtyConfig.getDurationMinutes();
        if (!isValidBusinessSlot(appointmentRequestDto.scheduledAt(), durationMinutes)) {
            throw new ScheduleConflictException("Selected time is outside the clinic schedule");
        }

        Doctor availableDoctor = findAvailableDoctor(
                        availableDoctors,
                        appointmentRequestDto.scheduledAt(),
                        durationMinutes,
                        null)
                .orElseThrow(() -> new ScheduleConflictException("No doctors available for the selected time"));

        Appointment appointment = appointmentMapper.toAppointment(appointmentRequestDto);
        appointment.assignPatient(client);
        appointment.assignDoctor(availableDoctor);
        appointment.setConsultationFee(specialtyConfig.getConsultationFee());
        appointment.setDurationMinutes(durationMinutes);
        appointment.setPaymentStatus(PaymentStatus.PENDING);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setAppointmentCode(
                generateAppointmentCode(appointmentRequestDto.scheduledAt())
        );
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return appointmentMapper.toDto(savedAppointment);
    }

    @Transactional(readOnly = true)
    public AppointmentAvailabilityResponseDto getAvailableSlots(
            Specialty specialty,
            LocalDate date
    ) {
        SpecialtyConfig specialtyConfig = specialtyConfigRepository.findBySpecialty(specialty)
                .orElseThrow(() -> new EntityNotFoundException("Specialty not found"));
        Integer durationMinutes = specialtyConfig.getDurationMinutes();
        List<Doctor> availableDoctors = doctorRepository.findBySpecialtyAndActiveTrue(specialty);
        return createAvailability(
                date,
                durationMinutes,
                specialtyConfig.getConsultationFee(),
                availableDoctors,
                null
        );
    }

    @Transactional(readOnly = true)
    public AppointmentAvailabilityResponseDto getRescheduleSlots(
            String email,
            UUID appointmentId,
            LocalDate date
    ) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        if (!appointment.getPatient().getUser().getEmail().equals(email)) {
            throw new EntityNotFoundException("You can only reschedule your own appointments");
        }
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ScheduleConflictException("Only scheduled appointments can be rescheduled");
        }

        return createAvailability(
                date,
                appointment.getDurationMinutes(),
                appointment.getConsultationFee(),
                List.of(appointment.getDoctor()),
                appointment.getId()
        );
    }

    private AppointmentAvailabilityResponseDto createAvailability(
            LocalDate date,
            Integer durationMinutes,
            BigDecimal consultationFee,
            List<Doctor> availableDoctors,
            UUID excludedAppointmentId
    ) {
        List<String> availableSlots = new ArrayList<>();
        if (availableDoctors.isEmpty()
                || date.getDayOfWeek().getValue() > 5
                || date.isBefore(LocalDate.now())) {
            return new AppointmentAvailabilityResponseDto(
                    durationMinutes,
                    consultationFee,
                    availableSlots
            );
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (LocalTime slot : getCandidateSlots(durationMinutes)) {
            LocalDateTime scheduledAt = date.atTime(slot);
            if (scheduledAt.isAfter(now)
                    && findAvailableDoctor(
                            availableDoctors,
                            scheduledAt,
                            durationMinutes,
                            excludedAppointmentId
                    ).isPresent()) {
                availableSlots.add(slot.format(timeFormatter));
            }
        }

        return new AppointmentAvailabilityResponseDto(durationMinutes, consultationFee, availableSlots);
    }


    @Transactional
    public AppointmentResponseDto updateAppointment(String email,UUID id, AppointmentUpdateRequestDto appointmentUpdateRequestDto){
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        if (!appointment.getPatient().getUser().getEmail().equals(email)){
            throw new EntityNotFoundException("You can only update your own appointments");
        }

        AppointmentStatus targetStatus = appointmentUpdateRequestDto.status() == null
                ? appointment.getStatus()
                : appointmentUpdateRequestDto.status();
        LocalDateTime targetScheduledAt = appointmentUpdateRequestDto.scheduledAt() == null
                ? appointment.getScheduledAt()
                : appointmentUpdateRequestDto.scheduledAt();

        if (appointmentUpdateRequestDto.scheduledAt() != null
                && targetStatus != AppointmentStatus.SCHEDULED) {
            throw new ScheduleConflictException("Only scheduled appointments can be rescheduled");
        }
        if (targetStatus == AppointmentStatus.SCHEDULED
                && (appointmentUpdateRequestDto.scheduledAt() != null
                || appointment.getStatus() != AppointmentStatus.SCHEDULED)) {
            if (!isValidBusinessSlot(targetScheduledAt, appointment.getDurationMinutes())
                    || hasScheduleConflict(
                            appointment.getDoctor().getId(),
                            targetScheduledAt,
                            appointment.getDurationMinutes(),
                            appointment.getId())) {
                throw new ScheduleConflictException("No doctor available for the selected time");
            }
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

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDto> findAllForDoctor(String email, Pageable pageable){
        return appointmentRepository.findByDoctor_User_Email(email, pageable)
                .map(appointmentMapper::toDto);
    }


    private boolean hasScheduleConflict(
            UUID doctorId,
            LocalDateTime newStart,
            Integer newDurationMinutes,
            UUID excludedAppointmentId
    ){
      LocalDateTime newEnd = newStart.plusMinutes(newDurationMinutes);

      return appointmentRepository
              .findPotentialConflicts(
                      doctorId,
                      newEnd,
                      AppointmentStatus.CANCELLED,
                      excludedAppointmentId)
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

    private Optional<Doctor> findAvailableDoctor(
            List<Doctor> doctors,
            LocalDateTime scheduledAt,
            Integer durationMinutes,
            UUID excludedAppointmentId
    ){
        return doctors.stream()
                .filter(doctor -> !hasScheduleConflict(
                        doctor.getId(), scheduledAt, durationMinutes, excludedAppointmentId))
                .findFirst();
    }

    private boolean isValidBusinessSlot(LocalDateTime scheduledAt, Integer durationMinutes) {
        if (scheduledAt == null
                || durationMinutes == null
                || durationMinutes <= 0
                || scheduledAt.getSecond() != 0
                || scheduledAt.getNano() != 0
                || scheduledAt.getDayOfWeek().getValue() > 5) {
            return false;
        }

        LocalTime time = scheduledAt.toLocalTime();
        return isValidSlotInPeriod(time, MORNING_OPEN, LUNCH_START, durationMinutes)
                || isValidSlotInPeriod(time, AFTERNOON_OPEN, CLOSING_TIME, durationMinutes);
    }

    private boolean isValidSlotInPeriod(
            LocalTime slot,
            LocalTime openingTime,
            LocalTime closingTime,
            Integer durationMinutes
    ) {
        long offsetFromOpening = ChronoUnit.MINUTES.between(openingTime, slot);
        long remainingMinutes = ChronoUnit.MINUTES.between(slot, closingTime);
        return offsetFromOpening >= 0
                && offsetFromOpening % durationMinutes == 0
                && remainingMinutes >= durationMinutes;
    }

    private List<LocalTime> getCandidateSlots(Integer durationMinutes) {
        List<LocalTime> slots = new ArrayList<>();
        addCandidateSlots(MORNING_OPEN, LUNCH_START, durationMinutes, slots);
        addCandidateSlots(AFTERNOON_OPEN, CLOSING_TIME, durationMinutes, slots);
        return slots;
    }

    private void addCandidateSlots(
            LocalTime openingTime,
            LocalTime closingTime,
            Integer durationMinutes,
            List<LocalTime> slots
    ) {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalStateException("Specialty duration must be greater than zero");
        }

        LocalTime slot = openingTime;
        while (ChronoUnit.MINUTES.between(slot, closingTime) >= durationMinutes) {
            slots.add(slot);
            slot = slot.plusMinutes(durationMinutes);
        }
    }



}
