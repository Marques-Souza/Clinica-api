package com.marquesdev.clinica.service;

import com.marquesdev.clinica.ResourceUrlBuilder.ResourceUrlBuilder;
import com.marquesdev.clinica.dto.AdminPasswordResetAuditResponseDto;
import com.marquesdev.clinica.dto.PasswordRequestDto;
import com.marquesdev.clinica.dto.PasswordResetRequestDto;
import com.marquesdev.clinica.dto.UserRequestDto;
import com.marquesdev.clinica.dto.UserResponseDto;
import com.marquesdev.clinica.entity.AdminPasswordResetAudit;
import com.marquesdev.clinica.entity.User;
import com.marquesdev.clinica.enums.Perfil;
import com.marquesdev.clinica.exception.EmailAlreadyExistsException;
import com.marquesdev.clinica.exception.EntityNotFoundException;
import com.marquesdev.clinica.exception.PasswordInvalidException;
import com.marquesdev.clinica.mapper.UserMapper;
import com.marquesdev.clinica.repository.AdminPasswordResetAuditRepository;
import com.marquesdev.clinica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${app.api.endpoints.users}")
    private String userEndpoint;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ResourceUrlBuilder resourceUrlBuilder;
    private final PasswordEncoder passwordEncoder;
    private final AdminPasswordResetAuditRepository adminPasswordResetAuditRepository;

    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto,Perfil perfil) {
       User user = userMapper.toUser(userRequestDto);

       try {
           user.setPerfil(perfil);
           user.setPassword(passwordEncoder.encode(user.getPassword()));
           User savedUser = userRepository.saveAndFlush(user);
           return toResponse(savedUser);
       }catch (org.springframework.dao.DataIntegrityViolationException ex){
           throw new EmailAlreadyExistsException("Email already exists.");
       }
    }


    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return getAllUsers(pageable, null, null);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable, String search, Perfil perfil) {
        String emailQuery = search == null || search.isBlank() ? null : search.trim();
        Page<User> users;

        if (emailQuery == null) {
            users = perfil == null
                    ? userRepository.findAll(pageable)
                    : userRepository.findByPerfil(perfil, pageable);
        } else if (perfil == null) {
            users = userRepository.findByEmailContainingIgnoreCase(emailQuery, pageable);
        } else {
            users = userRepository.findByEmailContainingIgnoreCaseAndPerfil(emailQuery, perfil, pageable);
        }

        return users
                .map(this::toResponse);
    }

    @Transactional
    public UserResponseDto updatePassword(
            UUID id,
            PasswordRequestDto passwordRequestDto,
            UUID actorUserId
    ) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id:" + id));

        UserResponseDto response = updatePassword(existingUser, passwordRequestDto);
        if (existingUser.getPerfil() == Perfil.DOCTOR && !existingUser.getId().equals(actorUserId)) {
            recordPasswordReset(actorUserId, existingUser.getId());
        }
        return response;
    }

    @Transactional
    public UserResponseDto updatePassword(String email, PasswordRequestDto passwordRequestDto) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        return updatePassword(existingUser, passwordRequestDto);
    }

    private UserResponseDto updatePassword(User existingUser, PasswordRequestDto passwordRequestDto) {
        if (!passwordRequestDto.newPassword().equals(passwordRequestDto.confirmPassword())){
            throw new PasswordInvalidException("New password and confirmation do not match.");
        }

        if (!passwordEncoder.matches(passwordRequestDto.currentPassword(), existingUser.getPassword())){
            throw new PasswordInvalidException("Current password is incorrect.");
        }
        existingUser.setPassword(passwordEncoder.encode(passwordRequestDto.newPassword()));
        return toResponse(existingUser);
    }

    @Transactional
    public UserResponseDto resetDoctorPassword(
            UUID id,
            PasswordResetRequestDto passwordResetRequestDto,
            UUID actorUserId
    ) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id:" + id));

        if (!passwordResetRequestDto.newPassword().equals(passwordResetRequestDto.confirmPassword())) {
            throw new PasswordInvalidException("New password and confirmation do not match.");
        }

        existingUser.setPassword(passwordEncoder.encode(passwordResetRequestDto.newPassword()));
        recordPasswordReset(actorUserId, id);
        return toResponse(existingUser);
    }

    private void recordPasswordReset(UUID actorUserId, UUID targetUserId) {
        adminPasswordResetAuditRepository.save(new AdminPasswordResetAudit(actorUserId, targetUserId));
    }

    @Transactional(readOnly = true)
    public Page<AdminPasswordResetAuditResponseDto> getPasswordResetAudit(Pageable pageable) {
        Page<AdminPasswordResetAudit> audits =
                adminPasswordResetAuditRepository.findAllByOrderByCreatedAtDesc(pageable);
        Map<UUID, User> usersById = userRepository.findAllById(
                        audits.getContent().stream()
                                .flatMap(audit -> Stream.of(
                                        audit.getActorUserId(),
                                        audit.getTargetUserId()
                                ))
                                .distinct()
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return audits.map(audit -> new AdminPasswordResetAuditResponseDto(
                audit.getId(),
                maskEmail(usersById.get(audit.getActorUserId())),
                maskEmail(usersById.get(audit.getTargetUserId())),
                audit.getCreatedAt()
        ));
    }

    private String maskEmail(User user) {
        if (user == null || user.getEmail() == null) {
            return "Conta removida";
        }

        String email = user.getEmail();
        int atIndex = email.lastIndexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return "***";
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        int lastDotIndex = domain.lastIndexOf('.');
        String domainName = lastDotIndex > 0 ? domain.substring(0, lastDotIndex) : domain;
        String suffix = lastDotIndex > 0 ? domain.substring(lastDotIndex) : "";

        return localPart.charAt(0) + "***@" + domainName.charAt(0) + "***" + suffix;
    }

    private UserResponseDto toResponse(User user){
        String url = resourceUrlBuilder.build(
                userEndpoint,
                user.getId()
        );
        return userMapper.toDto(user, url);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    public Perfil findPerfilByEmail(String email) {
        return userRepository.findPerfilByEmail(email);
    }
}
