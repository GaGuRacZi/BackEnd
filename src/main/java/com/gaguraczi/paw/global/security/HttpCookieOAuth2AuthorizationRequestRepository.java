package com.gaguraczi.paw.global.security;

import com.gaguraczi.paw.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

// Dev OAuth2 인가 요청(state 등)과 로그인 성공 후 리다이렉트할 URI를 쿠키에 보관.
// dev_oauth2_auth_request : 직렬화된 OAuth2AuthorizationRequest
// dev_redirect_uri        : 프론트가 /dev/oauth2/authorization/naver?dev_redirect_uri=... 로 전달한 값
@Component
@RequiredArgsConstructor
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String DEV_OAUTH2_AUTH_REQUEST_COOKIE = "dev_oauth2_auth_request";
    public static final String DEV_REDIRECT_URI_PARAM_COOKIE_NAME = "dev_redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;
    private final CookieUtils cookieUtils;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return cookieUtils.getCookie(request, DEV_OAUTH2_AUTH_REQUEST_COOKIE)
                .map(c -> {
                    try {
                        return cookieUtils.deserialize(c, OAuth2AuthorizationRequest.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeDevAuthorizationRequestCookies(request, response);
            return;
        }

        cookieUtils.addCookie(response, DEV_OAUTH2_AUTH_REQUEST_COOKIE,
                cookieUtils.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);

        // 프론트가 전달한 dev_redirect_uri 쿠키로 보관 (있을 때만)
        String devRedirectUri = request.getParameter(DEV_REDIRECT_URI_PARAM_COOKIE_NAME);
        if (devRedirectUri != null && !devRedirectUri.isBlank()) {
            cookieUtils.addCookie(response, DEV_REDIRECT_URI_PARAM_COOKIE_NAME,
                    devRedirectUri, COOKIE_EXPIRE_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
        cookieUtils.deleteCookie(request, response, DEV_OAUTH2_AUTH_REQUEST_COOKIE);
        return req;
    }

    // 인증 완료 후 모든 Dev OAuth2 관련 쿠키 정리
    public void removeDevAuthorizationRequestCookies(HttpServletRequest request,
                                                   HttpServletResponse response) {
        cookieUtils.deleteCookie(request, response, DEV_OAUTH2_AUTH_REQUEST_COOKIE);
        cookieUtils.deleteCookie(request, response, DEV_REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
