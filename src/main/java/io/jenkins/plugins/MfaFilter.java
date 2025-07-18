/*
 * Project: MFA Google Auth Plugin
 *
 * Class: MfaFilter
 *
 * Unified servlet filter that enforces multi-factor authentication (MFA) for Jenkins users.
 * It intercepts HTTP requests and redirects users with MFA enabled but not yet verified
 * to the MFA verification page. Exclusions include static resources, login pages, and
 * the verification page itself.
 *
 * Author: Allan Barcelos
 * Date: 2025-07-17 (Updated for unified implementation)
 */

package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.User;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;

@Extension
public class MfaFilter implements Filter {

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
        String path = req.getRequestURI();
        String contextPath = req.getContextPath();

        // Skip if Jenkins isn't fully initialized
        if (Jenkins.getInstanceOrNull() == null) {
            chain.doFilter(request, response);
            return;
        }

        // Skip static resources and common excluded paths
        if (isExcludedPath(path, contextPath)) {
            chain.doFilter(request, response);
            return;
        }

        User user = User.current();
        if (user != null) {
            MfaUserProperty mfa = user.getProperty(MfaUserProperty.class);
            if (mfa != null && mfa.isMfaEnabled()) {
                boolean verified = req.getSession() != null
                        && Boolean.TRUE.equals(req.getSession().getAttribute("mfa-verified"));

                if (!verified) {
                    rsp.sendRedirect(contextPath + "/mfa-verify");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
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
                || path.startsWith(contextPath + "/securityRealm");
    }
}
