package com.kidk.api.domain.missionprogress;

import com.kidk.api.domain.mission.entity.Mission;
import com.kidk.api.domain.mission.repository.MissionRepository;
import com.kidk.api.domain.missionprogress.entity.MissionProgress;
import com.kidk.api.domain.missionprogress.repository.MissionProgressRepository;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MissionProgressRepositoryTest {

    @Autowired private MissionRepository missionRepository;
    @Autowired private MissionProgressRepository progressRepository;
    @Autowired private UserRepository userRepository;

    @Test
    @DisplayName("미션 진행률 저장 + 조회 테스트")
    void saveAndFind() {
        User parent = userRepository.save(
                User.builder().firebaseUid("p1").email("p@test.com")
                        .userType("PARENT").name("부모").status("ACTIVE").build()
        );

        User child = userRepository.save(
                User.builder().firebaseUid("c1").email("c@test.com")
                        .userType("CHILD").name("아이").status("ACTIVE").build()
        );

        Mission mission = missionRepository.save(
                Mission.builder()
                        .creator(parent)
                        .owner(child)
                        .missionType("SAVINGS")
                        .title("10000 모으기")
                        .rewardAmount(new BigDecimal("1000"))
                        .status("IN_PROGRESS")
                        .build()
        );

        progressRepository.save(
                MissionProgress.builder()
                        .mission(mission)
                        .user(child)
                        .progressAmount(new BigDecimal("3000"))
                        .progressPercentage(new BigDecimal("30"))
                        .build()
        );

        progressRepository.save(
                MissionProgress.builder()
                        .mission(mission)
                        .user(child)
                        .progressAmount(new BigDecimal("7000"))
                        .progressPercentage(new BigDecimal("70"))
                        .build()
        );

        List<MissionProgress> list =
                progressRepository.findByMissionIdOrderByLastActivityAtDesc(mission.getId());

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getProgressPercentage()).isEqualTo(new BigDecimal("70"));
    }
}
