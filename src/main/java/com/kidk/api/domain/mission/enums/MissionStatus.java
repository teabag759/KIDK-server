package com.kidk.api.domain.mission.enums;

/**
 * 미션 상태
 */
public enum MissionStatus {
    /**
     * 진행 중
     */
    ACTIVE,

    /**
     * 완료됨
     */
    COMPLETED,

    /**
     * 취소됨
     */
    CANCELLED,

    /**
     * 기한 만료
     */
    EXPIRED
}
