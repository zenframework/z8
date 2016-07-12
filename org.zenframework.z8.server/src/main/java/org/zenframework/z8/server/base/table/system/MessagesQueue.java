package org.zenframework.z8.server.base.table.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Sequencer;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.base.view.filter.Filter;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.IsNot;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.StringUtils;

public class MessagesQueue extends Table {

	public static final String TableName = "SystemExportMessages";
	public static final String MessageClass = Message.class.getCanonicalName();
	
	static public class names {
		public final static String Ordinal = "Ordinal";
		public final static String ClassId = "ClassId";

		public final static String Processed = "Sent";
		public final static String Xml = "Xml";
		
		public final static String BytesTransferred = "BytesTransferred";
	}

	static public class strings {
		public final static String Title = "MessagesQueue.title";
		public final static String Sender = "MessagesQueue.sender";
		public final static String Address = "MessagesQueue.address";
		public final static String Info = "MessagesQueue.info";
		public final static String Result = "MessagesQueue.result";
		public final static String Message = "MessagesQueue.message";
		public final static String Ordinal = "MessagesQueue.ordinal";
		public final static String ClassId = "MessagesQueue.classId";
		public final static String Processed = "MessagesQueue.processed";
		public final static String BytesTransferred = "MessagesQueue.bytesTransferred";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
		public final static String Sender = Resources.get(strings.Sender);
		public final static String Address = Resources.get(strings.Address);
		public final static String Info = Resources.get(strings.Info);
		public final static String Result = Resources.get(strings.Result);
		public final static String Message = Resources.get(strings.Message);
		public final static String Ordinal = Resources.get(strings.Ordinal);
		public final static String ClassId = Resources.get(strings.ClassId);
		public final static String Processed = Resources.get(strings.Processed);
		public final static String BytesTransferred = Resources.get(strings.BytesTransferred);
	}

	public static class CLASS<T extends MessagesQueue> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(MessagesQueue.class);
			setName(TableName);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new MessagesQueue(container);
		}
	}

	public static enum Direction {

		IN("in_"), OUT("out_");

		private final String prefix;

		private Direction(String prefix) {
			this.prefix = prefix;
		}

		public String getPrefix() {
			return prefix;
		}

	}

	private static class PreserveMessagesQueueListener implements Properties.Listener {

		@Override
		public void onPropertyChange(String key, String value) {
			if(ServerRuntime.PreserveMessagesQueueProperty.equalsKey(key))
				preserveMessagesQueue = Boolean.parseBoolean(value);
		}
	}

	static {
		Properties.addListener(new PreserveMessagesQueueListener());
	}

	private static volatile Boolean preserveMessagesQueue = null;

	public final Domains.CLASS<Domains> domains = new Domains.CLASS<Domains>(this);
	public final IntegerField.CLASS<IntegerField> ordinal = new IntegerField.CLASS<IntegerField>(this);
	public final StringField.CLASS<StringField> classId = new StringField.CLASS<StringField>(this);
	public final TextField.CLASS<TextField> message = new TextField.CLASS<TextField>(this);
	public final BoolField.CLASS<BoolField> processed = new BoolField.CLASS<BoolField>(this);
	public final IntegerField.CLASS<IntegerField> bytesTransferred = new IntegerField.CLASS<IntegerField>(this);

	public final StringField.CLASS<? extends StringField> sender = id;
	public final StringField.CLASS<? extends StringField> address = id1;
	
	public static MessagesQueue newInstance() {
		return new MessagesQueue.CLASS<MessagesQueue>().get();
	}

	protected MessagesQueue(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		sender.setDisplayName(displayNames.Sender);
		sender.get().length = new integer(50);

		address.setDisplayName(displayNames.Address);
		address.get().length = new integer(50);

		name.setDisplayName(displayNames.Info);
		name.get().length = new integer(1024);

		description.setDisplayName(displayNames.Result);

		ordinal.setName(names.Ordinal);
		ordinal.setIndex("ordinal");
		ordinal.setDisplayName(displayNames.Ordinal);

		classId.setName(names.ClassId);
		classId.setIndex("classId");
		classId.setDisplayName(displayNames.ClassId);
		classId.get().length = new integer(100);

		processed.setName(names.Processed);
		processed.setIndex("processed");
		processed.setDisplayName(displayNames.Processed);

		bytesTransferred.setName(names.BytesTransferred);
		bytesTransferred.setIndex("bytesTransferred");
		bytesTransferred.setDisplayName(displayNames.BytesTransferred);

		message.setName(names.Xml);
		message.setIndex("xml");
		message.setDisplayName(displayNames.Message);

		registerDataField(ordinal);
		registerDataField(classId);
		registerDataField(processed);
		registerDataField(bytesTransferred);
		registerDataField(message);
	}

	public void addMessage(Message message, String transportInfo, Direction direction) throws JAXBException {
		guid recordId = new guid(message.getId());
		sender.get().set(new string(message.getSender()));
		address.get().set(new string(message.getAddress()));
		name.get().set(StringUtils.cut(message.getInfo(), this.name.get().length.getInt()));
		if(transportInfo != null)
			description.get().set(new string(transportInfo));
		ordinal.get().set(new integer(nextOrdinal(message, direction)));
		classId.get().set(new string(message.classId()));
		this.message.get().set(new string(message.getXml()));

		if(hasRecord(recordId))
			update(recordId);
		else
			create(recordId);
	}

	public void processed(guid id, String transportInfo) {
		if(preserveMessagesQueue == null)
			preserveMessagesQueue = Boolean.parseBoolean(Properties.getProperty(ServerRuntime.PreserveMessagesQueueProperty));

		if(preserveMessagesQueue) {
			if(transportInfo != null)
				description.get().set(transportInfo);
			processed.get().set(new bool(true));
			update(id);
		} else
			destroy(id);
	}

	public void transferred(guid id, long bytes) {
		bytesTransferred.get().set(new integer(bytes));
		update(id);
	}

	public void info(guid id, String info) {
		description.get().set(new string(info));
		update(id);
	}

	public String getTransportUrl() {
		return name.get().string().get();
	}

	private Collection<string> getDomains() {
		return string.wrap(Domains.newInstance().getNames());
	}

	public Collection<String> getAddresses() {
		Collection<String> result = new ArrayList<String>();

		Field sender = this.sender.get();
		Field address = this.address.get();

		Collection<Field> fields = Arrays.<Field> asList(address);

		Collection<string> locals = getDomains();

		SqlToken out = new IsNot(new InVector(address, locals));
		SqlToken domain = new InVector(sender, locals);

		group(fields, fields, new And(domain, out));

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

	public List<guid> getExportMessages(String sender, JsonArray filters) {
		return getExportMessages(sender, null, filters);
	}

	public List<guid> getExportMessages(String from, String to, JsonArray filters) {
		List<guid> result = new LinkedList<guid>();

		Field sender = this.sender.get();
		Field address = this.address.get();

		SqlToken notProcessed = new Unary(Operation.Not, processed.get());
		SqlToken notLocal = new Unary(Operation.Not, new InVector(address, getDomains()));
		SqlToken senderEq = new Equ(sender, from);
		SqlToken where = new And(new And(notProcessed, notLocal), senderEq);

		if(filters != null && !filters.isEmpty())
			where = new And(where, Query.parseWhere(Filter.parse(filters, this)));

		if(to != null) {
			SqlToken addressEq = new Equ(address, to);
			where = new And(where, addressEq);
		}

		Collection<Field> fields = Arrays.<Field> asList(recordId.get());
		Collection<Field> orderBy = Arrays.<Field> asList(ordinal.get());

		read(fields, orderBy, where);

		while(next())
			result.add(recordId());

		return result;
	}

	public List<guid> getImportMessages(String selfAddress) {
		SqlToken notProcessed = new IsNot(processed.get());
		SqlToken forMe = new Equ(address.get(), selfAddress);
		read(Arrays.<Field> asList(recordId.get()), Arrays.<Field> asList(ordinal.get()), new And(notProcessed, forMe));
		List<guid> result = new LinkedList<guid>();
		
		while(next())
			result.add(recordId());
		
		return result;
	}

	private long nextOrdinal(Message message, Direction direction) {
		return Sequencer.next(direction.getPrefix() + message.getAddress());
	}

	public Message getMessage(guid id) {
		if(!readRecord(id, getDataFields()))
			return null;
		
		String classId = this.classId.get().get().string().get();
		
		if(classId.isEmpty())
			classId = Message.class.getCanonicalName();

		Message result = (Message)Loader.getInstance(!classId.isEmpty() ? classId : MessageClass);
		result.setId(recordId());
		result.setTime(createdAt.get().datetime());
		result.setAddress(address.get().string().get());
		result.setSender(sender.get().string().get());
		result.setBytesTransferred(bytesTransferred.get().integer().get());
		result.setXml(message.get().get().toString());
		return result;
	}
}
