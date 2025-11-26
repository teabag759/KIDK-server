package com.kidk.api.domain.missionverification;

import com.kidk.api.domain.mission.Mission;
import com.kidk.api.domain.mission.MissionRepository;
import com.kidk.api.domain.mission.MissionService;
import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
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
    private final MissionService missionService;

    // 아이 인증 제출
    public MissionVerification submitVerification(
            Long missionId,
            Long childId,
            String verificationType,
            String content
    ) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if ("COMPLETED".equals(mission.getStatus())) {
            throw new CustomException(ErrorCode.ALREADY_COMPLETED_MISSION);
        }

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (!"PENDING".equals(verification.getStatus())) {
            throw new CustomException(ErrorCode.ALREADY_REVIEWED);
        }

        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        verification.setStatus("APPROVED");
        verification.setReviewedAt(LocalDateTime.now());
        verification.setReviewedBy(parent);

        missionService.completeMission(verification.getMission().getId());

        return verification;
    }

    // 부모 거절
    @Transactional
    public MissionVerification rejectVerification(Long verificationId, Long parentId, String reason) {
        MissionVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (!"PENDING".equals(verification.getStatus())) {
            throw new CustomException(ErrorCode.ALREADY_REVIEWED);
        }

        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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
    
}