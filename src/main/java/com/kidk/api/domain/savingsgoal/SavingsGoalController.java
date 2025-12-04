package com.kidk.api.domain.savingsgoal;

import com.kidk.api.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/savings-goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    /// 목표 생성 API
    @PostMapping
    public ApiResponse<SavingsGoal> createGoal(@RequestBody @Valid SavingsGoalRequest request) {
        SavingsGoal goal = savingsGoalService.createGoal(request);
        return ApiResponse.success(goal);
    }

    /// 내 목표 조회 API
    @GetMapping("/user/{userId}")
    public ApiResponse<List<SavingsGoal>> getMyGoals(@PathVariable Long userId) {
        return ApiResponse.success(savingsGoalService.getMyGoals(userId));
    }

    /// 목표 상세 조회 API
    @GetMapping("/{goalId}")
    public ApiResponse<SavingsGoal> getGoal(@PathVariable Long goalId) {
        return ApiResponse.success(savingsGoalService.getGoal(goalId));
    }

}
