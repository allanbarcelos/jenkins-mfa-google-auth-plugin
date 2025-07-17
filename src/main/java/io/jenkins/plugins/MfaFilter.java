/*
 * Project: MFA Google Auth Plugin
 *
 * Class: MfaFilter
 *
 * This servlet filter blocks access to Jenkins pages for users who have MFA enabled
 * but have not yet completed MFA verification in the current session.
 * It allows free access to MFA verification and login URLs.
 *
 * Author: Allan Barcelos
 * Date: 2025-07-17
 */

package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.User;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;

/**
 * Filtro que bloqueia o acesso se o usuário não tiver passado pela verificação MFA.
 */
@Extension
public class MfaFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // nada a inicializar
    }

    @Override
    public void destroy() {
        // nada a destruir
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

        // Garante que Jenkins já está inicializado
        if (Jenkins.getInstanceOrNull() == null) {
            chain.doFilter(request, response);
            return;
        }

        User u = User.current();
        if (u != null) {
            MfaUserProperty mfa = u.getProperty(MfaUserProperty.class);
            if (mfa != null && mfa.isMfaEnabled()) {
                Object verified = req.getSession().getAttribute("mfa-verified");
                String path = req.getRequestURI();
                String ctx = req.getContextPath();

                // Permitir acesso às próprias URLs de verificação MFA e login
                if (path.startsWith(ctx + "/mfa-verify") || path.startsWith(ctx + "/login")) {
                    chain.doFilter(request, response);
                    return;
                }

                // Se não verificado, redirecionar
                if (verified == null) {
                    rsp.sendRedirect(ctx + "/mfa-verify");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }
}
