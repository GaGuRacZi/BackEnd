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

    /**
     * Creates a cookie utility with the mapper and cookie security settings to use.
     *
     * @param objectMapper  the mapper used for cookie value serialization
     * @param cookieSecure  whether generated cookies use the Secure attribute
     * @param cookieSameSite the SameSite attribute for generated cookies
     */
    public CookieUtils(
            ObjectMapper objectMapper,
            @Value("${app.cookie.secure:false}") boolean cookieSecure,
            @Value("${app.cookie.same-site:Lax}") String cookieSameSite
    ) {
        this.objectMapper = objectMapper;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    /**
     * Finds the first request cookie with the specified name.
     *
     * @param request the HTTP request containing the cookies
     * @param name    the name of the cookie to find
     * @return the matching cookie, or an empty optional if no cookie matches
     */
    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst();
    }

    /**
     * Adds a cookie to the HTTP response with the configured security attributes.
     *
     * @param response       the HTTP response to which the cookie is added
     * @param name           the cookie name
     * @param value          the cookie value
     * @param maxAgeSeconds  the cookie lifetime in seconds
     */
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

    /**
     * Deletes cookies with the specified name from the response.
     *
     * @param request  the request containing the cookies to delete
     * @param response the response to which deletion headers are added
     * @param name     the name of the cookies to delete
     */
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

    /**
     * Serializes an object into a URL-safe Base64-encoded JSON value for use in a cookie.
     *
     * @param object the object to serialize
     * @return the URL-safe Base64-encoded JSON representation
     * @throws IllegalStateException if serialization fails
     */
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

    /**
     * Deserializes a cookie value into an instance of the specified class.
     *
     * @param cookie the cookie containing a URL-safe Base64-encoded JSON value
     * @param cls    the class of the object to create
     * @param <T>    the deserialized object type
     * @return       the object represented by the cookie value
     * @throws IllegalStateException if the cookie value cannot be decoded or deserialized
     */
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
