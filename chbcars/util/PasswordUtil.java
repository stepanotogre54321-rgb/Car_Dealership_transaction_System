package chbcars.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** Password generation and hashing (SHA-256, salted with the username). */
public final class PasswordUtil {

    /** Auto-generated passwords follow the pattern 202600 + 3 random digits. */
    public static final String PREFIX = "202600";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String generate() {
        return PREFIX + String.format("%03d", RANDOM.nextInt(1000));
    }

    public static String hash(String username, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((username.toLowerCase() + ":" + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new DataException("Password hashing is not available on this computer.", e);
        }
    }

    public static boolean matches(String username, String password, String storedHash) {
        return hash(username, password).equals(storedHash);
    }
}
