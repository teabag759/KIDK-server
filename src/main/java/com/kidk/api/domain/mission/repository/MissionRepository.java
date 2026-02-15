package com.kidk.api.domain.mission.repository;

import com.kidk.api.domain.mission.entity.Mission;
import com.kidk.api.domain.mission.enums.MissionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    // 1. 특정 사용자(아동)의 미션 조회 (creator, owner 함께 로딩)
    @EntityGraph(attributePaths = { "creator", "owner" })
    List<Mission> findByOwnerId(Long ownerId);

    // 2. 미션 생성자가 만든 미션 조회 (creator, owner 함께 로딩)
    @EntityGraph(attributePaths = { "creator", "owner" })
    List<Mission> findByCreatorId(Long creatorId);

    // 3. 상태별 조회 (creator, owner 함께 로딩)
    @EntityGraph(attributePaths = { "creator", "owner" })
    List<Mission> findByOwnerIdAndStatus(Long ownerId, MissionStatus status);

    // 단건 조회도 오버라이드해서 페치 조인 적용 가능
    @Override
    @EntityGraph(attributePaths = { "creator", "owner" })
    Optional<Mission> findById(Long id);
}
