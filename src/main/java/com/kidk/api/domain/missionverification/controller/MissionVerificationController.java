package com.kidk.api.domain.missionverification.controller;

import com.kidk.api.domain.missionverification.service.MissionVerificationService;
import com.kidk.api.domain.missionverification.entity.MissionVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/missions") // 요구사항의 기본 경로 (v1 포함)
@RequiredArgsConstructor
public class MissionVerificationController {

    private final MissionVerificationService verificationService;

    // 1. 미션 인증 제출
    @PostMapping("/{missionId}/verifications")
    public MissionVerification submitVerification(
            @PathVariable Long missionId,
            @RequestParam Long childId,
            @RequestParam String verificationType,
            @RequestParam(required = false) String content
    ) {
        return verificationService.submitVerification(missionId, childId, verificationType, content);
    }

    // 2. 특정 미션의 인증 내역 조회
    @GetMapping("/{missionId}/verifications")
    public List<MissionVerification> getVerifications(@PathVariable Long missionId) {
        return verificationService.getByMission(missionId);
    }

    // 3. 인증 승인
    @PatchMapping("/{missionId}/verifications/{verificationId}/approve")
    public MissionVerification approve(
            @PathVariable Long missionId,       // URL 맞추기용 (사용 안해도 됨)
            @PathVariable Long verificationId,
            @RequestParam Long parentId
    ) {
        return verificationService.approveVerification(verificationId, parentId);
    }

    // 4. 인증 거절
    @PatchMapping("/{missionId}/verifications/{verificationId}/reject")
    public MissionVerification reject(
            @PathVariable Long missionId,
            @PathVariable Long verificationId,
            @RequestParam Long parentId,
            @RequestParam String reason
    ) {
        return verificationService.rejectVerification(verificationId, parentId, reason);
    }
}