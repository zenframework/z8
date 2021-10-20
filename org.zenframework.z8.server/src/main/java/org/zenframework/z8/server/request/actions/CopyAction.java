package org.zenframework.z8.server.request.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.exceptions.AccessRightsViolationException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class CopyAction extends RequestAction {
	public CopyAction(ActionConfig config) {
		super(config);
	}

	private boolean checkAccess(Query query) {
		if(!query.access().copy())
			throw new AccessRightsViolationException();

		for(Field field : query.getPrimaryFields()) {
			if(!field.access().write())
				throw new AccessRightsViolationException();
		}

		return true;
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		Query query = getQuery();

		checkAccess(query);

		JsonArray records = new JsonArray(getRequestParameter(Json.data));

		if(records.isEmpty())
			throw new RuntimeException("CopyAction - bad data parameter");

		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			Collection<guid> recordIds = copy(records, getRecordIds(), query);
			connection.commit();

			writeFormFields(writer, query, recordIds);
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}

	private Collection<guid> copy(JsonArray records, Collection<guid> recordIds, Query query) {
		int index = 0;
		Collection<guid> result = new ArrayList<guid>();

		for(guid recordId : recordIds) {
			QueryUtils.setFieldValues(getContextQuery(), records.getJsonObject(index++));
			query.onCopyAction(recordId);
			result.add(run(query, recordId));
		}

		return result;
	}

	static private boolean canCopy(Field field) {
		return !field.unique();
	}

	static public guid run(Query query, guid recordId) {
		Collection<Field> changed = query.getChangedFields();

		query.onNew();

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

		Field primaryKey = query.primaryKey();

		guid newRecordId = primaryKey.guid();

		if(newRecordId.equals(recordId) || newRecordId.equals(guid.Null)) {
			newRecordId = guid.create();
			primaryKey.set(newRecordId);
		}

		query.onCopy(recordId);

		return query.insert(newRecordId);
	}
}
