package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.engine.RmiIO;
import org.zenframework.z8.server.engine.RmiSerializable;
import org.zenframework.z8.server.types.guid;

public class ExportRules implements RmiSerializable, Serializable {
	private static final long serialVersionUID = -1372906719057513100L;

	private Map<String, TableRules> tables = new HashMap<String, TableRules>();
	private ImportPolicy defaultPolicy = ImportPolicy.DEFAULT;

	public ImportPolicy getPolicy(String table, String field, guid recordId) {
		TableRules rules = tables.get(table);
		return rules != null ? rules.getPolicy(recordId, field) : defaultPolicy;
	}

	public void add(ImportPolicy policy) {
		defaultPolicy = policy;
	}

	private TableRules getTableRules(String table) {
		TableRules rules = tables.get(table);

		if(rules == null) {
			rules = new TableRules();
			tables.put(table, rules);
		}
		
		return rules;
	}
	
	public void add(String table, ImportPolicy policy) {
		TableRules rules = getTableRules(table);
		rules.setPolicy(policy);
	}

	public void add(String table, guid recordId, ImportPolicy policy) {
		TableRules rules = getTableRules(table);
		rules.setPolicy(recordId, policy);
	}

	public void add(String table, String field, ImportPolicy policy) {
		TableRules rules = getTableRules(table);
		rules.setPolicy(field, policy);
	}

	public void add(String table, guid recordId, String field, ImportPolicy policy) {
		TableRules rules = getTableRules(table);
		rules.setPolicy(recordId, field, policy);
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

		out.writeObject(tables);
		out.writeObject(defaultPolicy);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void deserialize(ObjectInputStream in) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		long version = RmiIO.readLong(in);

		tables = (Map<String, TableRules>)in.readObject();
		defaultPolicy = (ImportPolicy)in.readObject();
	}
}
