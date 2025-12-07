package com.kidk.api.domain.mission.service;

import com.kidk.api.domain.account.entity.Account;
import com.kidk.api.domain.account.service.AccountService;
import com.kidk.api.domain.mission.dto.MissionRequest;
import com.kidk.api.domain.mission.entity.Mission;
import com.kidk.api.domain.mission.repository.MissionRepository;
import com.kidk.api.domain.transaction.service.TransactionService;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final AccountService accountService;

    // 미션 생성
    public Mission createMission(MissionRequest request) {
        User creator = userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Mission mission = Mission.builder()
                .creator(creator)
                .owner(owner)
                .missionType(request.getMissionType())
                .title(request.getTitle())
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .rewardAmount(request.getRewardAmount())
                .status(request.getStatus())
                .targetDate(request.getTargetDate())
                .build();

        return missionRepository.save(mission);
    }

    // 미션 완료 처리
    @Transactional
    public Mission completeMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if ("COMPLETED".equals(mission.getStatus())) {
            throw new CustomException(ErrorCode.ALREADY_COMPLETED_MISSION);
        }

        // 1. 미션 상태 변경
        mission.complete();

        // 2. 보상 지급
        Account childAccount = accountService.getPrimaryAccount(mission.getOwner().getId());

        transactionService.createTransaction(
                childAccount.getId(),
                "REWARD",
                mission.getRewardAmount(),
                "MISSION_REWARD",
                "미션 보상: " + mission.getTitle(),
                mission.getId()
        );

        return missionRepository.save(mission);
    }

    public List<Mission> getMissionsForOwner(Long ownerId) {
        return missionRepository.findByOwnerId(ownerId);
    }

    public List<Mission> getMissionsCreatedBy(Long creatorId) {
        return missionRepository.findByCreatorId(creatorId);
    }
}
