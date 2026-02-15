package com.kidk.api.domain.friend.enums;

/**
 * 친구 관계 상태
 */
public enum FriendStatus {
    /**
     * 친구 요청이 전송된 상태
     */
    REQUESTED,

    /**
     * 친구 요청이 수락된 상태 (친구 관계 성립)
     */
    ACCEPTED,

    /**
     * 친구 요청이 거절된 상태
     */
    REJECTED
}
