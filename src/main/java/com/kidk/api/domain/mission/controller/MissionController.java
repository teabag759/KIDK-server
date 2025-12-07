package com.kidk.api.domain.mission.controller;

import com.kidk.api.domain.mission.dto.MissionRequest;
import com.kidk.api.domain.mission.dto.MissionResponse;
import com.kidk.api.domain.mission.service.MissionService;
import com.kidk.api.domain.mission.entity.Mission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    /// 미션 생성
    @PostMapping
    public ResponseEntity<MissionResponse> createMission(@RequestBody MissionRequest request) {

        Mission mission = missionService.createMission(request);
        MissionResponse missionResponse = new MissionResponse(mission);

        return ResponseEntity.ok(missionResponse);
    }

    /// 미션 완료 처리
    @PutMapping("/{missionId}/complete")
    public ResponseEntity<MissionResponse> completeMission(@PathVariable Long missionId) {
        Mission mission = missionService.completeMission(missionId);
        MissionResponse missionResponse = new MissionResponse(mission);
        return ResponseEntity.ok(missionResponse);
    }

    /// 특정 사용자(아이)의 미션 목록
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<MissionResponse>> getOwnerMissions(@PathVariable Long ownerId) {
        List<MissionResponse> missionResponses = missionService.getMissionsForOwner(ownerId).stream()
                .map(MissionResponse::new) // Entity -> DTO 변환
                .collect(Collectors.toList());

        return ResponseEntity.ok(missionResponses);
    }

    /// 부모(생성자)가 만든 미션 목록
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<MissionResponse>> getCreatorMissions(@PathVariable Long creatorId) {
        List<MissionResponse> missionResponses = missionService.getMissionsCreatedBy(creatorId).stream()
                .map(MissionResponse::new) // Entity -> DTO 변환
                .collect(Collectors.toList());

        return ResponseEntity.ok(missionResponses);
    }
}
