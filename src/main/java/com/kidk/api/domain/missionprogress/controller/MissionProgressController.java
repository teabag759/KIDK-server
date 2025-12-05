package com.kidk.api.domain.missionprogress.controller;

import com.kidk.api.domain.missionprogress.service.MissionProgressService;
import com.kidk.api.domain.missionprogress.entity.MissionProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mission-progress")
@RequiredArgsConstructor
public class MissionProgressController {

    private final MissionProgressService progressService;

    /// 미션 진행 상태 업데이트
    @PostMapping("/{missionId}")
    public ResponseEntity<MissionProgress> updateProgress(
            @PathVariable Long missionId,
            @RequestParam Long userId,
            @RequestParam(required = false) BigDecimal progressAmount,
            @RequestParam(required = false) BigDecimal progressPercentage
    ) {
        MissionProgress missionProgress = progressService.updateProgress(missionId, userId, progressAmount, progressPercentage);

        return ResponseEntity.ok(missionProgress);
    }

    /// 미션 진행 상태 조회
    @GetMapping("/{missionId}")
    public ResponseEntity<List<MissionProgress>> getMissionProgress(@PathVariable Long missionId) {
        List<MissionProgress> missionProgresses = progressService.getMissionProgress(missionId);

        return ResponseEntity.ok(missionProgresses);
    }

    /// 사용자의 미션 진행 상태 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MissionProgress>> getUserProgress(@PathVariable Long userId) {
        List<MissionProgress> missionProgresses = progressService.getUserProgressHistory(userId);

        return ResponseEntity.ok(missionProgresses);
    }
}
