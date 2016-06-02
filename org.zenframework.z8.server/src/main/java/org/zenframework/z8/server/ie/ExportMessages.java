package org.zenframework.z8.server.ie;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.base.table.value.AttachmentExpression;
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
	public MessageAttachmentExpression.CLASS<MessageAttachmentExpression> attachment = new MessageAttachmentExpression.CLASS<MessageAttachmentExpression>(
			this);

	private ExportMessages(IObject container) {
		super(container);
	}

	public void addMessage(Message message, String transportInfo, Direction direction) throws JAXBException {
		guid recordId = new guid(message.getId());
		this.id.get().set(new string(message.getSender()));
		this.id1.get().set(new string(message.getAddress()));
		if (Export.LOCAL_PROTOCOL.equals(message.getExportProtocol()))
			this.name.get().set(new string(Export.LOCAL_PROTOCOL));
		else if (transportInfo != null)
			this.name.get().set(new string(transportInfo));
		this.ordinal.get().set(new integer(nextOrdinal(message, direction)));
		this.message.get().set(new string(IeUtil.marshalExportEntry(message.getExportEntry())));

		if (hasRecord(recordId))
			update(recordId);
		else
			create(recordId);
	}

	static public void processed(guid id, String transportUrl, boolean preserve) {
		ExportMessages messages = new ExportMessages.CLASS<ExportMessages>().get();
		
		if (preserve) {
			if (transportUrl != null)
				messages.name.get().set(transportUrl);
			messages.processed.get().set(new bool(true));
			messages.update(id);
		} else {
			messages.destroy(id);
		}
	}

	static public void setError(Message message, Throwable e) {
		ExportMessages messages = new ExportMessages.CLASS<ExportMessages>().get();

		messages.error.get().set(new bool(true));
		messages.description.get().set(new string(new datetime() + " " + e.getClass() + ": " + e.getMessage()));
		messages.update(new guid(message.getId()));
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
		attachment.setIndex("attachment");

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
	}

	public List<guid> getExportMessages(String selfAddress) {
		SqlToken notProcessedNotErrorNotLocal = new And(new And(new Unary(Operation.Not, new SqlField(processed.get())),
				new Unary(Operation.Not, new SqlField(error.get()))), new Rel(name.get(), Operation.NotEq, new sql_string(
				Export.LOCAL_PROTOCOL)));
		SqlToken fromMeNotForMe = new And(new Rel(id.get(), Operation.Eq, new sql_string(selfAddress)), new Rel(id1.get(),
				Operation.NotEq, new sql_string(selfAddress)));
		read(Arrays.<Field> asList(recordId.get()), Arrays.<Field> asList(ordinal.get()), new And(
				notProcessedNotErrorNotLocal, fromMeNotForMe));
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

	private long nextOrdinal(Message message, Direction direction) {
		return Sequencer.next(direction.getPrefix() + message.getAddress());
	}
	
	public Message getMessage(guid id, Message message) {
		if(!readRecord(id, getDataFields()))
			return null;

		message.setId(recordId().get());
		message.setAddress(getReceiver());
		message.setSender(getSender());
		message.setExportEntry(IeUtil.unmarshalExportEntry(this.message.get().get().toString()));
		return message;
	}
}
