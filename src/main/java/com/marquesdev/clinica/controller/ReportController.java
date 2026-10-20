package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.service.ReportService;
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
@RequestMapping("${app.api.endpoints.reports}")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/me/appointments")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<byte[]> downloadMyAppointments(Authentication authentication) {
        byte[] pdf = reportService.generateClientAppointmentsReport(authentication.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=meu-relatorio-de-consultas.pdf")
                .body(pdf);
    }

    @GetMapping("/admin/appointments")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> downloadAllAppointments() {
        byte[] pdf = reportService.generateAdminAppointmentsReport();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-administrativo-de-consultas.pdf")
                .body(pdf);
    }

    @GetMapping("/admin/appointments/doctor")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> downloadDoctorAppointments(
            @RequestParam
            @Pattern(regexp = "\\d{6}-[A-Z]{2}", message = "CRM deve estar no formato 123456-SP") String crm) {
        byte[] pdf = reportService.generateDoctorAppointmentsReport(crm);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-medico-" + crm + ".pdf")
                .body(pdf);
    }
}
