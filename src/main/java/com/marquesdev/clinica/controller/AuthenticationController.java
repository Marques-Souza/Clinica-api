package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.UserLoginDto;
import com.marquesdev.clinica.entity.User;
import com.marquesdev.clinica.enums.Perfil;
import com.marquesdev.clinica.exception.ErrorMessage;
import com.marquesdev.clinica.jwt.JwtToken;
import com.marquesdev.clinica.jwt.JwtUserDetailsService;
import com.marquesdev.clinica.repository.DoctorRepository;
import com.marquesdev.clinica.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Contains all operations related to user authentication")
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthenticationController {

    private final JwtUserDetailsService detailsService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final DoctorRepository doctorRepository;

    @Operation(summary = "Authenticate user",
            description = "Authenticate a user with email and password and return a JWT token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtToken.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(@RequestBody @Valid UserLoginDto userDto, HttpServletRequest request) {
        log.info("Authentication process by the user {}", userDto.email());

        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDto.email(), userDto.password());
            authenticationManager.authenticate(authenticationToken);
            
            User user = userService.findByEmail(userDto.email());
            if (user.getPerfil() == Perfil.DOCTOR) {
                var doctor = doctorRepository.findByUserEmail(userDto.email());
                if (doctor.isPresent() && !Boolean.TRUE.equals(doctor.get().getActive())) {
                    log.warn("Login blocked for disabled doctor: {}", userDto.email());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ErrorMessage(
                                    request,
                                    HttpStatus.UNAUTHORIZED,
                                    "Doctor account is disabled"
                            ));
                }
            }
            
            JwtToken token = detailsService.getTokenAuthenticated(userDto.email());
            return ResponseEntity.ok(token);
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for user {}", userDto.email());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorMessage(
                            request,
                            HttpStatus.UNAUTHORIZED,
                            "Invalid credentials"
                    ));
        }
    }
}
