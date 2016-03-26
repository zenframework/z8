package org.zenframework.z8.web.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.emf.ecore.xml.type.internal.DataValue.Base64;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.web.servlet.Servlet;

public class TrustedAuthAdapter extends Adapter implements Properties.Listener {

    private static final String AdapterPath = "/trusted.json";
    private static final String PARAM_LOGIN = "login";

    public TrustedAuthAdapter(Servlet servlet) {
        super(servlet);
    }

    private volatile Boolean trustLocalOnly = null;

    @Override
    public void onPropertyChange(String key, String value) {
        if (ServletRuntime.TrustLocalOnlyProperty.equalsKey(key)) {
            trustLocalOnly = Boolean.valueOf(value);
        }
    }

    @Override
    public boolean canHandleRequest(HttpServletRequest request) {
        return request.getServletPath().equals(AdapterPath) && isRequestTrusted(request);
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ISession session = null;
        String login = new String(Base64.decode(request.getParameter(PARAM_LOGIN)), "UTF-8");
        String error = null;
        if (login != null) {
            try {
                session = Rmi.getAuthorityCenter().login(login);
            } catch (Throwable e) {
                error = e.getMessage();
            }
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        if (session == null) {
            response.getWriter().print("{ success:false, error:\"" + error + "\" }");
        } else {
            response.getWriter().print("{ success:true, sessionId:\"" + session.id() + "\" }");
        }
    }

    private boolean isTrustLocalOnly() {
        if (trustLocalOnly == null)
            trustLocalOnly = Boolean.valueOf(Properties.getProperty(ServletRuntime.TrustLocalOnlyProperty));
        return trustLocalOnly;
    }

    private boolean isRequestTrusted(HttpServletRequest request) {
        try {
            return !isTrustLocalOnly() || InetAddress.getByName(request.getRemoteAddr()).isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

}
