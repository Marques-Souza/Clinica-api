package com.marquesdev.clinica.service;

import com.marquesdev.clinica.dto.PasswordRecoveryConfirmDto;
import com.marquesdev.clinica.entity.PasswordResetCode;
import com.marquesdev.clinica.entity.User;
import com.marquesdev.clinica.enums.Perfil;
import com.marquesdev.clinica.exception.PasswordInvalidException;
import com.marquesdev.clinica.exception.PasswordRecoveryDeliveryException;
import com.marquesdev.clinica.repository.PasswordResetCodeRepository;
import com.marquesdev.clinica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration REQUEST_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.password-recovery.from:no-reply@clinica.local}")
    private String fromAddress;

    @Transactional
    public void requestCode(String requestedEmail) {
        Optional<User> userResult = userRepository.findByEmailIgnoreCase(requestedEmail.trim());
        if (userResult.isEmpty() || userResult.get().getPerfil() != Perfil.USER) {
            return;
        }

        User user = userResult.get();
        LocalDateTime now = LocalDateTime.now();
        Optional<PasswordResetCode> existingCode = passwordResetCodeRepository.findByUser_Id(user.getId());
        if (existingCode.isPresent()
                && existingCode.get().getCreatedAt().isAfter(now.minus(REQUEST_COOLDOWN))) {
            return;
        }

        String code = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
        String codeHash = passwordEncoder.encode(code);
        PasswordResetCode resetCode;

        if (existingCode.isPresent()) {
            resetCode = existingCode.get();
            resetCode.renew(codeHash, now, now.plus(CODE_TTL));
        } else {
            resetCode = new PasswordResetCode(user, codeHash, now, now.plus(CODE_TTL));
        }

        passwordResetCodeRepository.save(resetCode);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("Código para redefinir sua senha");
        message.setText("Seu código de recuperação é " + code + ". Ele expira em 10 minutos.");

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send password recovery email for user id {}", user.getId(), ex);
            throw new PasswordRecoveryDeliveryException(ex);
        }
    }

    @Transactional
    public boolean confirmCode(PasswordRecoveryConfirmDto request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new PasswordInvalidException("New password and confirmation do not match.");
        }

        Optional<User> userResult = userRepository.findByEmailIgnoreCase(request.email().trim());
        if (userResult.isEmpty() || userResult.get().getPerfil() != Perfil.USER) {
            return false;
        }

        User user = userResult.get();
        Optional<PasswordResetCode> resetCodeResult =
                passwordResetCodeRepository.findByUser_Id(user.getId());
        if (resetCodeResult.isEmpty()) {
            return false;
        }

        PasswordResetCode resetCode = resetCodeResult.get();
        if (!resetCode.getExpiresAt().isAfter(LocalDateTime.now())
                || resetCode.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            passwordResetCodeRepository.delete(resetCode);
            return false;
        }

        if (!passwordEncoder.matches(request.code(), resetCode.getCodeHash())) {
            resetCode.recordFailedAttempt();
            if (resetCode.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                passwordResetCodeRepository.delete(resetCode);
            }
            return false;
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        passwordResetCodeRepository.delete(resetCode);
        return true;
    }
}
