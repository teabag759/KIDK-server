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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FamilyService 단위 테스트")
class FamilyInvitationServiceTests {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilyMemberRepository familyMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FamilyService familyService;

    private User testUser;
    private Family testFamily;
    private FamilyMember testMember;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firebaseUid("test-firebase-uid")
                .name("테스트 사용자")
                .userType("PARENT")
                .build();

        testFamily = Family.builder()
                .id(1L)
                .familyName("테스트 가족")
                .inviteCode("ABC12345")
                .build();

        testMember = FamilyMember.builder()
                .id(1L)
                .family(testFamily)
                .user(testUser)
                .role("PARENT")
                .primaryParent(true)
                .status("ACCEPTED")
                .build();
    }

    @Test
    @DisplayName("초대 코드 생성 - 성공")
    void createInvite_Success() {
        // Given
        when(familyRepository.findById(1L)).thenReturn(Optional.of(testFamily));
        when(familyMemberRepository.findByFamily(testFamily)).thenReturn(Arrays.asList(testMember));
        when(familyRepository.save(any(Family.class))).thenReturn(testFamily);

        // When
        String inviteCode = familyService.createInvite(1L, 1L);

        // Then
        assertThat(inviteCode).isNotNull();
        assertThat(inviteCode).isNotEmpty();
        verify(familyRepository, times(1)).save(testFamily);
    }

    @Test
    @DisplayName("초대 코드 생성 - 권한 없음")
    void createInvite_AccessDenied() {
        // Given
        when(familyRepository.findById(1L)).thenReturn(Optional.of(testFamily));
        when(familyMemberRepository.findByFamily(testFamily)).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> familyService.createInvite(1L, 999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("대기 중인 초대 목록 조회")
    void getPendingInvitations() {
        // Given
        FamilyMember pendingMember = FamilyMember.builder()
                .id(2L)
                .family(testFamily)
                .user(testUser)
                .status("PENDING")
                .build();

        when(familyRepository.findById(1L)).thenReturn(Optional.of(testFamily));
        when(familyMemberRepository.findByFamily(testFamily))
                .thenReturn(Arrays.asList(testMember, pendingMember));

        // When
        List<FamilyMember> pendingInvitations = familyService.getPendingInvitations(1L);

        // Then
        assertThat(pendingInvitations).hasSize(1);
        assertThat(pendingInvitations.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("초대 승인 - 성공")
    void acceptInvitation_Success() {
        // Given
        FamilyMember pendingMember = FamilyMember.builder()
                .id(2L)
                .family(testFamily)
                .user(testUser)
                .status("PENDING")
                .build();

        when(familyMemberRepository.findById(2L)).thenReturn(Optional.of(pendingMember));
        when(familyMemberRepository.save(any(FamilyMember.class))).thenReturn(pendingMember);

        // When
        familyService.acceptInvitation(1L, 2L, 1L);

        // Then
        assertThat(pendingMember.getStatus()).isEqualTo("ACCEPTED");
        assertThat(pendingMember.getAcceptedAt()).isNotNull();
        verify(familyMemberRepository, times(1)).save(pendingMember);
    }

    @Test
    @DisplayName("가족 삭제 - 성공 (주 보호자)")
    void deleteFamily_Success() {
        // Given
        when(familyRepository.findById(1L)).thenReturn(Optional.of(testFamily));
        when(familyMemberRepository.findByFamily(testFamily)).thenReturn(Arrays.asList(testMember));
        doNothing().when(familyMemberRepository).deleteAll(anyList());
        doNothing().when(familyRepository).delete(testFamily);

        // When
        familyService.deleteFamily(1L, 1L);

        // Then
        verify(familyMemberRepository, times(1)).deleteAll(anyList());
        verify(familyRepository, times(1)).delete(testFamily);
    }

    @Test
    @DisplayName("가족 삭제 - 권한 없음 (주 보호자 아님)")
    void deleteFamily_AccessDenied() {
        // Given
        FamilyMember nonPrimaryMember = FamilyMember.builder()
                .id(2L)
                .family(testFamily)
                .user(testUser)
                .primaryParent(false)
                .status("ACCEPTED")
                .build();

        when(familyRepository.findById(1L)).thenReturn(Optional.of(testFamily));
        when(familyMemberRepository.findByFamily(testFamily)).thenReturn(Arrays.asList(nonPrimaryMember));

        // When & Then
        assertThatThrownBy(() -> familyService.deleteFamily(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }
}
