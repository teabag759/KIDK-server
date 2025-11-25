package com.kidk.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    // application.yml에 jwt.secret 설정이 없으면 기본값을 사용 (해커톤용 안전장치)
    @Value("${jwt.secret:kidk_secret_key_must_be_over_256_bits_length_for_security_please_change_it}")
    private String secretKey;

    private Key key;
    private final long ACCESS_TOKEN_VALID_TIME = 1000L * 60 * 60; // 1시간
    private final long REFRESH_TOKEN_VALID_TIME = 1000L * 60 * 60 * 24 * 90; // 90일

    private final CustomUserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        // 비밀키를 Base64로 인코딩하거나 바이트 배열로 변환하여 Key 객체 생성
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 1. Access Token 생성 (AuthService에서 호출)
    public String createAccessToken(String firebaseUid, String role) {
        Claims claims = Jwts.claims().setSubject(firebaseUid);
        claims.put("role", role);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Refresh Token 생성 (AuthService에서 호출)
    public String createRefreshToken(String firebaseUid) {
        Claims claims = Jwts.claims().setSubject(firebaseUid);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALID_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 3. 토큰에서 인증 정보 조회 (Filter에서 호출)
    public Authentication getAuthentication(String token) {
        // 토큰에서 firebaseUid 추출 후 UserDetails 로드
        String firebaseUid = this.getFirebaseUid(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(firebaseUid);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 4. 토큰에서 회원 정보(FirebaseUid) 추출
    public String getFirebaseUid(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // 5. Request Header에서 토큰 추출 "Authorization: Bearer <token>"
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 6. 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}