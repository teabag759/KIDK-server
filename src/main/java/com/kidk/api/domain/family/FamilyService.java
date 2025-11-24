package com.kidk.api.domain.family;

import com.kidk.api.domain.familymember.FamilyMember;
import com.kidk.api.domain.familymember.FamilyMemberRepository;
import com.kidk.api.domain.user.User;
import com.kidk.api.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new RuntimeException("User not found"));

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
                .orElseThrow(() -> new RuntimeException("User not found"));

        Family family = familyRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        if (familyMemberRepository.findByFamilyAndUser(family, user).isPresent()) {
            throw new RuntimeException("Already a member of this family");
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
