package com.marquesdev.clinica;

import com.marquesdev.clinica.dto.PasswordRequestDto;
import com.marquesdev.clinica.dto.PasswordRecoveryConfirmDto;
import com.marquesdev.clinica.dto.PasswordRecoveryRequestDto;
import com.marquesdev.clinica.dto.PasswordResetRequestDto;
import com.marquesdev.clinica.dto.AdminDashboardSummaryDto;
import com.marquesdev.clinica.dto.UserRequestDto;
import com.marquesdev.clinica.dto.UserResponseDto;
import com.marquesdev.clinica.exception.ErrorMessage;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Sql(scripts = "/sql/users/user-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/user-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserIT {

    @Autowired
    private WebTestClient testClient;

    @MockitoBean
    private JavaMailSender mailSender;

    // Constante para a URI base de usuários
    private static final String USERS_URI = "/api/v1/users";
    private static final String USERS_REGISTER_URI = "/api/v1/users/register";

    // Database IDs constants (SQL Script)
    private final UUID ADMIN_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private final UUID ATTENDANT_ID =
            UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

    private final UUID DOCTOR_ID =
            UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    private final UUID INEXISTENT_ID =
            UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");


    private final String ADMIN_PERFIL = "ADMIN";
    private final String USER_PERFIL = "USER";

    // Test credentials constants
    private final String ADMIN_USER = "support@gmail.com";
    private final String ADMIN_PASS = "123456";
    private final String ATTENDANT_USER = "karla@gmail.com";
    private final String ATTENDANT_PASS = "123456";
    private final String DOCTOR_USER = "kaio@gmail.com";
    private final String DOCTOR_PASS = "123456";


    // ===========================================
    // TEST BLOCK: POST /api/v1/users (Create User)
    // =============================================

    @Test
    public void createUser_WithValidCredentials_ReturnsCreatedUserWithStatus201() {
        UserResponseDto responseBody = testClient
                .post()
                .uri(USERS_REGISTER_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserRequestDto("cleber@gmail.com", "123456"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.email()).isEqualTo("cleber@gmail.com");
        org.assertj.core.api.Assertions.assertThat(responseBody.perfil()).isEqualTo(USER_PERFIL);

    }

    @Test
    public void createUser_WithDuplicateUsername_ReturnsStatus409() {
        ErrorMessage responseBody = testClient
                .post()
                .uri(USERS_REGISTER_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserRequestDto(ADMIN_USER, "123456"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void createUser_WithInvalidData_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .post()
                .uri(USERS_REGISTER_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserRequestDto("", ""))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    // ===========================================
    // TEST BLOCK: GET /api/v1/users/{id} (Get User by Id)
    // =============================================

    @Test
    public void getUserById_WithValidId_ReturnsUserWithStatus200() {
        UserResponseDto responseBody = testClient
                .get()
                .uri(USERS_URI + "/" + ADMIN_ID)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(ADMIN_ID);
        org.assertj.core.api.Assertions.assertThat(responseBody.email()).isEqualTo(ADMIN_USER);
        org.assertj.core.api.Assertions.assertThat(responseBody.perfil()).isEqualTo(ADMIN_PERFIL);
    }

    @Test
    public void getUserById_WithInexistentId_ReturnsStatus404() {
        ErrorMessage responseBody = testClient
                .get()
                .uri(USERS_URI + "/" + INEXISTENT_ID)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void getUserById_WithoutToken_ReturnsStatus401() {
        testClient
                .get()
                .uri(USERS_URI + "/" + ADMIN_ID)
                .exchange()
                .expectStatus().isUnauthorized();

    }

    @Test
    public void getUserById_WithUserTryingToAccessAnotherUser_ReturnsStatus403() {
        ErrorMessage responseBody = testClient
                .get()
                .uri(USERS_URI + "/" + ADMIN_ID)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ATTENDANT_USER, ATTENDANT_PASS))
                .exchange()
                .expectStatus().isEqualTo(403)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    // ===========================================
    // TEST BLOCK: GET /api/v1/users (Get All Users)
    // =============================================

    @Test
    public void getAllUsers_ReturnsListOfUsersWithStatus200() {
        String responseBody = testClient
                .get()
                .uri(USERS_URI)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody).contains("\"content\"");
        org.assertj.core.api.Assertions.assertThat(responseBody).contains("\"totalElements\"");
    }

    @Test
    public void getAllUsers_FiltersEmailAndPerfilBeforePagination() {
        testClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USERS_URI)
                        .queryParam("search", "KAIO")
                        .queryParam("perfil", "DOCTOR")
                        .queryParam("page", 0)
                        .queryParam("size", 1)
                        .build())
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].email").isEqualTo(DOCTOR_USER)
                .jsonPath("$.content[0].perfil").isEqualTo("DOCTOR")
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    public void getAllUsers_WithoutToken_ReturnsStatus401() {
        testClient
                .get()
                .uri(USERS_URI)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void getAllUsers_NonAdminCannotSearchUsers() {
        testClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(USERS_URI)
                        .queryParam("search", "kaio")
                        .build())
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ATTENDANT_USER, ATTENDANT_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    // ===========================================
    // TEST BLOCK: PATCH /api/v1/users/{id}/password (Update Password)
    // =================================================================

    @Test
    public void updatePassword_WithValidData_ReturnsUserWithStatus200() {
        UserResponseDto responseBody = testClient
                .patch()
                .uri(USERS_URI + "/" + ADMIN_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto(ADMIN_PASS, "654321", "654321"))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(ADMIN_ID);
    }

    @Test
    public void updateMyPassword_WithValidData_ReturnsAuthenticatedUserWithStatus200() {
        UserResponseDto responseBody = testClient
                .patch()
                .uri(USERS_URI + "/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto(ATTENDANT_PASS, "654321", "654321"))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ATTENDANT_USER, ATTENDANT_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDto.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(ATTENDANT_ID);
        org.assertj.core.api.Assertions.assertThat(responseBody.email()).isEqualTo(ATTENDANT_USER);
    }

    @Test
    public void updateMyPassword_WithoutToken_ReturnsStatus401() {
        testClient
                .patch()
                .uri(USERS_URI + "/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto(ATTENDANT_PASS, "654321", "654321"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void updatePassword_WithInvalidCurrentPassword_ReturnsStatus400() {
        ErrorMessage responseBody = testClient
                .patch()
                .uri(USERS_URI + "/" + ADMIN_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto("wrongpass", "654321", "654321"))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(400)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(400);
    }

    @Test
    public void updatePassword_WithMismatchedPasswords_ReturnsStatus400() {
        ErrorMessage responseBody = testClient
                .patch()
                .uri(USERS_URI + "/" + ADMIN_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto(ADMIN_PASS, "654321", "111111"))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(400)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(400);
    }

    @Test
    public void updatePassword_WithInexistentId_ReturnsStatus404() {
        ErrorMessage responseBody = testClient
                .patch()
                .uri(USERS_URI + "/" + INEXISTENT_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto(ADMIN_PASS, "654321", "654321"))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void updatePassword_WithInvalidData_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .patch()
                .uri(USERS_URI + "/" + ADMIN_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto("", "", ""))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void updatePassword_WithoutToken_ReturnsStatus401() {
        testClient
                .patch()
                .uri(USERS_URI + "/" + ADMIN_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto(ADMIN_PASS, "654321", "654321"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void updatePassword_WithUserTryingToUpdateAnotherUser_ReturnsStatus403() {
        ErrorMessage responseBody = testClient
                .patch()
                .uri(USERS_URI + "/" + DOCTOR_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto(DOCTOR_PASS, "654321", "654321"))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ATTENDANT_USER, ATTENDANT_PASS))
                .exchange()
                .expectStatus().isEqualTo(403)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);
    }

    @Test
    public void updateDoctorPassword_AdminUpdateIsAudited() {
        testClient
                .patch()
                .uri(USERS_URI + "/" + DOCTOR_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto(DOCTOR_PASS, "654321", "654321"))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk();

        testClient
                .get()
                .uri(USERS_URI + "/audit/password-resets")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].actorEmailMasked").isEqualTo("s***@g***.com")
                .jsonPath("$.content[0].targetEmailMasked").isEqualTo("k***@g***.com")
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    public void updatePassword_DoctorCannotUpdateOwnPassword_ReturnsStatus403() {
        testClient
                .patch()
                .uri(USERS_URI + "/" + DOCTOR_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto(DOCTOR_PASS, "654321", "654321"))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, DOCTOR_USER, DOCTOR_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void resetDoctorPassword_AdminCanResetWithoutCurrentPassword() {
        UserResponseDto responseBody = testClient
                .patch()
                .uri(USERS_URI + "/" + DOCTOR_ID + "/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordResetRequestDto("654321", "654321"))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDto.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(DOCTOR_ID);

        testClient
                .get()
                .uri(USERS_URI + "/audit/password-resets")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].actorEmailMasked").isEqualTo("s***@g***.com")
                .jsonPath("$.content[0].targetEmailMasked").isEqualTo("k***@g***.com")
                .jsonPath("$.content[0].createdAt").isNotEmpty()
                .jsonPath("$.totalElements").isEqualTo(1);

        testClient
                .get()
                .uri(USERS_URI + "/audit/password-resets")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ATTENDANT_USER, ATTENDANT_PASS))
                .exchange()
                .expectStatus().isForbidden();

        testClient
                .get()
                .uri(USERS_URI + "/" + DOCTOR_ID)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, DOCTOR_USER, "654321"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void resetDoctorPassword_ClientCannotResetPassword() {
        testClient
                .patch()
                .uri(USERS_URI + "/" + DOCTOR_ID + "/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordResetRequestDto("654321", "654321"))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ATTENDANT_USER, ATTENDANT_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void passwordRecovery_ClientCanResetWithEmailCode() {
        testClient
                .post()
                .uri("/api/v1/auth/password-recovery/request")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRecoveryRequestDto(ATTENDANT_USER))
                .exchange()
                .expectStatus().isAccepted();

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        Matcher codeMatcher = Pattern.compile("\\b(\\d{6})\\b").matcher(messageCaptor.getValue().getText());
        org.assertj.core.api.Assertions.assertThat(codeMatcher.find()).isTrue();
        String recoveryCode = codeMatcher.group(1);

        testClient
                .post()
                .uri("/api/v1/auth/password-recovery/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRecoveryConfirmDto(
                        ATTENDANT_USER,
                        recoveryCode,
                        "654321",
                        "654321"
                ))
                .exchange()
                .expectStatus().isNoContent();

        testClient
                .get()
                .uri(USERS_URI + "/" + ATTENDANT_ID)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ATTENDANT_USER, "654321"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void passwordRecovery_DoesNotSendCodeToNonClientAccounts() {
        testClient
                .post()
                .uri("/api/v1/auth/password-recovery/request")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRecoveryRequestDto(DOCTOR_USER))
                .exchange()
                .expectStatus().isAccepted();

        verifyNoInteractions(mailSender);
    }

    @Test
    public void adminDashboardSummary_AdminCanAccess() {
        AdminDashboardSummaryDto responseBody = testClient
                .get()
                .uri("/api/v1/admin/dashboard/summary")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdminDashboardSummaryDto.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.appointmentsToday()).isGreaterThanOrEqualTo(0);
        org.assertj.core.api.Assertions.assertThat(responseBody.activeDoctors()).isGreaterThanOrEqualTo(0);
        org.assertj.core.api.Assertions.assertThat(responseBody.registeredPatients()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(responseBody.dailyAppointments()).hasSize(7);
    }

    @Test
    public void adminDashboardSummary_NonAdminCannotAccess() {
        testClient
                .get()
                .uri("/api/v1/admin/dashboard/summary")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ATTENDANT_USER, ATTENDANT_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void adminDashboardSummary_ReturnsRequestedDateRange() {
        java.time.LocalDate endDate = java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo"));
        java.time.LocalDate startDate = endDate.minusDays(14);

        AdminDashboardSummaryDto responseBody = testClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/admin/dashboard/summary")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build())
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdminDashboardSummaryDto.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.dailyAppointments()).hasSize(15);
    }

    @Test
    public void getAllDoctors_AdminCanAccess() {
        testClient
                .get()
                .uri("/api/v1/doctors")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").isNumber();
    }

    @Test
    public void getAllDoctors_NonAdminCannotAccess() {
        testClient
                .get()
                .uri("/api/v1/doctors")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ATTENDANT_USER, ATTENDANT_PASS))
                .exchange()
                .expectStatus().isForbidden();
    }

}
