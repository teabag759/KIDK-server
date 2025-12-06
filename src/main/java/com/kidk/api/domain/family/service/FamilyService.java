package com.kidk.api.domain.family.service;

import com.kidk.api.domain.family.entity.Family;
import com.kidk.api.domain.family.repository.FamilyRepository;
import com.kidk.api.domain.familymember.entity.FamilyMember;
import com.kidk.api.domain.familymember.repository.FamilyMemberRepository;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;

    // 1. 가족 생성 (부모)
    public Family createFamily(Long userId, String familyName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Family family = Family.builder()
                .familyName(familyName)
                .build();
        familyRepository.save(family);

        // 생성자를 주 보호자(FamilyMember)로 자동 등록
        FamilyMember member = FamilyMember.builder()
                .family(family)
                .user(user)
                .role("PARENT")
                .primaryParent(true)
                .status("ACCEPTED")
                .build();

        familyMemberRepository.save(member);

        return family;
    }

    // 2. 가족 가입(자녀/배우자) - 초대 코드 입력
    public FamilyMember joinFamily(Long userId, String inviteCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Family family = familyRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE_CODE));

        // 초대 코드 만료 체크 (생성 후 7일)
        if (family.getCreatedAt().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.INVITE_CODE_EXPIRED);
        }

        // 이미 가입된 멤버인지 확인
        if (familyMemberRepository.findByFamilyAndUser(family, user).isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_IN_FAMILY);
        }

        // 주 보호자 찾기(초대자 기록용)
        User inviter = familyMemberRepository.findByFamily(family).stream()
                .filter(FamilyMember::isPrimaryParent)
                .findFirst()
                .map(FamilyMember::getUser)
                .orElse(null);

        // 멤버 등록
        FamilyMember member = FamilyMember.builder()
                .family(family)
                .user(user)
                .role(user.getUserType())
                .primaryParent(false)
                .invitedBy(inviter)
                .status("ACCEPTED")
                .build();

        return familyMemberRepository.save(member);
    }

    // 조회 로직들
    @Transactional(readOnly = true)
    public List<Family> findAll() {
        return familyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Family findById(Long id) {
        return familyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Family Not Found"));
    }
}
