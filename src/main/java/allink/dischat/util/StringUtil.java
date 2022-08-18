package allink.dischat.util;

import java.security.SecureRandom;

public class StringUtil {
    private static final String[] charset = "0123456789abcdef".split("");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateRandomString(int length) {
        final String[] chars = new String[length];

        for (int i = 0; i < length; i++) {
            chars[i] = charset[SECURE_RANDOM.nextInt(0, charset.length)];
        }

        return String.join("", chars);
    }
}
