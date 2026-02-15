package com.kidk.api.domain.auth.controller.dev;

import com.kidk.api.domain.auth.dto.AuthResponse;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.response.ApiResponse;
import com.kidk.api.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth/dev")
@RequiredArgsConstructor
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    /// 개발용(테스트유저)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> devLogin() {
        // 테스트 유저 생성(존재하는 경우 조회)
        String testUid = "test-firebase-uid";
        User user = userRepository.findByFirebaseUid(testUid)
                .orElseGet(() -> userRepository.save(User.builder()
                        .firebaseUid(testUid)
                        .email("test@example.com")
                        .name("테스트유저")
                        .userType("PARENT")     // or CHILD
                        .status("ACTIVE")
                        .build()));

        // JWT 강제 발급(검증 생략)
        String accessToken = jwtProvider.createAccessToken(user.getFirebaseUid(), user.getUserType(), user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getFirebaseUid());

        ApiResponse<AuthResponse> response = ApiResponse.success(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .userType(user.getUserType())
                .build());

        return ResponseEntity.ok(response);
    }

}
