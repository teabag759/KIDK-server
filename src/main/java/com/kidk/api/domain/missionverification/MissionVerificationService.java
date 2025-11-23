package com.kidk.api.domain.missionverification;

import com.kidk.api.domain.mission.Mission;
import com.kidk.api.domain.mission.MissionRepository;
import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionVerificationService {

    private final MissionVerificationRepository verificationRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;

    // 아이 인증 제출
    public MissionVerification submitVerification(
            Long missionId,
            Long childId,
            String verificationType,
            String content
    ) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found"));

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        MissionVerification verification = MissionVerification.builder()
                .mission(mission)
                .child(child)
                .verificationType(verificationType)
                .content(content)
                .status("PENDING")
                .build();

        return verificationRepository.save(verification);
    }

    // 부모 승인
    public MissionVerification approveVerification(Long verificationId, Long parentId) {
        MissionVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("Verification not found"));

        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        verification.setStatus("APPROVED");
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(parent);
        verification.setRejectReason(null);

        return verification;
    }

    // 부모 거절
    public MissionVerification rejectVerification(Long verificationId, Long parentId, String reason) {
        MissionVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("Verification not found"));

        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        verification.setStatus("REJECTED");
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(parent);
        verification.setRejectReason(reason);

        return verification;
    }

    // 미션별 인증 내역 조회
    public List<MissionVerification> getByMission(Long missionId) {
        return verificationRepository.findByMissionId(missionId);
    }

    // 아이 기준 인증 내역 조회
    public List<MissionVerification> getByChild(Long childId) {
        return verificationRepository.findByChildId(childId);
    }
}