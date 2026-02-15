package com.kidk.api.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kidk.api.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL) // 데이터가 null이면 JSON에서 아예 뺌
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private PageInfo page;
    private ErrorBody error;

    // 성공 응답 생성 (데이터 있음)
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }

    // 성공 응답 생성 (페이지 정보 포함)
    public static <T> ApiResponse<T> success(T data, PageInfo page) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.page = page;
        return response;
    }

    // 성공 응답 생성 (Page 객체 기반)
    public static <T> ApiResponse<java.util.List<T>> successPage(org.springframework.data.domain.Page<T> page) {
        ApiResponse<java.util.List<T>> response = new ApiResponse<>();
        response.success = true;
        response.data = page.getContent();
        response.page = PageInfo.from(page);
        return response;
    }

    // 성공 응답 생성 (데이터 없음)
    public static ApiResponse<Void> success() {
        ApiResponse<Void> response = new ApiResponse<>();
        response.success = true;
        return response;
    }

    // 실패 응답 생성
    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.success = false;
        response.error = new ErrorBody(errorCode.getCode(), errorCode.getMessage());
        return response;
    }

    // 실패 응답 생성 (메시지 직접 입력)
    public static ApiResponse<Void> fail(String code, String message) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.success = false;
        response.error = new ErrorBody(code, message);
        return response;
    }

    @Getter
    @NoArgsConstructor
    public static class ErrorBody {
        private String code;
        private String message;

        public ErrorBody(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PageInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;

        public static PageInfo from(org.springframework.data.domain.Page<?> page) {
            PageInfo info = new PageInfo();
            info.page = page.getNumber();
            info.size = page.getSize();
            info.totalElements = page.getTotalElements();
            info.totalPages = page.getTotalPages();
            info.first = page.isFirst();
            info.last = page.isLast();
            return info;
        }
    }
}
