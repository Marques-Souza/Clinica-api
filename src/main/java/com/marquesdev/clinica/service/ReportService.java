package com.marquesdev.clinica.service;

import com.marquesdev.clinica.entity.Appointment;
import com.marquesdev.clinica.entity.Client;
import com.marquesdev.clinica.entity.Doctor;
import com.marquesdev.clinica.exception.EntityNotFoundException;
import com.marquesdev.clinica.repository.AppointmentRepository;
import com.marquesdev.clinica.repository.ClientRepository;
import com.marquesdev.clinica.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public byte[] generateClientAppointmentsReport(String email) {
        Client client = clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        List<Appointment> appointments = appointmentRepository
                .findAllByPatient_User_EmailOrderByScheduledAtDesc(email);

        List<Map<String, Object>> rows = appointments.stream().map(appointment -> {
            Map<String, Object> row = new HashMap<>();
            row.put("appointmentCode", appointment.getAppointmentCode());
            row.put("scheduledAt", appointment.getScheduledAt().format(DATE_FORMAT));
            row.put("doctorName", appointment.getDoctor().getFullName());
            row.put("specialty", specialtyLabel(appointment.getDoctor().getSpecialty().name()));
            row.put("duration", appointment.getDurationMinutes() + " min");
            row.put("fee", appointment.getConsultationFee());
            row.put("paymentMethod", paymentMethodLabel(appointment.getPaymentMethod().name()));
            row.put("paymentStatus", paymentStatusLabel(appointment.getPaymentStatus().name()));
            row.put("appointmentStatus", appointmentStatusLabel(appointment.getStatus().name()));
            return row;
        }).toList();

        BigDecimal total = appointments.stream()
                .map(Appointment::getConsultationFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientName", client.getFullName());
        parameters.put("clientCpf", formatCpf(client.getCpf()));
        parameters.put("clientEmail", email);
        parameters.put("generatedAt", java.time.LocalDateTime.now().format(DATE_FORMAT));
        parameters.put("appointmentCount", appointments.size());
        parameters.put("totalAmount", total);

        try (InputStream template = new ClassPathResource("reports/relatorio_consultas_cliente.jrxml").getInputStream()) {
            var report = JasperCompileManager.compileReport(template);
            var print = JasperFillManager.fillReport(report, parameters, new JRBeanCollectionDataSource(rows));
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not generate client appointments report", exception);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateAdminAppointmentsReport() {
        List<Appointment> appointments = appointmentRepository.findAllByOrderByScheduledAtDesc();
        return generateAppointmentsReport(appointments, "Visao geral das consultas cadastradas na clinica");
    }

    @Transactional(readOnly = true)
    public byte[] generateDoctorAppointmentsReport(String crm) {
        Doctor doctor = doctorRepository.findByCrm(crm)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found for CRM: " + crm));
        List<Appointment> appointments = appointmentRepository.findAllByDoctor_CrmOrderByScheduledAtDesc(crm);
        return generateAppointmentsReport(appointments,
                "Consultas de " + doctor.getFullName() + " | CRM " + doctor.getCrm());
    }

    private byte[] generateAppointmentsReport(List<Appointment> appointments, String subtitle) {
        List<Map<String, Object>> rows = appointments.stream().map(appointment -> {
            Map<String, Object> row = new HashMap<>();
            row.put("appointmentCode", appointment.getAppointmentCode());
            row.put("scheduledAt", appointment.getScheduledAt().format(DATE_FORMAT));
            row.put("clientName", appointment.getPatient().getFullName());
            row.put("doctorName", appointment.getDoctor().getFullName());
            row.put("specialty", specialtyLabel(appointment.getDoctor().getSpecialty().name()));
            row.put("fee", appointment.getConsultationFee());
            row.put("paymentStatus", paymentStatusLabel(appointment.getPaymentStatus().name()));
            row.put("appointmentStatus", appointmentStatusLabel(appointment.getStatus().name()));
            return row;
        }).toList();

        BigDecimal total = appointments.stream()
                .map(Appointment::getConsultationFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long paidCount = appointments.stream()
                .filter(appointment -> appointment.getPaymentStatus().name().equals("PAID"))
                .count();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("generatedAt", java.time.LocalDateTime.now().format(DATE_FORMAT));
        parameters.put("subtitle", subtitle);
        parameters.put("appointmentCount", appointments.size());
        parameters.put("paidCount", (int) paidCount);
        parameters.put("totalAmount", total);

        try (InputStream template = new ClassPathResource("reports/relatorio_consultas_admin.jrxml").getInputStream()) {
            var report = JasperCompileManager.compileReport(template);
            var print = JasperFillManager.fillReport(report, parameters, new JRBeanCollectionDataSource(rows));
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not generate admin appointments report", exception);
        }
    }

    private String formatCpf(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    private String specialtyLabel(String specialty) {
        return switch (specialty) {
            case "CARDIOLOGY" -> "Cardiologia";
            case "PEDIATRICS" -> "Pediatria";
            case "ORTHOPEDICS" -> "Ortopedia";
            case "DERMATOLOGY" -> "Dermatologia";
            case "OPHTHALMOLOGY" -> "Oftalmologia";
            case "GYNECOLOGY" -> "Ginecologia";
            default -> specialty;
        };
    }

    private String paymentMethodLabel(String method) {
        return switch (method) {
            case "PIX" -> "Pix";
            case "CREDIT_CARD" -> "Cartão";
            case "CASH" -> "Dinheiro";
            default -> method;
        };
    }

    private String paymentStatusLabel(String status) {
        return switch (status) {
            case "PENDING" -> "Pendente";
            case "PAID" -> "Pago";
            case "CANCELLED" -> "Cancelado";
            default -> status;
        };
    }

    private String appointmentStatusLabel(String status) {
        return switch (status) {
            case "SCHEDULED" -> "Agendada";
            case "COMPLETED" -> "Realizada";
            case "CANCELLED" -> "Cancelada";
            default -> status;
        };
    }
}
