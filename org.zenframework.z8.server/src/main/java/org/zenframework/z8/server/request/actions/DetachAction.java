package org.zenframework.z8.server.request.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zenframework.z8.server.base.file.AttachmentProcessor;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.FileField;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class DetachAction extends RequestAction {

	public DetachAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {

		List<file> files = new ArrayList<file>();
		JsonArray jsonArray = new JsonArray(getDataParameter());

		for(int i = 0; i < jsonArray.length(); i++)
			files.add(new file(jsonArray.getGuid(i)));

		Query query = getQuery();
		guid target = getRecordIdParameter();
		String fieldId = getFieldParameter();

		Field field = fieldId != null ? query.findFieldById(fieldId) : null;

		AttachmentProcessor processor = new AttachmentProcessor((FileField)field);

		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			processor.remove(target, files);
			connection.commit();

			writeFormFields(writer, query, Arrays.asList(target));
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}

}
