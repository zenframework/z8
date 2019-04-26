package org.zenframework.z8.server.types;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.sql.sql_guid;

public class guid extends primary {

	private static final long serialVersionUID = -8410843852109029003L;

	private static final UUID nullValue = UUID.fromString("00000000-0000-0000-0000-000000000000");

	private UUID value;

	static final public guid Null = new guid() {

		private static final long serialVersionUID = 1745933898572308263L;

		@Override
		public void set(String guid) {
			throw new UnsupportedOperationException();
		}

	};

	public guid() {
		value = nullValue;
	}

	public guid(guid guid) {
		set(guid != null ? guid.value : nullValue);
	}

	public guid(UUID guid) {
		set(guid != null ? guid : nullValue);
	}

	public guid(String guid) {
		if (guid != null)
			set(guid);
		else
			set(nullValue);
	}

	public guid(byte[] data) {
		BigInteger ui = new BigInteger(data);
		set(ui.toString(16));
	}

	public guid(long mostSigBits, long leastSigBits) {
		this(new UUID(mostSigBits, leastSigBits));
	}

	public boolean isNull() {
		return equals(guid.Null);
	}

	static public guid create() {
		return new guid(UUID.randomUUID());
	}

	static public guid create(String value) {
		return new guid(UUID.nameUUIDFromBytes(value.getBytes()));
	}

	static public guid create(long value) {
		return create(0, value);
	}

	static public guid create(long n1, long n2) {
		return new guid(new UUID(n1, n2));
	}

	@Override
	public String toString() {
		return value.toString().toUpperCase();
	}

	public String toString(boolean useDelimiter) {
		return (useDelimiter ? value.toString() : value.toString().replace("-", "")).toUpperCase();
	}

	public UUID get() {
		return value;
	}

	public void set(UUID value) {
		this.value = value;
	}

	public void set(guid guid) {
		set(guid != null ? guid.value : null);
	}

	public void set(String guid) {
		if (guid == null || guid.trim().equals("") || guid.trim().equals("0")) {
			value = nullValue;
		} else {
			if (guid.length() == 32)
				guid = guid.substring(0, 8) + "-" + guid.substring(8, 12) + "-" + guid.substring(12, 16) + "-"
						+ guid.substring(16, 20) + "-" + guid.substring(20, 32);
			value = UUID.fromString(guid);
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Guid;
	}

	@Override
	public String toDbConstant(DatabaseVendor dbtype) {
		switch (dbtype) {
		case Oracle:
			return "HEXTORAW('" + toString(false) + "')";
		case Postgres:
		case SqlServer:
		default:
			return "'" + toString(true) + "'";
		}
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof guid)
			return operatorEqu((guid) obj).get();
		return false;
	}

	@Override
	public int compareTo(primary primary) {
		if(primary instanceof guid) {
			guid guid = (guid)primary;
			return value.compareTo(guid.value);
		}

		return -1;
	}

	public sql_guid sql_guid() {
		return new sql_guid(this);
	}

	public bool operatorEqu(guid x) {
		return new bool(value.equals(x.value));
	}

	public bool operatorNotEqu(guid x) {
		return new bool(!value.equals(x.value));
	}

	public RCollection<integer> z8_components() {
		return new RCollection<integer>(Arrays.asList(new integer(value.getMostSignificantBits()), new integer(value.getLeastSignificantBits())));
	}

	public integer z8_minor() {
		return new integer(value.getLeastSignificantBits());
	}

	public integer z8_major() {
		return new integer(value.getMostSignificantBits());
	}

	static public guid z8_create() {
		return create();
	}

	static public guid z8_create(integer n1, integer n2) {
		return create(n1.get(), n2.get());
	}

	static public guid z8_create(integer value) {
		return create(value.get());
	}

	static public guid z8_create(string value) {
		return create(value.get());
	}

	static public guid z8_parse(string string) {
		return new guid(string != null ? string.get() : null);
	}
}
