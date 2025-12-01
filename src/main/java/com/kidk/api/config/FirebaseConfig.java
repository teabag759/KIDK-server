package com.kidk.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {
    @PostConstruct
    public void init() {
        try {
            // 이미 초기화되어 있다면 건너뜀 (중복 초기화 방지)
            if (!FirebaseApp.getApps().isEmpty()) {
                return;
            }

            // resources 폴더에 있는 키 파일을 읽어옴
            InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("FirebaseApp Initialized Successfully!");

        } catch (Exception e) {
            log.error("FirebaseApp Initialization Failed", e);

        }
    }
}
