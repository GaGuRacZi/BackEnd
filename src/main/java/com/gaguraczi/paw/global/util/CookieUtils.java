package com.gaguraczi.paw.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Component
public class CookieUtils {

    private final ObjectMapper objectMapper;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public CookieUtils(
            ObjectMapper objectMapper,
            @Value("${app.cookie.secure:false}") boolean cookieSecure,
            @Value("${app.cookie.same-site:Lax}") String cookieSameSite
    ) {
        this.objectMapper = objectMapper;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst();
    }

    public void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        ResponseCookie responseCookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        if (request.getCookies() == null) return;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                ResponseCookie responseCookie = ResponseCookie.from(name, "")
                        .path("/")
                        .httpOnly(true)
                        .secure(cookieSecure)
                        .sameSite(cookieSameSite)
                        .maxAge(Duration.ZERO)
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
            }
        }
    }

    public String serialize(Object object) {
        try {
            ObjectMapper mapper = objectMapper.copy();
            mapper.registerModules(SecurityJackson2Modules.getModules(CookieUtils.class.getClassLoader()));
            byte[] jsonBytes = mapper.writeValueAsBytes(object);
            return Base64.getUrlEncoder().encodeToString(jsonBytes);
        } catch (Exception e) {
            throw new IllegalStateException("쿠키 직렬화 실패", e);
        }
    }

    public <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            ObjectMapper mapper = objectMapper.copy();
            mapper.registerModules(SecurityJackson2Modules.getModules(CookieUtils.class.getClassLoader()));
            byte[] jsonBytes = Base64.getUrlDecoder().decode(cookie.getValue());
            return mapper.readValue(jsonBytes, cls);
        } catch (Exception e) {
            throw new IllegalStateException("쿠키 역직렬화 실패", e);
        }
    }
}
