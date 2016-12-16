package org.zenframework.z8.server.base.model.actions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class CopyAction extends Action {
	public CopyAction(ActionParameters parameters) {
		super(parameters);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		Query query = getQuery();

		guid recordId = getRecordIdParameter();
		guid parentId = getParentIdParameter();

		boolean transactive = getQuery().isTransactive();
		Connection connection = transactive ? ConnectionManager.get() : null;

		try {
			if(transactive)
				connection.beginTransaction();

			recordId = run(query, recordId, parentId);

			if(transactive)
				connection.commit();
		} catch(Throwable e) {
			if(transactive)
				connection.rollback();
			throw new RuntimeException(e);
		}

		Collection<Field> fields = getFormFields(query);

		writer.startArray(Json.data);

		if(query.readRecord(recordId, fields)) {
			writer.startObject();

			for(Field field : fields)
				field.writeData(writer);

			writer.finishObject();
		}

		writer.finishArray();
	}

	static private boolean canCopy(Field field) {
		return !field.isPrimaryKey() && !field.unique();
	}

	static public guid run(Query query, guid recordId, guid parentId) {
		guid newRecordId = guid.create();

		Collection<Field> changed = query.getChangedFields();

		NewAction.run(query, newRecordId, parentId);

		Collection<Field> fields = query.getPrimaryFields();
		Map<Field, primary> values = new HashMap<Field, primary>();

		try {
			query.saveState();

			if(query.readRecord(recordId, fields)) {
				for(Field field : fields)
					values.put(field, field.get());
			}
		} finally {
			query.restoreState();
		}

		for(Map.Entry<Field, primary> entry : values.entrySet()) {
			Field field = entry.getKey();
			if(canCopy(field) && !changed.contains(field))
				field.set(entry.getValue());
		}

		query.onCopy();

		return query.insert(newRecordId, parentId);
	}
}
