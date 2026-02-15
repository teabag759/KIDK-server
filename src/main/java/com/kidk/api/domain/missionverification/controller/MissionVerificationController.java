package com.kidk.api.domain.missionverification.controller;

import com.kidk.api.domain.missionverification.service.MissionVerificationService;
import com.kidk.api.domain.missionverification.entity.MissionVerification;
import com.kidk.api.domain.user.service.UserService;
import com.kidk.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/missions") // 요구사항의 기본 경로 (v1 포함)
@RequiredArgsConstructor
public class MissionVerificationController {

    private final MissionVerificationService verificationService;
    private final UserService userService;

    private Long currentUserId(UserDetails userDetails) {
        return userService.getUserIdByFirebaseUid(userDetails.getUsername());
    }

    /// 미션 인증 제출
    @PostMapping("/{missionId}/verifications")
    public ResponseEntity<ApiResponse<MissionVerification>> submitVerification(
            @PathVariable Long missionId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String verificationType,
            @RequestParam(required = false) String content
    ) {
        MissionVerification missionVerification = verificationService.submitVerification(
                missionId, currentUserId(userDetails), verificationType, content);

        return ResponseEntity.ok(ApiResponse.success(missionVerification));
    }

    /// 특정 미션의 인증 내역 조회
    @GetMapping("/{missionId}/verifications")
    public ResponseEntity<ApiResponse<List<MissionVerification>>> getVerifications(@PathVariable Long missionId) {
        List<MissionVerification> missionVerifications = verificationService.getByMission(missionId);

        return ResponseEntity.ok(ApiResponse.success(missionVerifications));
    }

    /// 인증 승인
    @PatchMapping("/{missionId}/verifications/{verificationId}/approve")
    public ResponseEntity<ApiResponse<MissionVerification>> approve(
            @PathVariable Long missionId,       // URL 맞추기용 (사용 안해도 됨)
            @PathVariable Long verificationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MissionVerification missionVerification = verificationService.approveVerification(
                verificationId, currentUserId(userDetails));

        return ResponseEntity.ok(ApiResponse.success(missionVerification));
    }

    /// 인증 거절
    @PatchMapping("/{missionId}/verifications/{verificationId}/reject")
    public ResponseEntity<ApiResponse<MissionVerification>> reject(
            @PathVariable Long missionId,
            @PathVariable Long verificationId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String reason
    ) {
        MissionVerification missionVerification = verificationService.rejectVerification(
                verificationId, currentUserId(userDetails), reason);

        return ResponseEntity.ok(ApiResponse.success(missionVerification));
    }
}
