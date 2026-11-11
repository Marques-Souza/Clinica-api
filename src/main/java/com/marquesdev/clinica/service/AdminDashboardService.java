package com.marquesdev.clinica.service;

import com.marquesdev.clinica.dto.AdminDashboardSummaryDto;
import com.marquesdev.clinica.enums.AppointmentStatus;
import com.marquesdev.clinica.enums.Perfil;
import com.marquesdev.clinica.repository.AppointmentScheduledAtProjection;
import com.marquesdev.clinica.repository.AppointmentRepository;
import com.marquesdev.clinica.repository.DoctorRepository;
import com.marquesdev.clinica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final ZoneId CLINIC_TIME_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final long MAX_RANGE_DAYS = 366;

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AdminDashboardSummaryDto getSummary(LocalDate requestedStartDate, LocalDate requestedEndDate) {
        LocalDate today = LocalDate.now(CLINIC_TIME_ZONE);
        LocalDate startDate = requestedStartDate;
        LocalDate endDate = requestedEndDate;

        if (startDate == null && endDate == null) {
            startDate = today.minusDays(6);
            endDate = today;
        } else if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid start and end date are required.");
        }

        long rangeDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (rangeDays > MAX_RANGE_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date range cannot exceed 366 days.");
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        Map<LocalDate, Long> countsByDay = new LinkedHashMap<>();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            countsByDay.put(day, 0L);
        }

        List<AppointmentScheduledAtProjection> appointments =
                appointmentRepository.findDashboardAppointments(start, end, AppointmentStatus.CANCELLED);
        for (AppointmentScheduledAtProjection appointment : appointments) {
            LocalDate appointmentDate = appointment.getScheduledAt().toLocalDate();
            countsByDay.computeIfPresent(appointmentDate, (date, count) -> count + 1);
        }

        List<AdminDashboardSummaryDto.DailyAppointmentCount> dailyAppointments = countsByDay.entrySet()
                .stream()
                .map(entry -> new AdminDashboardSummaryDto.DailyAppointmentCount(entry.getKey(), entry.getValue()))
                .toList();

        return new AdminDashboardSummaryDto(
                countsByDay.get(today),
                doctorRepository.countByActiveTrue(),
                userRepository.countByPerfil(Perfil.USER),
                dailyAppointments
        );
    }
}
