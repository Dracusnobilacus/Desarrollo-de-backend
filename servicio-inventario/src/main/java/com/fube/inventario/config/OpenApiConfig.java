package com.fube.inventario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Servicio de Inventario API")
                        .version("1.0")
                        .description("API REST para la gestión de productos, consulta de disponibilidad y reservas de stock.")
                        .contact(new Contact().name("Equipo Backend UTN FRSF")));
    }
}
