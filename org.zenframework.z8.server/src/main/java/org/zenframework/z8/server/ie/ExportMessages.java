package org.zenframework.z8.server.ie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.table.system.SystemDomains;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.AttachmentExpression;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Sequencer;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.base.view.filter.Filter;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.IsNot;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;
import org.zenframework.z8.server.utils.StringUtils;

public class ExportMessages extends Table {

	public static final String TableName = "SystemExportMessages";

	public static class CLASS<T extends ExportMessages> extends Table.CLASS<T> {

		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(ExportMessages.class);
			setName(TableName);
			setDisplayName(Resources.get(strings.Title));
		}

		@Override
		public Object newObject(IObject container) {
			return new ExportMessages(container);
		}

	}

	public static class MessageAttachmentExpression extends AttachmentExpression {

		public static class CLASS<T extends MessageAttachmentExpression> extends AttachmentExpression.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(MessageAttachmentExpression.class);
			}

			@Override
			public Object newObject(IObject container) {
				return new MessageAttachmentExpression(container);
			}
		}

		public MessageAttachmentExpression(IObject container) {
			super(container);
		}

		@Override
		protected String attachmentName() {
			return "message.xml";
		}

		@Override
		protected String contentFieldName() {
			return "Xml";
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

	static public class names {
/*		public final static String Sender = "ExportMessages.sender";
		public final static String Receiver = "ExportMessages.receiver";
		public final static String Info = "ExportMessages.info";
		public final static String Transport = "ExportMessages.transport";
		public final static String Message = "ExportMessages.message";
		public final static String Ordinal = "ExportMessages.ordinal";
		public final static String ClassId = "ExportMessages.classId";
		public final static String Processed = "ExportMessages.processed";
		public final static String Error = "ExportMessages.error";*/
		public final static String BytesTransferred = "BytesTransferred";
	}

	static public class strings {
		public final static String Title = "ExportMessages.title";
		public final static String Sender = "ExportMessages.sender";
		public final static String Receiver = "ExportMessages.receiver";
		public final static String Info = "ExportMessages.info";
		public final static String Transport = "ExportMessages.transport";
		public final static String Message = "ExportMessages.message";
		public final static String Ordinal = "ExportMessages.ordinal";
		public final static String ClassId = "ExportMessages.classId";
		public final static String Processed = "ExportMessages.processed";
		public final static String Error = "ExportMessages.error";
		public final static String BytesTransferred = "ExportMessages.bytesTransferred";
	}

	private static class PreserveExportMessagesListener implements Properties.Listener {

		@Override
		public void onPropertyChange(String key, String value) {
			if (ServerRuntime.PreserveExportMessagesProperty.equalsKey(key))
				preserveExportMessages = Boolean.parseBoolean(value);
		}

	}

	static {
		Properties.addListener(new PreserveExportMessagesListener());
	}

	private static volatile Boolean preserveExportMessages = null;

	public final SystemDomains.CLASS<SystemDomains> domains = new SystemDomains.CLASS<SystemDomains>(this);
	public final IntegerField.CLASS<IntegerField> ordinal = new IntegerField.CLASS<IntegerField>(this);
	public final StringField.CLASS<StringField> classId = new StringField.CLASS<StringField>(this);
	public final TextField.CLASS<TextField> message = new TextField.CLASS<TextField>(this);
	public final BoolField.CLASS<BoolField> processed = new BoolField.CLASS<BoolField>(this);
	public final BoolField.CLASS<BoolField> error = new BoolField.CLASS<BoolField>(this);
	public final IntegerField.CLASS<IntegerField> bytesTransferred = new IntegerField.CLASS<IntegerField>(this);

	
	public final MessageAttachmentExpression.CLASS<MessageAttachmentExpression> attachment = new MessageAttachmentExpression.CLASS<MessageAttachmentExpression>(
			this);

	private ExportMessages(IObject container) {
		super(container);
	}

	public void addMessage(Message message, String transportInfo, Direction direction) throws JAXBException {
		guid recordId = new guid(message.getId());
		this.id.get().set(new string(message.getSender()));
		this.id1.get().set(new string(message.getAddress()));
		this.name.get().set(StringUtils.cut(message.getInfo(), this.name.get().length.getInt()));
		if (transportInfo != null)
			this.description.get().set(new string(transportInfo));
		this.ordinal.get().set(new integer(nextOrdinal(message, direction)));
		this.classId.get().set(new string(message.classId()));
		this.message.get().set(new string(message.getXml()));

		if (hasRecord(recordId))
			update(recordId);
		else
			create(recordId);
	}

	public void processed(guid id, String transportInfo) {
		ExportMessages messages = new ExportMessages.CLASS<ExportMessages>().get();

		if(preserveExportMessages == null)
			preserveExportMessages = Boolean.parseBoolean(Properties.getProperty(ServerRuntime.PreserveExportMessagesProperty));
				
		if (preserveExportMessages) {
			if (transportInfo != null)
				messages.description.get().set(transportInfo);
			messages.processed.get().set(new bool(true));
			messages.update(id);
		} else {
			messages.destroy(id);
		}
	}

	public void transferred(guid id, long bytes) {
		ExportMessages messages = new ExportMessages.CLASS<ExportMessages>().get();

		messages.description.get().set(bytes + "B");
		messages.bytesTransferred.get().set(new integer(bytes));
		messages.update(id);
	}

	public void info(guid id, String info) {
		ExportMessages messages = new ExportMessages.CLASS<ExportMessages>().get();
		messages.description.get().set(new string(info));
		messages.update(id);
	}

	public void error(guid id, String info) {
		description.get().set(new string(info));
		update(id);
	}

	public void setError(Message message, Throwable e) {
		error.get().set(new bool(true));
		description.get().set(
				new string(new datetime() + " " + ": '" + ErrorUtils.getMessage(e) + "' " + e.getClass()));
		update(new guid(message.getId()));
	}

	public String getSender() {
		return id.get().get().toString();
	}

	public String getReceiver() {
		return id1.get().get().toString();
	}

	public String getTransportUrl() {
		return name.get().get().string().get();
	}

	@Override
	public void constructor2() {
		super.constructor2();

		id.setDisplayName(Resources.get(strings.Sender));
		id.get().length = new integer(50);

		id1.setDisplayName(Resources.get(strings.Receiver));
		id1.get().length = new integer(50);

		createdAt.setSystem(false);

		name.setDisplayName(Resources.get(strings.Info));
		name.get().length = new integer(1024);

		description.setDisplayName(Resources.get(strings.Transport));

		ordinal.setName("Ordinal");
		ordinal.setIndex("ordinal");
		ordinal.setDisplayName(Resources.get(strings.Ordinal));
		ordinal.get().indexFields.add(id);
		ordinal.get().indexFields.add(id1);
		ordinal.get().unique = new bool(true);
		ordinal.get().aggregation = Aggregation.Max;

		classId.setName("ClassId");
		classId.setIndex("classId");
		classId.setDisplayName(Resources.get(strings.ClassId));
		classId.get().length = new integer(100);

		processed.setName("Sent");
		processed.setIndex("sent");
		processed.setDisplayName(Resources.get(strings.Processed));

		error.setName("Error");
		error.setIndex("error");
		error.setDisplayName(Resources.get(strings.Error));

		bytesTransferred.setName(names.BytesTransferred);
		bytesTransferred.setIndex("transferred");
		bytesTransferred.setDisplayName(Resources.get(strings.BytesTransferred));

		message.setName("Xml");
		message.setIndex("xml");
		message.setDisplayName(Resources.get(strings.Message));
		message.get().colspan = new integer(3);
		message.get().visible = new bool(false);

		attachment.setIndex("attachment");

		registerDataField(ordinal);
		registerDataField(classId);
		registerDataField(processed);
		registerDataField(error);
		registerDataField(bytesTransferred);
		registerDataField(message);
		registerDataField(attachment);

		registerFormField(createdAt);
		registerFormField(id);
		registerFormField(id1);
		registerFormField(name);
		registerFormField(description);
		registerFormField(ordinal);
		registerFormField(classId);
		registerFormField(processed);
		registerFormField(error);
	}

	private Collection<String> getDomains() {
		return domains.get().getNames();
	}
	
	public Collection<String> getAddresses() {
		Collection<String> result = new ArrayList<String>();

		Field sender = id.get();
		Field address = id1.get();

		Collection<Field> fields = Arrays.<Field> asList(address);
		
		Collection<string> locals = string.wrap(getDomains());
		
		SqlToken out = new IsNot(new InVector(address, locals));
		SqlToken domain = new InVector(sender, locals);
		
		group(fields, fields, new And(domain, out));

		while (next())
			result.add(address.string().get());

		return result;
	}

	public Collection<guid> getExportMessages(String domain) {
		Collection<guid> result = new ArrayList<guid>();

		Field address = id1.get();
		Field processedField = processed.get();

		Collection<Field> fields = Arrays.<Field> asList(recordId.get());
		Collection<Field> orderBy = Arrays.<Field> asList(ordinal.get());

		SqlToken where = new And(new IsNot(processedField), new Equ(address, domain));
		
		read(fields, orderBy, where);

		while (next())
			result.add(recordId());

		return result;
	}

	public List<guid> getExportMessages(String sender, JsonArray filters) {
		return getExportMessages(sender, null, filters);
	}

	public List<guid> getExportMessages(String sender, String address, JsonArray filters) {
		List<guid> result = new LinkedList<guid>();

		Collection<String> locals = getDomains();

		Field senderField = id.get();
		Field addressField = id1.get();

		SqlToken notProcessedNotError = new And(new Unary(Operation.Not, processed.get()), new Unary(Operation.Not, error.get()));
		SqlToken notLocal = new Unary(Operation.Not, new InVector(addressField, string.wrap(locals)));
		SqlToken senderEq = new Equ(senderField, sender);
		SqlToken where = new And(new And(notProcessedNotError, notLocal), senderEq);
		
		if (filters != null && !filters.isEmpty())
			where = new And(where, Query.parseWhere(Filter.parse(filters, this)));
		
		if (address != null) {
			SqlToken addressEq = new Equ(addressField, address);
			where = new And(where, addressEq);
		}

		Collection<Field> fields = Arrays.<Field> asList(recordId.get());
		Collection<Field> orderBy = Arrays.<Field> asList(ordinal.get());

		read(fields, orderBy, where);

		while (next())
			result.add(recordId());

		return result;
	}

	public List<guid> getImportMessages(String selfAddress) {
		SqlToken notProcessedNotError = new And(new Unary(Operation.Not, new SqlField(processed.get())), new Unary(
				Operation.Not, new SqlField(error.get())));
		SqlToken forMe = new Equ(id1.get(), selfAddress);
		read(Arrays.<Field> asList(recordId.get()), Arrays.<Field> asList(ordinal.get()), new And(notProcessedNotError,
				forMe));
		List<guid> ids = new LinkedList<guid>();
		while (next()) {
			ids.add(recordId());
		}
		return ids;
	}

	public boolean readMessage(guid messageId) {
		return readRecord(messageId, getDataFields());
	}

	private long nextOrdinal(Message message, Direction direction) {
		return Sequencer.next(direction.getPrefix() + message.getAddress());
	}

	public Message getMessage(guid id) {
		if (!readRecord(id, getDataFields()))
			return null;
		String xml = this.message.get().get().toString();
		String classId = this.classId.get().get().string().get();
		if (classId == null || classId.isEmpty())
			classId = Message.class.getCanonicalName();
		Message message = (Message) Loader.getInstance(classId);
		message.setId(recordId());
		message.setTime(createdAt.get().datetime());
		message.setAddress(getReceiver());
		message.setSender(getSender());
		message.setBytesTransferred(bytesTransferred.get().integer().get());
		message.setXml(xml);
		return message;
	}

	public static ExportMessages newInstance() {
		return new ExportMessages.CLASS<ExportMessages>().get();
	}

}
