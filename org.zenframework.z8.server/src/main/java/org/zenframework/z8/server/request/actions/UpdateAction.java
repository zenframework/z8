package org.zenframework.z8.server.request.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.exceptions.AccessRightsViolationException;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class UpdateAction extends RequestAction {
	public UpdateAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void writeResponse(JsonWriter writer) {
		JsonArray records = new JsonArray(getRequestParameter(Json.data));

		if(records.isEmpty())
			throw new RuntimeException("UpdateAction - bad data parameter"); 

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

	private void checkAccess(Collection<Field> fields) {
		for(Field field : fields) {
			if(!field.isPrimaryKey() && !field.access().getWrite())
				throw new AccessRightsViolationException();
		}
	}

	private Collection<guid> update(JsonArray records, Query owner, ILink link, Query query) {
		Collection<guid> result = new ArrayList<guid>();

		Query contextQuery = getContextQuery();

		Collection<file> files = request().getFiles();

		for(int index = 0; index < records.length(); index++) {
			JsonObject record = (JsonObject)records.get(index);

			Collection<Field> ownerFields = new ArrayList<Field>();
			Collection<Field> queryFields = new ArrayList<Field>();

			for(String fieldId : record.getNames()) {
				Field field = contextQuery.findFieldById(fieldId);

				if(field != null) {
					QueryUtils.setFieldValue(field, record.getString(fieldId), files);

					Query fieldOwner = field.owner();
					if(fieldOwner == owner)
						ownerFields.add(field);

					if(fieldOwner == query)
						queryFields.add(field);
				}
			}

			guid id = extractPrimaryKeyValue(query);
			guid ownerId = extractPrimaryKeyValue(owner);

			if(link != null) {
				boolean idNull = id.isNull();
				boolean ownerIdNull = ownerId.isNull();

				if(idNull && ownerIdNull)
					throw new RuntimeException("UpdateAction - bad recordId"); 

				if(idNull) {
					checkAccess(ownerFields);
					checkAccess(queryFields);
					createLink(owner, link, ownerId, query);
				} else {
					checkAccess(queryFields);
					query.onUpdateAction(id);
					run(query, id);
				}

				result.add(ownerId);
			} else {
				checkAccess(queryFields);
				query.onUpdateAction(id);
				run(query, id);
				result.add(id);
			}
		}

		return result;
	}

	private guid extractPrimaryKeyValue(Query query) {
		if(query == null)
			return null;

		Field primaryKey = query.primaryKey();
		guid id = primaryKey.guid();
		primaryKey.reset();
		return id;
	}

	private void createLink(Query owner, ILink link, guid ownerId, Query query) {
		Field primaryKey = query.primaryKey();

		guid recordId = ownerId;
		primaryKey.set(recordId);

		query.onNew();
		query.onCreateAction(recordId);
		query.insert(recordId);

		link.set(recordId);
		owner.update(ownerId);
	}

	static public int run(Query query, guid recordId) {
		return run(query, recordId, true);
	}

	static public int run(Query query, guid recordId, boolean resetChangedFields) {
		int result = 0;

		if(recordId == null ||recordId.equals(guid.Null))
			return result;

		query.beforeUpdate(recordId);

		Collection<Field> changedFields = query.getChangedFields();

		result = changedFields.isEmpty() ? 0 : query.executeUpdate(recordId);

		query.afterUpdate(recordId);

		if(resetChangedFields) {
			for(Field field : changedFields)
				field.reset();
		}

		return result;
	}
}
