package com.kidk.api.domain.mission;

import com.kidk.api.domain.account.Account;
import com.kidk.api.domain.account.AccountService;
import com.kidk.api.domain.transaction.TransactionService;
import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final TransactionService transactionalService;
    private final UserRepository userRepository;
    private final AccountService accountService;

    public Mission createMission(
            Long creatorId,
            Long ownerId,
            String missionType,
            String title,
            String description,
            BigDecimal targetAmount,
            BigDecimal rewardAmount,
            String status
    ) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Mission mission = Mission.builder()
                .creator(creator)
                .owner(owner)
                .missionType(missionType)
                .title(title)
                .description(description)
                .targetAmount(targetAmount)
                .rewardAmount(rewardAmount)
                .status(status)
                .build();

        return missionRepository.save(mission);
    }

    public Mission completeMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found"));

        if ("COMPLETED".equals(mission.getStatus())) {
            throw new RuntimeException("이미 완료된 미션");
        }

        mission.complete();

        // 보상 지급
        Account childAccount = accountService.getPrimaryAccount(mission.getOwner().getId());

        transactionalService.createTransaction(
                childAccount.getId(),
                "REWARD",
                mission.getRewardAmount(),
                "미션 보상",
                mission.getTitle(),
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
