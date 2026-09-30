package com.marquesdev.clinica.repository;

import com.marquesdev.clinica.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
