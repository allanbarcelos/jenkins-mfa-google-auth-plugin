/*
 * Project: MFA TOTP Auth Plugin
 *
 * Class: MfaGlobalConfig
 *
 * --
 * Author: Allan Barcelos
 * Date: 2025-07-18
 *
 * Author: Allan Barcelos
 * Date: 2025-07-21
 * The class should override getCategory() so that the config is shown as part of the security configuration page
 */

package io.jenkins.plugins;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
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

    @Override
    public GlobalConfigurationCategory getCategory() {
        return GlobalConfigurationCategory.get(GlobalConfigurationCategory.Security.class);
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
