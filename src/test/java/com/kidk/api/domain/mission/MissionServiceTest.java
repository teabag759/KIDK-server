package com.kidk.api.domain.mission;

import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
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
class MissionServiceTest {

    @Autowired
    private MissionService missionService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("미션 생성 서비스 테스트")
    void createMission() {
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

        Mission mission = missionService.createMission(
                creator.getId(),
                owner.getId(),
                "CLEAN_ROOM",
                "방청소 하기",
                "깔끔하게 청소하기",
                null, // targetAmount 없음
                new BigDecimal("500"),
                "PENDING"
        );

        assertThat(mission.getTitle()).isEqualTo("방청소 하기");
        assertThat(mission.getMissionType()).isEqualTo("CLEAN_ROOM");
        assertThat(mission.getCreator().getId()).isEqualTo(creator.getId());
        assertThat(mission.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    @DisplayName("미션 완료 처리 테스트")
    void completeMission() {
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

        Mission mission = missionService.createMission(
                creator.getId(),
                owner.getId(),
                "SAVING",
                "용돈 아끼기",
                null,
                new BigDecimal("5000"),
                new BigDecimal("1000"),
                "PENDING"
        );

        Mission completed = missionService.completeMission(mission.getId());

        assertThat(completed.getStatus()).isEqualTo("COMPLETED");
        assertThat(completed.getCompletedAt()).isNotNull();
    }
}
