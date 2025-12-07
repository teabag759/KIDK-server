package com.kidk.api.domain.mission.dto;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionRequest {
    private Long creatorId;
    private Long ownerId;
    private String missionType;
    private String title;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal rewardAmount;
    private String status;
    private LocalDate targetDate;

}
