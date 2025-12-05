package com.kidk.api.domain.user.controller;

import com.kidk.api.domain.user.dto.UserRequest;
import com.kidk.api.domain.user.dto.UserResponse;
import com.kidk.api.domain.user.service.UserService;
import com.kidk.api.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /// 내 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /// 내 프로필 수정
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserRequest.Update request
    ) {
        UserResponse response = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /// 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal UserDetails userDetails) {
        userService.withdraw(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /// 프로필 이미지 업로드
    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = userService.updateProfileImage(userDetails.getUsername(), file);
        return ResponseEntity.ok(ApiResponse.success(imageUrl));
    }

    /// 특정 유저 조회 (가족 초대 시 확인용 등)
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@PathVariable Long userId) {
        UserResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /// 상태 변경 (필요 시)
    @PatchMapping("/me/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserRequest.StatusUpdate request
    ) {
        userService.updateStatus(userDetails.getUsername(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.success());
    }
}