package org.zenframework.z8.server.base.model.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.Files;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Z8Context;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.PdfUtils;

public class PreviewAction extends Action {

	private static final String PREVIEW = "preview.pdf";

	public PreviewAction(ActionParameters parameters) {
		super(parameters);
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		// TODO Create record preview based on template

		String requestId = actionParameters().requestId;
		guid recordId = getRecordIdParameter();
		Query query = getQuery();
		String fieldId = getFieldParameter();
		AttachmentField field = (AttachmentField) query.getFieldById(fieldId);

		if (!query.readRecord(recordId, Arrays.<Field> asList(field)))
			throw new RuntimeException("Record '" + recordId + "' does not exist in '" + query.getIndex() + "'");

		Collection<FileInfo> attachments = field.getAttachmentProcessor().get();

		if (attachments.size() == 0)
			throw new RuntimeException("'" + actionParameters().requestId + "." + fieldId + "' is empty");
		
		List<File> converted = new ArrayList<File>(attachments.size());
		Files files = Files.newInstance();
		String previewRelativePath = null;
		for (FileInfo fileInfo : attachments) {
			if (previewRelativePath == null)
				previewRelativePath = getPreviewPath(fileInfo, requestId, recordId, field);
			File preview = files.getPreview(fileInfo);
			if (preview != null)
				converted.add(preview);
		}
		File preview = new File(Z8Context.getWorkingPath(), previewRelativePath);
		PdfUtils.merge(converted, preview);
		writer.writeProperty(Json.source, previewRelativePath);
		writer.writeProperty(Json.serverId, ApplicationServer.Id());
	}

	private static String getPreviewPath(FileInfo file, String requestId, guid recordId, Field field) {
		try {
			return new File(file.path.get()).getParentFile().getParent() + '/' + PREVIEW;
		} catch (Throwable e) {
			return new StringBuilder().append(Folders.Storage).append('/').append(requestId).append('/').append(recordId)
					.append('/').append(field.name()).append('/').append(PREVIEW).toString();
		}
	}

}
