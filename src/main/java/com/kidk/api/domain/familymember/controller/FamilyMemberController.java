package com.kidk.api.domain.familymember.controller;

import com.kidk.api.domain.familymember.service.FamilyMemberService;
import com.kidk.api.domain.familymember.entity.FamilyMember;
import com.kidk.api.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/family-members")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;

    /// 전체 조회 (테스트용)
    @GetMapping
    public ApiResponse<List<FamilyMember>> getAllFamilyMembers() {
        return ApiResponse.success(familyMemberService.findAll());
    }

    /// 단일 조회
    @GetMapping("/{id}")
    public ApiResponse<FamilyMember> getFamilyMember(@PathVariable Long id) {
        return ApiResponse.success(familyMemberService.findById(id));
    }

    /// 가족 기준 조회
    @GetMapping("/family/{familyId}")
    public ApiResponse<List<FamilyMember>> getFamilyMembersByFamily(@PathVariable Long familyId) {
        return ApiResponse.success(familyMemberService.findByFamilyId(familyId));
    }

    /// 유저 기준 조회
    @GetMapping("/user/{userId}")
    public ApiResponse<List<FamilyMember>> getFamilyMembersByUser(@PathVariable Long userId) {
        return ApiResponse.success(familyMemberService.findByUserId(userId));
    }
}
