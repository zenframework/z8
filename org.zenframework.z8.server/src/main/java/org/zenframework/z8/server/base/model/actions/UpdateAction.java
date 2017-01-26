package org.zenframework.z8.server.base.model.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.model.sql.Update;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.exceptions.AccessRightsViolationException;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.guid;

public class UpdateAction extends Action {
	public UpdateAction(ActionParameters parameters) {
		super(parameters);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		String jsonData = getDataParameter();

		if(jsonData.charAt(0) == '{')
			jsonData = "[" + jsonData + "]";

		JsonArray records = new JsonArray(jsonData);

		Query query = getQuery();

		ILink link = this.getLink();
		Query owner = link != null ? link.owner() : null;

		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();
			Collection<guid> recordIds = update(records, owner, link, query);
			connection.commit();

			writeFormFields(writer, owner != null ? owner : query, recordIds);
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		}
	}

	private boolean checkAccess(Field field) {
		if(!field.access().write())
			throw new AccessRightsViolationException();
		return true;
	}

	private Collection<guid> update(JsonArray records, Query owner, ILink link, Query query) {
		Collection<guid> result = new ArrayList<guid>();

		Query requestQuery = getRequestQuery();
		for(int index = 0; index < records.length(); index++) {
			JsonObject record = (JsonObject)records.get(index);

			for(String fieldId : JsonObject.getNames(record)) {
				Field field = requestQuery.findFieldById(fieldId);

				if(field != null && checkAccess(field))
					QueryUtils.setFieldValue(field, record.getString(fieldId));
			}

			guid id = query.primaryKey().guid();
			guid ownerId = owner != null ? owner.primaryKey().guid() : null;

			if(link != null) {
				boolean idNull = id.isNull();
				boolean ownerIdNull = ownerId.isNull();

				if(idNull && ownerIdNull)
					throw new RuntimeException("UpdateAction - bad recordId"); 

				if(idNull)
					createLink(owner, link, ownerId, query);
				else
					run(query, id);

				result.add(ownerId);
			} else {
				run(query, id);
				result.add(id);
			}
		}

		return result;
	}

	private void createLink(Query owner, ILink link, guid ownerId, Query query) {
		Field primaryKey = query.primaryKey();
		Field parentKey = query.parentKey();

		guid recordId = guid.create();
		primaryKey.set(recordId);

		guid parentId = parentKey != null ? parentKey.guid() : null;

		NewAction.run(query, recordId, parentId);
		query.insert(recordId, parentId);

		link.set(recordId);
		owner.update(ownerId);
	}

	static public int run(Query query, guid recordId) {
		return run(query, recordId, true);
	}

	static public int run(Query query, guid recordId, boolean resetChangedFields) {
		int result = 0;

		if(recordId == null || !recordId.equals(guid.Null)) {
			if(recordId != null)
				query.beforeUpdate(recordId);

			Collection<Field> changedFields = query.getChangedFields();

			result = changedFields.isEmpty() ? 0 : new Update(query, query.getChangedFields(), recordId).execute();

			if(recordId != null)
				query.afterUpdate(recordId);

			if(resetChangedFields) {
				for(Field field : changedFields)
					field.reset();
			}
		}

		return result;
	}
}
