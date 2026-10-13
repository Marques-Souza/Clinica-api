package com.marquesdev.clinica;

import com.marquesdev.clinica.dto.DoctorRequestDto;
import com.marquesdev.clinica.dto.DoctorResponseDto;
import com.marquesdev.clinica.dto.DoctorUpdateRequestDto;
import com.marquesdev.clinica.enums.Specialty;
import com.marquesdev.clinica.exception.ErrorMessage;
import com.marquesdev.clinica.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;
import java.util.function.Consumer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Sql(scripts = "/sql/doctors/doctor-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/doctors/doctor-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class DoctorIT {

    @Autowired
    private WebTestClient testClient;

    private static final String DOCTORS_URI = "/api/v1/doctors";

    private static final UUID EXISTING_DOCTOR_ID =
            UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private static final UUID INEXISTENT_DOCTOR_ID =
            UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    private final String ADMIN_EMAIL = "support@gmail.com";
    private final String ADMIN_PASS = "123456";
    private final String DOCTOR_EMAIL = "kaio@gmail.com";
    private final String DOCTOR_PASS = "123456";
    private final String DOCTOR_WITHOUT_PROFILE_EMAIL = "ana.medica@gmail.com";
    private final String USER_EMAIL = "karla@gmail.com";
    private final String USER_PASS = "123456";

    // ===========================================
    // TEST BLOCK: PUT /api/v1/doctors/me (Create Doctor)
    // =============================================

    @Test
    public void createDoctor_WithValidData_ReturnsDoctorWithStatus201() {
        DoctorResponseDto responseBody = testClient
                .put()
                .uri(DOCTORS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DoctorRequestDto(
                        "Ana Medica",
                        "11144477735",
                        "654321-SP",
                        Specialty.CARDIOLOGY
                ))
                .headers(getAuthorizationForUnregisteredDoctor())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DoctorResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.fullName()).isEqualTo("Ana Medica");
        org.assertj.core.api.Assertions.assertThat(responseBody.cpf()).isEqualTo("11144477735");
        org.assertj.core.api.Assertions.assertThat(responseBody.crm()).isEqualTo("654321-SP");
        org.assertj.core.api.Assertions.assertThat(responseBody.specialty()).isEqualTo(Specialty.CARDIOLOGY);
        org.assertj.core.api.Assertions.assertThat(responseBody.active()).isTrue();
        org.assertj.core.api.Assertions.assertThat(responseBody.email()).isEqualTo(DOCTOR_WITHOUT_PROFILE_EMAIL);
    }

    @Test
    public void createDoctor_WithExistingProfile_ReturnsStatus409() {
        ErrorMessage responseBody = testClient
                .put()
                .uri(DOCTORS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DoctorRequestDto(
                        "Kaio Medico",
                        "11144477735",
                        "654321-SP",
                        Specialty.CARDIOLOGY
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, DOCTOR_EMAIL, DOCTOR_PASS))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void createDoctor_WithInvalidCpf_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .put()
                .uri(DOCTORS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DoctorRequestDto(
                        "Ana Medica",
                        "123",
                        "654321-SP",
                        Specialty.CARDIOLOGY
                ))
                .headers(getAuthorizationForUnregisteredDoctor())
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void createDoctor_WithInvalidCrm_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .put()
                .uri(DOCTORS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DoctorRequestDto(
                        "Ana Medica",
                        "11144477735",
                        "123-SP",
                        Specialty.CARDIOLOGY
                ))
                .headers(getAuthorizationForUnregisteredDoctor())
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void createDoctor_WithBlankFields_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .put()
                .uri(DOCTORS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DoctorRequestDto("", "", "", null))
                .headers(getAuthorizationForUnregisteredDoctor())
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void createDoctor_WithUserRole_ReturnsStatus403() {
        testClient
                .put()
                .uri(DOCTORS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DoctorRequestDto(
                        "Karla Santos",
                        "11144477735",
                        "654321-SP",
                        Specialty.CARDIOLOGY
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_EMAIL, USER_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void createDoctor_WithoutToken_ReturnsStatus401() {
        testClient
                .put()
                .uri(DOCTORS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DoctorRequestDto(
                        "Ana Medica",
                        "11144477735",
                        "654321-SP",
                        Specialty.CARDIOLOGY
                ))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ===========================================
    // TEST BLOCK: GET /api/v1/doctors/me (Get My Doctor)
    // =============================================

    @Test
    public void getMyDoctor_WithExistingProfile_ReturnsDoctorWithStatus200() {
        DoctorResponseDto responseBody = testClient
                .get()
                .uri(DOCTORS_URI + "/me")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, DOCTOR_EMAIL, DOCTOR_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(DoctorResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(EXISTING_DOCTOR_ID);
        org.assertj.core.api.Assertions.assertThat(responseBody.email()).isEqualTo(DOCTOR_EMAIL);
        org.assertj.core.api.Assertions.assertThat(responseBody.specialty()).isEqualTo(Specialty.CARDIOLOGY);
    }

    @Test
    public void getMyDoctor_WhenProfileNotFound_ReturnsStatus404() {
        ErrorMessage responseBody = testClient
                .get()
                .uri(DOCTORS_URI + "/me")
                .headers(getAuthorizationForUnregisteredDoctor())
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void getMyDoctor_WithUserRole_ReturnsStatus403() {
        testClient
                .get()
                .uri(DOCTORS_URI + "/me")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_EMAIL, USER_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void getMyDoctor_WithoutToken_ReturnsStatus401() {
        testClient
                .get()
                .uri(DOCTORS_URI + "/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ===========================================
    // TEST BLOCK: GET /api/v1/doctors/{id} (Get Doctor by Id)
    // =============================================

    @Test
    public void getDoctorById_WithValidId_ReturnsDoctorWithStatus200() {
        DoctorResponseDto responseBody = testClient
                .get()
                .uri(DOCTORS_URI + "/" + EXISTING_DOCTOR_ID)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(DoctorResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(EXISTING_DOCTOR_ID);
        org.assertj.core.api.Assertions.assertThat(responseBody.email()).isEqualTo(DOCTOR_EMAIL);
    }

    @Test
    public void getDoctorById_WithInexistentId_ReturnsStatus404() {
        ErrorMessage responseBody = testClient
                .get()
                .uri(DOCTORS_URI + "/" + INEXISTENT_DOCTOR_ID)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void getDoctorById_WithUserRole_ReturnsStatus403() {
        testClient
                .get()
                .uri(DOCTORS_URI + "/" + EXISTING_DOCTOR_ID)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_EMAIL, USER_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void getDoctorById_WithoutToken_ReturnsStatus401() {
        testClient
                .get()
                .uri(DOCTORS_URI + "/" + EXISTING_DOCTOR_ID)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ===========================================
    // TEST BLOCK: PATCH /api/v1/doctors/{id} (Update Doctor)
    // =============================================

    @Test
    public void updateDoctorById_WithValidData_ReturnsDoctorWithStatus200() {
        DoctorResponseDto responseBody = testClient
                .patch()
                .uri(DOCTORS_URI + "/" + EXISTING_DOCTOR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DoctorUpdateRequestDto(Specialty.DERMATOLOGY, false))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(DoctorResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(EXISTING_DOCTOR_ID);
        org.assertj.core.api.Assertions.assertThat(responseBody.specialty()).isEqualTo(Specialty.DERMATOLOGY);
        org.assertj.core.api.Assertions.assertThat(responseBody.active()).isFalse();
    }

    @Test
    public void updateDoctorById_WithInexistentId_ReturnsStatus404() {
        ErrorMessage responseBody = testClient
                .patch()
                .uri(DOCTORS_URI + "/" + INEXISTENT_DOCTOR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DoctorUpdateRequestDto(Specialty.DERMATOLOGY, false))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_EMAIL, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void updateDoctorById_WithUserRole_ReturnsStatus403() {
        testClient
                .patch()
                .uri(DOCTORS_URI + "/" + EXISTING_DOCTOR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DoctorUpdateRequestDto(Specialty.DERMATOLOGY, false))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_EMAIL, USER_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void updateDoctorById_WithoutToken_ReturnsStatus401() {
        testClient
                .patch()
                .uri(DOCTORS_URI + "/" + EXISTING_DOCTOR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DoctorUpdateRequestDto(Specialty.DERMATOLOGY, false))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private Consumer<HttpHeaders> getAuthorizationForUnregisteredDoctor() {
        String token = JwtUtils.createToken(DOCTOR_WITHOUT_PROFILE_EMAIL, "DOCTOR").getToken();
        return headers -> headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
}
