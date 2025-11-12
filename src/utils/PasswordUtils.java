package utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Password Utilities
 * Provides secure password hashing and verification using PBKDF2
 */
public class PasswordUtils {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits
    private static final int SALT_LENGTH = 16; // bytes

    /**
     * Generate a random salt
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hash a plaintext password
     * @param password plaintext password
     * @return encoded hash in the format: ALGORITHM:ITERATIONS:SALT_BASE64:HASH_BASE64
     */
    public static String hashPassword(String password) {
        try {
            byte[] salt = generateSalt();
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return String.format("%s:%d:%s:%s",
                    ALGORITHM,
                    ITERATIONS,
                    Base64.getEncoder().encodeToString(salt),
                    Base64.getEncoder().encodeToString(hash));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify a plaintext password against a stored hash
     * @param password plaintext password
     * @param storedHash stored hash in the format produced by hashPassword
     * @return true if matches
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            if (storedHash == null || !storedHash.contains(":")) return false;
            String[] parts = storedHash.split(":");
            if (parts.length != 4) return false;
            String alg = parts[0];
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, expectedHash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(alg);
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            // Constant-time comparison
            if (testHash.length != expectedHash.length) return false;
            int diff = 0;
            for (int i = 0; i < testHash.length; i++) {
                diff |= testHash[i] ^ expectedHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
