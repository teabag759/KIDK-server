package com.kidk.api.domain.auth.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.kidk.api.domain.auth.dto.AuthRequest;
import com.kidk.api.domain.auth.dto.AuthResponse;
import com.kidk.api.domain.refreshtoken.repository.RefreshTokenRepository;
import com.kidk.api.domain.refreshtoken.service.RefreshTokenService;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import com.kidk.api.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;

    /// 로그인/회원가입
    public AuthResponse loginOrRegister(AuthRequest request) {
        String firebaseUid;
        String email;

        // 1. Firebase 토큰 검증
        try {
             FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.getFirebaseToken());
             firebaseUid = decodedToken.getUid();
             email = decodedToken.getEmail();

        } catch (Exception e) {
            log.error("Firebase Token Verification Failed", e);
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 2. 유저 조회 또는 회원가입 (없으면 생성)
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .firebaseUid(firebaseUid)
                            .email(email)
                            .name("New User") // 초기 이름
                            .userType("UNKNOWN") // 가입 후 선택 필요 (PARENT/CHILD)
                            .socialProvider("KAKAO") // 임시
                            .status("ACTIVE")
                            .build();
                    return userRepository.save(newUser);
                });

        // 3. JWT 토큰 발급
        String accessToken = jwtProvider.createAccessToken(user.getFirebaseUid(), user.getUserType());
        String refreshTokenStr = jwtProvider.createRefreshToken(user.getFirebaseUid());

        // 4. Refresh Token DB 저장 (기기별 관리)
        // 기존에 해당 기기의 토큰이 있다면 삭제하거나 업데이트
        refreshTokenRepository.findByUserAndDeviceId(user, request.getDeviceId())
                .ifPresent(refreshTokenRepository::delete);

        refreshTokenService.saveOrUpdate(user, refreshTokenStr, request.getDeviceId());

        // 5. 응답 반환
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .userId(user.getId())
                .name(user.getName())
                .userType(user.getUserType())
                .build();
    }

    // 로그아웃
    public void logout(String refreshToken) {
        // DB에서 해당 리프레시 토큰 삭제
        refreshTokenService.deleteToken(refreshToken);
    }
}