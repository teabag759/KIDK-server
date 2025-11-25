package com.kidk.api.domain.refreshtoken;

import com.kidk.api.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private static final int MAX_DEVICE_COUNT = 3;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 90;

    // 리프레시 토큰 저장
    public void saveOrUpdate(User user, String token, String deviceId) {
        // 1. 해당 기기로 이미 로그인된 토큰이 있는지 확인
        refreshTokenRepository.findByUserAndDeviceId(user, deviceId)
                .ifPresentOrElse(
                        existingToken -> {
                            // 이미 있으면 토큰 값만 업데이트 (Rotation)
                            existingToken.rotateToken(token, LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS));
                        },
                        () -> {
                            // 없으면 새로 생성 (개수 제한 체크)
                            checkAndRemoveOldestToken(user.getId());
                            RefreshToken newToken = RefreshToken.builder()
                                    .user(user)
                                    .token(token)
                                    .deviceId(deviceId)
                                    .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS))
                                    .lastUsedAt(LocalDateTime.now())
                                    .isValid(true)
                                    .build();
                            refreshTokenRepository.save(newToken);
                        }
                );
    }

    // 최대 디바이스 개수 초과 시 가장 오래된 토큰 삭제
    private void checkAndRemoveOldestToken(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);

        if (tokens.size() >= MAX_DEVICE_COUNT) {
            // lastUsedAt 기준 오름차순 정렬 (null이면 createdAt 사용)
            RefreshToken oldestToken = tokens.stream()
                    .min(Comparator.comparing(t -> t.getLastUsedAt() != null ? t.getLastUsedAt() : t.getCreatedAt()))
                    .orElse(tokens.get(0));

            refreshTokenRepository.delete(oldestToken);
        }
    }

    // 토큰 검증 및 조회
    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now())) // 만료 시간 체크
                .filter(RefreshToken::isValid) // 유효 상태 체크
                .orElseThrow(() -> new IllegalArgumentException("Invalid or Expired Refresh Token"));
    }
}