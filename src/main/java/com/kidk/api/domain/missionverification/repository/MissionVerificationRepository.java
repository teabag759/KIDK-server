package com.kidk.api.domain.missionverification.repository;

import com.kidk.api.domain.missionverification.entity.MissionVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionVerificationRepository extends JpaRepository<MissionVerification, Long> {

    List<MissionVerification> findByMissionId(Long missionId);

    List<MissionVerification> findByChildId(Long childId);

    List<MissionVerification> findByStatus(String status);
}