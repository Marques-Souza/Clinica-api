package com.marquesdev.clinica.mapper;

import com.marquesdev.clinica.dto.AppointmentRequestDto;
import com.marquesdev.clinica.dto.AppointmentResponseDto;
import com.marquesdev.clinica.dto.AppointmentUpdateRequestDto;
import com.marquesdev.clinica.dto.DoctorDto;
import com.marquesdev.clinica.dto.PatientDto;
import com.marquesdev.clinica.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AppointmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "consultationFee", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Appointment toAppointment(AppointmentRequestDto dto);

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "consultationFee", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "appointmentCode", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void updateAppointment(
            AppointmentUpdateRequestDto dto,
            @MappingTarget Appointment appointment
    );

    @Mapping(target = "patient", expression = "java(new PatientDto(appointment.getPatient().getId(), appointment.getPatient().getFullName()))")
    @Mapping(target = "doctor", expression = "java(new DoctorDto(appointment.getDoctor().getId(), appointment.getDoctor().getFullName(), appointment.getDoctor().getSpecialty()))")
    AppointmentResponseDto toDto(Appointment appointment);
}