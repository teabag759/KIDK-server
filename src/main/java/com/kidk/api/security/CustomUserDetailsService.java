package com.kidk.api.security;

import com.kidk.api.domain.user.entity.User;
import com.kidk.api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String firebaseUid) throws UsernameNotFoundException {
        // DB에서 유저 조회
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with firebaseUid: " + firebaseUid));

        // Spring Security의 UserDetails 객체로 변환하여 반환
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getFirebaseUid()) // 인증 주체는 firebaseUid
                .password("") // 패스워드는 사용하지 않음 (JWT 인증)
                .authorities(Collections.emptyList()) // 권한 목록 (필요 시 role 추가)
                .build();
    }
}