package com.marquesdev.clinica;

import com.marquesdev.clinica.dto.UserLoginDto;
import com.marquesdev.clinica.exception.ErrorMessage;
import com.marquesdev.clinica.jwt.JwtToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Sql(scripts = "/sql/users/user-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/user-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AuthenticationIT {

    @Autowired
    private WebTestClient testClient;

    // Constante para a URI base de autenticação
    private static final String AUTH_URI = "/api/v1/auth";

    // Test credentials constants
    private final String ADMIN_USER = "support@gmail.com";
    private final String ADMIN_PASS = "123456";
    private final String INVALID_USER = "invalid@gmail.com";
    private final String INVALID_PASS = "wrongpass";

      // ===========================================
     // TEST BLOCK: POST /api/v1/auth (Authenticate)
    // =============================================

    @Test
    public void authenticate_WithValidCredentials_ReturnsTokenWithStatus200(){
        JwtToken responseBody = testClient
                .post()
                .uri(AUTH_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserLoginDto(ADMIN_USER, ADMIN_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtToken.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getToken()).isNotNull();
    }

    @Test
    public void authenticate_WithInvalidCredentials_ReturnsStatus400(){
        ErrorMessage responseBody = testClient
                .post()
                .uri(AUTH_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserLoginDto(INVALID_USER, INVALID_PASS))
                .exchange()
                .expectStatus().isEqualTo(400)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(400);
    }

    @Test
    public void authenticate_WithInvalidData_ReturnsStatus422(){
        ErrorMessage responseBody = testClient
                .post()
                .uri(AUTH_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserLoginDto("", ""))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

}
