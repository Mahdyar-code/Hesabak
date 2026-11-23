
package com.example.hesabak;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PinSecurity {

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;

    public static String hashPin(String pin) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(
                    pin.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            byte[] hash;
            try {
                hash = SecretKeyFactory
                        .getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec)
                        .getEncoded();
            } finally {
                spec.clearPassword();
            }

            return "pbkdf2:" + ITERATIONS + ":" +
                    Base64.encodeToString(
                            salt, Base64.NO_WRAP) + ":" +
                    Base64.encodeToString(
                            hash, Base64.NO_WRAP);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "PIN hashing failed", e);
        }
    }

    public static boolean verifyPin(
            String pin, String storedHash) {

        try {
            // Support old SHA-256 hashes
            if (!storedHash.startsWith("pbkdf2:")) {

                MessageDigest digest =
                        MessageDigest.getInstance("SHA-256");

                byte[] oldHash = digest.digest(
                        pin.getBytes(StandardCharsets.UTF_8));

                StringBuilder hex = new StringBuilder();

                for (byte b : oldHash) {
                    hex.append(String.format(
                            "%02x", b & 0xff));
                }

                return MessageDigest.isEqual(
                        hex.toString().getBytes(
                                StandardCharsets.UTF_8),
                        storedHash.getBytes(
                                StandardCharsets.UTF_8)
                );
            }

            String[] parts = storedHash.split(":");

            if (parts.length != 4) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);

            if (iterations < 100000 ||
                    iterations > 1000000) {
                return false;
            }

            byte[] salt = Base64.decode(
                    parts[2], Base64.NO_WRAP);

            byte[] expected = Base64.decode(
                    parts[3], Base64.NO_WRAP);

            PBEKeySpec spec = new PBEKeySpec(
                    pin.toCharArray(),
                    salt,
                    iterations,
                    expected.length * 8
            );

            byte[] actual;

            try {
                actual = SecretKeyFactory
                        .getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec)
                        .getEncoded();
            } finally {
                spec.clearPassword();
            }

            return MessageDigest.isEqual(
                    expected, actual);

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isLegacyHash(String hash) {
        return !hash.startsWith("pbkdf2:");
    }
}
