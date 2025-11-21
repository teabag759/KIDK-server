package com.kidk.api.domain.missionprogress;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/mission-progress")
@RequiredArgsConstructor
public class MissionProgressController {

    private final MissionProgressService progressService;

    @PostMapping("/{missionId}")
    public MissionProgress updateProgress(
            @PathVariable Long missionId,
            @RequestParam Long userId,
            @RequestParam(required = false) BigDecimal progressAmount,
            @RequestParam(required = false) BigDecimal progressPercentage
    ) {
        return progressService.updateProgress(missionId, userId, progressAmount, progressPercentage);
    }

    @GetMapping("/{missionId}")
    public List<MissionProgress> getMissionProgress(@PathVariable Long missionId) {
        return progressService.getMissionProgress(missionId);
    }

    @GetMapping("/user/{userId}")
    public List<MissionProgress> getUserProgress(@PathVariable Long userId) {
        return progressService.getUserProgressHistory(userId);
    }
}
