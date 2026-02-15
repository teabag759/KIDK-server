package com.kidk.api.domain.auth.controller;

import com.kidk.api.domain.auth.dto.AuthRequest;
import com.kidk.api.domain.auth.dto.AuthResponse;
import com.kidk.api.domain.auth.service.AuthService;
import com.kidk.api.domain.refreshtoken.entity.RefreshToken;
import com.kidk.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /// 회원가입 API (로그인/회원가입 통합)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody AuthRequest request) {
        ApiResponse<AuthResponse> response = ApiResponse.success(authService.loginOrRegister(request));
        return ResponseEntity.ok(response);
    }

    /// 로그인 API
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest request) {
        ApiResponse<AuthResponse> response = ApiResponse.success(authService.loginOrRegister(request));
        return ResponseEntity.ok(response);
    }

    /// Refresh Token으로 Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        AuthResponse authResponse = authService.refresh(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    // 디바이스 목록 조회
    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<RefreshToken>>> getDevices(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            org.springframework.security.core.userdetails.UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(authService.getDevices(userDetails.getUsername())));
    }

    // 디바이스 삭제
    @DeleteMapping("/devices/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            org.springframework.security.core.userdetails.UserDetails userDetails,
            @PathVariable String deviceId) {
        authService.deleteDevice(userDetails.getUsername(), deviceId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /// 로그아웃 API
    // 헤더에 'Refresh-Token' 키로 토큰을 담아 보냄
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Refresh-Token") String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
