package org.zenframework.z8.server.request.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.AttachmentUtils;
import org.zenframework.z8.server.utils.PdfUtils;
import org.zenframework.z8.server.utils.PrimaryUtils;

public class PreviewAction extends RequestAction {

	private static final String UNSUPPORTED_FILE_FORMAT = "Preview.unsupportedFileFormat";

	private static final String PREVIEW = "preview.pdf";

	public PreviewAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		String requestId = getConfig().requestId;
		guid recordId = getRecordId();
		Query query = getQuery();

		String fieldId = getRequestParameter(Json.field);
		Field field = query.findFieldById(fieldId);

		if(field == null)
			throw new RuntimeException("Field '" + fieldId + "' does not exist");

		if(!query.readRecord(recordId, Arrays.<Field>asList(field)))
			throw new RuntimeException("Record '" + recordId + "' does not exist");

		Collection<file> attachments = file.parse(field.string().get());

		if(attachments.size() == 0)
			throw new RuntimeException("'" + getConfig().requestId + "." + fieldId + "' is empty");

		List<File> converted = new ArrayList<File>(attachments.size());
		List<String> unsupported = new ArrayList<String>(attachments.size());
		String previewRelativePath = null;
		for(file file : attachments) {
			if(previewRelativePath == null)
				previewRelativePath = getPreviewPath(file, requestId, recordId, field);

			File preview = AttachmentUtils.getPreview(file, PrimaryUtils.unwrapStringMap(getParameters()));

			if (preview != null)
				converted.add(preview);
			else
				unsupported.add(file.name.get());
		}
		StringBuilder comment = new StringBuilder();
		for (String fileName : unsupported)
			comment.append(Resources.format(UNSUPPORTED_FILE_FORMAT, fileName)).append(' ');
		File preview = new File(Folders.Base, previewRelativePath);
		preview.getParentFile().mkdirs();
		PdfUtils.merge(converted, preview, comment.toString());
		writer.writeProperty(Json.source, previewRelativePath);
		writer.writeProperty(Json.server, ApplicationServer.id);
	}

	private static String getPreviewPath(file file, String requestId, guid recordId, Field field) {
		try {
			return new File(file.path.get()).getParentFile().getParent() + '/' + PREVIEW;
		} catch(Throwable e) {
			return new StringBuilder().append(Files.Storage).append(requestId).append('/').append(recordId).append('/').append(field.name()).append('/').append(PREVIEW).toString();
		}
	}

}
