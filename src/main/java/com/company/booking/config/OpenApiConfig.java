package com.company.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI / Swagger para documentación de la API.
 * La UI está disponible en: http://localhost:8080/swagger-ui.html
 * El JSON del spec está en: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookingOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Booking Request API")
                .version("1.0.0")
                .description("""
                        API REST para gestión de solicitudes de embarque (Booking Requests).
                        
                        **Arquitectura Hexagonal:** domain, application, infrastructure.
                        
                        **Reglas de negocio:**
                        - El booking debe tener al menos un ítem
                        - Solo se puede actualizar/modificar en estado DRAFT
                        - Solo se puede eliminar (soft delete) en estado DRAFT o CANCELLED
                        - Transiciones válidas: DRAFT → CONFIRMED, CONFIRMED → CANCELLED
                        - CANCELLED es estado terminal
                        """)
                .contact(new Contact()
                    .name("Equipo de Desarrollo")
                    .email("dev@company.com"))
            )
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Servidor local de desarrollo")
            ));
    }
}
