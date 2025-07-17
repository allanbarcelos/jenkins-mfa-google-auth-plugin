/*
 * Project: MFA Google Auth Plugin
 *
 * Class: MfaEnforceFilter
 *
 * This servlet filter enforces multi-factor authentication (MFA) for Jenkins users.
 * It intercepts HTTP requests and redirects users with MFA enabled but not yet verified
 * to the MFA verification page before allowing access to other Jenkins pages.
 * Static resources and the verification page itself are excluded from this enforcement.
 *
 * Author: Allan Barcelos
 * Date: 2025-07-17
 */

package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.User;
import hudson.util.PluginServletFilter;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Extension
public class MfaEnforceFilter implements javax.servlet.Filter {

    static {
        try {
            PluginServletFilter.addFilter(new MfaEnforceFilter());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse rsp = (HttpServletResponse) response;

            String path = req.getRequestURI();

            // Skip static resources
            if (path.startsWith(req.getContextPath() + "/static/")
                    || path.startsWith(req.getContextPath() + "/adjuncts/")) {
                chain.doFilter(request, response);
                return;
            }

            User current = User.current();

            if (current != null) {
                HttpSession session = req.getSession(false);
                boolean verified = session != null && Boolean.TRUE.equals(session.getAttribute("mfa-verified"));

                MfaUserProperty mfa = current.getProperty(MfaUserProperty.class);
                boolean mfaEnabled = mfa != null && mfa.isMfaEnabled();

                // String path = req.getRequestURI();

                if (mfaEnabled && !verified && !path.contains("/mfa-verify")) {
                    rsp.sendRedirect(req.getContextPath() + "/mfa-verify/");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(javax.servlet.FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}
