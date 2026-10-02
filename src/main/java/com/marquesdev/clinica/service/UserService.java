package com.marquesdev.clinica.service;

import com.marquesdev.clinica.ResourceUrlBuilder.ResourceUrlBuilder;
import com.marquesdev.clinica.dto.PasswordRequestDto;
import com.marquesdev.clinica.dto.UserRequestDto;
import com.marquesdev.clinica.dto.UserResponseDto;
import com.marquesdev.clinica.entity.User;
import com.marquesdev.clinica.enums.Perfil;
import com.marquesdev.clinica.exception.EmailAlreadyExistsException;
import com.marquesdev.clinica.exception.EntityNotFoundException;
import com.marquesdev.clinica.exception.PasswordInvalidException;
import com.marquesdev.clinica.mapper.UserMapper;
import com.marquesdev.clinica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${app.api.endpoints.users}")
    private String userEndpoint;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ResourceUrlBuilder resourceUrlBuilder;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
       User user = userMapper.toUser(userRequestDto);

       try {
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
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public UserResponseDto updatePassword(UUID id, PasswordRequestDto passwordRequestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id:" + id));

        if (!passwordRequestDto.newPassword().equals(passwordRequestDto.confirmPassword())){
            throw new PasswordInvalidException("New password and confirmation do not match.");
        }

        if (!passwordEncoder.matches(passwordRequestDto.currentPassword(), existingUser.getPassword())){
            throw new PasswordInvalidException("Current password is incorrect.");
        }
        existingUser.setPassword(passwordEncoder.encode(passwordRequestDto.newPassword()));
        return toResponse(existingUser);
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
