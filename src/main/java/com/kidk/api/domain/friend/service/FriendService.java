package com.kidk.api.domain.friend.service;

import com.kidk.api.domain.friend.dto.FriendResponse;
import com.kidk.api.domain.friend.entity.Friend;
import com.kidk.api.domain.friend.enums.FriendStatus;
import com.kidk.api.domain.friend.repository.FriendRepository;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    /**
     * 친구 목록 조회 (ACCEPTED 상태만)
     */
    public List<FriendResponse> getUserFriends(Long userId) {
        User user = findUserById(userId);

        List<Friend> sentFriends = friendRepository.findByUserAndStatus(user, FriendStatus.ACCEPTED);
        List<Friend> receivedFriends = friendRepository.findByFriendUserAndStatus(user, FriendStatus.ACCEPTED);

        return Stream.concat(sentFriends.stream(), receivedFriends.stream())
                .map(FriendResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 친구 요청 전송
     */
    @Transactional
    public FriendResponse sendFriendRequest(Long userId, Long friendUserId) {
        // 자기 자신에게 요청 방지
        if (userId.equals(friendUserId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        User user = findUserById(userId);
        User friendUser = findUserById(friendUserId);

        // 이미 친구 관계가 있는지 확인 (양방향)
        boolean alreadyExists = friendRepository.findByUserAndFriendUser(user, friendUser).isPresent()
                || friendRepository.findByUserAndFriendUser(friendUser, user).isPresent();

        if (alreadyExists) {
            throw new CustomException(ErrorCode.DUPLICATE_FRIEND_REQUEST);
        }

        // 친구 요청 생성
        Friend friendRequest = Friend.builder()
                .user(user)
                .friendUser(friendUser)
                .status(FriendStatus.REQUESTED)
                .build();

        Friend saved = friendRepository.save(friendRequest);
        return new FriendResponse(saved);
    }

    /**
     * 친구 요청 승인
     */
    @Transactional
    public FriendResponse acceptFriendRequest(Long requestId, Long userId) {
        Friend friendRequest = findFriendRequestById(requestId);

        // 요청을 받은 사람만 승인 가능
        if (!friendRequest.isReceiverUser(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 이미 승인된 요청인지 확인
        if (friendRequest.getStatus() == FriendStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 상태를 ACCEPTED로 변경
        friendRequest.setStatus(FriendStatus.ACCEPTED);
        Friend updated = friendRepository.save(friendRequest);
        return new FriendResponse(updated);
    }

    /**
     * 친구 요청 거부
     */
    @Transactional
    public void rejectFriendRequest(Long requestId, Long userId) {
        Friend friendRequest = findFriendRequestById(requestId);

        // 요청을 받은 사람만 거부 가능
        if (!friendRequest.isReceiverUser(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 상태를 REJECTED로 변경
        friendRequest.setStatus(FriendStatus.REJECTED);
        friendRepository.save(friendRequest);
    }

    /**
     * 친구 삭제 (친구 관계 해제)
     */
    @Transactional
    public void removeFriend(Long friendId, Long userId) {
        Friend friend = findFriendRequestById(friendId);

        // 관련된 사용자만 삭제 가능
        if (!friend.involvesUser(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        friendRepository.delete(friend);
    }

    /**
     * 받은 친구 요청 목록 조회
     */
    public List<FriendResponse> getReceivedRequests(Long userId) {
        User user = findUserById(userId);
        List<Friend> requests = friendRepository.findByFriendUserAndStatus(user, FriendStatus.REQUESTED);
        return requests.stream()
                .map(FriendResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 보낸 친구 요청 목록 조회
     */
    public List<FriendResponse> getSentRequests(Long userId) {
        User user = findUserById(userId);
        List<Friend> requests = friendRepository.findByUserAndStatus(user, FriendStatus.REQUESTED);
        return requests.stream()
                .map(FriendResponse::new)
                .collect(Collectors.toList());
    }

    // Helper methods
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Friend findFriendRequestById(Long friendId) {
        return friendRepository.findById(friendId)
                .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_NOT_FOUND));
    }
}
