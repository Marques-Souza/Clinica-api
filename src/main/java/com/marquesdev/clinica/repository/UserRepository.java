package com.marquesdev.clinica.repository;

import com.marquesdev.clinica.entity.User;
import com.marquesdev.clinica.enums.Perfil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<User> findByPerfil(Perfil perfil, Pageable pageable);
    Page<User> findByEmailContainingIgnoreCaseAndPerfil(String email, Perfil perfil, Pageable pageable);
    long countByPerfil(Perfil perfil);

    @Query("select u.perfil from User u where u.email like :email")
    Perfil findPerfilByEmail(String email);
}
