package com.kidk.api.domain.auth.controller;

import com.kidk.api.domain.auth.dto.AuthRequest;
import com.kidk.api.domain.auth.dto.AuthResponse;
import com.kidk.api.domain.auth.service.AuthService;
import com.kidk.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /// 로그인 API
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest request) {
        ApiResponse<AuthResponse> response= ApiResponse.success(authService.loginOrRegister(request));
        return ResponseEntity.ok(response);
    }

    /// 로그아웃 API
    // 헤더에 'Refresh-Token' 키로 토큰을 담아 보냄
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Refresh-Token") String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.success());
    }
}