package com.kidk.api.domain.mission;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    // 미션 생성
    @PostMapping
    public Mission createMission(
            @RequestParam Long creatorId,
            @RequestParam Long ownerId,
            @RequestParam String missionType,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal targetAmount,
            @RequestParam BigDecimal rewardAmount,
            @RequestParam String status
    ) {
        return missionService.createMission(
                creatorId, ownerId, missionType, title, description,
                targetAmount, rewardAmount, status
        );
    }

    // 미션 완료 처리
    @PutMapping("/{missionId}/complete")
    public Mission completeMission(@PathVariable Long missionId) {
        return missionService.completeMission(missionId);
    }

    // 특정 사용자(아이)의 미션 목록
    @GetMapping("/owner/{ownerId}")
    public List<Mission> getOwnerMissions(@PathVariable Long ownerId) {
        return missionService.getMissionsForOwner(ownerId);
    }

    // 부모(생성자)가 만든 미션 목록
    @GetMapping("/creator/{creatorId}")
    public List<Mission> getCreatorMissions(@PathVariable Long creatorId) {
        return missionService.getMissionsCreatedBy(creatorId);
    }
}
