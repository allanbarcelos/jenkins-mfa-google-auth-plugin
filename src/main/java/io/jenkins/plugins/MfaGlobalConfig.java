/*
 * Project: MFA TOTP Auth Plugin
 *
 * Class: MfaGlobalConfig
 *
 *
 * Author: Allan Barcelos
 * Date: 2025-07-18
 */

package io.jenkins.plugins;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundSetter;

@Extension
public class MfaGlobalConfig extends GlobalConfiguration {

    private boolean enforceMfaForAllUsers;
    private boolean excludeApiTokens;

    public static MfaGlobalConfig get() {
        return GlobalConfiguration.all().get(MfaGlobalConfig.class);
    }

    public MfaGlobalConfig() {
        load();
    }

    public boolean isEnforceMfaForAllUsers() {
        return enforceMfaForAllUsers;
    }

    @DataBoundSetter
    public void setEnforceMfaForAllUsers(boolean enforceMfaForAllUsers) {
        this.enforceMfaForAllUsers = enforceMfaForAllUsers;
        save();
    }

    public boolean isExcludeApiTokens() {
        return excludeApiTokens;
    }

    @DataBoundSetter
    public void setExcludeApiTokens(boolean excludeApiTokens) {
        this.excludeApiTokens = excludeApiTokens;
        save();
    }
}
