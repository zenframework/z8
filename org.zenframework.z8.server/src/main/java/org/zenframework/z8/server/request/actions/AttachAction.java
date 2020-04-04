package org.zenframework.z8.server.request.actions;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.file.AttachmentProcessor;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.FileField;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class AttachAction extends RequestAction {
	public AttachAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		Collection<file> files = request().getFiles();

		Query query = getQuery();
		guid target = getRecordIdParameter();
		String fieldId = getFieldParameter();

		JsonObject json = new JsonObject(getDetailsParameter());
		Map<string, string> details = new HashMap<string, string>();

		for(String name : JsonObject.getNames(json))
			details.put(new string(name), new string(json.getString(name)));

		for(file file : files)
			file.details.putAll(details);

		Field field = fieldId != null ? query.findFieldById(fieldId) : null;

		AttachmentProcessor processor = new AttachmentProcessor((FileField)field);

		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			if(target.isNull())
				target = processor.getTable().create();
			processor.update(target, files);
			connection.commit();

			writeFormFields(writer, query, Arrays.asList(target));
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}
}
