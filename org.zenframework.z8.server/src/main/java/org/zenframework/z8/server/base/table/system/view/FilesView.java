package org.zenframework.z8.server.base.table.system.view;

import java.io.File;
import java.util.Arrays;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.SystemFiles;
import org.zenframework.z8.server.base.table.value.AttachmentExpression;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidExpression;
import org.zenframework.z8.server.base.table.value.StringExpression;
import org.zenframework.z8.server.base.view.command.Command;
import org.zenframework.z8.server.base.view.command.Parameter;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class FilesView extends SystemFiles {
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

	public static class CLASS<T extends FilesView> extends SystemFiles.CLASS<T> {
		public CLASS() {
			this(null);
		}

		public CLASS(IObject container) {
			super(container);
			setJavaClass(FilesView.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new FilesView(container);
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

	private static class FileAttachmentExpression extends AttachmentExpression {
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
			return names.File;
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

	public FilesView(IObject container) {
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
}
