package com.marquesdev.clinica.repository;

import com.marquesdev.clinica.entity.SpecialtyConfig;
import com.marquesdev.clinica.enums.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpecialtyConfigRepository extends JpaRepository<SpecialtyConfig, UUID> {

    boolean existsBySpecialty(Specialty specialty);
    Optional<SpecialtyConfig> findBySpecialty(Specialty specialty);
}
