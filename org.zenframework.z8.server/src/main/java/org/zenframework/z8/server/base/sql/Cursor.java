package org.zenframework.z8.server.base.sql;

import java.sql.SQLException;

import org.zenframework.z8.server.db.BasicSelect;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Cursor extends OBJECT {

	public static class CLASS<T extends Cursor> extends OBJECT.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Cursor.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Cursor(container);
		}
	}

	public org.zenframework.z8.server.db.Cursor cursor;

	public Cursor(IObject container) {
		super(container);
	}

	static public Cursor.CLASS<? extends Cursor> z8_open(Database.CLASS<? extends Database> database, string sql) {
		Cursor.CLASS<Cursor> cls = new Cursor.CLASS<Cursor>(null);
		try {
			cls.get().cursor = BasicSelect.cursor(database.get().database, sql.get());
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
		return cls;
	}

	public bool z8_next() {
		try {
			return new bool(cursor.next());
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void z8_close() {
		cursor.close();
	}

	public string z8_string(integer position) {
		try {
			return cursor.getString(position.getInt());
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public integer z8_integer(integer position) {
		try {
			return cursor.getInteger(position.getInt());
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public decimal z8_decimal(integer position) {
		try {
			return cursor.getDecimal(position.getInt());
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public date z8_date(integer position) {
		try {
			return cursor.getDate(position.getInt());
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public date z8_timestamp(integer position) {
		try {
			return cursor.getTimestamp(position.getInt());
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public guid z8_guid(integer position) {
		try {
			return cursor.getGuid(position.getInt());
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public RCollection z8_string_array(integer position) {
		try {
			RCollection array = new RCollection();
			JsonArray json = new JsonArray(cursor.getString(position.getInt()));
			for (int i = 0; i < json.size(); i++)
				array.add(new string(json.getString(i)));
			return array;
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public RCollection z8_integer_array(integer position) {
		try {
			RCollection array = new RCollection();
			JsonArray json = new JsonArray(cursor.getString(position.getInt()));
			for (int i = 0; i < json.size(); i++)
				array.add(new integer(json.getString(i)));
			return array;
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public RCollection z8_decimal_array(integer position) {
		try {
			RCollection array = new RCollection();
			JsonArray json = new JsonArray(cursor.getString(position.getInt()));
			for (int i = 0; i < json.size(); i++)
				array.add(new decimal(json.getString(i)));
			return array;
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public RCollection z8_date_array(integer position) {
		try {
			RCollection array = new RCollection();
			JsonArray json = new JsonArray(cursor.getString(position.getInt()));
			for (int i = 0; i < json.size(); i++)
				array.add(new date(json.getString(i)));
			return array;
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public RCollection z8_guid_array(integer position) {
		try {
			RCollection array = new RCollection();
			JsonArray json = new JsonArray(cursor.getString(position.getInt()));
			for (int i = 0; i < json.size(); i++)
				array.add(new guid(json.getString(i)));
			return array;
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
