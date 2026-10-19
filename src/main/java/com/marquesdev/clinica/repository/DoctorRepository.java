package com.marquesdev.clinica.repository;

import com.marquesdev.clinica.entity.Doctor;
import com.marquesdev.clinica.enums.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByUserEmail(String email);
    boolean existsByUser_Id(UUID id);
    List<Doctor> findBySpecialtyAndActiveTrue(Specialty specialty);
}
