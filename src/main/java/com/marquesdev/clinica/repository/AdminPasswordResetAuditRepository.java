package com.marquesdev.clinica.repository;

import com.marquesdev.clinica.entity.AdminPasswordResetAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminPasswordResetAuditRepository extends JpaRepository<AdminPasswordResetAudit, UUID> {

    Page<AdminPasswordResetAudit> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
