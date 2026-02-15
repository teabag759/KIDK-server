package com.kidk.api.domain.mission.controller;

import com.kidk.api.domain.mission.dto.MissionRequest;
import com.kidk.api.domain.mission.dto.MissionResponse;
import com.kidk.api.domain.mission.service.MissionService;
import com.kidk.api.domain.mission.entity.Mission;
import com.kidk.api.global.response.ApiResponse;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<MissionResponse>> createMission(@Valid @RequestBody MissionRequest request) {

        Mission mission = missionService.createMission(request);
        MissionResponse missionResponse = new MissionResponse(mission);

        return ResponseEntity.ok(ApiResponse.success(missionResponse));
    }

    /// 미션 목록 조회 (필터링)
    @GetMapping
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getMissions(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) com.kidk.api.domain.mission.enums.MissionStatus status) {
        List<MissionResponse> missionResponses = missionService.getAllMissions(ownerId, creatorId, status).stream()
                .map(MissionResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(missionResponses));
    }

    /// 미션 상세 조회
    @GetMapping("/{missionId}")
    public ResponseEntity<ApiResponse<MissionResponse>> getMission(@PathVariable Long missionId) {
        Mission mission = missionService.getMission(missionId);
        MissionResponse missionResponse = new MissionResponse(mission);
        return ResponseEntity.ok(ApiResponse.success(missionResponse));
    }

    /// 미션 수정
    @PutMapping("/{missionId}")
    public ResponseEntity<ApiResponse<MissionResponse>> updateMission(
            @PathVariable Long missionId,
            @Valid @RequestBody MissionRequest request) {
        Mission mission = missionService.updateMission(missionId, request);
        MissionResponse missionResponse = new MissionResponse(mission);
        return ResponseEntity.ok(ApiResponse.success(missionResponse));
    }

    /// 미션 취소
    @DeleteMapping("/{missionId}")
    public ResponseEntity<ApiResponse<Void>> cancelMission(@PathVariable Long missionId) {
        missionService.cancelMission(missionId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /// 미션 완료 처리
    @PutMapping("/{missionId}/complete")
    public ResponseEntity<ApiResponse<MissionResponse>> completeMission(@PathVariable Long missionId) {
        Mission mission = missionService.completeMission(missionId);
        MissionResponse missionResponse = new MissionResponse(mission);
        return ResponseEntity.ok(ApiResponse.success(missionResponse));
    }

    /// 미션 완료 처리 (요구사항 경로)
    @PostMapping("/{missionId}/complete")
    public ResponseEntity<ApiResponse<MissionResponse>> completeMissionPost(@PathVariable Long missionId) {
        return completeMission(missionId);
    }

    /// 특정 사용자(아이)의 미션 목록
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getOwnerMissions(@PathVariable Long ownerId) {
        List<MissionResponse> missionResponses = missionService.getMissionsForOwner(ownerId).stream()
                .map(MissionResponse::new) // Entity -> DTO 변환
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(missionResponses));
    }

    /// 자녀 미션 목록 (요구사항 경로)
    @GetMapping("/children/{childId}")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getChildMissions(@PathVariable Long childId) {
        return getOwnerMissions(childId);
    }

    /// 부모(생성자)가 만든 미션 목록
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<ApiResponse<List<MissionResponse>>> getCreatorMissions(@PathVariable Long creatorId) {
        List<MissionResponse> missionResponses = missionService.getMissionsCreatedBy(creatorId).stream()
                .map(MissionResponse::new) // Entity -> DTO 변환
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(missionResponses));
    }
}
