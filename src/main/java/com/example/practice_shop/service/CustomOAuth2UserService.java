package com.example.practice_shop.service;

import com.example.practice_shop.constant.Role;
import com.example.practice_shop.constant.Status;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Security의 OAuth2UserService 인터페이스를 구현한 커스텀 서비스입니다.
 * OAuth2 공급자(예: Google)로부터 사용자 정보를 가져온 후의 로직을 담당합니다.
 */
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    /**
     * OAuth2 공급자로부터 사용자 정보를 로드합니다.
     * 이 메소드는 사용자가 OAuth2 로그인을 성공적으로 완료하고, 공급자가 사용자 정보를 리디렉션할 때 호출됩니다.
     *
     * @param userRequest OAuth2 사용자 요청 정보
     * @return 처리된 사용자 정보를 담은 OAuth2User 객체
     * @throws OAuth2AuthenticationException 인증 처리 중 예외 발생 시
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OAuth2UserService를 사용하여 사용자 정보를 로드합니다.
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 현재 로그인 진행 중인 서비스를 구분하는 ID (예: google, naver, ...)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // OAuth2 로그인 진행 시 키가 되는 필드값. (PK와 같은 의미)
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // OAuth2UserService를 통해 가져온 OAuth2User의 attribute를 담을 Map
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 각 OAuth2 공급자(Google, Naver 등)에 따라 속성을 파싱하는 로직이 필요할 수 있습니다.
        // 여기서는 간단하게 이메일과 이름만 추출합니다.
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // 데이터베이스에 사용자가 이미 있는지 확인하고, 없으면 새로 저장하거나 있으면 업데이트합니다.
        User user = saveOrUpdate(email, name, registrationId);

        // Spring Security에서 사용할 OAuth2User 객체를 반환합니다.
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().toString())),
                attributes,
                userNameAttributeName);
    }

    /**
     * 데이터베이스에 사용자 정보를 저장하거나 업데이트합니다.
     *
     * @param email    사용자 이메일
     * @param name     사용자 이름
     * @param provider OAuth2 공급자 ID
     * @return 저장되거나 업데이트된 User 엔티티
     */
    private User saveOrUpdate(String email, String name, String provider) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            // 이미 존재하는 사용자인 경우, 정보를 업데이트할 수 있습니다.
            user = userOptional.get();
        } else {
            // 새로운 사용자인 경우, User 엔티티를 생성하여 데이터베이스에 저장합니다.
            user = User.builder()
                    .email(email)
                    .name(name)
                    .provider(provider)
                    .providerId(provider + "_" + UUID.randomUUID())
                    .role(Role.USER) // 기본 역할을 USER로 설정
                    .status(Status.ACTIVE) // 계정 상태를 활성으로 설정
                    .build();
        }
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
            user.setEmailVerificationToken(null);
            user.setEmailVerificationExpiredAt(null);
            user.setEmailVerificationSentAt(null);
            user.setStatus(Status.ACTIVE);
        }
        userRepository.save(user);
        return user;
    }
}
