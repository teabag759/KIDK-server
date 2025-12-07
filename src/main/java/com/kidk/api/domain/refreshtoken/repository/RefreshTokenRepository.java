package com.kidk.api.domain.refreshtoken.repository;

import com.kidk.api.domain.refreshtoken.entity.RefreshToken;
import com.kidk.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 토큰 값으로 조회
    Optional<RefreshToken> findByToken(String token);

    // 유저와 기기 ID로 조회 (기존 로그인 확인용)
    Optional<RefreshToken> findByUserAndDeviceId(User user, String deviceId);

    // 특정 유저의 모든 토큰 조회 (디바이스 개수 제한 확인용)
    List<RefreshToken> findByUserId(Long userId);

    // 특정 유저의 토큰 모두 삭제 (로그아웃/탈퇴 시)
    void deleteByToken(String token);
}