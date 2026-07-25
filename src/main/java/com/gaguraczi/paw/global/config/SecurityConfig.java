package com.gaguraczi.paw.global.config;

//import com.gaguraczi.paw.domain.auth.service.NaverOAuth2LoginSuccessHandler;
//import com.gaguraczi.paw.domain.auth.service.NaverOAuth2UserService;
//import com.gaguraczi.paw.global.security.HttpCookieOAuth2AuthorizationRequestRepository;
import com.gaguraczi.paw.global.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    //private final NaverOAuth2UserService devNaverOAuth2UserService;
    //private final NaverOAuth2LoginSuccessHandler devNaverOAuth2LoginSuccessHandler;
    /**
     * Configures the stateless HTTP security filter chain with CORS, JWT authentication,
     * authorization rules, and a JSON response for authentication failures.
     *
     * @param http the HTTP security configuration
     * @return the configured security filter chain
     * @throws Exception if the security configuration cannot be built
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write(
                                    "{\"isSuccess\":false,\"code\":\"JWT_401\",\"message\":\"인증이 필요합니다.\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/signup/**",
                                "/auth/login/**",
                                "/auth/email/**",
                                "/auth/reissue",
                                "/auth/logout",
                                "/auth/link/confirm/**"
                        ).permitAll()
                        .requestMatchers("/auth/onboarding", "/auth/onboarding/**", "/auth/link/kakao").authenticated()
                        .requestMatchers("/dev/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/health", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                /*
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(ep -> ep
                                .baseUri("/dev/oauth2/authorization")
                                .authorizationRequestRepository(authRequestRepository)
                        )
                        .redirectionEndpoint(ep -> ep
                                .baseUri("/dev/login/oauth2/code/*")
                        )
                        .userInfoEndpoint(ep -> ep
                                .userService(devNaverOAuth2UserService)
                        )
                        .successHandler(devNaverOAuth2LoginSuccessHandler)
                        .failureHandler((request, response, ex) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            String msg = ex.getMessage() != null
                                    ? ex.getMessage().replace("\"", "'")
                                    : "인증 처리 중 오류가 발생했습니다";
                            response.getWriter().write(
                                    "{\"isSuccess\":false,\"code\":\"NAVER_LOGIN_401\",\"message\":\"네이버 로그인 실패: "
                                            + msg + "\"}");
                        })
                )*/
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Creates the password encoder used to hash and verify passwords.
     *
     * @return a BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures cross-origin resource sharing for all application paths.
     *
     * @return the CORS configuration source allowing the configured origins, methods, and headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
