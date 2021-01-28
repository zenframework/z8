package org.zenframework.z8.webserver.spnego;

import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.SpnegoAuthenticator;
import org.eclipse.jetty.server.Authentication;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class CustomSpnegoAuthenticator extends SpnegoAuthenticator {
    /**
     * If authenticating succeed then {@link Authentication} object will be saved to the session's attribute,
     * under `authentication` key
     */
    @Override
    public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException {
        Authentication authentication = super.validateRequest(request, response, mandatory);
        if (authentication instanceof UserAuthentication) {
            HttpSession session = ((HttpServletRequest)request).getSession();
            if (session != null) {
                synchronized (session) {
                    session.setAttribute("authentication", authentication);
                    session.setAttribute("userPrincipalName",
                            ((UserAuthentication) authentication).getUserIdentity().getUserPrincipal().getName());
                }
            }
        }
        return authentication;
    }

}
