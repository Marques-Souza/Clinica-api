package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.service.ReportService;
import com.marquesdev.clinica.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@Tag(name = "Reports", description = "Contains operations for downloading appointment reports as PDF.")
@RequestMapping("${app.api.endpoints.reports}")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Download appointments report for authenticated client",
            description = "Generates a PDF containing appointments belonging to the authenticated client. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Client appointments PDF generated successfully",
                            content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary"))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only clients can download this report",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Client profile not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @GetMapping("/me/appointments")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<byte[]> downloadMyAppointments(Authentication authentication) {
        byte[] pdf = reportService.generateClientAppointmentsReport(authentication.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=meu-relatorio-de-consultas.pdf")
                .body(pdf);
    }

    @Operation(summary = "Download administrative appointments report",
            description = "Generates a PDF containing all appointments. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Administrative appointments PDF generated successfully",
                            content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary"))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only admins can download this report",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @GetMapping("/admin/appointments")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> downloadAllAppointments() {
        byte[] pdf = reportService.generateAdminAppointmentsReport();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-administrativo-de-consultas.pdf")
                .body(pdf);
    }

    @Operation(summary = "Download appointments report by doctor CRM",
            description = "Generates a PDF containing appointments assigned to the doctor identified by CRM. CRM must use the format 123456-SP. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Doctor appointments PDF generated successfully",
                            content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary"))),
                    @ApiResponse(responseCode = "400", description = "Invalid CRM format",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only admins can download this report",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Doctor with the provided CRM not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @GetMapping("/admin/appointments/doctor")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> downloadDoctorAppointments(
            @Parameter(description = "Doctor CRM, six digits followed by a hyphen and the two-letter state abbreviation", example = "123456-SP", required = true)
            @RequestParam
            @Pattern(regexp = "\\d{6}-[A-Z]{2}", message = "CRM deve estar no formato 123456-SP") String crm) {
        byte[] pdf = reportService.generateDoctorAppointmentsReport(crm);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-medico-" + crm + ".pdf")
                .body(pdf);
    }
}
