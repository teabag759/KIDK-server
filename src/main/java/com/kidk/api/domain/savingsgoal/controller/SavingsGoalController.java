package com.kidk.api.domain.savingsgoal.controller;

import com.kidk.api.domain.savingsgoal.dto.SavingsGoalRequest;
import com.kidk.api.domain.savingsgoal.service.SavingsGoalService;
import com.kidk.api.domain.savingsgoal.entity.SavingsGoal;
import com.kidk.api.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/savings-goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    /// 목표 생성 API
    @PostMapping
    public ResponseEntity<ApiResponse<SavingsGoal>> createGoal(@RequestBody @Valid SavingsGoalRequest request) {
        ApiResponse<SavingsGoal> response = ApiResponse.success(savingsGoalService.createGoal(request));
        return ResponseEntity.ok(response);
    }

    /// 내 목표 조회 API
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<SavingsGoal>>> getMyGoals(@PathVariable Long userId) {
        ApiResponse<List<SavingsGoal>> response = ApiResponse.success(savingsGoalService.getMyGoals(userId));
        return ResponseEntity.ok(response);
    }

    /// 목표 상세 조회 API
    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<SavingsGoal>> getGoal(@PathVariable Long goalId) {
        ApiResponse<SavingsGoal> response = ApiResponse.success(savingsGoalService.getGoal(goalId));
        return ResponseEntity.ok(response);
    }

}
