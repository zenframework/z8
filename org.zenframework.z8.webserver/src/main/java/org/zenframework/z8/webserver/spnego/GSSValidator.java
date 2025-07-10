package org.zenframework.z8.webserver.spnego;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Base64;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.zenframework.z8.server.json.JsonWriter;

public class GSSValidator {
	public static final String SubjCredsOnlyProperty = "javax.security.auth.useSubjectCredsOnly";
	public static final String SpnegoDebugProperty = "sun.security.spnego.debug";
	public static final String JaasConfProperty = "java.security.auth.login.config";
	public static final String Krb5ConfProperty = "java.security.krb5.conf";

	public static final String[] SpnegoProperties = { SubjCredsOnlyProperty, SpnegoDebugProperty, JaasConfProperty, Krb5ConfProperty };

	public static final String Success = "success";
	public static final String Message = "message";
	public static final String Client = "client";
	public static final String Server = "server";
	public static final String Token = "token";
	public static final String Logs = "logs";

	public static void main(String[] args) {
		String encodedAuthToken = args[0];

		ByteArrayOutputStream logs = new ByteArrayOutputStream();
		PrintStream systemOut = System.out;
		PrintStream logsOut = new PrintStream(logs);
		System.setOut(logsOut);

		String targetName = PrincipalName.getJaasPrincipal().getTargetName();

		System.out.println("Token: " + encodedAuthToken);
		System.out.println("Target: " + targetName);
		System.out.println("Properties:");
		for (String key : SpnegoProperties)
			System.out.println("   - " + key + ": " + System.getProperty(key));

		try {
			GSSManager gssManager = GSSManager.getInstance();
			GSSName gssName = gssManager.createName(targetName, null);
			GSSCredential gssCredential = gssManager.createCredential(gssName, GSSCredential.INDEFINITE_LIFETIME, new Oid(SpnegoLoginService.Spnego), GSSCredential.ACCEPT_ONLY);
			GSSContext gssContext = gssManager.createContext(gssCredential);

			System.out.println("GSSName: " + gssName);
			System.out.println("GSSCredential: " + toString(gssCredential));
			System.out.println("GSSContext: " + gssContext);

			if(gssContext == null) {
				writeError(systemOut, "Failed to establish GSSContext", logs.toByteArray());
				return;
			}

			byte[] authToken = Base64.getDecoder().decode(encodedAuthToken);

			while (!gssContext.isEstablished()) {
				authToken = gssContext.acceptSecContext(authToken, 0, authToken.length);
				encodedAuthToken = Base64.getEncoder().encodeToString(authToken);
				System.out.println("GSSContext is not established. Next token: " + encodedAuthToken);
			}
			System.out.println("GSSContext is established");
			writeResult(systemOut, gssContext.getSrcName().toString(), gssContext.getTargName().toString(), encodedAuthToken, logs.toByteArray());
		} catch (GSSException gsse) {
			gsse.printStackTrace(logsOut);
			writeError(systemOut, gsse.getMessage(), logs.toByteArray());
		}
	}


	private static void writeResult(PrintStream out, String client, String server, String token, byte[] logs) {
		JsonWriter json = new JsonWriter();
		json.startObject();
		json.writeProperty(Success, true);
		json.writeProperty(Client, client);
		json.writeProperty(Server, server);
		json.writeProperty(Token, token);
		json.writeProperty(Logs, new String(logs));
		json.finishObject();
		out.print(json.toString());
	}

	private static void writeError(PrintStream out, String message, byte[] logs) {
		JsonWriter json = new JsonWriter();
		json.startObject();
		json.writeProperty(Success, false);
		json.writeProperty(Message, message);
		json.writeProperty(Logs, new String(logs));
		json.finishObject();
		out.print(json.toString());
	}

	private static String toString(GSSCredential cred) {
		StringBuilder str = new StringBuilder();
		Oid[] oids;

		try {
			str.append("{ ").append("GSSName: ").append(cred.getName().toString()).append(", Usage: ").append(usageToString(cred.getUsage()))
					.append(", RemainingLifetime: ").append(cred.getRemainingLifetime()).append(", OIDs: ").append(Arrays.toString(oids = cred.getMechs())).append(" ] }");
		} catch (GSSException e) {
			return str.append("[ Error: ").append(e.getMessage()).append(" ] }").toString();
		}

		for (Oid oid : oids) {
			try {
				str.append("\n    - ").append("{ ").append(" - GSSName: ").append(cred.getName(oid).toString()).append(", Usage: ").append(usageToString(cred.getUsage(oid)))
						.append(", RemainingInitLifetime: ").append(cred.getRemainingInitLifetime(oid)).append(", RemainingAcceptLifetime: ").append(cred.getRemainingAcceptLifetime(oid)).append(" ] }");
			} catch (GSSException e) {
				return str.append("[ Error: ").append(e.getMessage()).append(" ] }").toString();
			}
		}
		return str.toString();
	}

	private static String usageToString(int usage) {
		switch(usage) {
		case GSSCredential.INITIATE_AND_ACCEPT:
			return "INITIATE_AND_ACCEPT";
		case GSSCredential.INITIATE_ONLY:
			return "INITIATE_ONLY";
		case GSSCredential.ACCEPT_ONLY:
			return "ACCEPT_ONLY";
		default:
			return "Undefined(" + usage + ')';
		}
	}
}
