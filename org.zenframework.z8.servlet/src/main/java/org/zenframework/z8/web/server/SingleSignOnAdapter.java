package org.zenframework.z8.web.server;
import org.zenframework.z8.server.config.ServerConfig;

import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.web.servlet.Servlet;
import org.zenframework.z8.web.utils.ServletUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Authentication and authorization user through kerberos protocol
 */
public class SingleSignOnAdapter extends Adapter {
    static public final String AdapterPath = "/sso_auth";

    public SingleSignOnAdapter(Servlet servlet) {
        super(servlet);
    }

    @Override
    public boolean canHandleRequest(HttpServletRequest request) {
        return request.getServletPath().equals(AdapterPath);
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession httpSession = request.getSession();
        if(ServerConfig.ldapUrl() == null) {
            httpSession.invalidate();
            response.sendRedirect("/");
        }
        String principalName = (String) httpSession.getAttribute("userPrincipalName");
        String login = principalName.contains("@") ? principalName.split("@")[0] : principalName;
        ISession session;
        try {
            session = ServerConfig.authorityCenter().createIfNotExistAndLogin(login, ServletUtil.getSchema(request));
        } catch(AccessDeniedException e) {
            httpSession.invalidate();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch(Throwable e) {
            httpSession.invalidate();
            Trace.logError(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if(useContainerSession)
            httpSession.setAttribute(Json.session.get(), session.id());
        response.sendRedirect("/");
    }
}
