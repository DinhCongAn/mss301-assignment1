package com.fptu.fucarrenting.rentingservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rentingServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FU Car Renting - Renting Service")
                        .version("1.0.0")
                        .description(
                                "Renting transaction, history "
                                        + "and reporting APIs"
                        )
                )
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("API Gateway")
                ))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                );
    }
}