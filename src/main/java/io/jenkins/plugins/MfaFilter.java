/*
 * Project: MFA TOTP Plugin
 *
 * Class: MfaFilter
 *
 * Unified servlet filter that enforces multi-factor authentication (MFA) for Jenkins users.
 * It intercepts HTTP requests and redirects users with MFA enabled but not yet verified
 * to the MFA verification page. Exclusions include static resources, login pages, and
 * the verification page itself.
 *
 * Author: Allan Barcelos
 * Date: 2025-07-18 (Updated for Jakarta EE and unified implementation)
 */

package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

@Extension
public class MfaFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(MfaFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization not needed
    }

    @Override
    public void destroy() {
        // Cleanup not needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse rsp = (HttpServletResponse) response;

        // Security headers for all responses
        rsp.setHeader("X-Frame-Options", "DENY");
        rsp.setHeader("Content-Security-Policy", "frame-ancestors 'none'");
        rsp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();

        // Skip if Jenkins isn't fully initialized
        if (Jenkins.getInstanceOrNull() == null || isExcludedPath(path, contextPath)) {
            chain.doFilter(request, response);
            return;
        }

        User user = User.current();
        MfaGlobalConfig globalConfig = GlobalConfiguration.all().get(MfaGlobalConfig.class);

        if (user == null || globalConfig == null) {
            chain.doFilter(request, response);
            return;
        }

        // Check if it is a tokenized API call (if the option is enabled)
        if (globalConfig.isExcludeApiTokens() && isApiTokenRequest(req)) {
            chain.doFilter(request, response);
            return;
        }

        MfaUserProperty mfa = user.getProperty(MfaUserProperty.class);

        boolean mfaRequired = (mfa != null && mfa.isMfaEnabled()) || globalConfig.isEnforceMfaForAllUsers();

        if (mfaRequired) {

            boolean verified = req.getSession() != null
                    && Boolean.TRUE.equals(req.getSession().getAttribute("mfa-verified"));

            if (!verified) {
                LOGGER.log(Level.INFO, "MFA required for user {0}", user.getId());
                rsp.sendRedirect(contextPath + "/mfa-verify");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isApiTokenRequest(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null) return false;
        return authHeader.startsWith("Bearer ")
                || (authHeader.startsWith("Basic ") && req.getRequestURI().startsWith(req.getContextPath() + "/api/"));
    }

    /**
     * Determines if the requested path should be excluded from MFA enforcement
     */
    private boolean isExcludedPath(String path, String contextPath) {
        // List of paths that don't require MFA verification
        return path.startsWith(contextPath + "/static/")
                || path.startsWith(contextPath + "/adjuncts/")
                || path.startsWith(contextPath + "/mfa-verify")
                || path.startsWith(contextPath + "/login")
                || path.startsWith(contextPath + "/signup")
                || path.startsWith(contextPath + "/error")
                || path.startsWith(contextPath + "/securityRealm")
                || path.startsWith(contextPath + "/api/")
                || path.startsWith(contextPath + "/favicon.ico");
    }
}
