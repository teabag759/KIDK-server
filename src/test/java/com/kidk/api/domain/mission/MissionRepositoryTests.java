package com.kidk.api.domain.mission;

import com.kidk.api.domain.mission.entity.Mission;
import com.kidk.api.domain.mission.repository.MissionRepository;
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
class MissionRepositoryTests {

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("미션 생성 및 조회")
    void saveAndFind() {
        // 1. creator / owner 유저 생성
        User creator = userRepository.save(
                User.builder()
                        .firebaseUid("creator-uid")
                        .email("creator@test.com")
                        .userType("PARENT")
                        .name("부모1")
                        .status("ACTIVE")
                        .build()
        );

        User owner = userRepository.save(
                User.builder()
                        .firebaseUid("owner-uid")
                        .email("owner@test.com")
                        .userType("CHILD")
                        .name("아이1")
                        .status("ACTIVE")
                        .build()
        );

        // 2. mission 저장
        Mission mission = Mission.builder()
                .creator(creator)
                .owner(owner)
                .missionType("SAVING")
                .title("용돈 모으기")
                .description("5000원 모으기")
                .targetAmount(new BigDecimal("5000"))
                .rewardAmount(new BigDecimal("1000"))
                .status("PENDING")
                .build();

        Mission saved = missionRepository.save(mission);

        // 3. 단일 조회
        Mission found = missionRepository.findById(saved.getId())
                .orElseThrow();

        assertThat(found.getTitle()).isEqualTo("용돈 모으기");
        assertThat(found.getCreator().getEmail()).isEqualTo("creator@test.com");
        assertThat(found.getOwner().getEmail()).isEqualTo("owner@test.com");
        assertThat(found.getMissionType()).isEqualTo("SAVING");
    }

    @Test
    @DisplayName("ownerId 기준 미션 조회")
    void findByOwnerId() {
        User creator = userRepository.save(
                User.builder()
                        .firebaseUid("p1")
                        .email("p@test.com")
                        .userType("PARENT")
                        .name("부모")
                        .status("ACTIVE")
                        .build()
        );

        User owner = userRepository.save(
                User.builder()
                        .firebaseUid("c1")
                        .email("c@test.com")
                        .userType("CHILD")
                        .name("아이")
                        .status("ACTIVE")
                        .build()
        );

        missionRepository.save(
                Mission.builder()
                        .creator(creator)
                        .owner(owner)
                        .missionType("CLEAN_ROOM")
                        .title("방 청소")
                        .rewardAmount(new BigDecimal("500"))
                        .status("PENDING")
                        .build()
        );

        missionRepository.save(
                Mission.builder()
                        .creator(creator)
                        .owner(owner)
                        .missionType("STUDY")
                        .title("수학 공부 30분")
                        .rewardAmount(new BigDecimal("1000"))
                        .status("PENDING")
                        .build()
        );

        List<Mission> missions = missionRepository.findByOwnerId(owner.getId());

        assertThat(missions).hasSize(2);
    }
}
