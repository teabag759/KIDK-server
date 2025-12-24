package com.kidk.api.domain.user.service;

import com.kidk.api.domain.user.repository.UserRepository;
import com.kidk.api.domain.user.dto.UserRequest;
import com.kidk.api.domain.user.dto.UserResponse;
import com.kidk.api.domain.user.entity.User;
import com.kidk.api.global.exception.CustomException;
import com.kidk.api.global.exception.ErrorCode;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

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

    // 프로필 이미지 업로드(S3 연동)
    @Transactional
    public String updateProfileImage(String firebaseUid, MultipartFile file) {
        User user = findUserByUid(firebaseUid);

        // 파일 검증
        validateImageFile(file);

        // 파일명 생성
        String fileName = "users/" + user.getId() + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        // S3에 파일 업로드
        try (InputStream inputStream = file.getInputStream()) {
            s3Template.upload(bucketName, fileName, inputStream);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FAIL_TO_UPLOAD);
        }

        // 업로드된 이미지의 URL 생성
//        String mockUrl = "https://kdk-bucket.s3.ap-northeast-2.amazonaws.com/users/" + user.getId() + "/" + file.getOriginalFilename();
        String fileUrl = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

        // DB에 URL 저장
        user.setProfileImageUrl(fileUrl);

        return fileUrl;
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

    // 회원 탈퇴 (상태 변경)
    @Transactional
    public void withdraw(String firebaseUid) {
        User user = findUserByUid(firebaseUid);

        user.changeStatus("WITHDRAWN");
        user.updateLastLogin();
    }

    // 사용자 상태 변경 (관리자용 혹은 특정 로직용)
    @Transactional
    public void updateStatus(String firebaseUid, String newStatus) {
        User user = findUserByUid(firebaseUid);
        user.changeStatus(newStatus);
        user.updateLastLogin();
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

        // 크기 제한 (5MB) - application.properties 설정이 우선하지만 로직으로도 체크 가능
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new CustomException(ErrorCode.INVALID_FILE_SIZE);
        }

        // 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
}