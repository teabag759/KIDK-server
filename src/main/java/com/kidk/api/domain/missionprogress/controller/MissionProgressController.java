package com.kidk.api.domain.missionprogress.controller;

import com.kidk.api.domain.missionprogress.service.MissionProgressService;
import com.kidk.api.domain.missionprogress.entity.MissionProgress;
import com.kidk.api.domain.user.service.UserService;
import com.kidk.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/missions")
@RequiredArgsConstructor
public class MissionProgressController {

    private final MissionProgressService progressService;
    private final UserService userService;

    private Long currentUserId(UserDetails userDetails) {
        return userService.getUserIdByFirebaseUid(userDetails.getUsername());
    }

    /// 미션 진행 상태 업데이트
    @PatchMapping("/{missionId}/progress")
    public ResponseEntity<ApiResponse<MissionProgress>> updateProgress(
            @PathVariable Long missionId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) BigDecimal progressAmount,
            @RequestParam(required = false) BigDecimal progressPercentage
    ) {
        MissionProgress missionProgress = progressService.updateProgress(
                missionId, currentUserId(userDetails), progressAmount, progressPercentage);

        return ResponseEntity.ok(ApiResponse.success(missionProgress));
    }

    /// 미션 진행 상태 조회
    @GetMapping("/{missionId}/progress")
    public ResponseEntity<ApiResponse<List<MissionProgress>>> getMissionProgress(@PathVariable Long missionId) {
        List<MissionProgress> missionProgresses = progressService.getMissionProgress(missionId);

        return ResponseEntity.ok(ApiResponse.success(missionProgresses));
    }

    /// 사용자의 미션 진행 상태 조회
    @GetMapping("/progress/user/{userId}")
    public ResponseEntity<ApiResponse<List<MissionProgress>>> getUserProgress(@PathVariable Long userId) {
        List<MissionProgress> missionProgresses = progressService.getUserProgressHistory(userId);

        return ResponseEntity.ok(ApiResponse.success(missionProgresses));
    }
}
