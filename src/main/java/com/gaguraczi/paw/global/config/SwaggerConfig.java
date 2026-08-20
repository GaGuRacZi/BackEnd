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
                        - visits (진료 녹음·전사·처방·AI 요약) / medications (처방 CATALOG 검색)
                        - Walk (`/api/walks`): 수동 기록 + 타이머(Redis 6시간). 날씨/강도는 **한글**
                        - Expense (`/api/v1/.../expenses`): 의료비. 결제금액 = 세부항목 합계
                        - pet-weights (`/pets/{petId}/weights`): 체중. multipart 사진 최대 3장
                        
                        ## 진료 기록 화면 흐름
                        1. `POST /visits` 로 녹음 업로드 → 즉시 `status=PROCESSING`
                        2. 목록/상세 폴링 또는 FCM(`VISIT_READY` / `VISIT_FAILED`)으로 완료 확인
                        3. `READY` 후 `GET /visits/{id}` 요약, `GET /visits/{id}/transcript` 전사문
                        4. 약물: `GET /medications` 검색 → `POST /visits/{id}/medications` (CATALOG 또는 CUSTOM)
                        5. `POST /visits/{id}/ai-summary` 로 코인 1개 AI 상세 요약
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

    @Bean
    public GroupedOpenApi visitGroup() {
        return GroupedOpenApi.builder()
                .group("visits")
                .pathsToMatch("/visits/**", "/medications/**", "/users/me")
                .build();
    }
}
