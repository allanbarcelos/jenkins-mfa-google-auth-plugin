/*
 * Project: MFA TOTP Auth Plugin
 *
 * Class: QrCodeAction
 *
 * Provides HTTP endpoints for TOTP-based MFA setup.
 * Generates secrets and QR codes compatible with any TOTP app.
 *
 * URLs:
 *   /plugin/mfa-totp/generateSecret - generates secret and otpauth URL
 *   /plugin/mfa-totp/qrcode        - returns QR code PNG image
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
    private static final String ISSUER = "Jenkins";

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "MFA TOTP Action";
    }

    @Override
    public String getUrlName() {
        return "mfa-totp";
    }

    public void doGenerateSecret(StaplerRequest req, StaplerResponse rsp) throws IOException {
        try {
            String username = getCurrentUsername(req);
            String secret = TOTPUtil.generateSecret();
            String otpAuthUrl = buildOtpAuthUrl(username, secret);

            sendJsonResponse(rsp, secret, otpAuthUrl);
        } catch (Exception e) {
            LOGGER.severe("Failed to generate secret: " + e.getMessage());
            rsp.sendError(500, "Failed to generate secret");
        }
    }

    public void doQrcode(StaplerRequest req, StaplerResponse rsp) throws IOException {
        String secret = req.getParameter("secret");
        if (secret == null || secret.isEmpty()) {
            rsp.sendError(400, "Missing secret parameter");
            return;
        }

        try {
            generateQRCodeImage(rsp, buildOtpAuthUrl("user", secret));
        } catch (Exception e) {
            LOGGER.severe("QR Code generation failed: " + e.getMessage());
            rsp.sendError(500, "Failed to generate QR Code");
        }
    }

    private String getCurrentUsername(StaplerRequest req) {
        Object userAttr = req.getSession().getAttribute("jenkins.security.SecurityRealm.user");
        return userAttr != null ? userAttr.toString() : "user";
    }

    private String buildOtpAuthUrl(String username, String secret) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                ISSUER, username, secret, ISSUER);
    }

    private void sendJsonResponse(StaplerResponse rsp, String secret, String otpAuthUrl) throws IOException {
        rsp.setContentType("application/json;charset=UTF-8");
        JSONObject json = new JSONObject();
        json.put("secret", secret);
        json.put("otpAuthUrl", otpAuthUrl);
        rsp.getWriter().print(json.toString());
    }

    private void generateQRCodeImage(StaplerResponse rsp, String otpAuth) throws IOException, WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(otpAuth, BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        rsp.setContentType("image/png");
        javax.imageio.ImageIO.write(qrImage, "PNG", rsp.getOutputStream());
    }
}
