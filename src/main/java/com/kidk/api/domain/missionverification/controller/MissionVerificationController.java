package com.kidk.api.domain.missionverification.controller;

import com.kidk.api.domain.missionverification.service.MissionVerificationService;
import com.kidk.api.domain.missionverification.entity.MissionVerification;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/missions") // 요구사항의 기본 경로 (v1 포함)
@RequiredArgsConstructor
public class MissionVerificationController {

    private final MissionVerificationService verificationService;

    /// 미션 인증 제출
    @PostMapping("/{missionId}/verifications")
    public ResponseEntity<MissionVerification> submitVerification(
            @PathVariable Long missionId,
            @RequestParam Long childId,
            @RequestParam String verificationType,
            @RequestParam(required = false) String content
    ) {
        MissionVerification missionVerification = verificationService.submitVerification(missionId, childId, verificationType, content);

        return ResponseEntity.ok(missionVerification);
    }

    /// 특정 미션의 인증 내역 조회
    @GetMapping("/{missionId}/verifications")
    public ResponseEntity<List<MissionVerification>> getVerifications(@PathVariable Long missionId) {
        List<MissionVerification> missionVerifications = verificationService.getByMission(missionId);

        return ResponseEntity.ok(missionVerifications);
    }

    /// 인증 승인
    @PatchMapping("/{missionId}/verifications/{verificationId}/approve")
    public ResponseEntity<MissionVerification> approve(
            @PathVariable Long missionId,       // URL 맞추기용 (사용 안해도 됨)
            @PathVariable Long verificationId,
            @RequestParam Long parentId
    ) {
        MissionVerification missionVerification = verificationService.approveVerification(verificationId, parentId);

        return ResponseEntity.ok(missionVerification);
    }

    /// 인증 거절
    @PatchMapping("/{missionId}/verifications/{verificationId}/reject")
    public ResponseEntity<MissionVerification> reject(
            @PathVariable Long missionId,
            @PathVariable Long verificationId,
            @RequestParam Long parentId,
            @RequestParam String reason
    ) {
        MissionVerification missionVerification = verificationService.rejectVerification(verificationId, parentId, reason);

        return ResponseEntity.ok(missionVerification);
    }
}