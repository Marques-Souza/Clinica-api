package com.marquesdev.clinica;

import com.marquesdev.clinica.exception.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Sql(scripts = "/sql/reports/report-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/reports/report-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ReportIT {

    private static final String REPORTS_URI = "/api/v1/reports";
    private static final String CLIENT_EMAIL = "karla@gmail.com";
    private static final String CLIENT_PASS = "123456";
    private static final String ADMIN_EMAIL = "support@gmail.com";
    private static final String ADMIN_PASS = "123456";
    private static final String DOCTOR_EMAIL = "kaio@gmail.com";
    private static final String DOCTOR_PASS = "123456";
    private static final String DOCTOR_CRM = "123456-SP";

    @Autowired
    private WebTestClient testClient;

    @Test
    public void downloadMyAppointments_WithClientToken_ReturnsPdfWithStatus200() {
        testClient
                .get()
                .uri(REPORTS_URI + "/me/appointments")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, CLIENT_EMAIL, CLIENT_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=meu-relatorio-de-consultas.pdf")
                .expectBody()
                .consumeWith(result -> {
                    org.assertj.core.api.Assertions.assertThat(result.getResponseBody()).isNotNull();
                    org.assertj.core.api.Assertions.assertThat(result.getResponseBody()).startsWith("%PDF".getBytes());
                });
    }

    @Test
    public void downloadMyAppointments_WithNoAppointments_ReturnsEmptyReportPdf() {
        testClient
                .get()
                .uri(REPORTS_URI + "/me/appointments")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, "joao@gmail.com", CLIENT_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody()
                .consumeWith(result -> {
                    org.assertj.core.api.Assertions.assertThat(result.getResponseBody()).isNotNull();
                    org.assertj.core.api.Assertions.assertThat(result.getResponseBody()).startsWith("%PDF".getBytes());
                });
    }

    @Test
    public void downloadMyAppointments_WithAdminToken_ReturnsStatus403() {
        testClient
                .get()
                .uri(REPORTS_URI + "/me/appointments")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void downloadMyAppointments_WithoutToken_ReturnsStatus401() {
        testClient
                .get()
                .uri(REPORTS_URI + "/me/appointments")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void downloadAllAppointments_WithAdminToken_ReturnsPdfWithStatus200() {
        testClient
                .get()
                .uri(REPORTS_URI + "/admin/appointments")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=relatorio-administrativo-de-consultas.pdf")
                .expectBody()
                .consumeWith(result -> {
                    org.assertj.core.api.Assertions.assertThat(result.getResponseBody()).isNotNull();
                    org.assertj.core.api.Assertions.assertThat(result.getResponseBody()).startsWith("%PDF".getBytes());
                });
    }

    @Test
    public void downloadAllAppointments_WithClientToken_ReturnsStatus403() {
        testClient
                .get()
                .uri(REPORTS_URI + "/admin/appointments")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, CLIENT_EMAIL, CLIENT_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void downloadAllAppointments_WithoutToken_ReturnsStatus401() {
        testClient
                .get()
                .uri(REPORTS_URI + "/admin/appointments")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void downloadDoctorAppointments_WithExistingCrm_ReturnsPdfWithStatus200() {
        testClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(REPORTS_URI + "/admin/appointments/doctor")
                        .queryParam("crm", DOCTOR_CRM)
                        .build())
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=relatorio-medico-" + DOCTOR_CRM + ".pdf")
                .expectBody()
                .consumeWith(result -> {
                    org.assertj.core.api.Assertions.assertThat(result.getResponseBody()).isNotNull();
                    org.assertj.core.api.Assertions.assertThat(result.getResponseBody()).startsWith("%PDF".getBytes());
                });
    }

    @Test
    public void downloadDoctorAppointments_WithUnknownCrm_ReturnsStatus404() {
        ErrorMessage responseBody = testClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(REPORTS_URI + "/admin/appointments/doctor")
                        .queryParam("crm", "999999-SP")
                        .build())
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void downloadDoctorAppointments_WithInvalidCrm_ReturnsStatus400() {
        testClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(REPORTS_URI + "/admin/appointments/doctor")
                        .queryParam("crm", "invalid-crm")
                        .build())
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void downloadDoctorAppointments_WithDoctorToken_ReturnsStatus403() {
        testClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(REPORTS_URI + "/admin/appointments/doctor")
                        .queryParam("crm", DOCTOR_CRM)
                        .build())
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, DOCTOR_EMAIL, DOCTOR_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void downloadDoctorAppointments_WithoutToken_ReturnsStatus401() {
        testClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(REPORTS_URI + "/admin/appointments/doctor")
                        .queryParam("crm", DOCTOR_CRM)
                        .build())
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
