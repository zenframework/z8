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
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.IsNot;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class MessageQueue extends Table {

	static public String TableName = "SystemMessageQueue";

	static public class fieldNames {
		static public String Address = "Address";
		static public String Sender = "Sender";
		static public String Ordinal = "Ordinal";
		static public String ClassId = "ClassId";
		static public String Data = "Data";
		static public String Processing = "Processing";
	}

	static public class strings {
		static public String Title = "MessagesQueue.title";
		public static String Name = "MessagesQueue.name";
		public static String Description = "MessagesQueue.description";
		static public String Sender = "MessagesQueue.sender";
		static public String Address = "MessagesQueue.address";
		static public String Ordinal = "MessagesQueue.ordinal";
		static public String Processing = "MessagesQueue.processing";
	}

	static public class displayNames {
		static public String Title = Resources.get(strings.Title);
		public static String Name = Resources.get(strings.Name);
		public static String Description = Resources.get(strings.Description);
		static public String Sender = Resources.get(strings.Sender);
		static public String Address = Resources.get(strings.Address);
		static public String Ordinal = Resources.get(strings.Ordinal);
		static public String Processing = Resources.get(strings.Processing);
	}

	static public class CLASS<T extends MessageQueue> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(MessageQueue.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new MessageQueue(container);
		}
	}

	public StringField.CLASS<StringField> address = new StringField.CLASS<StringField>(this);
	public StringField.CLASS<StringField> sender = new StringField.CLASS<StringField>(this);

	public IntegerField.CLASS<IntegerField> ordinal = new IntegerField.CLASS<IntegerField>(this);
	public StringField.CLASS<StringField> classId = new StringField.CLASS<StringField>(this);
	public BinaryField.CLASS<BinaryField> data = new BinaryField.CLASS<BinaryField>(this);
	public BoolField.CLASS<BoolField> processing = new BoolField.CLASS<BoolField>(this);

	static public MessageQueue newInstance() {
		return new MessageQueue.CLASS<MessageQueue>().get();
	}

	protected MessageQueue(IObject container) {
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
		objects.add(processing);
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

		ordinal.setName(fieldNames.Ordinal);
		ordinal.setIndex("ordinal");
		ordinal.setDisplayName(displayNames.Ordinal);

		classId.setName(fieldNames.ClassId);
		classId.setIndex("classId");
		classId.get().length = new integer(100);

		data.setName(fieldNames.Data);
		data.setIndex("data");

		processing.setName(fieldNames.Processing);
		processing.setIndex("processing");
		processing.setDisplayName(displayNames.Processing);
	}

	public void add(Message message) {
		name.get().set(StringUtils.left(message.getName(), name.get().length.getInt()));
		description.get().set(message.getDescription());
		sender.get().set(new string(message.getSender()));
		address.get().set(new string(message.getAddress()));
		ordinal.get().set(new integer(Sequencer.next(message.getAddress() + ".message")));
		classId.get().set(new string(message.classId()));
		data.get().set(message.toBinary());
		create();
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

	public Collection<Message> getMessages(String domain) {
		Collection<Message> result = new ArrayList<Message>();

		Field name = this.name.get();
		Field description = this.description.get();
		Field address = this.address.get();
		Field processing = this.processing.get();
		Field data = this.data.get();
		Field classId = this.classId.get();

		Collection<Field> fields = Arrays.<Field>asList(name, description, data, classId);
		Collection<Field> orderBy = Arrays.<Field>asList(ordinal.get());

		SqlToken where = new And(new IsNot(processing), new Equ(address, domain));

		read(fields, orderBy, where, 10);

		while(next()) {
			Message message = (Message) Loader.getInstance(classId.string().get());
			message.setName(name.get().toString());
			message.setDescription(description.get().toString());
			message.fromBinary(data.binary());
			message.setId(recordId());
			result.add(message);
		}

		return result;
	}

	public void beginProcessing(guid id) {
		processing.get().set(bool.True);
		update(id);
	}

	public void endProcessing(guid id) {
		destroy(id);
	}
}
