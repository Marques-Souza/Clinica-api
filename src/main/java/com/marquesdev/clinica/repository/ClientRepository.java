package com.marquesdev.clinica.repository;

import com.marquesdev.clinica.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByUserEmail(String email);
}
