package com.marquesdev.clinica.entity;


import com.marquesdev.clinica.enums.Specialty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "specialty_config")
@Getter
@NoArgsConstructor
public class SpecialtyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Enumerated(EnumType.STRING)
    @Column(name = "specialty", nullable = false, unique = true)
    private Specialty specialty;


    @Column(name = "consultation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee;


    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialtyConfig that = (SpecialtyConfig) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SpecialtyConfig{" +
                "id=" + id +
                '}';
    }
}
