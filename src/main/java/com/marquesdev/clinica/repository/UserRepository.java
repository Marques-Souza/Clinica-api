package com.marquesdev.clinica.repository;

import com.marquesdev.clinica.entity.User;
import com.marquesdev.clinica.enums.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("select u.perfil from User u where u.email like :email")
    Perfil findPerfilByEmail(String email);
}
