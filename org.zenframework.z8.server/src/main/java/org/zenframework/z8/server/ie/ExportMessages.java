package org.zenframework.z8.server.ie;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.IntegerField;
import org.zenframework.z8.server.base.table.value.Sequencer;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.expressions.Unary;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;

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

	static public class strings {
		public final static String Title = "ExportMessages.title";
		public final static String Sender = "ExportMessages.sender";
		public final static String Receiver = "ExportMessages.receiver";
		public final static String TransportUrl = "ExportMessages.transportUrl";
		public final static String Message = "ExportMessages.message";
		public final static String Ordinal = "ExportMessages.ordinal";
		public final static String Processed = "ExportMessages.processed";
		public final static String Error = "ExportMessages.error";
	}

	public IntegerField.CLASS<IntegerField> ordinal = new IntegerField.CLASS<IntegerField>(this);
	public TextField.CLASS<TextField> message = new TextField.CLASS<TextField>(this);
	public BoolField.CLASS<BoolField> processed = new BoolField.CLASS<BoolField>(this);
	public BoolField.CLASS<BoolField> error = new BoolField.CLASS<BoolField>(this);
	public AttachmentField.CLASS<AttachmentField> attachment = new AttachmentField.CLASS<AttachmentField>(this);

	private ExportMessages(IObject container) {
		super(container);
	}

	public void addMessage(Message message, String transportInfo) throws JAXBException {
		guid recordId = new guid(message.getId());
		boolean exists = hasRecord(recordId);
		this.id.get().set(new string(message.getSender()));
		this.id1.get().set(new string(message.getAddress()));
		if (Export.LOCAL_PROTOCOL.equals(message.getExportProtocol()))
			this.name.get().set(new string(Export.LOCAL_PROTOCOL));
		else if (transportInfo != null)
			this.name.get().set(new string(transportInfo));
		this.ordinal.get().set(new integer(nextOrdinal(message)));
		this.message.get().set(new string(IeUtil.marshalExportEntry(message.getExportEntry())));
		this.attachment.get().set(getAttachment(message.getId()));
		if (exists)
			update(recordId);
		else
			create(recordId);
	}

	public void processCurrentMessage(String transportUrl, boolean preserveExportMessages) {
		if (preserveExportMessages) {
			if (transportUrl != null)
				name.get().set(transportUrl);
			processed.get().set(new bool(true));
			update(recordId());
		} else {
			destroy(recordId());
		}
	}

	public void setError(guid messageId, Throwable e) {
		this.error.get().set(new bool(true));
		this.description.get().set(new string(e.getMessage()));
		update(messageId);
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

	public Message.CLASS<Message> getMessage() throws JAXBException {
		Message.CLASS<Message> message = new Message.CLASS<Message>();
		message.get().setId(recordId().get());
		message.get().setAddress(getReceiver());
		message.get().setSender(getSender());
		message.get().setExportEntry(IeUtil.unmarshalExportEntry(this.message.get().get().toString()));
		return message;
	}

	@Override
	public void constructor2() {
		super.constructor2();
		id.setDisplayName(Resources.get(strings.Sender));
		id.get().length = new integer(50);
		id1.setDisplayName(Resources.get(strings.Receiver));
		id1.get().length = new integer(50);
		createdAt.get().system.set(false);
		name.setDisplayName(Resources.get(strings.TransportUrl));
		name.get().length = new integer(256);
		ordinal.setName("Ordinal");
		ordinal.setIndex("ordinal");
		ordinal.setDisplayName(Resources.get(strings.Ordinal));
		ordinal.get().indexFields.add(id);
		ordinal.get().indexFields.add(id1);
		ordinal.get().unique.set(true);
		ordinal.get().aggregation = Aggregation.Max;
		processed.setName("Sent");
		processed.setIndex("sent");
		processed.setDisplayName(Resources.get(strings.Processed));
		error.setName("Error");
		error.setIndex("error");
		error.setDisplayName(Resources.get(strings.Error));
		message.setName("Xml");
		message.setIndex("xml");
		message.setDisplayName(Resources.get(strings.Message));
		message.get().colspan.set(3);
		message.get().visible = new bool(false);
		attachment.setName("Attachment");
		attachment.setIndex("attachment");
		attachment.get().readOnly = new bool(true);

		registerDataField(ordinal);
		registerDataField(processed);
		registerDataField(error);
		registerDataField(message);
		registerDataField(attachment);

		registerFormField(createdAt);
		registerFormField(id);
		registerFormField(id1);
		registerFormField(name);
		registerFormField(description);
		registerFormField(ordinal);
		registerFormField(processed);
		registerFormField(error);
		registerFormField(attachment);
	}

	public static ExportMessages instance() {
		return new CLASS<ExportMessages>().get();
	}

	public List<guid> getExportMessages(String selfAddress) {
		SqlToken notProcessedNotErrorNotLocal = new And(new And(new Unary(Operation.Not, new SqlField(processed.get())), new Unary(
				Operation.Not, new SqlField(error.get()))), new Rel(name.get(), Operation.NotEq, new sql_string(Export.LOCAL_PROTOCOL)));
		SqlToken fromMeNotForMe = new And(new Rel(id.get(), Operation.Eq, new sql_string(selfAddress)), new Rel(id1.get(),
				Operation.NotEq, new sql_string(selfAddress)));
		read(Arrays.<Field> asList(recordId.get()), Arrays.<Field> asList(ordinal.get()), new And(notProcessedNotErrorNotLocal,
				fromMeNotForMe));
		List<guid> ids = new LinkedList<guid>();
		while (next()) {
			ids.add(recordId());
		}
		return ids;
	}

	public List<guid> getImportMessages(String selfAddress) {
		SqlToken notProcessedNotError = new And(new Unary(Operation.Not, new SqlField(processed.get())), new Unary(
				Operation.Not, new SqlField(error.get())));
		SqlToken forMe = new Rel(id1.get(), Operation.Eq, new sql_string(selfAddress));
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

	private String getAttachment(UUID id) {
		JsonArray writer = new JsonArray();
		JsonObject obj = new JsonObject();
		String fileName = "table/org.zenframework.z8.server.ie.ExportMessages/" + id + "/Xml/message.xml";
		obj.put(Json.size, 0);
		obj.put(Json.time, new datetime());
		obj.put(Json.name, "message.xml");
		obj.put(Json.path, fileName);
		writer.put(obj);
		return writer.toString();
	}

	private long nextOrdinal(Message message) {
		return Sequencer.next(message.getSender() + "->" + message.getAddress(), 1000000L);
	}

}
