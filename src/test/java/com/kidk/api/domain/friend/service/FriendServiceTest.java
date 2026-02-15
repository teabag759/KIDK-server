package com.kidk.api.domain.friend.service;

import com.kidk.api.domain.friend.dto.FriendResponse;
import com.kidk.api.domain.friend.entity.Friend;
import com.kidk.api.domain.friend.enums.FriendStatus;
import com.kidk.api.domain.friend.repository.FriendRepository;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.util.TestDataBuilder;
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
@DisplayName("FriendService 단위 테스트")
class FriendServiceTest {

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendService friendService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = TestDataBuilder.createParent("uid1", "user1@test.com", "User1");
        user1.setId(1L);

        user2 = TestDataBuilder.createParent("uid2", "user2@test.com", "User2");
        user2.setId(2L);
    }

    @Test
    @DisplayName("친구 요청 전송 성공")
    void sendFriendRequest_Success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(friendRepository.findByUserAndFriendUser(user1, user2)).thenReturn(Optional.empty());
        when(friendRepository.findByUserAndFriendUser(user2, user1)).thenReturn(Optional.empty());

        Friend savedFriend = TestDataBuilder.createFriendRequest(user1, user2);
        savedFriend.setId(1L);
        when(friendRepository.save(any(Friend.class))).thenReturn(savedFriend);

        // when
        FriendResponse response = friendService.sendFriendRequest(1L, 2L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getFriendUserId()).isEqualTo(2L);
        assertThat(response.getStatus()).isEqualTo(FriendStatus.REQUESTED);

        verify(friendRepository, times(1)).save(any(Friend.class));
    }

    @Test
    @DisplayName("자기 자신에게 친구 요청 실패")
    void sendFriendRequest_ToSelf_Fail() {
        // when & then
        assertThatThrownBy(() -> friendService.sendFriendRequest(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("잘못된 입력값");
    }

    @Test
    @DisplayName("중복 친구 요청 실패")
    void sendFriendRequest_Duplicate_Fail() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        Friend existingRequest = TestDataBuilder.createFriendRequest(user1, user2);
        when(friendRepository.findByUserAndFriendUser(user1, user2)).thenReturn(Optional.of(existingRequest));

        // when & then
        assertThatThrownBy(() -> friendService.sendFriendRequest(1L, 2L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 친구 요청이 존재하거나");
    }

    @Test
    @DisplayName("친구 요청 승인 성공")
    void acceptFriendRequest_Success() {
        // given
        Friend friendRequest = TestDataBuilder.createFriendRequest(user1, user2);
        friendRequest.setId(1L);

        when(friendRepository.findById(1L)).thenReturn(Optional.of(friendRequest));
        when(friendRepository.save(any(Friend.class))).thenReturn(friendRequest);

        // when
        FriendResponse response = friendService.acceptFriendRequest(1L, 2L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(FriendStatus.ACCEPTED);
        verify(friendRepository, times(1)).save(friendRequest);
    }

    @Test
    @DisplayName("권한 없는 사용자의 친구 요청 승인 실패")
    void acceptFriendRequest_Unauthorized_Fail() {
        // given
        Friend friendRequest = TestDataBuilder.createFriendRequest(user1, user2);
        friendRequest.setId(1L);

        when(friendRepository.findById(1L)).thenReturn(Optional.of(friendRequest));

        // when & then (user1이 승인 시도 - user2만 승인 가능)
        assertThatThrownBy(() -> friendService.acceptFriendRequest(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("접근 권한이 없습니다");
    }

    @Test
    @DisplayName("친구 요청 거부 성공")
    void rejectFriendRequest_Success() {
        // given
        Friend friendRequest = TestDataBuilder.createFriendRequest(user1, user2);
        friendRequest.setId(1L);

        when(friendRepository.findById(1L)).thenReturn(Optional.of(friendRequest));

        // when
        friendService.rejectFriendRequest(1L, 2L);

        // then
        verify(friendRepository, times(1)).save(friendRequest);
        assertThat(friendRequest.getStatus()).isEqualTo(FriendStatus.REJECTED);
    }

    @Test
    @DisplayName("친구 삭제 성공")
    void removeFriend_Success() {
        // given
        Friend friend = TestDataBuilder.createAcceptedFriend(user1, user2);
        friend.setId(1L);

        when(friendRepository.findById(1L)).thenReturn(Optional.of(friend));

        // when
        friendService.removeFriend(1L, 1L);

        // then
        verify(friendRepository, times(1)).delete(friend);
    }

    @Test
    @DisplayName("받은 친구 요청 목록 조회")
    void getReceivedRequests_Success() {
        // given
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        Friend request1 = TestDataBuilder.createFriendRequest(user1, user2);
        List<Friend> requests = Arrays.asList(request1);

        when(friendRepository.findByFriendUserAndStatus(user2, FriendStatus.REQUESTED))
                .thenReturn(requests);

        // when
        List<FriendResponse> responses = friendService.getReceivedRequests(2L);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(FriendStatus.REQUESTED);
    }

    @Test
    @DisplayName("내 친구 목록 조회 성공")
    void getUserFriends_Success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        Friend friend = TestDataBuilder.createAcceptedFriend(user1, user2);
        List<Friend> sentFriends = Arrays.asList(friend);

        when(friendRepository.findByUserAndStatus(user1, FriendStatus.ACCEPTED))
                .thenReturn(sentFriends);
        when(friendRepository.findByFriendUserAndStatus(user1, FriendStatus.ACCEPTED))
                .thenReturn(Arrays.asList());

        // when
        List<FriendResponse> responses = friendService.getUserFriends(1L);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(FriendStatus.ACCEPTED);
    }
}
