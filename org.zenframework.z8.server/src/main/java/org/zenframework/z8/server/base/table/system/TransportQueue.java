package org.zenframework.z8.server.base.table.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BinaryField;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Sequencer;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.IsNot;
import org.zenframework.z8.server.ie.BaseMessage;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class TransportQueue extends Table {

	static public String TableName = "SystemTransportQueue";
	
	static public class names {
		static public String Address = "Address";
		static public String Sender = "Sender";
		static public String Ordinal = "Ordinal";
		static public String ClassId = "ClassId";
		static public String Data = "Data";
		static public String BytesTransferred = "BytesTransferred";
		static public String Processed = "Processed";
	}

	static public class strings {
		public static String Title = "TransportQueue.title";
		public static String Sender = "TransportQueue.sender";
		public static String Address = "TransportQueue.address";
		public static String Info = "TransportQueue.info";
		public static String Result = "TransportQueue.result";
		public static String Ordinal = "TransportQueue.ordinal";
		public static String Processed = "TransportQueue.processed";
		public static String BytesTransferred = "TransportQueue.bytesTransferred";
	}

	static public class displayNames {
		public static String Title = Resources.get(strings.Title);
		public static String Sender = Resources.get(strings.Sender);
		public static String Address = Resources.get(strings.Address);
		public static String Info = Resources.get(strings.Info);
		public static String Result = Resources.get(strings.Result);
		public static String Ordinal = Resources.get(strings.Ordinal);
		public static String Processed = Resources.get(strings.Processed);
		public static String BytesTransferred = Resources.get(strings.BytesTransferred);
	}

	static public class CLASS<T extends TransportQueue> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(TransportQueue.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new TransportQueue(container);
		}
	}

	public StringField.CLASS<StringField> address = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> sender = new StringField.CLASS<StringField>(this);

	public IntegerField.CLASS<IntegerField> ordinal = new IntegerField.CLASS<IntegerField>(this);
	public StringField.CLASS<StringField> classId = new StringField.CLASS<StringField>(this);
	public BinaryField.CLASS<BinaryField> data = new BinaryField.CLASS<BinaryField>(this);
	public IntegerField.CLASS<IntegerField> bytesTransferred = new IntegerField.CLASS<IntegerField>(this);
	public BoolField.CLASS<BoolField> processed = new BoolField.CLASS<BoolField>(this);

	static public TransportQueue newInstance() {
		return new TransportQueue.CLASS<TransportQueue>().get();
	}

	protected TransportQueue(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		sender.setName(names.Sender);
		sender.setIndex("sender");
		sender.setDisplayName(displayNames.Sender);
		sender.get().length = new integer(50);

		address.setName(names.Address);
		address.setIndex("address");
		address.setDisplayName(displayNames.Address);
		address.get().length = new integer(50);

		description.setDisplayName(displayNames.Result);

		ordinal.setName(names.Ordinal);
		ordinal.setIndex("ordinal");
		ordinal.setDisplayName(displayNames.Ordinal);

		classId.setName(names.ClassId);
		classId.setIndex("classId");
		classId.get().length = new integer(100);

		data.setName(names.Data);
		data.setIndex("data");

		bytesTransferred.setName(names.BytesTransferred);
		bytesTransferred.setIndex("bytesTransferred");
		bytesTransferred.setDisplayName(displayNames.BytesTransferred);

		processed.setName(names.Processed);
		processed.setIndex("processed");
		processed.setDisplayName(displayNames.Processed);

		registerDataField(address);
		registerDataField(sender);
		registerDataField(ordinal);
		registerDataField(classId);
		registerDataField(data);
		registerDataField(bytesTransferred);
		registerDataField(processed);
	}

	public void add(BaseMessage message) {
		sender.get().set(new string(message.getSender()));
		address.get().set(new string(message.getAddress()));
		ordinal.get().set(new integer(Sequencer.next(message.getAddress() + ".transport")));
		classId.get().set(new string(message.classId()));
		data.get().set(message.toBinary());
		create();
	}

	public void setProcessed(guid id, String info) {
		setProcessed(id, info, -1);
	}
	
	public void setProcessed(guid id, String info, long bytes) {
		destroy(id);
	}

	public void setBytesTrasferred(guid id, long bytes) {
		bytesTransferred.get().set(new integer(bytes));
		update(id);
	}

	public void setInfo(guid id, String info) {
		description.get().set(new string(info));
		update(id);
	}

	public Collection<String> getAddresses() {
		Collection<String> result = new ArrayList<String>();

		Field address = this.address.get();

		Collection<Field> fields = Arrays.<Field> asList(address);

		group(fields, fields);

		while(next())
			result.add(address.string().get());

		return result;
	}

	public Collection<guid> getMessages(String domain) {
		Collection<guid> result = new ArrayList<guid>();

		Field address = this.address.get();
		Field processed = this.processed.get();

		Collection<Field> fields = Arrays.<Field> asList(recordId.get());
		Collection<Field> orderBy = Arrays.<Field> asList(ordinal.get());

		SqlToken where = new And(new IsNot(processed), new Equ(address, domain));

		read(fields, orderBy, where);

		while(next())
			result.add(recordId());

		return result;
	}

	public BaseMessage getMessage(guid id) {
		Field data = this.data.get();
		Field classId = this.classId.get();
		Field bytesTransferred = this.bytesTransferred.get();

		Collection<Field> fields = Arrays.<Field> asList(data, classId, bytesTransferred);

		if(!readRecord(id, fields))
			return null;
				
		BaseMessage result = (BaseMessage)Loader.getInstance(classId.string().get());
		result.fromBinary(data.get().binary());
		result.setId(recordId());
		result.setBytesTransferred(bytesTransferred.integer().get());
		return result;
	}
}
