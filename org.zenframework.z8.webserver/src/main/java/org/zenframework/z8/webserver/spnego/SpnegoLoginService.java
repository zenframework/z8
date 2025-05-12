package org.zenframework.z8.webserver.spnego;

import java.util.Base64;

import javax.servlet.ServletRequest;

import org.eclipse.jetty.security.SpnegoUserPrincipal;
import org.eclipse.jetty.server.UserIdentity;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.webserver.AbstractLoginService;

public class SpnegoLoginService extends AbstractLoginService {
	/* http://java.sun.com/javase/6/docs/technotes/guides/security/jgss/jgss-features.html */
	static public String Spnego = "1.3.6.1.5.5.2";

	public SpnegoLoginService(String name) {
		super(name);
	}

	@Override
	public UserIdentity login(String username, Object credentials, ServletRequest request) {
		String targetName = PrincipalName.getJaasPrincipal().getTargetName();

		byte[] authToken = Base64.getDecoder().decode((String) credentials);

		try {
			GSSManager gssManager = GSSManager.getInstance();
			GSSName gssName = gssManager.createName(targetName, null);
			GSSCredential gssCredential = gssManager.createCredential(gssName, GSSCredential.INDEFINITE_LIFETIME, new Oid(Spnego), GSSCredential.ACCEPT_ONLY);
			GSSContext gssContext = gssManager.createContext(gssCredential);

			if(gssContext == null) {
				Trace.logEvent("SpnegoUserRealm: failed to establish GSSContext");
				return null;
			}

			while (!gssContext.isEstablished())
				authToken = gssContext.acceptSecContext(authToken, 0, authToken.length);

			String clientName = gssContext.getSrcName().toString();

			Trace.logEvent("SpnegoUserRealm: established a security context");
			Trace.logEvent("Client Principal is: " + gssContext.getSrcName());
			Trace.logEvent("Server Principal is: " + gssContext.getTargName());
			Trace.logEvent("Client Default Role: " + name);

			return newUserIdentity(new SpnegoUserPrincipal(clientName, authToken));
		} catch (GSSException gsse) {
			Trace.logError("SpnegoUserRealm: failed to establish GSSContext", gsse);
		}

		return null;
	}
}