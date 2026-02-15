package com.kidk.api.domain.friend.dto;

import com.kidk.api.domain.friend.entity.Friend;
import com.kidk.api.domain.friend.enums.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 친구 정보 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long friendUserId;
    private String friendUserName;
    private FriendStatus status;
    private LocalDateTime createdAt;

    public FriendResponse(Friend friend) {
        this.id = friend.getId();
        this.userId = friend.getUser().getId();
        this.userName = friend.getUser().getName();
        this.friendUserId = friend.getFriendUser().getId();
        this.friendUserName = friend.getFriendUser().getName();
        this.status = friend.getStatus();
        this.createdAt = friend.getCreatedAt();
    }
}
