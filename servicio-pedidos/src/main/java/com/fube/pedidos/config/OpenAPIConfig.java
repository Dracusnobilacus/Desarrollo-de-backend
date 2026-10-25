package com.fube.pedidos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Servicio de Pedidos")
                        .version("1.0.0")
                        .description("Microservicio encargado de la administración y ciclo de vida de pedidos, reserva y liberación de stock.")
                        .contact(new Contact().name("Fube").email("contacto@fube.com")));
    }
}
