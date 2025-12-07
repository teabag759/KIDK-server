package com.kidk.api.domain.savingsgoal.service;

import com.kidk.api.common.Status;
import com.kidk.api.domain.savingsgoal.dto.SavingsGoalRequest;
import com.kidk.api.domain.savingsgoal.entity.SavingsGoal;
import com.kidk.api.domain.savingsgoal.repository.SavingsGoalRepository;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;

    /// 저축 목표 생성
    @Transactional
    public SavingsGoal createGoal(SavingsGoalRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        SavingsGoal goal = SavingsGoal.builder()
                .user(user)
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .currentAmount(BigDecimal.ZERO)
                .status(Status.IN_PROGRESS)
                .build();

        return savingsGoalRepository.save(goal);
    }

    /// 내 저축 목표 목록 조회
    public List<SavingsGoal> getMyGoals(Long userId) {
        return savingsGoalRepository.findByUserId(userId);
    }

    ///  단일 목표 상세 조회
    public SavingsGoal getGoal(Long goalId) {
        return savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new CustomException(ErrorCode.SAVINGS_GOAL_NOT_FOUND));
    }

    /// 금액 추가 및 달성 체크 로직
    @Transactional
    public SavingsGoal addAmount(Long goalId, BigDecimal amount) {
        SavingsGoal goal = getGoal(goalId);

        BigDecimal newAmount = goal.getCurrentAmount().add(amount);
        goal.setCurrentAmount(newAmount);

        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(Status.ACHIEVED);
            goal.setAchievedAt(LocalDateTime.now());
        }

        return goal;
    }

}
