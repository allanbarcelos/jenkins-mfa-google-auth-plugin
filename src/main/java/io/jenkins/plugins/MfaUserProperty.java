/*
 * Project: MFA TOTP Auth Plugin
 *
 * Class: MfaUserProperty
 *
 * Represents a user property that manages multi-factor authentication (MFA) settings
 * for a Jenkins user. It stores whether MFA is enabled and the secret key used for
 * TOTP verification. The constructor validates the TOTP code when MFA is enabled.
 *
 * Includes an inner Descriptor class to integrate with Jenkins user property UI,
 * providing validation and display name.
 *
 * Author: Allan Barcelos
 * Date: 2025-07-18
 */

package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.Descriptor.FormException;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import hudson.util.FormValidation;
import hudson.util.Secret;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class MfaUserProperty extends UserProperty {
    private static final Logger LOGGER = Logger.getLogger(MfaUserProperty.class.getName());

    private final boolean userConfiguredMfa;
    private final Secret secretKey;

    @DataBoundConstructor
    public MfaUserProperty(boolean mfaEnabled, String secretKey, String totpCode) throws FormException {
        if (mfaEnabled) {
            if (secretKey == null || secretKey.isEmpty()) {
                throw new FormException(Messages.MfaUserProperty_secretKey_missing(), "secretKey");
            }
            if (totpCode == null || totpCode.isEmpty()) {
                throw new FormException(Messages.MfaUserProperty_totpCode_missing(), "totpCode");
            }
            if (!TOTPUtil.verifyCode(secretKey, totpCode)) {
                throw new FormException(Messages.MfaUserProperty_totpCode_invalid(), "totpCode");
            }
        }

        this.userConfiguredMfa = mfaEnabled;
        this.secretKey = secretKey != null ? Secret.fromString(secretKey) : null;
    }

    public boolean isMfaEnabled() {
        MfaGlobalConfig globalConfig = MfaGlobalConfig.get();
        return globalConfig != null && globalConfig.isEnforceMfaForAllUsers() || userConfiguredMfa;
    }

    public Secret getSecretKey() {
        return secretKey;
    }

    @Extension
    public static final class DescriptorImpl extends UserPropertyDescriptor {
        public DescriptorImpl() {
            super(MfaUserProperty.class);
        }

        @Override
        public UserProperty newInstance(User user) {
            try {
                return new MfaUserProperty(false, null, null);
            } catch (FormException e) {
                LOGGER.log(Level.SEVERE, "Failed to create MfaUserProperty instance", e);
                return null;
            }
        }

        @Override
        public String getDisplayName() {
            return Messages.MfaUserProperty_displayName();
        }

        public FormValidation doCheckTotpCode(@QueryParameter String value, @QueryParameter String secretKey) {
            if (value == null || value.isEmpty()) {
                return FormValidation.ok();
            }
            if (secretKey == null || secretKey.isEmpty()) {
                return FormValidation.warning(Messages.MfaUserProperty_totpCode_warning());
            }
            boolean valid = TOTPUtil.verifyCode(secretKey, value);
            return valid
                    ? FormValidation.ok(Messages.MfaUserProperty_totpCode_valid())
                    : FormValidation.error(Messages.MfaUserProperty_totpCode_invalid());
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
