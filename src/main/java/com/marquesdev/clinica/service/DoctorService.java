package com.marquesdev.clinica.service;

import com.marquesdev.clinica.dto.DoctorRequestDto;
import com.marquesdev.clinica.dto.DoctorResponseDto;
import com.marquesdev.clinica.dto.DoctorUpdateRequestDto;
import com.marquesdev.clinica.entity.Doctor;
import com.marquesdev.clinica.entity.User;
import com.marquesdev.clinica.exception.DoctorAlreadyExistsException;
import com.marquesdev.clinica.exception.EntityNotFoundException;
import com.marquesdev.clinica.mapper.DoctorMapper;
import com.marquesdev.clinica.repository.DoctorRepository;
import jakarta.validation.Valid;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;
    private final UserService userService;


    @Transactional
    public DoctorResponseDto createDoctor (String email, DoctorRequestDto doctorRequestDto){
        User user = userService.findByEmail(email);
        if (doctorRepository.existsByUser_Id(user.getId())){
            throw new DoctorAlreadyExistsException("Doctor profile exists for this user.");
        }
        Doctor doctor = doctorMapper.toDoctor(doctorRequestDto);
        doctor.assignUser(user);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDto(savedDoctor);
    }

    @Transactional(readOnly = true)
    public DoctorResponseDto getMyDoctor(String email){
        Doctor doctor = doctorRepository.findByUserEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));
        return doctorMapper.toDto(doctor);
    }

    @Transactional(readOnly = true)
    public DoctorResponseDto getDoctorById(UUID id){
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public DoctorResponseDto updateDoctorById(UUID id, @Valid DoctorUpdateRequestDto doctorUpdateRequestDto) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));
        doctorMapper.updateDoctor(doctorUpdateRequestDto, doctor);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDto(savedDoctor);
    }
}
