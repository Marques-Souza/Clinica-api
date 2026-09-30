package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.PasswordRequestDto;
import com.marquesdev.clinica.dto.UserRequestDto;
import com.marquesdev.clinica.dto.UserResponseDto;
import com.marquesdev.clinica.exception.ErrorMessage;
import com.marquesdev.clinica.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto){
       UserResponseDto savedUser = userService.createUser(userRequestDto);
       return ResponseEntity.ok(savedUser);
    }


    @Operation(summary = "Retrieve a user by ID",
            description = "Retrieve a user by ID in the database",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = "User not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }

    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id){
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    @Operation(summary = "Retrieve all users",
            description = "Retrieve all users in the database",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Users retrieved successfully",
                            content = @Content(mediaType = "application/json",array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class))))
            }

    )
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }


    @Operation(summary = "Upadte password",
            description = "Update user password in the database",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Password updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Password does not match",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }

    )
    @PatchMapping("/{id}/password")
    public ResponseEntity<UserResponseDto> updatePassword(@PathVariable UUID id,@Valid @RequestBody PasswordRequestDto passwordRequestDto){
        UserResponseDto updatedPassword = userService.updatePassword(id, passwordRequestDto);
        return ResponseEntity.ok(updatedPassword);
    }


}
