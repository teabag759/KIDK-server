package com.kidk.api.domain.mission.dto;

import com.kidk.api.domain.mission.enums.MissionStatus;
import com.kidk.api.domain.mission.enums.MissionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionRequest {

    @NotNull(message = "미션 생성자 ID는 필수입니다")
    private Long creatorId;

    @NotNull(message = "미션 수행자 ID는 필수입니다")
    private Long ownerId;

    @NotNull(message = "미션 유형은 필수입니다")
    private MissionType missionType;

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "목표 금액은 0 이상이어야 합니다")
    @Digits(integer = 13, fraction = 2, message = "목표 금액 형식이 올바르지 않습니다")
    private BigDecimal targetAmount;

    @NotNull(message = "보상 금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "보상 금액은 0보다 커야 합니다")
    @Digits(integer = 13, fraction = 2, message = "보상 금액 형식이 올바르지 않습니다")
    private BigDecimal rewardAmount;

    @NotNull(message = "미션 상태는 필수입니다")
    private MissionStatus status;

    private LocalDate targetDate;

}
