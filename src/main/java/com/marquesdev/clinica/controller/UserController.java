package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.PasswordRequestDto;
import com.marquesdev.clinica.dto.UserRequestDto;
import com.marquesdev.clinica.dto.UserResponseDto;
import com.marquesdev.clinica.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${app.api.endpoints.users}")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto){
       UserResponseDto savedUser = userService.createUser(userRequestDto);
       return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id){
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<UserResponseDto> updatePassword(@PathVariable UUID id,@Valid @RequestBody PasswordRequestDto passwordRequestDto){
        UserResponseDto updatedPassword = userService.updatePassword(id, passwordRequestDto);
        return ResponseEntity.ok(updatedPassword);
    }


}
