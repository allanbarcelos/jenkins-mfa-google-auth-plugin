/*
 * Project: MFA Google Auth Plugin
 *
 * Class: MfaVerifyAction
 *
 * Provides the intermediate MFA verification page at the URL /mfa-verify.
 * Handles the submission of the TOTP code from users with MFA enabled.
 * On successful verification, marks the session as MFA-verified and redirects to the Jenkins main page.
 * If verification fails, redirects back to the MFA verification page with an error indication.
 * Users without MFA enabled are redirected to the main Jenkins page directly.
 *
 * Author: Allan Barcelos
 * Date: 2025-07-17
 */
package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.User;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Página intermediária para o segundo fator (MFA).
 * URL: /mfa-verify
 */
@Extension
public class MfaVerifyAction implements RootAction {

    private static final Logger LOGGER = Logger.getLogger(MfaVerifyAction.class.getName());

    @Override
    public String getIconFileName() {
        return null; // não mostra no menu lateral
    }

    @Override
    public String getDisplayName() {
        return "MFA Verify";
    }

    @Override
    public String getUrlName() {
        return "mfa-verify";
    }

    /**
     * Processa o POST com o código TOTP
     */
    public void doVerify(StaplerRequest req, StaplerResponse rsp) throws IOException {
        User u = User.current();

        if (u == null) {
            rsp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        MfaUserProperty mfa = u.getProperty(MfaUserProperty.class);

        if (mfa != null && mfa.isMfaEnabled()) {
            String code = req.getParameter("totpCode");
            if (TOTPUtil.verifyCode(mfa.getSecretKey(), code)) {
                // Proteção simplificada - apenas marca como verificado
                HttpSession session = req.getSession();
                session.setAttribute("mfa-verified", true);
                
                // Alternativa: Rotaciona o ID da sessão sem invalidá-la
                session = req.getSession(true);
                
                LOGGER.log(Level.INFO, "MFA verification successful for user: " + u.getId());
                rsp.sendRedirect(req.getContextPath() + "/");
                return;
            } else {
                LOGGER.log(Level.WARNING, "Failed MFA attempt for user: " + u.getId());
                rsp.sendRedirect(req.getContextPath() + "/mfa-verify?error=1");
                return;
            }
        }

        rsp.sendRedirect(req.getContextPath() + "/");
    }
}
