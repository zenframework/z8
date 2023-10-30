package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.crypto.Digest;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.exceptions.AccessDeniedException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.security.LoginParameters;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class SystemAdapter extends Adapter {
	static private final String AdapterPath = "/request.json";

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().endsWith(AdapterPath);
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response, Map<String, String> parameters, List<file> files) throws IOException {
		String req = parameters.get(Json.request.get());

		if(Json.changePassword.get().equals(req))
			changePassword(parameters, request, response);
		else if(Json.remindInit.get().equals(req))
			remindInit(parameters, request, response);
		else if(Json.remind.get().equals(req))
			remind(parameters, request, response);
		else if(Json.verify.get().equals(req))
			verify(parameters, request, response);
		else if(Json.register.get().equals(req))
			register(parameters, request, response);
		else if(Json.logout.get().equals(req))
			logout(parameters, request, response);
		else
			super.service(request, response, parameters, files);
	}

	@Override
	protected ISession authorize(HttpServletRequest request, Map<String, String> parameters) throws IOException {
		ISession session = super.authorize(request, parameters);

		boolean isLogin = Json.login.get().equals(parameters.get(Json.request.get()));
		if(!isLogin || session != null)
			return session;

		String login = parameters.get(Json.login.get());
		String password = parameters.get(Json.password.get());

		if(login == null)
			throw new AccessDeniedException();

		if(password == null)
			password = "";

		if(!ServerConfig.webClientHashPassword())
			password = Digest.md5(password);

		return ServerConfig.authorityCenter().login(getLoginParameters(login, request, false), password);
	}

	private void remindInit(Map<String, String> parameters, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String email = parameters.get(Json.email.get());

		ServerConfig.authorityCenter().remindInit(email, extractSchemaName(request), getRequestHost(request));

		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeInfo(Collections.emptyList(), Collections.emptyList(), null);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();

		writeResponse(response, writer.toString());
	}

	@SuppressWarnings("unchecked")
	private void remind(Map<String, String> parameters, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String verificationCode = parameters.get(Json.verificationCode.get());

		IUser user = ServerConfig.authorityCenter().remind(verificationCode, extractSchemaName(request), getRequestHost(request));
		boolean accessed = verificationCode.equals(user.verification());

		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeInfo(Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
		writer.startArray(Json.data);
		writer.startObject();
		if(accessed)
			writer.writeProperty(Json.verification, user.verification());
		writer.writeProperty(Json.login, user.login());
		writer.writeProperty(Json.success, accessed);
		writer.finishObject();
		writer.finishArray();
		writer.finishResponse();

		writeResponse(response, writer.toString());
	}

	@SuppressWarnings("unchecked")
	private void changePassword(Map<String, String> parameters, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String verificationCode = parameters.get(Json.verificationCode.get());
		String password = parameters.get(Json.password.get());

		ServerConfig.authorityCenter().changePassword(verificationCode, password, extractSchemaName(request), getRequestHost(request));

		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeInfo(Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();

		writeResponse(response, writer.toString());
	}

	@SuppressWarnings("unchecked")
	private void verify(Map<String, String> parameters, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String verificationCode = parameters.get(Json.verificationCode.get());

		IUser user = ServerConfig.authorityCenter().verify(verificationCode, extractSchemaName(request), getRequestHost(request));

		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeInfo(Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
		writer.startArray(Json.data);
		writer.startObject();
		writer.writeProperty(Json.login, user.login());
		writer.writeProperty(Json.success, !user.banned());
		writer.finishObject();
		writer.finishArray();
		writer.finishResponse();

		writeResponse(response, writer.toString());
	}

	@SuppressWarnings("unchecked")
	private void register(Map<String, String> parameters, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String login = parameters.get(Json.email.get());
		String email = parameters.get(Json.email.get());
		String firstName = parameters.get(Json.firstName.get());
		String lastName = parameters.get(Json.lastName.get());
		String password = parameters.get(Json.password.get());
		String company = parameters.get(Json.company.get());
		String position = parameters.get(Json.position.get());

		LoginParameters loginParameters = new LoginParameters(login).setEmail(email).setFirstName(firstName).setLastName(lastName).setCompany(company).setPosition(position).setSchema(extractSchemaName(request));

		ServerConfig.authorityCenter().register(loginParameters, password, getRequestHost(request));

		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeInfo(Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();

		writeResponse(response, writer.toString());
	}

	private void logout(Map<String, String> parameters, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String id = parameters.get(Json.id.get());
		String login = parameters.get(Json.login.get());

		LoginParameters loginParams = getLoginParameters(login, request, false);
		loginParams.setUserId(new guid(id));

		ServerConfig.authorityCenter().logout(loginParams);
	}
}
