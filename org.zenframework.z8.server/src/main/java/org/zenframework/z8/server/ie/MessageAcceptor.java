package org.zenframework.z8.server.ie;

import java.util.HashSet;
import java.util.Set;

import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class MessageAcceptor {
	static private Object lock = new Object();
	static private Set<guid> messages = new HashSet<guid>();

	static public boolean has(Message message) {
		guid id = getId(message);

		if(messages.contains(id))
			return true;

		if(message instanceof FileMessage) {
			ApplicationServer.setRequest(new Request(new Session(ApplicationServer.getSchema())));
			boolean result = Files.newInstance().hasRecord(id);
			ConnectionManager.release();
			return result;
		}

		return false;
	}

	static public boolean accept(Message message) {
		guid id = getId(message);

		synchronized(lock) {
			messages.add(id);
		}

		try {
			return message.accept();
		} finally {
			synchronized(lock) {
				messages.remove(id);
			}
		}
	}

	static private guid getId(Message message) {
		if(message instanceof FileMessage) {
			file file = ((FileMessage)message).getFile();
			return file.id;
		}
		return message.getId();
	}
}
