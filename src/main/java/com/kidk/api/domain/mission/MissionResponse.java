package com.kidk.api.domain.mission;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MissionResponse {
    private Long id;
    private Long creatorId;
    private String creatorName;
    private Long ownerId;
    private String ownerName;
    private String missionType;
    private String title;
    private String description;
    private BigDecimal rewardAmount;
    private String status;
    private LocalDate targetDate;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

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
