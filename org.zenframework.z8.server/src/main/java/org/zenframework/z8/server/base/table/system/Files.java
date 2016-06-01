package org.zenframework.z8.server.base.table.system;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.FileInfoNotFoundException;
import org.zenframework.z8.server.base.file.FilesFactory;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.file.InputOnlyFileItem;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.AttachmentExpression;
import org.zenframework.z8.server.base.table.value.BinaryField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidExpression;
import org.zenframework.z8.server.base.table.value.StringExpression;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.view.command.Command;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.db.sql.expressions.Or;
import org.zenframework.z8.server.db.sql.expressions.Rel;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.engine.Z8Context;
import org.zenframework.z8.server.ie.Export;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.ie.Transport;
import org.zenframework.z8.server.ie.TransportContext;
import org.zenframework.z8.server.ie.TransportEngine;
import org.zenframework.z8.server.ie.TransportException;
import org.zenframework.z8.server.ie.TransportRoute;
import org.zenframework.z8.server.ie.TransportRoutes;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.types.sql.sql_string;
import org.zenframework.z8.server.utils.IOUtils;

public class Files extends Table {

	private static final Log LOG = LogFactory.getLog(Files.class);

	private static final String TABLE_PREFIX = "table";

	public static final String TableName = "SystemFiles";

	static public class names {
		public final static String File = "File";
		public final static String Path = "Path";
		public final static String Status = "Status";
		public final static String Requested = "Requested";
	}

	static public class strings {
		public final static String Title = "Files.title";
		public final static String Name = "Files.name";
		public final static String Path = "Files.path";
		public final static String InstanceId = "Files.instanceId";
		public final static String Status = "Files.status";
		public final static String Requested = "Files.requested";
		public final static String Attachment = "Files.attachment";
	}

	public static class CLASS<T extends Files> extends Table.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(Files.class);
			setName(TableName);
			setDisplayName(Resources.get(strings.Title));
		}

		@Override
		public Object newObject(IObject container) {
			return new Files(container);
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

		@SuppressWarnings("unchecked")
		@Override
		public guid z8_get() {
			Files container = ((Files.CLASS<Files>) getContainer().getCLASS()).get();
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

		@SuppressWarnings("unchecked")
		@Override
		public string z8_get() {
			Files container = ((Files.CLASS<Files>) getContainer().getCLASS()).get();
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

		@SuppressWarnings("unchecked")
		@Override
		protected String attachmentName() {
			Files container = ((Files.CLASS<Files>) getContainer().getCLASS()).get();
			File path = new File(Folders.Base, container.path.get().get().string().get());
			return container.getStatus() == FileInfo.Status.LOCAL || path.exists() ? container.name.get().get().string()
					.get() : null;
		}

		@Override
		protected String contentFieldName() {
			return "File";
		}

	}

	private static final string CommandRequestFile = new string("0B953539-268F-4361-9459-7760A19A63A3");
	private static final string CommandSendFile = new string("5AC63980-254B-44BB-BC72-407520008D25");

	@SuppressWarnings("unchecked")
	private static final RCollection<Parameter.CLASS<? extends Parameter>> SendFileParameters = new RCollection<Parameter.CLASS<? extends Parameter>>(
			Arrays.<Parameter.CLASS<? extends Parameter>> asList(Parameter.z8_create(
					new string(Resources.get("Files.command.sendFile.addresses")), new string("??"))));

	public final StringField.CLASS<StringField> path = new StringField.CLASS<StringField>(this);
	public final BinaryField.CLASS<BinaryField> file = new BinaryField.CLASS<BinaryField>(this);
	public final RecordIdExpression.CLASS<RecordIdExpression> recordIdExp = new RecordIdExpression.CLASS<RecordIdExpression>(
			this);
	public final FileAttachmentExpression.CLASS<FileAttachmentExpression> attachment = new FileAttachmentExpression.CLASS<FileAttachmentExpression>(
			this);

	public Files(IObject container) {
		super(container);
	}

	@Override
	public void constructor2() {
		super.constructor2();

		recordIdExp.setIndex("recordIdExp");
		recordIdExp.setDisplayName("RecordId");

		name.setDisplayName(Resources.get(strings.Name));
		name.get().length.set(512);

		id.setDisplayName(Resources.get(strings.InstanceId));

		id1.setDisplayName(Resources.get(strings.Status));

		file.setName(names.File);
		file.setIndex("file");

		path.setName(names.Path);
		path.setIndex("path");
		path.setDisplayName(Resources.get(strings.Path));
		path.get().length.set(512);

		attachment.setIndex("attachment");
		attachment.setDisplayName(Resources.get(strings.Attachment));

		registerDataField(file);
		registerDataField(path);
		registerDataField(recordIdExp);
		registerDataField(attachment);

		unregisterDataField(archive);

		registerFormField(recordIdExp);
		registerFormField(createdAt);
		registerFormField(id);
		registerFormField(id1);
		registerFormField(name);
		registerFormField(description);
		registerFormField(path);
		registerFormField(attachment);

		commands.add(Command.z8_create(CommandRequestFile, new string(Resources.get("Files.command.requestFile"))));
		commands.add(Command.z8_create(CommandSendFile, new string(Resources.get("Files.command.sendFile")),
				SendFileParameters));
	}

	public static Files instance() {
		return new Files.CLASS<Files>().get();
	}

	public FileInfo getFile(FileInfo fileInfo) throws IOException, FileInfoNotFoundException {
		File path = new File(Folders.Base, fileInfo.path.get());
		if (path.exists()) {
			fileInfo.name = new string(path.getName());
			fillFileInfoFile(fileInfo, path);
		} else if (fileInfo.path.get().startsWith(TABLE_PREFIX)) {
			fillFileInfoFromTable(fileInfo, path);
		} else {
			if (!fillFileInfoFromStorage(fileInfo, path))
				fillFileInfoFromRemote(fileInfo, path);
		}
		return fileInfo;
	}

	public void addFile(FileInfo fileInfo) {
		boolean exists = readFirst(Arrays.<Field> asList(id1.get()), fileInfo, false);
		if (!exists) {
			name.get().set(fileInfo.name);
			if (fileInfo.instanceId != null)
				id.get().set(fileInfo.instanceId);
			else if (fileInfo.file != null)
				id.get().set(Z8Context.getInstanceId());
			if (fileInfo.file != null)
				file.get().set(fileInfo.getInputStream());
			id1.get().set(new string((fileInfo.file == null ? FileInfo.Status.REMOTE : FileInfo.Status.LOCAL).getValue()));
			path.get().set(fileInfo.path);
			if (fileInfo.id == null || fileInfo.id.isNull())
				fileInfo.id = guid.create();
			create(fileInfo.id);
		} else if (getStatus() != FileInfo.Status.LOCAL && fileInfo.file != null) {
			id1.get().set(new string(FileInfo.Status.LOCAL.getValue()));
			file.get().set(fileInfo.getInputStream());
			fileInfo.id = recordId();
			update(fileInfo.id);
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void z8_onCommand(Query.CLASS<? extends Query> query, Command.CLASS<? extends Command> command,
			RCollection recordIds) {
		if (command.get().id.equals(CommandSendFile)) {
			read(Arrays.<Field> asList(id.get(), name.get(), path.get()), new InVector(recordId.get(), recordIds));
			while (next()) {
				FileInfo fileInfo = new FileInfo(recordId(), name.get().get().string().get(), id.get().get().string().get(),
						path.get().get().string().get());
				sendFile(fileInfo, command.get().parameters.get(0).get().z8_string().get());
			}
		}
	}

	public static void sendFile(FileInfo fileInfo, String address) {
		Export export = new Export.CLASS<Export>().get();
		export.setSendFilesSeparately(false);
		export.setSendFilesContent(true);
		export.setExportUrl(Export.getExportUrl(Export.REMOTE_PROTOCOL, address));
		export.properties.put(Message.PROP_TYPE, Message.TYPE_FILE_CONTENT);
		export.addFile(fileInfo);
		export.execute();
	}

	private boolean fillFileInfoFromStorage(FileInfo fileInfo, File path) throws IOException {
		if (readFirst(Arrays.asList(id.get(), id1.get(), name.get(), file.get()), fileInfo, false)) {
			fileInfo.id = recordId();
			fileInfo.name = name.get().get().string();
			string instanceId = id.get().get().string();
			fileInfo.status = getStatus();
			if (!instanceId.isEmpty())
				fileInfo.instanceId = instanceId;
			if (fileInfo.status == FileInfo.Status.LOCAL) {
				IOUtils.copy(file.get().get().binary().get(), path);
				fillFileInfoFile(fileInfo, path);
				return true;
			}
		}
		return false;
	}

	private boolean readFirst(Collection<Field> fields, FileInfo fileInfo, boolean localOnly) {
		SqlToken where = (fileInfo.id != null && !fileInfo.id.isNull()) ? new Rel(recordId.get(), Operation.Eq,
				fileInfo.id.sql_guid()) : new Rel(path.get(), Operation.Eq, fileInfo.path.sql_string());
		if (localOnly)
			where = new And(where, new Or(id1.get().sql_string().isEmpty(), new Rel(id1.get(), Operation.Eq, new sql_string(
					FileInfo.Status.LOCAL.getValue()))));
		return readFirst(fields, where);
	}

	private FileInfo.Status getStatus() {
		return FileInfo.Status.getStatus(id1.get().get().string().get());
	}

	private static boolean fillFileInfoFromTable(FileInfo fileInfo, File path) throws IOException {
		fileInfo.name = new string(path.getName());
		InputStream inputStream = getTableFieldInputStream(fileInfo);
		if (inputStream == null)
			return false;
		IOUtils.copy(inputStream, path);
		fillFileInfoFile(fileInfo, path);
		return true;
	}

	private static InputStream getTableFieldInputStream(FileInfo fileInfo) throws IOException {
		File fileName = new File(fileInfo.path.get());
		File field = fileName.getParentFile();
		File recordId = field.getParentFile();
		File table = recordId.getParentFile();
		Query query = (Query) Loader.getInstance(table.getName());
		Field dataField = query.getFieldByName(field.getName());
		if (query.readRecord(new guid(recordId.getName()), Arrays.asList(dataField))) {
			if (dataField instanceof BinaryField) {
				return ((BinaryField) dataField).get().binary().get();
			} else {
				return new ByteArrayInputStream(dataField.get().toString().getBytes());
			}
		} else {
			throw new IOException("Incorrect path '" + fileInfo.path + "'");
		}
	}

	private void fillFileInfoFromRemote(FileInfo fileInfo, File path) throws IOException, FileInfoNotFoundException {
		if (fileInfo.instanceId == null || fileInfo.instanceId.isEmpty()
				|| fileInfo.instanceId.get().equals(Z8Context.getInstanceId()))
			throw new FileInfoNotFoundException(fileInfo, false);

		TransportRoutes transportRoutes = TransportRoutes.instance();
		TransportEngine engine = TransportEngine.getInstance();
		TransportContext context = new TransportContext.CLASS<TransportContext>().get();
		List<TransportRoute> routes = transportRoutes.readActiveRoutes(fileInfo.instanceId.get(),
				Properties.getProperty(ServerRuntime.TransportCenterAddressProperty).trim());

		// Try to get file synchronously
		for (TransportRoute route : routes) {
			Transport transport = engine.getTransport(context, route.getProtocol());
			if (transport == null || !transport.isSynchronousRequestSupported())
				continue;
			try {
				transport.connect();
				FileInfo fi = transport.readFileSynchronously(fileInfo, route.getAddress());
				IOUtils.copy(fi.getInputStream(), path);
				fillFileInfoFile(fileInfo, path);
				return;
			} catch (TransportException e) {
				LOG.info("Can't get remote file '" + fileInfo + "' from '" + route.getTransportUrl() + "'", e);
				transport.close();
				transportRoutes.disableRoute(route.getRouteId(), e.getMessage());
				continue;
			}
		}

		if (fileInfo.status != FileInfo.Status.REQUEST_SENT) {
			// Send asynchronous request
			Export export = new Export.CLASS<Export>().get();
			export.setExportUrl(Export.getExportUrl(Export.REMOTE_PROTOCOL, fileInfo.instanceId.get()));
			export.properties.put(Message.PROP_TYPE, Message.TYPE_FILE_REQUEST);
			export.properties.put(Message.PROP_RECORD_ID, fileInfo.id);
			export.properties.put(Message.PROP_FILE_PATH, fileInfo.path);
			export.execute();
			id1.get().set(new string(FileInfo.Status.REQUEST_SENT.getValue()));
			if (!guid.NULL.equals(fileInfo.id)) {
				update(fileInfo.id);
			} else {
				name.get().set(fileInfo.name);
				id.get().set(fileInfo.instanceId);
				this.path.get().set(fileInfo.path);
				fileInfo.id = create();
			}
		}

		throw new FileInfoNotFoundException(fileInfo, true);
	}

	private static void fillFileInfoFile(FileInfo fileInfo, File path) throws IOException {
		if (FileInfo.isDefaultWrite()) {
			fileInfo.file = FilesFactory.createFileItem(fileInfo.name.get());
			IOUtils.copy(new FileInputStream(path), fileInfo.getOutputStream());
		} else {
			fileInfo.file = new InputOnlyFileItem(path, fileInfo.name.get());
		}
	}

}
