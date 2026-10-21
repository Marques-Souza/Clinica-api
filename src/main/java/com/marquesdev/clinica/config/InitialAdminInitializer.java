package com.marquesdev.clinica.config;

import com.marquesdev.clinica.entity.User;
import com.marquesdev.clinica.enums.Perfil;
import com.marquesdev.clinica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitialAdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.initial-admin.email:}")
    private String adminEmail;

    @Value("${app.initial-admin.password:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        boolean emailConfigured = !adminEmail.isBlank();
        boolean passwordConfigured = !adminPassword.isBlank();

        if (!emailConfigured && !passwordConfigured) {
            return;
        }
        if (!emailConfigured || !passwordConfigured) {
            throw new IllegalStateException("Both INITIAL_ADMIN_EMAIL and INITIAL_ADMIN_PASSWORD must be configured.");
        }
        if (adminPassword.length() < 12) {
            throw new IllegalStateException("INITIAL_ADMIN_PASSWORD must contain at least 12 characters.");
        }

        userRepository.findByEmail(adminEmail).ifPresentOrElse(user -> {
            if (user.getPerfil() != Perfil.ADMIN) {
                throw new IllegalStateException("The configured initial admin email belongs to a non-admin user.");
            }
        }, () -> {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setPerfil(Perfil.ADMIN);
            userRepository.save(admin);
        });
    }
}
