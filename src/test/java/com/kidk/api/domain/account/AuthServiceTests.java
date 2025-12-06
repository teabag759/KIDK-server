package com.kidk.api.domain.account;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceTests {
    // TODO: Firebase Admin SDK Mocking 필요
    // @Autowired
    // private AuthService authService;
    //
    // @Autowired
    // private UserRepository userRepository;
    //
    // @Autowired
    // private RefreshTokenRepository refreshTokenRepository;
    //
    // @MockBean
    // private FirebaseAuth firebaseAuth;
    //
    // @Test
    // @DisplayName("신규 유저 회원가입 및 토큰 발급")
    // void loginOrRegister_NewUser() throws FirebaseAuthException {
    //     // Mocking FirebaseToken
    //     FirebaseToken mockToken = mock(FirebaseToken.class);
    //     when(mockToken.getUid()).thenReturn("new-firebase-uid");
    //     when(mockToken.getEmail()).thenReturn("newuser@example.com");
    //
    //     // Mocking FirebaseAuth.verifyIdToken
    //     when(firebaseAuth.verifyIdToken(anyString())).thenReturn(mockToken);
    //
    //     AuthRequest request = new AuthRequest("mockFirebaseToken", "testDeviceId");
    //     AuthResponse response = authService.loginOrRegister(request);
    //
    //     assertThat(response).isNotNull();
    //     assertThat(response.getAccessToken()).isNotNull();
    //     assertThat(response.getRefreshToken()).isNotNull();
    //     assertThat(response.getUserType()).isEqualTo("UNKNOWN");
    //
    //     User newUser = userRepository.findByFirebaseUid("new-firebase-uid").orElse(null);
    //     assertThat(newUser).isNotNull();
    //     assertThat(newUser.getEmail()).isEqualTo("newuser@example.com");
    //
    //     RefreshToken savedRefreshToken = refreshTokenRepository.findByUserAndDeviceId(newUser, "testDeviceId").orElse(null);
    //     assertThat(savedRefreshToken).isNotNull();
    //     assertThat(savedRefreshToken.getToken()).isEqualTo(response.getRefreshToken());
    // }
    //
    // @Test
    // @DisplayName("기존 유저 로그인 및 토큰 재발급")
    // void loginOrRegister_ExistingUser() throws FirebaseAuthException {
    //     // 기존 유저 미리 저장
    //     User existingUser = User.builder()
    //             .firebaseUid("existing-firebase-uid")
}
