/*package com.gaguraczi.paw.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FirebaseConfig {

    // JSON 파싱
    private final ObjectMapper objectMapper;
    @Value("${fcm.type}")
    private String type;

    @Value("${fcm.project-id}")
    private String projectId;

    @Value("${fcm.private-key-id}")
    private String privateKeyId;

    @Value("${fcm.private-key}")
    private String privateKey;

    @Value("${fcm.client-email}")
    private String clientEmail;

    @Value("${fcm.client-id}")
    private String clientId;

    @Value("${fcm.auth-uri}")
    private String authUri;

    @Value("${fcm.token-uri}")
    private String tokenUri;

    @Value("${fcm.auth-provider-x509-cert-url}")
    private String authProviderX509CertUrl;

    @Value("${fcm.client-x509-cert-url}")
    private String clientX509CertUrl;

    @Value("${fcm.universe-domain}")
    private String universeDomain;

    public FirebaseConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Firebase SDK 초기화
    @PostConstruct
    public void init() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            Map<String, String> fcmProperties = new HashMap<>();
            fcmProperties.put("type", type);
            fcmProperties.put("project_id", projectId);
            fcmProperties.put("private_key_id", privateKeyId);
            fcmProperties.put("private_key", privateKey.replace("\\n", "\n"));
            fcmProperties.put("client_email", clientEmail);
            fcmProperties.put("client_id", clientId);
            fcmProperties.put("auth_uri", authUri);
            fcmProperties.put("token_uri", tokenUri);
            fcmProperties.put("auth_provider_x509_cert_url", authProviderX509CertUrl);
            fcmProperties.put("client_x509_cert_url", clientX509CertUrl);
            fcmProperties.put("universe_domain", universeDomain);

            String jsonCredentials = objectMapper.writeValueAsString(fcmProperties);
            byte[] credentialsBytes = jsonCredentials.getBytes(StandardCharsets.UTF_8);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsBytes)))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized with environment variables.");
        } catch (Exception e) {
            log.error("Firebase initialization failed: {}", e.getMessage(), e);
        }
    }
}*/
