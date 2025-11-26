package com.kidk.api.domain.auth;

import com.kidk.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 로그인 API
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody AuthRequest request) {
        return ApiResponse.success(authService.loginOrRegister(request));
    }

    // 로그아웃 API
    // 헤더에 'Refresh-Token' 키로 토큰을 담아 보낸다고 가정
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Refresh-Token") String refreshToken) {
        authService.logout(refreshToken);
        return ApiResponse.success();
    }
}