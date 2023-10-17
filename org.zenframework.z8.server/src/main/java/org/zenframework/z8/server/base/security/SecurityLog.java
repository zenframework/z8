package org.zenframework.z8.server.base.security;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.security.IUser;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class SecurityLog extends OBJECT {

	public static class CLASS<T extends SecurityLog> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(SecurityLog.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new SecurityLog(container);
		}
	}

	private static class SecurityLogRecord extends LogRecord {

		private static final long serialVersionUID = 1L;

		final SecurityLog log;
		final SecurityEvent event;

		SecurityLogRecord(SecurityLog log, SecurityEvent event) {
			super(Level.INFO, event.getMessage());
			this.log = log;
			this.event = event;
		}

	}

	private static final ThreadLocal<List<SecurityEvent.CLASS<SecurityEvent>>> threadEvents = new ThreadLocal<List<SecurityEvent.CLASS<SecurityEvent>>>();
	private static final ThreadLocal<User.CLASS<User>> threadUser = new ThreadLocal<User.CLASS<User>>();

	private static final String Name = "Security";
	private static final int FileLimit = 10 * 1024 * 1024;
	private static final int FileCount = 10;

	private static final Logger Log = getLogger();

	public static final Collection<string> LoggedParameters = Arrays.asList(Json.session, Json.action, Json.ip, Json.user, Json.schema);

	public SecurityLog(IObject container) {
		super(container);
	}

	public void addLoginEvent(IUser user) {
		User.CLASS<User> userCls = new User.CLASS<User>();
		userCls.get().initialize(user);

		threadUser.set(userCls);

		addEvent("", guid.Null, "", Json.login.get());
	}

	public void addEvent(OBJECT request, String objectType, guid objectId, String objectName, String action) {
		addEvent(request, new string(objectType), objectId, new string(objectName), new string(action));
	}

	public void addEvent(String objectType, guid objectId, String objectName, String action) {
		addEvent(null, new string(objectType), objectId, new string(objectName), new string(action));
	}

	public void addEvent(OBJECT request, string objectType, guid objectId, string objectName, string action) {
		addEvent(request, SecurityObject.object(objectType, objectId, objectName), action);
	}

	@SuppressWarnings("unchecked")
	public void addEvent(OBJECT request, SecurityObject object, string action) {
		List<SecurityEvent.CLASS<SecurityEvent>> events = threadEvents.get();

		if (events == null)
			threadEvents.set(events = new ArrayList<SecurityEvent.CLASS<SecurityEvent>>(10));

		SecurityEvent.CLASS<SecurityEvent> event = SecurityEvent.event(request != null ? (OBJECT.CLASS<? extends OBJECT>) request.getCLASS() : null, (SecurityObject.CLASS<? extends SecurityObject>) object.getCLASS(), action);
		events.add(event);
		z8_acceptEvent(event);
	}

	public void setResult(boolean success, String message) {
		List<SecurityEvent.CLASS<SecurityEvent>> events = threadEvents.get();
		if (events != null && !events.isEmpty())
			events.get(events.size() - 1).get().setResult(success, message);
	}

	public void commitEvents() {
		List<SecurityEvent.CLASS<SecurityEvent>> events = threadEvents.get();

		if (events != null) {
			for (SecurityEvent.CLASS<SecurityEvent> event : events) {
				try {
					z8_writeEvent(event);
				} catch (Throwable e) {
					Trace.logError("Can't write security log", e);
				}
			}
			events.clear();
		}

		threadUser.remove();
	}

	public static User.CLASS<? extends User> z8_user() {
		User.CLASS<? extends User> user = threadUser.get();
		return user != null ? user : OBJECT.z8_user();
	}

	public string z8_format(User.CLASS<? extends User> userCls) {
		User user = userCls.get();
		StringBuilder str = new StringBuilder(100);
		if (!user.firstName.isEmpty())
			str.append(user.firstName).append(' ');
		if (!user.middleName.isEmpty())
			str.append(user.middleName).append(' ');
		if (!user.lastName.isEmpty())
			str.append(user.lastName).append(' ');
		if (!user.firstName.isEmpty() || !user.middleName.isEmpty() || !user.lastName.isEmpty())
			str.append('(');
		str.append(user.login);
		if (!user.firstName.isEmpty() || !user.middleName.isEmpty() || !user.lastName.isEmpty())
			str.append(')');
		return new string(str.toString());
	}

	public string z8_format(SecurityObject.CLASS<? extends SecurityObject> objectCls) {
		SecurityObject object = objectCls.get();
		StringBuilder str = new StringBuilder(100);
		if (!object.z8_getType().isEmpty())
			str.append(object.z8_getType()).append(':');
		if (guid.Null.equals(object.z8_getId()))
			str.append(object.z8_getId()).append(':');
		if (!object.z8_getName().isEmpty())
			str.append(object.z8_getName()).append(':');
		if (str.length() > 0)
			str.setLength(str.length() - 1);
		return new string(str.toString());
	}

	public bool z8_acceptEvent(SecurityEvent.CLASS<? extends SecurityEvent> event) {
		return bool.True;
	}

	public void z8_writeEvent(SecurityEvent.CLASS<? extends SecurityEvent> event) {
		if (Log != null)
			Log.log(new SecurityLogRecord(this, event.get()));
	}

	protected static Map<string, string> filterParameters(Map<string, string> parameters) {
		Map<string, string> result = new HashMap<string, string>();
		for (Map.Entry<string, string> entry : parameters.entrySet())
			if (LoggedParameters.contains(entry.getKey()))
				result.put(entry.getKey(), entry.getValue());
		return result;
	}

	private static Logger getLogger() {
		final File securityLogFile = ServerConfig.securityLogFile();
		final String securityLogFormat = ServerConfig.securityLogFormat();

		if (securityLogFile == null)
			return null;

		try {
			prepareLogFolder(securityLogFile.getParentFile());
			Handler handler = new FileHandler(securityLogFile.getPath(), FileLimit, FileCount, true);
			handler.setFormatter(new SimpleFormatter() {
				@Override
				public synchronized String format(LogRecord lr) {
					SecurityLogRecord slr = (SecurityLogRecord) lr;
					User.CLASS<? extends User> user = z8_user();
					String details = slr.event.getDetails();
					return String.format(securityLogFormat, new Date(slr.getMillis()),
							user.get().id, slr.log.z8_format(user).get(),
							slr.log.z8_format(slr.event.z8_getObject()).get(), slr.event.getAction(),
							filterParameters(slr.log.getParameters()), slr.event.isSuccess(), slr.getMessage(), details.isEmpty() ? "No details" : details);
				}
			});
			Logger logger = Logger.getLogger(Name);
			logger.setUseParentHandlers(false);
			logger.addHandler(handler);
			return logger;
		} catch (IOException e) {
			Trace.logError("Can't initialize security log", e);
			return null;
		}
	}

	private static void prepareLogFolder(File folder) {
		folder.mkdirs();
		for (File file : folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".lck");
			}
		}))
			file.delete();
	}
}
