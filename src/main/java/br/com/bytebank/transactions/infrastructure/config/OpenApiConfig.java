package br.com.bytebank.transactions.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        servers = {
                @Server(url = "http://localhost:8080", description = "Gateway")
        }
)
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountsOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("ByteBank Transaction API")
                        .description("API responsible to manage ByteBank transactions")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Thales Fernandes")
                                .email("thalesgarcezf@gmail.com")
                                .url("https://github.com/thalesF93")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Repository")
                        .url("https://github.com/thalesF93/bytebank"));
    }
    }

