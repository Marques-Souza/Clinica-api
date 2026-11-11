package com.marquesdev.clinica.dto;

import java.time.LocalDate;
import java.util.List;

public record AdminDashboardSummaryDto(
        long appointmentsToday,
        long activeDoctors,
        long registeredPatients,
        List<DailyAppointmentCount> dailyAppointments
) {
    public record DailyAppointmentCount(LocalDate date, long count) {
    }
}
