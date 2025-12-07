package com.kidk.api.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    IN_PROGRESS("진행중"),
    ACHIEVED("달성"),
    CANCELLED("취소");

    private final String description;
}
