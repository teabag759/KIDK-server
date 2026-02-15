package com.kidk.api.domain.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class FriendRequest {

    /**
     * 친구 요청 전송 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendRequest {
        @NotNull(message = "친구 사용자 ID는 필수입니다")
        private Long friendUserId;
    }
}
