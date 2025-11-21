package com.kidk.api.domain.mission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    // 특정 사용자(아동)의 미션 조회
    List<Mission> findByOwnerId(Long ownerId);

    // 미션 생성자가 만든 미션 조회
    List<Mission> findByCreatorId(Long creatorId);

    // 상태별 조회
    List<Mission> findByOwnerIdAndStatus(Long ownerId, String status);
}
