package com.kidk.api.domain.family;

import com.kidk.api.domain.family.entity.Family;
import com.kidk.api.domain.family.repository.FamilyRepository;
import com.kidk.api.domain.family.service.FamilyService;
import com.kidk.api.domain.familymember.entity.FamilyMember;
import com.kidk.api.domain.familymember.repository.FamilyMemberRepository;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FamilyServiceTests {
    @Autowired
    private FamilyService familyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private FamilyMemberRepository familyMemberRepository;

    @Test
    @DisplayName("가족 생성 - 생성자가 주 양육자로 등록되어야 함")
    void createFamily_Success() {
        // 1. 유저 생성
        User parent = createUser("parent1", "PARENT");

        // 2. 가족 생성 호출
        Family family = familyService.createFamily(parent.getId(), "우리집");

        // 3. 검증
        // 가족이 생성되었는지?
        assertThat(family.getId()).isNotNull();
        assertThat(family.getInviteCode()).isNotNull(); // 초대코드 자동 생성 확인

        // 멤버로 잘 등록되었는지?
        List<FamilyMember> members = familyMemberRepository.findByFamily(family);
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUser().getId()).isEqualTo(parent.getId());
        assertThat(members.get(0).isPrimaryParent()).isTrue(); // 주 양육자 여부
    }

    @Test
    @DisplayName("가족 가입 - 초대 코드로 정상 가입")
    void joinFamily_Success() {
        // 1. [준비] 가족을 미리 하나 만듦
        User dad = createUser("dad", "PARENT");
        Family family = familyService.createFamily(dad.getId(), "행복한집");
        String inviteCode = family.getInviteCode();

        // 2. [준비] 가입할 자녀 유저 생성
        User son = createUser("son", "CHILD");

        // 3. [실행] 가입 시도
        FamilyMember joinedMember = familyService.joinFamily(son.getId(), inviteCode);

        // 4. [검증]
        assertThat(joinedMember.getFamily().getId()).isEqualTo(family.getId());
        assertThat(joinedMember.getUser().getId()).isEqualTo(son.getId());
        assertThat(joinedMember.getRole()).isEqualTo("CHILD");
        assertThat(joinedMember.getInvitedBy().getId()).isEqualTo(dad.getId()); // 초대한 사람이 아빠로 찍혔는지
    }

    @Test
    @DisplayName("가족 가입 실패 - 유효하지 않은 초대 코드")
    void joinFamily_Fail_InvalidCode() {
        User user = createUser("stranger", "CHILD");

        assertThatThrownBy(() ->
                familyService.joinFamily(user.getId(), "INVALID_CODE_123")
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INVITE_CODE);

    }

    @Test
    @DisplayName("가족 가입 실패 - 이미 가입된 유저")
    void joinFamily_Fail_AlreadyJoined() {
        // 1. 아빠가 가족 만듦
        User dad = createUser("dad", "PARENT");
        Family family = familyService.createFamily(dad.getId(), "우리집");

        // 2. 아빠가 자기 가족 초대코드로 또 가입 시도 -> 실패해야 함
        assertThatThrownBy(() ->
                familyService.joinFamily(dad.getId(), family.getInviteCode())
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_IN_FAMILY);
    }

    @Test
    @DisplayName("가족 가입 실패 - 초대 코드 만료 (7일 경과)")
    void joinFamily_Fail_ExpiredCode() {
        // 1. 가족 생성
        User mom = createUser("mom", "PARENT");
        Family family = familyService.createFamily(mom.getId(), "오래된집");

        // [중요] CreatedAt을 강제로 8일 전으로 조작 (Reflection 사용)
        ReflectionTestUtils.setField(family, "createdAt", LocalDateTime.now().minusDays(8));
        familyRepository.save(family);
//        familyRepository.saveAndFlush(family);


        User child = createUser("child", "CHILD");

        // 2. 가입 시도 -> 만료 에러 예상
        assertThatThrownBy(() ->
                familyService.joinFamily(child.getId(), family.getInviteCode())
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVITE_CODE_EXPIRED);
    }

    // 헬퍼 메서드
    private User createUser(String uid, String type) {
        return userRepository.save(User.builder()
                .firebaseUid(uid)
                .email(uid + "@test.com")
                .name(uid)
                .userType(type)
                .status("ACTIVE")
                .build());
    }
}
