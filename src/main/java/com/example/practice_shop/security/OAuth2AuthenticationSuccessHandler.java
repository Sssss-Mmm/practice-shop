package com.example.practice_shop.security;

import com.example.practice_shop.entity.User;
import com.example.practice_shop.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 인증 성공 시 호출되는 핸들러입니다.
 * 인증된 사용자에 대해 JWT 토큰을 생성하고, 클라이언트(프론트엔드)로 리디렉션합니다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 인증 성공 시 실행되는 메소드입니다.
     *
     * @param request        HTTP 요청
     * @param response       HTTP 응답
     * @param authentication 인증 정보
     * @throws IOException      입출력 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 인증된 사용자 정보를 가져옵니다.
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // 이메일을 기반으로 데이터베이스에서 사용자를 찾습니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 해당 사용자에 대한 Access Token과 Refresh Token을 생성합니다.
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // Refresh Token을 데이터베이스에 저장하여 추후 유효성 검증에 사용합니다.
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // 프론트엔드로 리디렉션할 URL을 생성합니다. 토큰 정보를 쿼리 파라미터로 포함합니다.
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        // 생성된 URL로 클라이언트를 리디렉션합니다.
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
