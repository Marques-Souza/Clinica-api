package com.marquesdev.clinica;

import com.marquesdev.clinica.dto.PasswordRequestDto;
import com.marquesdev.clinica.dto.UserRequestDto;
import com.marquesdev.clinica.dto.UserResponseDto;
import com.marquesdev.clinica.exception.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Sql(scripts = "/sql/users/user-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/user-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserIT {

    @Autowired
    private WebTestClient testClient;

    // Constante para a URI base de usuários
    private static final String USERS_URI = "/api/v1/users";

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
    public void createUser_WithValidCredentials_ReturnsCreatedUserWithStatus201(){
        UserResponseDto responseBody = testClient
                .post()
                .uri(USERS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserRequestDto("cleber@gmail.com","123456", ADMIN_PERFIL))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.email()).isEqualTo("cleber@gmail.com");
        org.assertj.core.api.Assertions.assertThat(responseBody.perfil()).isEqualTo(ADMIN_PERFIL);

    }
    @Test
    public void createUser_WithDuplicateUsername_ReturnsStatus409(){
        ErrorMessage responseBody = testClient
                .post()
                .uri(USERS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserRequestDto(ADMIN_USER, "123456", ADMIN_PERFIL))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void createUser_WithInvalidData_ReturnsStatus422(){
        ErrorMessage responseBody = testClient
                .post()
                .uri(USERS_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserRequestDto("", "", ""))
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
    public void getUserById_WithValidId_ReturnsUserWithStatus200(){
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
    public void getUserById_WithInexistentId_ReturnsStatus404(){
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
    public void getUserById_WithoutToken_ReturnsStatus401(){
         testClient
                .get()
                .uri(USERS_URI + "/" + ADMIN_ID)
                .exchange()
                .expectStatus().isUnauthorized();

    }

    @Test
    public void getUserById_WithUserTryingToAccessAnotherUser_ReturnsStatus403(){
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
    public void getAllUsers_ReturnsListOfUsersWithStatus200(){
        UserResponseDto[] responseBody = testClient
                .get()
                .uri(USERS_URI)
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDto[].class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.length).isEqualTo(3);
    }

    @Test
    public void getAllUsers_WithoutToken_ReturnsStatus401(){
      testClient
                .get()
                .uri(USERS_URI)
                .exchange()
                .expectStatus().isUnauthorized();
    }

      // ===========================================
     // TEST BLOCK: PATCH /api/v1/users/{id}/password (Update Password)
    // =================================================================

    @Test
    public void updatePassword_WithValidData_ReturnsUserWithStatus200(){
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
    public void updatePassword_WithInvalidCurrentPassword_ReturnsStatus400(){
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
    public void updatePassword_WithMismatchedPasswords_ReturnsStatus400(){
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
    public void updatePassword_WithInexistentId_ReturnsStatus404(){
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
    public void updatePassword_WithInvalidData_ReturnsStatus422(){
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
    public void updatePassword_WithoutToken_ReturnsStatus401(){
         testClient
                .patch()
                .uri(USERS_URI + "/" + ADMIN_ID + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDto(ADMIN_PASS, "654321", "654321"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void updatePassword_WithUserTryingToUpdateAnotherUser_ReturnsStatus403(){
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

}
