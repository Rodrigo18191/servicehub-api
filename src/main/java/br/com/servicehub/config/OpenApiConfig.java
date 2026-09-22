package br.com.servicehub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados exibidos no topo do Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI serviceHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServiceHub API")
                        .version("1.0.0 - Checkpoint")
                        .description("""
                                API REST da plataforma ServiceHub para gerenciar o catálogo de serviços.

                                Todas as respostas de erro seguem o formato padrão `ErrorResponse`.
                                """)
                        .contact(new Contact()
                                .name("Rodrigo Henrique Toniolli Oliveira")));
    }
}
