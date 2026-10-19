package com.marquesdev.clinica;

import com.marquesdev.clinica.dto.SpecialtyConfigRequestDto;
import com.marquesdev.clinica.dto.SpecialtyConfigResponseDto;
import com.marquesdev.clinica.dto.SpecialtyConfigUpdateRequestDto;
import com.marquesdev.clinica.enums.Specialty;
import com.marquesdev.clinica.exception.ErrorMessage;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Sql(scripts = "/sql/specialties/specialty-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/specialties/specialty-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class SpecialtyConfigIT {

    @Autowired
    private WebTestClient testClient;

    private static final String SPECIALTIES_URI = "/api/v1/specialties";

    private final String ADMIN_EMAIL = "support@gmail.com";
    private final String ADMIN_PASS = "123456";
    private final String USER_EMAIL = "karla@gmail.com";
    private final String USER_PASS = "123456";

    // ===========================================
    // TEST BLOCK: POST /api/v1/specialties (Create Specialty Config)
    // =============================================

    @Test
    public void createSpecialtyConfig_WithValidData_ReturnsSpecialtyConfigWithStatus201() {
        SpecialtyConfigResponseDto responseBody = testClient
                .post()
                .uri(SPECIALTIES_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigRequestDto(
                        Specialty.GYNECOLOGY,
                        new BigDecimal("300.00"),
                        45
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SpecialtyConfigResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.specialty()).isEqualTo(Specialty.GYNECOLOGY);
        org.assertj.core.api.Assertions.assertThat(responseBody.consultationFee()).isEqualByComparingTo(new BigDecimal("300.00"));
        org.assertj.core.api.Assertions.assertThat(responseBody.durationMinutes()).isEqualTo(45);
    }

    @Test
    public void createSpecialtyConfig_WithDuplicateSpecialty_ReturnsStatus409() {
        ErrorMessage responseBody = testClient
                .post()
                .uri(SPECIALTIES_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigRequestDto(
                        Specialty.CARDIOLOGY,
                        new BigDecimal("250.00"),
                        30
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void createSpecialtyConfig_WithInvalidFee_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .post()
                .uri(SPECIALTIES_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigRequestDto(
                        Specialty.GYNECOLOGY,
                        new BigDecimal("-100.00"),
                        30
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void createSpecialtyConfig_WithInvalidDuration_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .post()
                .uri(SPECIALTIES_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigRequestDto(
                        Specialty.GYNECOLOGY,
                        new BigDecimal("250.00"),
                        0
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void createSpecialtyConfig_WithoutToken_ReturnsStatus401() {
        testClient
                .post()
                .uri(SPECIALTIES_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigRequestDto(
                        Specialty.GYNECOLOGY,
                        new BigDecimal("250.00"),
                        30
                ))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void createSpecialtyConfig_WithUserRole_ReturnsStatus403() {
        testClient
                .post()
                .uri(SPECIALTIES_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigRequestDto(
                        Specialty.GYNECOLOGY,
                        new BigDecimal("250.00"),
                        30
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_EMAIL, USER_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    // ===========================================
    // TEST BLOCK: GET /api/v1/specialties/{specialty} (Get Specialty Config)
    // =============================================

    @Test
    public void getSpecialtyConfig_WithExistingSpecialty_ReturnsSpecialtyConfigWithStatus200() {
        SpecialtyConfigResponseDto responseBody = testClient
                .get()
                .uri(SPECIALTIES_URI + "/CARDIOLOGY")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SpecialtyConfigResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.specialty()).isEqualTo(Specialty.CARDIOLOGY);
        org.assertj.core.api.Assertions.assertThat(responseBody.consultationFee()).isEqualByComparingTo(new BigDecimal("250.00"));
        org.assertj.core.api.Assertions.assertThat(responseBody.durationMinutes()).isEqualTo(30);
    }

    @Test
    public void getSpecialtyConfig_WithNonExistingSpecialty_ReturnsStatus404() {
        ErrorMessage responseBody = testClient
                .get()
                .uri(SPECIALTIES_URI + "/GYNECOLOGY")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void getSpecialtyConfig_WithoutToken_ReturnsStatus401() {
        testClient
                .get()
                .uri(SPECIALTIES_URI + "/CARDIOLOGY")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void getSpecialtyConfig_WithUserRole_ReturnsStatus403() {
        testClient
                .get()
                .uri(SPECIALTIES_URI + "/CARDIOLOGY")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_EMAIL, USER_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    // ===========================================
    // TEST BLOCK: PUT /api/v1/specialties/{specialty} (Update Specialty Config)
    // =============================================

    @Test
    public void updateSpecialtyConfig_WithValidData_ReturnsSpecialtyConfigWithStatus200() {
        SpecialtyConfigResponseDto responseBody = testClient
                .put()
                .uri(SPECIALTIES_URI + "/CARDIOLOGY")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigUpdateRequestDto(
                        new BigDecimal("350.00"),
                        40
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SpecialtyConfigResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.specialty()).isEqualTo(Specialty.CARDIOLOGY);
        org.assertj.core.api.Assertions.assertThat(responseBody.consultationFee()).isEqualByComparingTo(new BigDecimal("350.00"));
        org.assertj.core.api.Assertions.assertThat(responseBody.durationMinutes()).isEqualTo(40);
    }

    @Test
    public void updateSpecialtyConfig_WithInvalidFee_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .put()
                .uri(SPECIALTIES_URI + "/CARDIOLOGY")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigUpdateRequestDto(
                        new BigDecimal("-100.00"),
                        30
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void updateSpecialtyConfig_WithInvalidDuration_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .put()
                .uri(SPECIALTIES_URI + "/CARDIOLOGY")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigUpdateRequestDto(
                        new BigDecimal("250.00"),
                        0
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void updateSpecialtyConfig_WithNonExistingSpecialty_ReturnsStatus404() {
        ErrorMessage responseBody = testClient
                .put()
                .uri(SPECIALTIES_URI + "/GYNECOLOGY")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigUpdateRequestDto(
                        new BigDecimal("250.00"),
                        30
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void updateSpecialtyConfig_WithoutToken_ReturnsStatus401() {
        testClient
                .put()
                .uri(SPECIALTIES_URI + "/CARDIOLOGY")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigUpdateRequestDto(
                        new BigDecimal("250.00"),
                        30
                ))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void updateSpecialtyConfig_WithUserRole_ReturnsStatus403() {
        testClient
                .put()
                .uri(SPECIALTIES_URI + "/CARDIOLOGY")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SpecialtyConfigUpdateRequestDto(
                        new BigDecimal("250.00"),
                        30
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_EMAIL, USER_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }
}
