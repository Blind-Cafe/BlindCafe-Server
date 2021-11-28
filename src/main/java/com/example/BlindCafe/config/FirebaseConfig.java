package com.example.BlindCafe.config;

import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.exception.CodeAndMessage;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final String KEY_PATH = "firebase/blind-cafe-firebase-key.json";

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource(KEY_PATH);
            InputStream serviceAccount = resource.getInputStream();
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        }
        catch(Exception e){
            throw new BlindCafeException(CodeAndMessage.FIREBASE_CREDENTIALS_ERROR);
        }
    }
}
