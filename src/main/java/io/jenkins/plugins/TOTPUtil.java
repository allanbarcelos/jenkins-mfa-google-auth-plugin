/*
 * Project: MFA TOTP Auth Plugin
 *
 * Class: TOTPUtil
 *
 * Utility class for standards-compliant TOTP (RFC 6238) operations.
 * Provides methods to generate secrets, build otpauth URIs, and verify codes.
 * Compatible with all major authenticator apps.
 *
 * Author: Allan Barcelos
 * Date: 2025-07-17
 */
package io.jenkins.plugins;

import hudson.util.Secret;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;

public class TOTPUtil {
    private static final Logger LOGGER = Logger.getLogger(TOTPUtil.class.getName());
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int CODE_LENGTH = 6;
    private static final int TIME_STEP = 30; // seconds

    public static String generateSecret() {
        byte[] buffer = new byte[20]; // 160 bits
        SECURE_RANDOM.nextBytes(buffer);
        return new Base32().encodeToString(buffer);
    }

    public static String getQRBarcodeURL(String user, String issuer, String secret) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                issuer, user, secret, issuer, CODE_LENGTH, TIME_STEP);
    }

    public static boolean verifyCode(Secret secret, String code) {
        try {
            return secret != null && verifyCode(secret.getPlainText(), code);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to verify code", e);
            return false;
        }
    }

    public static boolean verifyCode(String secret, String code) {
        try {
            if (secret == null || code == null || code.length() != CODE_LENGTH) {
                return false;
            }

            long time = System.currentTimeMillis() / 1000 / TIME_STEP;
            String expectedCode = generateTOTP(secret, time);
            return expectedCode.equals(code);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "TOTP verification failed", e);
            return false;
        }
    }

    private static String generateTOTP(String secret, long time) throws NoSuchAlgorithmException, InvalidKeyException {
        Base32 base32 = new Base32();
        byte[] keyBytes = base32.decode(secret);
        byte[] timeBytes = new byte[8];

        for (int i = 7; i >= 0; i--) {
            timeBytes[i] = (byte) (time & 0xFF);
            time >>= 8;
        }

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));
        byte[] hash = mac.doFinal(timeBytes);

        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, CODE_LENGTH);
        return String.format("%0" + CODE_LENGTH + "d", otp);
    }
}
