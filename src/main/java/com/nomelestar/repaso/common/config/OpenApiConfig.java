package com.nomelestar.repaso.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global para Swagger / OpenAPI.
 * Aquí definimos la información general de la API que aparecerá en la interfaz visual.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Repaso - Gestión de Restaurante",
                version = "1.0",
                description = "Documentación interactiva de la API para gestionar Chefs y sus Platos. Ideal para repasar conceptos de Spring Boot.",
                contact = @Contact(name = "Sebastian Zapata", email = "sebastian.zapata23@eia.edu.co")
        )
)
public class OpenApiConfig {
}
