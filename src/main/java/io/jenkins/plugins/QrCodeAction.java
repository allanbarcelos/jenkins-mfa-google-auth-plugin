/*
 * Project: MFA Google Auth Plugin
 *
 * Class: QrCodeAction
 *
 * Provides HTTP endpoints to support Google Authenticator MFA setup for Jenkins users.
 *
 * - Generates a new TOTP secret and returns the secret along with the otpauth URL in JSON format.
 * - Generates a QR code image for the provided secret to facilitate easy scanning by authenticator apps.
 *
 * URLs:
 *   /plugin/mfa-google-auth/generateSecret - generates and returns the secret and otpauth URL
 *   /plugin/mfa-google-auth/qrcode          - returns a PNG QR code image for the secret
 *
 * Author: Allan Barcelos
 * Date: 2025-07-17
 */

package io.jenkins.plugins;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import hudson.Extension;
import hudson.model.RootAction;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
public class QrCodeAction implements RootAction {

    private static final Logger LOGGER = Logger.getLogger(QrCodeAction.class.getName());

    public QrCodeAction() {
        LOGGER.info("QrCodeAction initialized");
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "MFA Google Auth Action";
    }

    @Override
    public String getUrlName() {
        return "mfa-google-auth";
    }

    // URL: /plugin/mfa-google-auth/generateSecret
    public void doGenerateSecret(StaplerRequest req, StaplerResponse rsp) throws Exception {
        String username = req.getSession().getAttribute("jenkins.security.SecurityRealm.user") != null
                ? req.getSession()
                        .getAttribute("jenkins.security.SecurityRealm.user")
                        .toString()
                : "user";

        var key = TOTPUtil.generateSecret();
        String secret = key.getKey();

        String otpAuthUrl = TOTPUtil.getQRBarcodeURL(username, "jenkins", secret);

        rsp.setContentType("application/json;charset=UTF-8");
        JSONObject json = new JSONObject();
        json.put("secret", secret);
        json.put("otpAuthUrl", otpAuthUrl);
        rsp.getWriter().print(json.toString());
    }

    // URL: /plugin/mfa-google-auth/qrcode
    public void doQrcode(StaplerRequest req, StaplerResponse rsp) throws IOException {
        String secret = req.getParameter("secret");
        if (secret == null || secret.isEmpty()) {
            rsp.sendError(400, "Missing secret parameter");
            return;
        }

        String otpAuth = TOTPUtil.getQRBarcodeURL("user", "jenkins", secret);

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuth, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            rsp.setContentType("image/png");
            javax.imageio.ImageIO.write(qrImage, "PNG", rsp.getOutputStream());
        } catch (WriterException e) {
            rsp.sendError(500, "Failed to generate QR Code");
        }
    }
}
