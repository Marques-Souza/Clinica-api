package com.marquesdev.clinica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocOpenApi {

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("security", securityScheme()))
                .info(
                      new Info()
                              .title("REST API - Spring Park")
                              .description("REST API for clinic management, including patients, doctors, and appointments.")
                              .version("1.0.0")
                              .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
                              .contact(new Contact().name("Marques Souza").email("marques.wsouza357@gmail.com"))
                );
    }


    private SecurityScheme securityScheme(){
        return new SecurityScheme()
                .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer) {token}\"")
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("security");
    }
}
