package et.gov.osta.jobportal.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH_SCHEME = "basicAuth";

    @Bean
    public OpenAPI jobPortalOpenApi(
            @Value("${spring.application.name}") String applicationName,
            @Value("${app.api.version:v1}") String apiVersion
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version(apiVersion)
                        .description("OpenAPI definition for the Job Portal service.")
                        .contact(new Contact().name("OSTA Job Portal")))
                .components(new Components()
                        .addSecuritySchemes(BASIC_AUTH_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")));
    }
}
