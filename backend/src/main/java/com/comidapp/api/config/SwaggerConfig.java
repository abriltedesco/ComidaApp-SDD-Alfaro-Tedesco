package com.comidapp.api.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Configuración OpenAPI/Swagger — migrada de TP3 com.example.demo.security.SwaggerConfig.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "ComidApp API",
        version = "1.0",
        description = "API REST para la plataforma de pedidos de comida ComidApp"
))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {
}
