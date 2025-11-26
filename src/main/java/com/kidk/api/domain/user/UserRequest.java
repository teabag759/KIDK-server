package com.kidk.api.domain.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class UserRequest {

    @Getter
    @NoArgsConstructor
    public static class Update {
        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 1, max = 100, message = "이름은 1자 이상 100자 이하로 입력해주세요.")
        private String name;

        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        private LocalDate birthDate;

        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        private String phone;
    }

    @Getter
    @NoArgsConstructor
    public static class StatusUpdate {
        @NotBlank
        private String status; // ACTIVE, DORMANT 등
    }
}
