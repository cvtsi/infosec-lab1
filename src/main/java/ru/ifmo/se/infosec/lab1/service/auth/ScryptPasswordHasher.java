package ru.ifmo.se.infosec.lab1.service.auth;

import jakarta.enterprise.context.ApplicationScoped;
import org.bouncycastle.crypto.generators.SCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@ApplicationScoped
public class ScryptPasswordHasher {

    private static final int DEFAULT_N           = 16384;
    private static final int DEFAULT_R           = 8;
    private static final int DEFAULT_P           = 1;
    private static final int DEFAULT_KEY_LENGTH  = 32;
    private static final int DEFAULT_SALT_LENGTH = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[DEFAULT_SALT_LENGTH];
        secureRandom.nextBytes(salt);

        byte[] derived = SCrypt.generate(
                password.getBytes(StandardCharsets.UTF_8),
                salt,
                DEFAULT_N,
                DEFAULT_R,
                DEFAULT_P,
                DEFAULT_KEY_LENGTH
        );

        // as per PHC spec
        return "$scrypt$N=" + DEFAULT_N + ",r=" + DEFAULT_R + ",p=" + DEFAULT_P
                + "$" + encode(salt)
                + "$" + encode(derived);
    }

    public boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        String[] parts = storedHash.split("\\$");
        if (parts.length != 5 || !"scrypt".equals(parts[1])) {
            return false;
        }

        try {
            byte[] salt = decode(parts[3]);
            byte[] expected = decode(parts[4]);

            byte[] actual = SCrypt.generate(
                    password.getBytes(StandardCharsets.UTF_8),
                    salt,
                    DEFAULT_N,
                    DEFAULT_R,
                    DEFAULT_P,
                    expected.length
            );

            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}