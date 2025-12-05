package com.kidk.api.domain.savingsgoal.repository;

import com.kidk.api.common.Status;
import com.kidk.api.domain.savingsgoal.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    /// 특정 유저의 모든 목표 조회
    List<SavingsGoal> findByUserId(Long userId);

    /// 특정 유저의 상태별 목표 조회(예: 진행중인 목표만 보기)
    List<SavingsGoal> findByUserIdAndStatus(Long userId, Status status);
}
