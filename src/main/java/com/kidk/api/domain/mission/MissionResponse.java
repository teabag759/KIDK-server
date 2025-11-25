package com.kidk.api.domain.mission;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MissionResponse {
    private final Long id;
    private final Long creatorId;
    private final String creatorName;
    private final Long ownerId;
    private final String ownerName;
    private final String missionType;
    private final String title;
    private final String description;
    private final BigDecimal rewardAmount;
    private final String status;
    private final LocalDate targetDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime completedAt;

    public MissionResponse(Mission mission) {
        this.id = mission.getId();
        this.creatorId = mission.getCreator().getId();
        this.creatorName = mission.getCreator().getName();
        this.ownerId = mission.getOwner().getId();
        this.ownerName = mission.getOwner().getName();
        this.missionType = mission.getMissionType();
        this.title = mission.getTitle();
        this.description = mission.getDescription();
        this.rewardAmount = mission.getRewardAmount();
        this.status = mission.getStatus();
        this.targetDate = mission.getTargetDate();
        this.createdAt = mission.getCreatedAt();
        this.completedAt = mission.getCompletedAt();
    }

}
