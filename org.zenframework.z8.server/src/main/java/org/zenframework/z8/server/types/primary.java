package org.zenframework.z8.server.types;

import java.io.Serializable;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;

public class primary implements Serializable {

	private static final long serialVersionUID = -6139111122281366413L;

	public primary defaultValue() {
		throw new UnsupportedOperationException();
	}

	public FieldType type() {
		throw new UnsupportedOperationException();
	}

	public String format() {
		return toString();
	}

	public String toDbConstant(DatabaseVendor dbtype) {
		throw new UnsupportedOperationException();
	}

	public integer z8_hashCode() {
		return new integer(hashCode());
	}

	public string z8_toString() {
		return new string(toString());
	}

	public string string() {
		return z8_toString();
	}

	public file file() {
		return (file) this;
	}

	public binary binary() {
		return (binary) this;
	}

	public bool bool() {
		return (bool) this;
	}

	public guid guid() {
		return (guid) this;
	}

	public date date() {
		return (date) this;
	}

	public datespan datespan() {
		return (datespan) this;
	}

	public decimal decimal() {
		return (decimal) this;
	}

	public integer integer() {
		return (integer) this;
	}

	static public primary create(String type, String value) {
		return create(FieldType.fromString(type), value);
	}

	static public primary create(FieldType type, String value) {
		switch(type) {
		case Binary:
			return new binary(value);
		case Boolean:
			return new bool(value);
		case Date:
		case Datetime:
			return new date(value);
		case Datespan:
			return new datespan(value);
		case Decimal:
			return new decimal(value);
		case Guid:
			return new guid(value);
		case Integer:
			return new integer(value);
		case String:
		case Text:
		case Attachments:
			return new string(value);
		default:
			throw new UnsupportedOperationException();
		}
	}

	static public primary clone(primary value) {
		if(value == null)
			return null;

		if(value instanceof binary)
			return new binary((binary) value);
		else if(value instanceof bool)
			return new bool((bool) value);
		else if(value instanceof date)
			return new date((date) value);
		else if(value instanceof datespan)
			return new datespan((datespan) value);
		else if(value instanceof decimal)
			return new decimal((decimal) value);
		else if(value instanceof file)
			return new file((file) value);
		else if(value instanceof guid)
			return new guid((guid) value);
		else if(value instanceof integer)
			return new integer((integer) value);
		else if(value instanceof string)
			return new string((string) value);

		throw new UnsupportedOperationException();
	}

	public bool z8_toBool() {
		return (bool) this;
	}

	public guid z8_toGuid() {
		return (guid) this;
	}

	public date z8_toDate() {
		return (date) this;
	}

	public datespan z8_toDatespan() {
		return (datespan) this;
	}

	public decimal z8_toDecimal() {
		return (decimal) this;
	}

	public integer z8_toInt() {
		return (integer) this;
	}
}
