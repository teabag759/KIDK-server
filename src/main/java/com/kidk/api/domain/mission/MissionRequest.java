package com.kidk.api.domain.mission;


import lombok.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
