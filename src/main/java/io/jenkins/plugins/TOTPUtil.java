/*
 * Project: MFA Google Auth Plugin
 *
 * Class: TOTPUtil
 *
 * Utility class for handling Time-based One-Time Password (TOTP) operations
 * using Google Authenticator library. Provides methods to generate secrets,
 * create otpauth URLs for QR codes, and verify TOTP codes.
 *
 * Author: Allan Barcelos
 * Date: 2025-07-17
 */

package io.jenkins.plugins;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

public class TOTPUtil {

    public static GoogleAuthenticatorKey generateSecret() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        return gAuth.createCredentials();
    }

    public static String getQRBarcodeURL(String user, String host, String secret) {
        String issuer = host;
        return String.format("otpauth://totp/%s@%s?secret=%s&issuer=%s", user, host, secret, issuer);
    }

    public static boolean verifyCode(String secret, String code) {
        try {
            int codeInt = Integer.parseInt(code);
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            return gAuth.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
