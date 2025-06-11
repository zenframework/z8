package org.zenframework.z8.webserver.spnego;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.eclipse.jetty.security.SpnegoUserPrincipal;
import org.eclipse.jetty.server.UserIdentity;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.json.parser.JsonTokener;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.webserver.AbstractLoginService;

public class MultiSpnegoLoginService extends AbstractLoginService {

	private static final List<String> SpnegoProperties = Arrays.asList(GSSValidator.SubjCredsOnlyProperty, GSSValidator.SpnegoDebugProperty);

	private final File configs;

	public MultiSpnegoLoginService(String name, File configs) {
		super(name);
		this.configs = configs;
	}

	@Override
	public UserIdentity login(String username, Object credentials, ServletRequest request) {
		File[] configs = this.configs.listFiles();

		if(configs == null)
			throw new RuntimeException(this.configs.getAbsolutePath() + " is empty");

		for(File config : configs) {
			SpnegoUserPrincipal user = login((String)credentials, config);
			if(user != null)
				return newUserIdentity(user);
		}

		return null;
	}

	private SpnegoUserPrincipal login(String token, File config) {
		File krb5Conf = new File(config, "krb5.ini");
		File jaasConf = new File(config, "jaas.conf");

		if(!krb5Conf.exists() || !krb5Conf.isFile() || !jaasConf.exists() || !jaasConf.isFile())
			return null;

		Map<String, Object> args = getDefaultArgs();
		args.put(GSSValidator.Krb5ConfProperty, krb5Conf.getAbsolutePath());
		args.put(GSSValidator.JaasConfProperty, jaasConf.getAbsolutePath());

		InputStream in = null;

		try {
			JsonObject json = new JsonObject(new JsonTokener(in = runGSSValidator(token, args).getInputStream()));
			return json.getBoolean(GSSValidator.Success) ? new SpnegoUserPrincipal(json.getString(GSSValidator.Client), json.getString(GSSValidator.Token)) : null;
		} catch(Throwable e) {
			Trace.logError(e);
		} finally {
			IOUtils.closeQuietly(in);
		}

		return null;
	}

	private Process runGSSValidator(String token, Map<String, Object> params) {
		List<String> javaOpts = new ArrayList<String>(params.size());
		for(Map.Entry<String, Object> entry : params.entrySet())
			javaOpts.add("-D" + entry.getKey() + '=' + entry.getValue());
		return IOUtils.runJava(GSSValidator.class, true, javaOpts, Arrays.asList(token));
	}

	private static Map<String, Object> getDefaultArgs() {
		Map<String, Object> args = new HashMap<String, Object>();
		for(String property : SpnegoProperties) {
			String value = System.getProperty(property);
			if(value != null && !value.isEmpty())
				args.put(property, value);
		}
		return args;
	}
}