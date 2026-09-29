package org.zenframework.z8.server.ie;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class MessageAcceptor {
	static private Object lock = new Object();
	static private Set<guid> messages = new HashSet<guid>();

	static public boolean has(Message message) {
		guid id = getId(message);

		if (messages.contains(id))
			return true;

		if (message instanceof FileMessage) {
			ApplicationServer.setRequest(new Request(new Session(ApplicationServer.getSchema())));
			boolean result = Files.newInstance().hasRecord(id);
			ConnectionManager.release();
			return result;
		}

		return false;
	}

	static public boolean accept(Message message) {
		guid id = getId(message);

		synchronized (lock) {
			messages.add(id);
		}

		try {
			return message.accept();
		} catch (Throwable th) {
			if (shouldLog(message.getSender()))
				logMessage(id, message, th);
			throw th;
		} finally {
			synchronized (lock) {
				messages.remove(id);
			}
		}
	}

	static private guid getId(Message message) {
		if (message instanceof FileMessage) {
			file file = ((FileMessage) message).getFile();
			return file.id;
		}
		return message.getId();
	}

	private static boolean shouldLog(String sender) {
		Set<String> shouldLog = ServerConfig.transportLogFailsFrom();
		if (shouldLog.contains("*")) {
			return true;
		}
		return shouldLog.contains(sender);
	}

	static private void logMessage(guid id, Message message, Throwable th) {
		try {
			String sender = message.getSender().replaceAll("[\\\\/:*?\"<>|]", "_");
			File logDir = new File(Folders.Base, "failed-messages");
			logDir = new File(logDir, "from-" + sender);
			if (!logDir.exists() && !logDir.mkdirs()) {
				throw new IOException("Не удалось создать директорию для логов: " + logDir.getAbsolutePath());
			}

			String fileName = String.format("%s_%d.json", id.toString(), System.currentTimeMillis());
			Path logPath = new File(logDir, fileName).toPath();
			JsonObject rootJson = new JsonObject();
			rootJson.put("message", message.toJson());
			String thMessage = th.getMessage();
			rootJson.put("exception", thMessage == null ? th.getClass().getName() : thMessage);
			byte[] bytes = rootJson.toString().getBytes(StandardCharsets.UTF_8);
			java.nio.file.Files.write(logPath, bytes);
		} catch (Throwable e) {
			Trace.logError(e);
		}
	}
}
