package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.guid;

public class TableRules implements RmiSerializable, Serializable {
	private static final long serialVersionUID = 7712259310049227053L;

	private Map<guid, Map<String, ImportPolicy>> recordFields = new HashMap<guid, Map<String, ImportPolicy>>();
	private Map<guid, ImportPolicy> records = new HashMap<guid, ImportPolicy>();
	private Map<String, ImportPolicy> fields = new HashMap<String, ImportPolicy>();
	private ImportPolicy defaultPolicy = ImportPolicy.DEFAULT;

	public TableRules() {
	}

	public TableRules(ImportPolicy policy) {
		setPolicy(policy);
	}

	public TableRules(guid recordId, ImportPolicy policy) {
		setPolicy(recordId, policy);
	}

	public TableRules(String field, ImportPolicy policy) {
		setPolicy(field, policy);
	}

	public TableRules(guid recordId, String field, ImportPolicy policy) {
		setPolicy(recordId, field, policy);
	}

	public void setPolicy(ImportPolicy policy) {
		defaultPolicy = policy;
	}

	public void setPolicy(String field, ImportPolicy policy) {
		fields.put(field, policy);
	}

	public void setPolicy(guid recordId, ImportPolicy policy) {
		records.put(recordId, policy);
	}

	public void setPolicy(guid recordId, String field, ImportPolicy policy) {
		Map<String, ImportPolicy> fields = recordFields.get(recordId);

		if(fields == null) {
			fields = new HashMap<String, ImportPolicy>();
			recordFields.put(recordId, fields);
		}
		
		fields.put(field, policy);
	}

	public ImportPolicy getPolicy(guid recordId) {
		ImportPolicy policy = records.get(recordId);
		return policy != null ? policy : defaultPolicy;
	}

	public ImportPolicy getPolicy(guid recordId, Field field) {
		return getPolicy(recordId, field.id());
	}

	public ImportPolicy getPolicy(guid recordId, String field) {
		Map<String, ImportPolicy> fieldsMap = recordFields.get(recordId);
		
		ImportPolicy policy = null;
		
		if(fieldsMap != null)
			policy = fieldsMap.get(field);
		
		if(policy != null)
			return policy;
		
		policy = records.get(field);
		
		if(policy != null)
			return policy;

		policy = fields.get(field);
		
		return policy != null ? policy : defaultPolicy;
	}

	
	private void writeObject(ObjectOutputStream out) throws IOException {
		serialize(out);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		deserialize(in);
	}

	@Override
	public void serialize(ObjectOutputStream out) throws IOException {
		RmiIO.writeLong(out, serialVersionUID);

		out.writeObject(recordFields);
		out.writeObject(records);
		out.writeObject(fields);
		out.writeObject(defaultPolicy);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		recordFields = (Map<guid, Map<String, ImportPolicy>>)in.readObject();
		records = (Map<guid, ImportPolicy>)in.readObject();
		fields = (Map<String, ImportPolicy>)in.readObject();
		defaultPolicy = (ImportPolicy)in.readObject();
	}
}
