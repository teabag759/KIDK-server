package com.kidk.api.domain.mission;

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
    private final UserRepository userRepository;

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
        mission.complete();
        return missionRepository.save(mission);
    }

    public List<Mission> getMissionsForOwner(Long ownerId) {
        return missionRepository.findByOwnerId(ownerId);
    }

    public List<Mission> getMissionsCreatedBy(Long creatorId) {
        return missionRepository.findByCreatorId(creatorId);
    }
}
