package org.zenframework.z8.server.base.table.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BinaryField;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Sequencer;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.IsNot;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class TransportQueue extends Table {

	static public String TableName = "SystemTransportQueue";

	static public class fieldNames {
		static public String Address = "Address";
		static public String Sender = "Sender";
		static public String Ordinal = "Ordinal";
		static public String ClassId = "ClassId";
		static public String Data = "Data";
		static public String BytesTransferred = "BytesTransferred";
		static public String Processed = "Processed";
		static public String Result = "Result";
	}

	static public class strings {
		public static String Title = "TransportQueue.title";
		public static String Name = "TransportQueue.name";
		public static String Description = "TransportQueue.description";
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
		public static String Name = Resources.get(strings.Name);
		public static String Description = Resources.get(strings.Description);
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
	public TextField.CLASS<TextField> result = new TextField.CLASS<TextField>(this);

	static public TransportQueue newInstance() {
		return new TransportQueue.CLASS<TransportQueue>().get();
	}

	protected TransportQueue(IObject container) {
		super(container);
	}

	@Override
	public void initMembers() {
		super.initMembers();

		objects.add(address);
		objects.add(sender);
		objects.add(ordinal);
		objects.add(classId);
		objects.add(data);
		objects.add(bytesTransferred);
		objects.add(processed);
		objects.add(result);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		name.get().setDisplayName(displayNames.Name);
		name.get().length = new integer(100);

		description.get().setDisplayName(displayNames.Description);

		sender.setName(fieldNames.Sender);
		sender.setIndex("sender");
		sender.setDisplayName(displayNames.Sender);
		sender.get().length = new integer(50);

		address.setName(fieldNames.Address);
		address.setIndex("address");
		address.setDisplayName(displayNames.Address);
		address.get().length = new integer(50);

		result.setName(fieldNames.Result);
		result.setIndex("result");
		result.setDisplayName(displayNames.Result);

		ordinal.setName(fieldNames.Ordinal);
		ordinal.setIndex("ordinal");
		ordinal.setDisplayName(displayNames.Ordinal);

		classId.setName(fieldNames.ClassId);
		classId.setIndex("classId");
		classId.get().length = new integer(100);

		data.setName(fieldNames.Data);
		data.setIndex("data");

		bytesTransferred.setName(fieldNames.BytesTransferred);
		bytesTransferred.setIndex("bytesTransferred");
		bytesTransferred.setDisplayName(displayNames.BytesTransferred);

		processed.setName(fieldNames.Processed);
		processed.setIndex("processed");
		processed.setDisplayName(displayNames.Processed);
	}

	public void add(Message message) {
		name.get().set(StringUtils.left(message.getName(), name.get().length.getInt()));
		description.get().set(message.getDescription());
		sender.get().set(new string(message.getSender()));
		address.get().set(new string(message.getAddress()));
		ordinal.get().set(new integer(Sequencer.next(message.getAddress() + ".transport")));
		classId.get().set(new string(message.classId()));
		data.get().set(message.toBinary());
		create();
	}

	public void setProcessed(guid id) {
		destroy(id);
	}

	public void setBytesTrasferred(guid id, long bytes) {
		bytesTransferred.get().set(new integer(bytes));
		result.get().set(new string());
		update(id);
	}

	public void setInfo(guid id, String info) {
		result.get().set(new string(info));
		update(id);
	}

	public Collection<String> getAddresses() {
		Collection<String> result = new ArrayList<String>();

		Field address = this.address.get();

		Collection<Field> fields = Arrays.<Field>asList(address);

		group(fields, fields);

		while(next())
			result.add(address.string().get());

		return result;
	}

	public Collection<guid> getMessages(String domain) {
		Collection<guid> result = new ArrayList<guid>();

		Field address = this.address.get();
		Field processed = this.processed.get();

		Collection<Field> fields = Arrays.<Field>asList(recordId.get());
		Collection<Field> orderBy = Arrays.<Field>asList(ordinal.get());

		SqlToken where = new And(new IsNot(processed), new Equ(address, domain));

		read(fields, orderBy, where);

		while(next())
			result.add(recordId());

		return result;
	}

	public Message getMessage(guid id) {
		Field data = this.data.get();
		Field classId = this.classId.get();
		Field bytesTransferred = this.bytesTransferred.get();

		Collection<Field> fields = Arrays.<Field>asList(data, classId, bytesTransferred);

		if(!readRecord(id, fields))
			return null;

		Message result = (Message) Loader.getInstance(classId.string().get());
		result.fromBinary((binary)data.get());
		result.setId(recordId());
		result.setBytesTransferred(bytesTransferred.integer().get());
		return result;
	}
}
