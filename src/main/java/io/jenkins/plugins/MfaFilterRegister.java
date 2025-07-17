/*
 * Project: MFA Google Auth Plugin
 *
 * Class: MfaFilterRegister
 *
 * Registers the MfaFilter servlet filter with Jenkins after all plugins have started.
 * This ensures the MFA enforcement filter is applied to incoming HTTP requests.
 *
 * Author: Allan Barcelos
 * Date: 2025-07-17
 */

package io.jenkins.plugins;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.PluginServletFilter;
import javax.servlet.ServletException;

@Extension
public class MfaFilterRegister {

    @Initializer(after = InitMilestone.PLUGINS_STARTED)
    public static void init() throws ServletException {
        PluginServletFilter.addFilter(new MfaFilter());
    }
}
