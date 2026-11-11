package com.marquesdev.clinica.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminPasswordResetAuditResponseDto(
        UUID id,
        String actorEmailMasked,
        String targetEmailMasked,
        LocalDateTime createdAt
) {
}
