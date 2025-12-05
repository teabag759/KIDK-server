package com.kidk.api.domain.mission.controller;

import com.kidk.api.domain.mission.dto.MissionRequest;
import com.kidk.api.domain.mission.dto.MissionResponse;
import com.kidk.api.domain.mission.service.MissionService;
import com.kidk.api.domain.mission.entity.Mission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    // 미션 생성
    @PostMapping
    public MissionResponse createMission(@RequestBody MissionRequest request) {

        Mission mission = missionService.createMission(request);

        return new MissionResponse(mission);
    }

    // 미션 완료 처리
    @PutMapping("/{missionId}/complete")
    public MissionResponse completeMission(@PathVariable Long missionId) {
        Mission mission = missionService.completeMission(missionId);
        return new MissionResponse(mission);
    }

    // 특정 사용자(아이)의 미션 목록
    @GetMapping("/owner/{ownerId}")
    public List<MissionResponse> getOwnerMissions(@PathVariable Long ownerId) {
        return missionService.getMissionsForOwner(ownerId).stream()
                .map(MissionResponse::new) // Entity -> DTO 변환
                .collect(Collectors.toList());
    }

    // 부모(생성자)가 만든 미션 목록
    @GetMapping("/creator/{creatorId}")
    public List<MissionResponse> getCreatorMissions(@PathVariable Long creatorId) {
        return missionService.getMissionsCreatedBy(creatorId).stream()
                .map(MissionResponse::new) // Entity -> DTO 변환
                .collect(Collectors.toList());
    }
}
