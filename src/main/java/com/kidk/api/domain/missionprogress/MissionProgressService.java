package com.kidk.api.domain.missionprogress;

import com.kidk.api.domain.mission.Mission;
import com.kidk.api.domain.mission.MissionRepository;
import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionProgressService {

    private final MissionProgressRepository progressRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;

    // 진행률 업데이트
    public MissionProgress updateProgress(
            Long missionId,
            Long userId,
            BigDecimal progressAmount,
            BigDecimal progressPercentage
    ) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MissionProgress progress = MissionProgress.builder()
                .mission(mission)
                .user(user)
                .progressAmount(progressAmount)
                .progressPercentage(progressPercentage)
                .build();

        return progressRepository.save(progress);
    }

    public List<MissionProgress> getMissionProgress(Long missionId) {
        return progressRepository.findByMissionIdOrderByLastActivityAtDesc(missionId);
    }

    public List<MissionProgress> getUserProgressHistory(Long userId) {
        return progressRepository.findByUserId(userId);
    }
}
