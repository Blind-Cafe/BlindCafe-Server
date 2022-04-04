package me.blindcafe.blindcafe.utils;

import me.blindcafe.blindcafe.exception.BlindCafeException;
import me.blindcafe.blindcafe.exception.CodeAndMessage;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@Component
public class RandomUtil {

    private Random random;

    @PostConstruct
    private void init() {
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new BlindCafeException(CodeAndMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public int getRandomValue(int size) {
        return random.nextInt(size);
    }
}
