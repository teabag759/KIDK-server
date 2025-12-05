package com.kidk.api.domain.user.service;

import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.domain.user.dto.UserRequest;
import com.kidk.api.domain.user.dto.UserResponse;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 내 정보 조회
    public UserResponse getMyProfile(String firebaseUid) {
        User user = findUserByUid(firebaseUid);
        return new UserResponse(user);
    }

    // 특정 유저 정보 조회
    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return new UserResponse(user);
    }

    // 내 정보 수정
    @Transactional
    public UserResponse updateProfile(String firebaseUid, UserRequest.Update request) {
        User user = findUserByUid(firebaseUid);

        // Dirty Checking으로 Update 쿼리 자동 실행
        user.setName(request.getName());
        user.setBirthDate(request.getBirthDate());
        user.setPhone(request.getPhone());

        // 연령 확인 로직 (필요 시 추가)
        if ("CHILD".equals(user.getUserType()) && request.getBirthDate() != null) {
            validateChildAge(request.getBirthDate());
        }

        return new UserResponse(user);
    }

    // 연령 검증
    private void validateChildAge(LocalDate birthDate) {
        int currentYear = LocalDate.now().getYear();
        int birthYear = birthDate.getYear();
        int age = currentYear - birthYear;

        if (age < 5 || age > 15) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }


    // 프로필 이미지 업로드 (S3 연동 전 Mock 로직 포함)
    @Transactional
    public String updateProfileImage(String firebaseUid, MultipartFile file) {
        User user = findUserByUid(firebaseUid);

        // 1. 파일 검증
        validateImageFile(file);

        // 2. 파일 업로드 로직 (AWS S3 붙이기 전 임시 URL 반환)
        // TODO: 실제 AWS S3 업로드 코드로 교체 필요
        String fileName = file.getOriginalFilename();
        String mockUrl = "https://kdk-bucket.s3.ap-northeast-2.amazonaws.com/users/" + user.getId() + "/" + fileName;

        // 3. DB 업데이트
        user.setProfileImageUrl(mockUrl);

        return mockUrl;
    }

    // 회원 탈퇴 (상태 변경)
    @Transactional
    public void withdraw(String firebaseUid) {
        User user = findUserByUid(firebaseUid);

        user.setStatus("WITHDRAWN");
        user.setStatusChangedAt(LocalDateTime.now());
    }

    // 사용자 상태 변경 (관리자용 혹은 특정 로직용)
    @Transactional
    public void updateStatus(String firebaseUid, String newStatus) {
        User user = findUserByUid(firebaseUid);
        user.setStatus(newStatus);
        user.setStatusChangedAt(LocalDateTime.now());
    }

    // 헬퍼 메서드: UID로 유저 찾기
    private User findUserByUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 헬퍼 메서드: 이미지 파일 검증
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE); // 파일 없음 에러
        }

        // 크기 제한 (5MB) - 사실 application.properties 설정이 우선하지만 로직으로도 체크 가능
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("파일 크기는 5MB를 초과할 수 없습니다.");
        }

        // 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
            throw new RuntimeException("지원하지 않는 파일 형식입니다. (jpg, png, webp만 가능)");
        }
    }
}