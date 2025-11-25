package com.kidk.api.domain.family;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FamilyRequest {
    // 가족 생성용
    public static class Create {
        private Long userId;
        private String familyName;
        // Getter
        public Long getUserId() { return userId; }
        public String getFamilyName() { return familyName; }
    }

    // 가족 가입용
    public static class Join {
        private Long userId;
        private String inviteCode;
        // Getter
        public Long getUserId() { return userId; }
        public String getInviteCode() { return inviteCode; }
    }
}