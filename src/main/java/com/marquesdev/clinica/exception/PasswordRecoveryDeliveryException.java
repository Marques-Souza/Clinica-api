package com.marquesdev.clinica.exception;

public class PasswordRecoveryDeliveryException extends RuntimeException {
    public PasswordRecoveryDeliveryException(Throwable cause) {
        super("Unable to send password recovery email.", cause);
    }
}
