package com.kidk.api.domain.family.controller;

import com.kidk.api.domain.family.dto.FamilyRequest;
import com.kidk.api.domain.family.dto.FamilyResponse;
import com.kidk.api.domain.family.service.FamilyService;
import com.kidk.api.domain.family.entity.Family;
import com.kidk.api.domain.familymember.entity.FamilyMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    /// 가족 생성
    @PostMapping
    public ResponseEntity<FamilyResponse> createFamily(@RequestBody FamilyRequest.Create request) {
        Family family = familyService.createFamily(request.getUserId(), request.getFamilyName());
        FamilyResponse familyResponse = new FamilyResponse(family);
        return ResponseEntity.ok(familyResponse);
    }

    /// 가족 초대
    @PostMapping("/join")
    public ResponseEntity<FamilyMember> joinFamily(@RequestBody FamilyRequest.Join request) {
        FamilyMember familyMember = familyService.joinFamily(request.getUserId(), request.getInviteCode());
        return ResponseEntity.ok(familyMember);
    }

    /// 가족 목록 조회
    @GetMapping
    public ResponseEntity<List<FamilyResponse>> getFamilies() {
        List<FamilyResponse> familyResponses = familyService.findAll().stream()
                                .map(FamilyResponse::new)
                                .toList();
        return ResponseEntity.ok(familyResponses);
    }

    /// 특정 가족 조회
    @GetMapping("/{familyId}")
    public ResponseEntity<FamilyResponse> getFamily(@PathVariable Long familyId) {
        Family family = familyService.findById(familyId);
        FamilyResponse familyResponse = new FamilyResponse(family);
        return ResponseEntity.ok(familyResponse);
    }
}