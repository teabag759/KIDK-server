package com.kidk.api.domain.family.controller;

import com.kidk.api.domain.family.dto.FamilyRequest;
import com.kidk.api.domain.family.dto.FamilyResponse;
import com.kidk.api.domain.family.service.FamilyService;
import com.kidk.api.domain.family.entity.Family;
import com.kidk.api.domain.familymember.entity.FamilyMember;
import com.kidk.api.domain.user.service.UserService;
import com.kidk.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;
    private final UserService userService;

    private Long currentUserId(UserDetails userDetails) {
        return userService.getUserIdByFirebaseUid(userDetails.getUsername());
    }

    /// 가족 생성
    @PostMapping
    public ResponseEntity<ApiResponse<FamilyResponse>> createFamily(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FamilyRequest.Create request) {
        Family family = familyService.createFamily(currentUserId(userDetails), request.getFamilyName());
        FamilyResponse familyResponse = new FamilyResponse(family);
        return ResponseEntity.ok(ApiResponse.success(familyResponse));
    }

    /// 가족 초대
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<FamilyMember>> joinFamily(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FamilyRequest.Join request) {
        FamilyMember familyMember = familyService.joinFamily(currentUserId(userDetails), request.getInviteCode());
        return ResponseEntity.ok(ApiResponse.success(familyMember));
    }

    /// 가족 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<FamilyResponse>>> getFamilies() {
        List<FamilyResponse> familyResponses = familyService.findAll().stream()
                .map(FamilyResponse::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(familyResponses));
    }

    /// 특정 가족 조회
    @GetMapping("/{familyId}")
    public ResponseEntity<ApiResponse<FamilyResponse>> getFamily(@PathVariable Long familyId) {
        Family family = familyService.findById(familyId);
        FamilyResponse familyResponse = new FamilyResponse(family);
        return ResponseEntity.ok(ApiResponse.success(familyResponse));
    }

    /// 내 가족 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<FamilyResponse>> getMyFamily(
            @AuthenticationPrincipal UserDetails userDetails) {
        Family family = familyService.getMyFamily(currentUserId(userDetails));
        FamilyResponse familyResponse = new FamilyResponse(family);
        return ResponseEntity.ok(ApiResponse.success(familyResponse));
    }

    /// 초대 코드 생성
    @PostMapping("/{familyId}/invite")
    public ResponseEntity<ApiResponse<String>> createInvite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long familyId) {
        String inviteCode = familyService.createInvite(familyId, currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success(inviteCode));
    }

    /// 초대 승인
    @PostMapping("/{familyId}/members/{memberId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @PathVariable Long familyId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails) {
        familyService.acceptInvitation(familyId, memberId, currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success());
    }

    /// 초대 거부
    @PostMapping("/{familyId}/members/{memberId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectInvitation(
            @PathVariable Long familyId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails) {
        familyService.rejectInvitation(familyId, memberId, currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success());
    }

    /// 대기 중인 초대 목록
    @GetMapping("/{familyId}/invitations")
    public ResponseEntity<ApiResponse<List<FamilyMember>>> getPendingInvitations(
            @PathVariable Long familyId) {
        List<FamilyMember> invitations = familyService
                .getPendingInvitations(familyId);
        return ResponseEntity.ok(ApiResponse.success(invitations));
    }

    /// 가족 구성원 목록 조회
    @GetMapping("/{familyId}/members")
    public ResponseEntity<ApiResponse<List<FamilyMember>>> getFamilyMembers(@PathVariable Long familyId) {
        return ResponseEntity.ok(ApiResponse.success(familyService.getFamilyMembers(familyId)));
    }

    /// 가족 구성원 삭제(퇴장)
    @DeleteMapping("/{familyId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeFamilyMember(
            @PathVariable Long familyId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails) {
        familyService.removeMember(familyId, memberId, currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success());
    }

    /// 주 보호자 변경
    @PatchMapping("/{familyId}/members/{memberId}/primary")
    public ResponseEntity<ApiResponse<Void>> setPrimaryParent(
            @PathVariable Long familyId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails) {
        familyService.setPrimaryParent(familyId, memberId, currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success());
    }

    /// 가족 삭제
    @DeleteMapping("/{familyId}")
    public ResponseEntity<ApiResponse<Void>> deleteFamily(
            @PathVariable Long familyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        familyService.deleteFamily(familyId, currentUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success());
    }
}
