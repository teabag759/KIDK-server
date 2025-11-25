package com.kidk.api.domain.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthRequest {
    private String firebaseToken;
    private String deviceId;
}