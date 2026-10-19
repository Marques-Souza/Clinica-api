package com.marquesdev.clinica.exception;

public class DoctorInactiveException extends RuntimeException {
    public DoctorInactiveException(String message) {
        super(message);
    }
}
