package com.marquesdev.clinica.repository;

import com.marquesdev.clinica.entity.Appointment;
import com.marquesdev.clinica.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.id =  :doctorId
        AND a.status <> :cancelledStatus
        AND a.scheduledAt < :newEnd
        AND (:excludedAppointmentId IS NULL OR a.id <> :excludedAppointmentId)
""")
    List<Appointment> findPotentialConflicts(
           @Param("doctorId") UUID doctorId,
           @Param("newEnd") LocalDateTime newEnd,
           @Param("cancelledStatus") AppointmentStatus cancelledStatus,
           @Param("excludedAppointmentId") UUID excludedAppointmentId
    );


    Page<Appointment> findByPatient_User_Email(String email, Pageable pageable);
    Page<Appointment> findByDoctor_User_Email(String email, Pageable pageable);
    List<Appointment> findAllByPatient_User_EmailOrderByScheduledAtDesc(String email);
    List<Appointment> findAllByOrderByScheduledAtDesc();
    List<Appointment> findAllByDoctor_CrmOrderByScheduledAtDesc(String crm);
    Optional<Appointment> findByAppointmentCode(String code);

    boolean existsByAppointmentCode(String appointmentCode);

    @Query("""
            SELECT a.scheduledAt AS scheduledAt
            FROM Appointment a
            WHERE a.scheduledAt >= :start
              AND a.scheduledAt < :end
              AND a.status <> :excludedStatus
            """)
    List<AppointmentScheduledAtProjection> findDashboardAppointments(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludedStatus") AppointmentStatus excludedStatus
    );
}
