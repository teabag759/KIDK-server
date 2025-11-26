package com.kidk.api.domain.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthRequest {
    private String firebaseToken;
    private String deviceId;

    // 그러면 지금 id랑 pw 도 request에 포함되어야 하는 것 아닌가?
}