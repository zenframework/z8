package org.zenframework.z8.oda.driver;

import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.INamedObject;
import org.zenframework.z8.server.runtime.IClass;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.security.IAccess;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class OdaField implements IField {
	private String id;
	private String displayName;
	private FieldType type;

	public OdaField(String id, String displayName, FieldType type) {
		this.id = id;
		this.type= type;
		this.displayName = displayName;
	}

	public OdaField(JsonObject json) {
		this.id = json.getString(Json.id);
		this.type = FieldType.fromString(json.getString(Json.type));
		this.displayName = json.getString(Json.displayName);
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String displayName() {
		return displayName;
	}

	@Override
	public FieldType type() {
		return type;
	}

	@Override
	public FieldType metaType() {
		return type();
	}

	@Override
	public String classId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public guid classIdKey() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int controlSum() {
		throw new UnsupportedOperationException();
	}

	@Override
	public guid key() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setKey(guid key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int ordinal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOrdinal(int ordinal) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String name() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDisplayName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String description() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDescription(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean system() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSystem(boolean system) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String ui() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUi(String ui) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String presentation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPresentation(String presentation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean foreignKey() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setForeignKey(boolean foreignKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IObject getContainer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void initMembers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<IClass<? extends IObject>> members() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IClass<? extends IObject> getMember(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IObject getOwner() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOwner(IObject owner) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IClass<? extends IObject> getCLASS() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCLASS(IClass<? extends IObject> cls) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String index() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIndex(String index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, String> getAttributes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAttributes(Map<String, String> attributes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasAttribute(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAttribute(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAttribute(String key, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttribute(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void constructor1() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void constructor2() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void constructor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toDebugString() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(INamedObject o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String sqlType(DatabaseVendor vendor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int scale() {
		throw new UnsupportedOperationException();
	}

	@Override
	public primary getDefaultValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public primary get() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(primary value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public primary parse(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean wasNull() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean changed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query owner() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IAccess access() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPrimaryKey() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isParentKey() {
		throw new UnsupportedOperationException();
	}
}
