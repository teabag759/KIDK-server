package com.kidk.api.domain.missionverification;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mission-verifications")
@RequiredArgsConstructor
public class MissionVerificationController {

    private final MissionVerificationService verificationService;

    // 아이가 인증 제출
    @PostMapping("/{missionId}")
    public MissionVerification submit(
            @PathVariable Long missionId,
            @RequestParam Long childId,
            @RequestParam String verificationType,
            @RequestParam(required = false) String content
    ) {
        return verificationService.submitVerification(missionId, childId, verificationType, content);
    }

    // 부모 승인
    @PostMapping("/{verificationId}/approve")
    public MissionVerification approve(
            @PathVariable Long verificationId,
            @RequestParam Long parentId
    ) {
        return verificationService.approveVerification(verificationId, parentId);
    }

    // 부모 거절
    @PostMapping("/{verificationId}/reject")
    public MissionVerification reject(
            @PathVariable Long verificationId,
            @RequestParam Long parentId,
            @RequestParam String reason
    ) {
        return verificationService.rejectVerification(verificationId, parentId, reason);
    }

    // 미션별 조회
    @GetMapping("/mission/{missionId}")
    public List<MissionVerification> getByMission(@PathVariable Long missionId) {
        return verificationService.getByMission(missionId);
    }

    // 아이별 조회
    @GetMapping("/child/{childId}")
    public List<MissionVerification> getByChild(@PathVariable Long childId) {
        return verificationService.getByChild(childId);
    }
}