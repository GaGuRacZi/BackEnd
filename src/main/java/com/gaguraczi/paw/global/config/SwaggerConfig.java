package com.gaguraczi.paw.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${paw.swagger.prod-server-url}")
    private String prodServerUrl;

    /**
     * Configures the OpenAPI definition for the Paw API, including metadata, JWT bearer security,
     * and local/prod servers. Local remains first so Try it out defaults to the local server.
     */
    @Bean
    public OpenAPI openAPI() {
        Info apiInfo = new Info()
                .version("v1.0.0")
                .title("Paw API")
                .description("""
                        Paw API Documentation
                        
                        ## 공통
                        - 응답 래퍼: `{ isSuccess, code, message, result }`
                        - 인증: Authorize에 `Bearer {accessToken}` (JWT)
                        - 각 엔드포인트 Responses 탭에 성공/에러 코드·JSON 예시 포함
                        
                        ## 주요 태그
                        - auth / users / pets / location / terms / breeds / regions
                        - communities / comments
                        """);

        String jwtSchemeName = "BearerToken";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Paw Local Server");

        Server prodServer = new Server()
                .url(prodServerUrl)
                .description("Paw Prod Server");

        return new OpenAPI()
                .info(apiInfo)
                .addSecurityItem(securityRequirement)
                .components(components)
                .servers(List.of(localServer, prodServer));
    }

    /**
     * Creates the OpenAPI group containing all API paths.
     *
     * @return the configured group named {@code All APIs}
     */
    @Bean
    public GroupedOpenApi allGroup() {
        return GroupedOpenApi.builder()
                .group("All APIs")
                .pathsToMatch("/**")
                .build();
    }
}
