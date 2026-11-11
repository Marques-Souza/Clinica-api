package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.AdminPasswordResetAuditResponseDto;
import com.marquesdev.clinica.dto.PasswordRequestDto;
import com.marquesdev.clinica.dto.PasswordResetRequestDto;
import com.marquesdev.clinica.dto.UserRequestDto;
import com.marquesdev.clinica.dto.UserResponseDto;
import com.marquesdev.clinica.enums.Perfil;
import com.marquesdev.clinica.exception.ErrorMessage;
import com.marquesdev.clinica.jwt.JwtUserDetails;
import com.marquesdev.clinica.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@Tag(name = "Users", description = "Contains all operations related to user resources for creating, updating and reading.")
@RestController
@RequiredArgsConstructor
@RequestMapping("${app.api.endpoints.users}")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create a new user",
            description = "Create a new user in the database",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = "Email already exists",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
            }

    )
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto){
       UserResponseDto savedUser = userService.createUser(userRequestDto, Perfil.USER);
       return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }



    @PostMapping("/doctor")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponseDto> createDoctor(@Valid @RequestBody UserRequestDto userRequestDto){
        UserResponseDto savedUser = userService.createUser(userRequestDto, Perfil.DOCTOR);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }


    @Operation(summary = "Retrieve a user by ID",
            description = "Endpoint to get a user by ID (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "User not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }

    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') OR ((hasAuthority('USER') OR hasAuthority('DOCTOR')) AND #id == authentication.principal.id)")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id){
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    @Operation(summary = "Retrieve all users",
            description = "Endpoint to list all users accessible by admin (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "202", description = "Users retrieved successfully",
                            content = @Content(mediaType = "application/json",array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class)))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }

    )
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Perfil perfil,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponseDto> users = userService.getAllUsers(pageable, search, perfil);
        return ResponseEntity.ok(users);
    }


    @Operation(summary = "Update password",
            description = "Update user password. Users can update their own password, while Doctors and Admins can only be updated by an Admin. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Password does not match",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }

    )
    @PatchMapping("/{id}/password")
    @PreAuthorize("@userAuthorization.canUpdatePassword(#id, authentication)")
    public ResponseEntity<UserResponseDto> updatePassword(
            @PathVariable UUID id,
            @Valid @RequestBody PasswordRequestDto passwordRequestDto,
            Authentication authentication
    ){
        JwtUserDetails principal = (JwtUserDetails) authentication.getPrincipal();
        UserResponseDto updatedPassword =
                userService.updatePassword(id, passwordRequestDto, principal.getId());
        return ResponseEntity.ok(updatedPassword);
    }

    @Operation(summary = "Update authenticated user's password",
            description = "Update the password of the authenticated account without requiring its user ID.",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Current password is incorrect or confirmation does not match",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @PatchMapping("/me/password")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<UserResponseDto> updateMyPassword(
            Authentication authentication,
            @Valid @RequestBody PasswordRequestDto passwordRequestDto) {
        UserResponseDto updatedPassword = userService.updatePassword(authentication.getName(), passwordRequestDto);
        return ResponseEntity.ok(updatedPassword);
    }

    @Operation(summary = "Reset a doctor's password",
            description = "Allows an admin to set a new password for a doctor without the current password.")
    @PatchMapping("/{id}/password/reset")
    @PreAuthorize("@userAuthorization.canResetDoctorPassword(#id, authentication)")
    public ResponseEntity<UserResponseDto> resetDoctorPassword(
            @PathVariable UUID id,
            @Valid @RequestBody PasswordResetRequestDto passwordResetRequestDto,
            Authentication authentication
    ) {
        JwtUserDetails principal = (JwtUserDetails) authentication.getPrincipal();
        UserResponseDto updatedPassword =
                userService.resetDoctorPassword(id, passwordResetRequestDto, principal.getId());
        return ResponseEntity.ok(updatedPassword);
    }

    @Operation(summary = "Retrieve the password reset audit history")
    @GetMapping("/audit/password-resets")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<AdminPasswordResetAuditResponseDto>> getPasswordResetAudit(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getPasswordResetAudit(pageable));
    }


}
