package com.kidk.api.domain.missionprogress.repository;

import com.kidk.api.domain.missionprogress.entity.MissionProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionProgressRepository extends JpaRepository<MissionProgress, Long> {

    List<MissionProgress> findByMissionIdOrderByLastActivityAtDesc(Long missionId);

    List<MissionProgress> findByUserId(Long userId);
}
