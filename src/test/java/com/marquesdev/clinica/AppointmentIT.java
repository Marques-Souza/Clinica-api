package com.marquesdev.clinica;

import com.marquesdev.clinica.dto.AppointmentRequestDto;
import com.marquesdev.clinica.dto.AppointmentResponseDto;
import com.marquesdev.clinica.dto.AppointmentUpdateRequestDto;
import com.marquesdev.clinica.enums.AppointmentStatus;
import com.marquesdev.clinica.enums.PaymentMethod;
import com.marquesdev.clinica.enums.PaymentStatus;
import com.marquesdev.clinica.enums.Specialty;
import com.marquesdev.clinica.exception.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Sql(scripts = "/sql/appointments/appointment-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/appointments/appointment-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AppointmentIT {

    @Autowired
    private WebTestClient testClient;

    private static final String APPOINTMENTS_URI = "/api/v1/appointments";

    private final String USER_WITH_APPOINTMENT_EMAIL = "karla@gmail.com";
    private final String USER_WITH_APPOINTMENT_PASS = "123456";
    private final String USER_WITHOUT_APPOINTMENT_EMAIL = "joao@gmail.com";
    private final String USER_WITHOUT_APPOINTMENT_PASS = "123456";
    private final String ADMIN_EMAIL = "support@gmail.com";
    private final String ADMIN_PASS = "123456";

    // ===========================================
    // TEST BLOCK: POST /api/v1/appointments (Create Appointment)
    // =============================================

    @Test
    public void createAppointment_WithValidData_ReturnsAppointmentWithStatus201() {
        AppointmentResponseDto responseBody = testClient
                .post()
                .uri(APPOINTMENTS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AppointmentRequestDto(
                        Specialty.CARDIOLOGY,
                        LocalDateTime.of(2026, 12, 2, 14, 0),
                        PaymentMethod.CASH,
                        "Consulta de rotina"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITHOUT_APPOINTMENT_EMAIL, USER_WITHOUT_APPOINTMENT_PASS))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AppointmentResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.status()).isEqualTo(AppointmentStatus.SCHEDULED);
        org.assertj.core.api.Assertions.assertThat(responseBody.paymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    public void createAppointment_WithPastDate_ReturnsStatus400() {
        ErrorMessage responseBody = testClient
                .post()
                .uri(APPOINTMENTS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AppointmentRequestDto(
                        Specialty.CARDIOLOGY,
                        LocalDateTime.of(2020, 12, 2, 14, 0),
                        PaymentMethod.CREDIT_CARD,
                        "Consulta de rotina"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITHOUT_APPOINTMENT_EMAIL, USER_WITHOUT_APPOINTMENT_PASS))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void createAppointment_WithoutToken_ReturnsStatus401() {
        testClient
                .post()
                .uri(APPOINTMENTS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AppointmentRequestDto(
                        Specialty.CARDIOLOGY,
                        LocalDateTime.of(2026, 12, 2, 14, 0),
                        PaymentMethod.CASH,
                        "Consulta de rotina"
                ))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ===========================================
    // TEST BLOCK: GET /api/v1/appointments (List Appointments)
    // =============================================

    @Test
    public void findAll_WithExistingAppointments_ReturnsPageWithStatus200() {
        testClient
                .get()
                .uri(APPOINTMENTS_URI)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITH_APPOINTMENT_EMAIL, USER_WITH_APPOINTMENT_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray();
    }

    @Test
    public void findAll_WithoutToken_ReturnsStatus401() {
        testClient
                .get()
                .uri(APPOINTMENTS_URI)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ===========================================
    // TEST BLOCK: GET /api/v1/appointments/code/{code} (Find by Code)
    // =============================================

    @Test
    public void findByCode_WithExistingCode_ReturnsAppointmentWithStatus200() {
        AppointmentResponseDto responseBody = testClient
                .get()
                .uri(APPOINTMENTS_URI + "/code/20261201-a1b2c3d4")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITH_APPOINTMENT_EMAIL, USER_WITH_APPOINTMENT_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AppointmentResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.appointmentCode()).isEqualTo("20261201-a1b2c3d4");
    }

    @Test
    public void findByCode_WithNonExistingCode_ReturnsStatus404() {
        ErrorMessage responseBody = testClient
                .get()
                .uri(APPOINTMENTS_URI + "/code/20261201-00000000")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITH_APPOINTMENT_EMAIL, USER_WITH_APPOINTMENT_PASS))
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void findByCode_WithoutToken_ReturnsStatus401() {
        testClient
                .get()
                .uri(APPOINTMENTS_URI + "/code/20261201-a1b2c3d4")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ===========================================
    // TEST BLOCK: PUT /api/v1/appointments/{id} (Update Appointment)
    // =============================================

    @Test
    public void updateAppointment_WithValidData_ReturnsAppointmentWithStatus200() {
        UUID appointmentId = UUID.fromString("d4e5f6a7-b8c9-0123-def0-456789012345");
        AppointmentResponseDto responseBody = testClient
                .put()
                .uri(APPOINTMENTS_URI + "/" + appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AppointmentUpdateRequestDto(
                        LocalDateTime.of(2026, 12, 1, 11, 0),
                        AppointmentStatus.SCHEDULED,
                        "Consulta atualizada"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITH_APPOINTMENT_EMAIL, USER_WITH_APPOINTMENT_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AppointmentResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.reason()).isEqualTo("Consulta atualizada");
    }

    @Test
    public void updateAppointment_WithCancelledStatus_ReturnsAppointmentWithCancelledPaymentStatus() {
        UUID appointmentId = UUID.fromString("d4e5f6a7-b8c9-0123-def0-456789012345");
        AppointmentResponseDto responseBody = testClient
                .put()
                .uri(APPOINTMENTS_URI + "/" + appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AppointmentUpdateRequestDto(
                        null,
                        AppointmentStatus.CANCELLED,
                        null
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITH_APPOINTMENT_EMAIL, USER_WITH_APPOINTMENT_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AppointmentResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.status()).isEqualTo(AppointmentStatus.CANCELLED);
        org.assertj.core.api.Assertions.assertThat(responseBody.paymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    public void updateAppointment_WithNonExistingId_ReturnsStatus404() {
        UUID nonExistingId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        ErrorMessage responseBody = testClient
                .put()
                .uri(APPOINTMENTS_URI + "/" + nonExistingId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AppointmentUpdateRequestDto(
                        LocalDateTime.of(2026, 12, 1, 11, 0),
                        AppointmentStatus.SCHEDULED,
                        "Consulta atualizada"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITH_APPOINTMENT_EMAIL, USER_WITH_APPOINTMENT_PASS))
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void updateAppointment_WithDifferentUser_ReturnsStatus404() {
        UUID appointmentId = UUID.fromString("d4e5f6a7-b8c9-0123-def0-456789012345");
        ErrorMessage responseBody = testClient
                .put()
                .uri(APPOINTMENTS_URI + "/" + appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AppointmentUpdateRequestDto(
                        LocalDateTime.of(2026, 12, 1, 11, 0),
                        AppointmentStatus.SCHEDULED,
                        "Consulta atualizada"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITHOUT_APPOINTMENT_EMAIL, USER_WITHOUT_APPOINTMENT_PASS))
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void updateAppointment_WithoutToken_ReturnsStatus401() {
        UUID appointmentId = UUID.fromString("d4e5f6a7-b8c9-0123-def0-456789012345");
        testClient
                .put()
                .uri(APPOINTMENTS_URI + "/" + appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AppointmentUpdateRequestDto(
                        LocalDateTime.of(2026, 12, 1, 11, 0),
                        AppointmentStatus.SCHEDULED,
                        "Consulta atualizada"
                ))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
