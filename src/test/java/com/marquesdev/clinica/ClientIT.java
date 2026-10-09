package com.marquesdev.clinica;

import com.marquesdev.clinica.dto.ClientRequestDto;
import com.marquesdev.clinica.dto.ClientResponseDto;
import com.marquesdev.clinica.dto.ClientUpdateRequestDto;
import com.marquesdev.clinica.exception.ErrorMessage;
import com.marquesdev.clinica.integration.viacep.ViaCepResponseDto;
import com.marquesdev.clinica.integration.viacep.ViaCepService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Sql(scripts = "/sql/clients/client-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clients/client-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ClientIT {

    @Autowired
    private WebTestClient testClient;

    @MockitoBean
    private ViaCepService viaCepService;

    private static final String CLIENTS_URI = "/api/v1/clients";

    private final String USER_WITH_CLIENT_EMAIL = "karla@gmail.com";
    private final String USER_WITH_CLIENT_PASS = "123456";
    private final String USER_WITHOUT_CLIENT_EMAIL = "joao@gmail.com";
    private final String USER_WITHOUT_CLIENT_PASS = "123456";
    private final String DOCTOR_EMAIL = "kaio@gmail.com";
    private final String DOCTOR_PASS = "123456";

    @BeforeEach
    void setUpViaCepResponse() {
        when(viaCepService.findAddressByCep(anyString()))
                .thenReturn(new ViaCepResponseDto("Praça da Sé", "Sé", "São Paulo", "SP"));
    }

    // ===========================================
    // TEST BLOCK: PUT /api/v1/clients/me (Create Client)
    // =============================================

    @Test
    public void createClient_WithValidData_ReturnsClientWithStatus200() {
        ClientResponseDto responseBody = testClient
                .put()
                .uri(CLIENTS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientRequestDto(
                        "João Silva",
                        "52998224725",
                        "11999999999",
                        "01001000",
                        "123"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITHOUT_CLIENT_EMAIL, USER_WITHOUT_CLIENT_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClientResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.fullName()).isEqualTo("João Silva");
        org.assertj.core.api.Assertions.assertThat(responseBody.cpf()).isEqualTo("52998224725");
        org.assertj.core.api.Assertions.assertThat(responseBody.phone()).isEqualTo("11999999999");
        org.assertj.core.api.Assertions.assertThat(responseBody.email()).isEqualTo(USER_WITHOUT_CLIENT_EMAIL);
    }

    @Test
    public void createClient_WithInvalidCpf_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .put()
                .uri(CLIENTS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientRequestDto(
                        "João Silva",
                        "123",
                        "11999999999",
                        "01001000",
                        "123"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITHOUT_CLIENT_EMAIL, USER_WITHOUT_CLIENT_PASS))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void createClient_WithInvalidCep_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .put()
                .uri(CLIENTS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientRequestDto(
                        "João Silva",
                        "52998224725",
                        "11999999999",
                        "123",
                        "123"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITHOUT_CLIENT_EMAIL, USER_WITHOUT_CLIENT_PASS))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void createClient_WithBlankFields_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .put()
                .uri(CLIENTS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientRequestDto(
                        "",
                        "",
                        "",
                        "",
                        ""
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITHOUT_CLIENT_EMAIL, USER_WITHOUT_CLIENT_PASS))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void createClient_WithoutToken_ReturnsStatus401() {
        testClient
                .put()
                .uri(CLIENTS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientRequestDto(
                        "João Silva",
                        "12345678901",
                        "11999999999",
                        "01001000",
                        "123"
                ))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ===========================================
    // TEST BLOCK: PATCH /api/v1/clients/me (Update Client)
    // =============================================

    @Test
    public void updateClient_WithValidData_ReturnsClientWithStatus200() {
        ClientResponseDto responseBody = testClient
                .patch()
                .uri(CLIENTS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientUpdateRequestDto(
                        "Karla Santos Atualizada",
                        "52998224725",
                        "11988888888",
                        "01001000",
                        "456"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITH_CLIENT_EMAIL, USER_WITH_CLIENT_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClientResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.fullName()).isEqualTo("Karla Santos Atualizada");
        org.assertj.core.api.Assertions.assertThat(responseBody.phone()).isEqualTo("11988888888");
    }

    @Test
    public void updateClient_WithInvalidCpf_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .patch()
                .uri(CLIENTS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientUpdateRequestDto(
                        "Karla Santos",
                        "123",
                        "11987654321",
                        "01001000",
                        "100"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITH_CLIENT_EMAIL, USER_WITH_CLIENT_PASS))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void updateClient_WithInvalidCep_ReturnsStatus422() {
        ErrorMessage responseBody = testClient
                .patch()
                .uri(CLIENTS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientUpdateRequestDto(
                        "Karla Santos",
                        "52998224725",
                        "11987654321",
                        "123",
                        "100"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITH_CLIENT_EMAIL, USER_WITH_CLIENT_PASS))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void updateClient_WhenClientNotFound_ReturnsStatus404() {
        ErrorMessage responseBody = testClient
                .patch()
                .uri(CLIENTS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientUpdateRequestDto(
                        "João Silva",
                        "52998224725",
                        "11999999999",
                        "01001000",
                        "123"
                ))
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITHOUT_CLIENT_EMAIL, USER_WITHOUT_CLIENT_PASS))
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void updateClient_WithoutToken_ReturnsStatus401() {
        testClient
                .patch()
                .uri(CLIENTS_URI + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClientUpdateRequestDto(
                        "João Silva",
                        "12345678901",
                        "11999999999",
                        "01001000",
                        "123"
                ))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ===========================================
    // TEST BLOCK: GET /api/v1/clients/me (Get My Client)
    // =============================================

    @Test
    public void getMyClient_WithExistingClient_ReturnsClientWithStatus200() {
        ClientResponseDto responseBody = testClient
                .get()
                .uri(CLIENTS_URI + "/me")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITH_CLIENT_EMAIL, USER_WITH_CLIENT_PASS))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClientResponseDto.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.email()).isEqualTo(USER_WITH_CLIENT_EMAIL);
    }

    @Test
    public void getMyClient_WhenClientNotFound_ReturnsStatus404() {
        ErrorMessage responseBody = testClient
                .get()
                .uri(CLIENTS_URI + "/me")
                .headers(JwtAuthentication.getHeaderAuthorization(testClient, USER_WITHOUT_CLIENT_EMAIL, USER_WITHOUT_CLIENT_PASS))
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();
        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void getMyClient_WithoutToken_ReturnsStatus401() {
        testClient
                .get()
                .uri(CLIENTS_URI + "/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

}
