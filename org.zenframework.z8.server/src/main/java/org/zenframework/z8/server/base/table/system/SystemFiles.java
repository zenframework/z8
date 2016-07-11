package org.zenframework.z8.server.base.table.system;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.AttachmentExpression;
import org.zenframework.z8.server.base.table.value.BinaryField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidExpression;
import org.zenframework.z8.server.base.table.value.StringExpression;
import org.zenframework.z8.server.base.view.command.Command;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.db.sql.functions.string.IsEmpty;
import org.zenframework.z8.server.ie.Export;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.ie.Transport;
import org.zenframework.z8.server.ie.TransportContext;
import org.zenframework.z8.server.ie.TransportEngine;
import org.zenframework.z8.server.ie.TransportException;
import org.zenframework.z8.server.ie.TransportRoute;
import org.zenframework.z8.server.ie.TransportRoutes;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ErrorUtils;
import org.zenframework.z8.server.utils.IOUtils;

public class SystemFiles extends Files {

	private static final String TABLE_PREFIX = "table";

	static public class names {
		public final static String Status = "Status";
		public final static String Requested = "Requested";
	}

	static public class strings {
		public final static String InstanceId = "SystemFiles.instanceId";
		public final static String Status = "SystemFiles.status";
		public final static String Request = "SystemFiles.command.requestFile";
		public final static String Send = "SystemFiles.command.sendFile";
		public final static String Attachment = "SystemFiles.attachment";
		public final static String Addresses = "SystemFiles.command.sendFile.addresses";
	}

	static public class displayNames {
		public final static String InstanceId = Resources.get(strings.InstanceId);
		public final static String Status = Resources.get(strings.Status);
		public final static String Request = Resources.get(strings.Request);
		public final static String Send = Resources.get(strings.Send);
		public final static String Attachment = Resources.get(strings.Attachment);
		public final static String Addresses = Resources.get(strings.Addresses);
	}

	public static class CLASS<T extends SystemFiles> extends Files.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(SystemFiles.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new SystemFiles(container);
		}
	}

	public static class RecordIdExpression extends GuidExpression {

		public static class CLASS<T extends GuidExpression> extends GuidExpression.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(RecordIdExpression.class);
			}

			@Override
			public Object newObject(IObject container) {
				return new RecordIdExpression(container);
			}
		}

		public RecordIdExpression(IObject container) {
			super(container);
		}

		@Override
		public guid z8_get() {
			SystemFiles container = (SystemFiles)getContainer();
			return container.recordId();
		}

	}

	public static class StatusExpression extends StringExpression {

		public static class CLASS<T extends StringExpression> extends StringExpression.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(StringExpression.class);
			}

			@Override
			public Object newObject(IObject container) {
				return new StringExpression(container);
			}
		}

		public StatusExpression(IObject container) {
			super(container);
		}

		@Override
		public string z8_get() {
			SystemFiles container = (SystemFiles)getContainer();
			return new string(container.getStatus().getText());
		}

	}

	public static class FileAttachmentExpression extends AttachmentExpression {

		public static class CLASS<T extends FileAttachmentExpression> extends AttachmentExpression.CLASS<T> {
			public CLASS(IObject container) {
				super(container);
				setJavaClass(FileAttachmentExpression.class);
			}

			@Override
			public Object newObject(IObject container) {
				return new FileAttachmentExpression(container);
			}
		}

		public FileAttachmentExpression(IObject container) {
			super(container);
		}

		@Override
		protected String attachmentName() {
			SystemFiles container = (SystemFiles)getContainer();
			File path = new File(Folders.Base, container.path.get().get().string().get());
			return container.getStatus() == file.Status.LOCAL || path.exists() ? container.name.get().get().string().get() : null;
		}

		@Override
		protected String contentFieldName() {
			return "File";
		}

	}

	private static final string CommandRequestFile = new string("0B953539-268F-4361-9459-7760A19A63A3");
	private static final string CommandSendFile = new string("5AC63980-254B-44BB-BC72-407520008D25");

	@SuppressWarnings("unchecked")
	private static final RCollection<Parameter.CLASS<? extends Parameter>> SendFileParameters = new RCollection<Parameter.CLASS<? extends Parameter>>(Arrays.<Parameter.CLASS<? extends Parameter>> asList(Parameter.z8_create(
			new string(displayNames.Addresses), new string("??"))));

	public final RecordIdExpression.CLASS<RecordIdExpression> recordIdExp = new RecordIdExpression.CLASS<RecordIdExpression>(this);
	public final FileAttachmentExpression.CLASS<FileAttachmentExpression> attachment = new FileAttachmentExpression.CLASS<FileAttachmentExpression>(this);

	static public SystemFiles newInstance() {
		return new SystemFiles.CLASS<SystemFiles>().get();
	}

	public SystemFiles(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		recordIdExp.setIndex("recordIdExp");
		recordIdExp.setDisplayName("RecordId");

		id.setDisplayName(displayNames.InstanceId);

		id1.setDisplayName(displayNames.Status);

		attachment.setIndex("attachment");
		attachment.setDisplayName(displayNames.Attachment);

		registerDataField(recordIdExp);
		registerDataField(attachment);

		registerFormField(recordIdExp);
		registerFormField(createdAt);
		registerFormField(id);
		registerFormField(id1);
		registerFormField(name);
		registerFormField(description);
		registerFormField(path);
		registerFormField(attachment);

		commands.add(Command.z8_create(CommandRequestFile, new string(displayNames.Request)));
		commands.add(Command.z8_create(CommandSendFile, new string(displayNames.Send), SendFileParameters));
	}

	public file getFile(file file) throws IOException {
		File path = new File(Folders.Base, file.path.get());
		if(path.exists()) {
			file.name = new string(path.getName());
			fillFileInfoFile(file, path);
		} else if(file.path.get().startsWith(TABLE_PREFIX)) {
			fillFileInfoFromTable(file, path);
		} else {
			if(!fillFromStorage(file, path))
				fillFromRemote(file, path);
		}
		return file;
	}

	public void addFile(file fileInfo) {
		boolean exists = readFirst(Arrays.<Field> asList(id1.get()), fileInfo, false);
		InputStream input = null;
		try {
			if(!exists) {
				name.get().set(fileInfo.name);
				id.get().set(fileInfo.instanceId);
				if(fileInfo.get() != null) {
					input = fileInfo.getInputStream();
					data.get().set(input);
				}
				id1.get().set(new string((fileInfo.get() == null ? file.Status.REMOTE : file.Status.LOCAL).getValue()));
				path.get().set(fileInfo.path);
				if(fileInfo.id == null || fileInfo.id.isNull())
					fileInfo.id = guid.create();
				if(fileInfo.time != null && !fileInfo.time.equals(datetime.MIN))
					createdAt.get().set(fileInfo.time);
				create(fileInfo.id);
			} else if(getStatus() != file.Status.LOCAL && fileInfo.get() != null) {
				id1.get().set(new string(file.Status.LOCAL.getValue()));
				input = fileInfo.getInputStream();
				data.get().set(input);
				fileInfo.id = recordId();
				update(fileInfo.id);
			}
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_onCommand(Query.CLASS<? extends Query> query, Command.CLASS<? extends Command> command, RCollection recordIds) {
		if(command.get().id.equals(CommandSendFile)) {
			read(Arrays.<Field> asList(id.get(), name.get(), path.get()), new InVector(recordId.get(), recordIds));
			while(next()) {
				file fileInfo = new file(recordId(), name.get().string().get(), id.get().string().get(), path.get().string().get());
				sendFile(fileInfo, command.get().parameters.get(0).get().z8_string().get());
			}
		}
	}

	public static void sendFile(file fileInfo, String address) {
		Export export = new Export.CLASS<Export>().get();
		export.setSendFilesSeparately(false);
		export.setSendFilesContent(true);
		export.setAddress(address);
		export.properties.put(Message.PROP_TYPE, Message.TYPE_FILE_CONTENT);
		export.properties.put(Message.PROP_GROUP, fileInfo.path);
		export.addFile(fileInfo);
		export.execute();
	}

	private boolean fillFromStorage(file fileInfo, File path) throws IOException {
		if(readFirst(Arrays.asList(id.get(), id1.get(), name.get(), data.get()), fileInfo, false)) {
			fileInfo.id = recordId();
			fileInfo.name = name.get().get().string();
			string instanceId = id.get().get().string();
			fileInfo.status = getStatus();
			if(!instanceId.isEmpty())
				fileInfo.instanceId = instanceId;
			if(fileInfo.status == file.Status.LOCAL) {
				IOUtils.copy(data.get().get().binary().get(), path);
				fillFileInfoFile(fileInfo, path);
				return true;
			}
		}
		return false;
	}

	private boolean readFirst(Collection<Field> fields, file fileInfo, boolean localOnly) {
		SqlToken where = (fileInfo.id != null && !fileInfo.id.isNull()) ? new Equ(recordId.get(), fileInfo.id) : new Equ(path.get(), fileInfo.path);
	
		if(localOnly)
			where = new And(where, new Or(new IsEmpty(id1.get()), new Equ(id1.get(), file.Status.LOCAL.getValue())));
		
		return readFirst(fields, where);
	}

	private file.Status getStatus() {
		return file.Status.getStatus(id1.get().get().string().get());
	}

	private static boolean fillFileInfoFromTable(file fileInfo, File path) throws IOException {
		fileInfo.name = new string(path.getName());
		InputStream inputStream = getTableFieldInputStream(fileInfo);
		if(inputStream == null)
			return false;
		IOUtils.copy(inputStream, path);
		fillFileInfoFile(fileInfo, path);
		return true;
	}

	private static InputStream getTableFieldInputStream(file fileInfo) throws IOException {
		File fileName = new File(fileInfo.path.get());
		File field = fileName.getParentFile();
		File recordId = field.getParentFile();
		File table = recordId.getParentFile();
		Query query = (Query)Loader.getInstance(table.getName());
		Field dataField = query.getFieldByName(field.getName());
		if(query.readRecord(new guid(recordId.getName()), Arrays.asList(dataField))) {
			if(dataField instanceof BinaryField) {
				return ((BinaryField)dataField).get().binary().get();
			} else {
				return new ByteArrayInputStream(dataField.get().toString().getBytes());
			}
		} else {
			throw new IOException("Incorrect path '" + fileInfo.path + "'");
		}
	}

	private void fillFromRemote(file fileInfo, File path) throws IOException {
		if(fileInfo.instanceId == null || fileInfo.instanceId.isEmpty() || fileInfo.instanceId.get().equals(ServerConfig.instanceId()))
			throw new FileNotFoundException(fileInfo.path.get());

		TransportRoutes transportRoutes = TransportRoutes.newInstance();
		TransportEngine engine = TransportEngine.getInstance();
		TransportContext context = new TransportContext.CLASS<TransportContext>().get();
		List<TransportRoute> routes = transportRoutes.readRoutes(fileInfo.instanceId.get(), true);

		// Try to get file synchronously
		for(TransportRoute route : routes) {
			Transport transport = engine.getTransport(context, route.getProtocol());
			if(transport == null || !transport.isSynchronousRequestSupported())
				continue;
			try {
				transport.connect();
				file fi = transport.readFileSynchronously(fileInfo, route.getAddress());
				IOUtils.copy(fi.getInputStream(), path);
				fillFileInfoFile(fileInfo, path);
				return;
			} catch(TransportException e) {
				Trace.logError("Can't get remote file '" + fileInfo + "' from '" + route.getTransportUrl() + "'", e);
				transport.close();
				transportRoutes.disableRoute(route, ErrorUtils.getMessage(e));
				continue;
			}
		}

		if(fileInfo.status != file.Status.REQUEST_SENT) {
			// Send asynchronous request
			Export export = new Export.CLASS<Export>().get();
			export.setAddress(fileInfo.instanceId.get());
			export.properties.put(Message.PROP_TYPE, Message.TYPE_FILE_REQUEST);
			export.properties.put(Message.PROP_GROUP, fileInfo.path);
			export.properties.put(Message.PROP_RECORD_ID, fileInfo.id);
			export.properties.put(Message.PROP_FILE_PATH, fileInfo.path);
			export.execute();
			id1.get().set(new string(file.Status.REQUEST_SENT.getValue()));
			if(!guid.NULL.equals(fileInfo.id)) {
				update(fileInfo.id);
			} else {
				name.get().set(fileInfo.name);
				id.get().set(fileInfo.instanceId);
				this.path.get().set(fileInfo.path);
				fileInfo.id = create();
			}
			fileInfo.status = file.Status.REQUEST_SENT;
		}
	}

	private static void fillFileInfoFile(file file, File path) throws IOException {
		file.set(new InputOnlyFileItem(path, file.name.get()));
	}
}
