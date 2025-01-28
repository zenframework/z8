package org.zenframework.z8.server.request.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class ArchiveAction extends RequestAction {

	private static final String ARCHIVE_NAME = "archive";

	public ArchiveAction(ActionConfig config) {
		super(config);
	}

	@Override
	public void writeResponse(JsonWriter writer) throws Throwable {
		JsonArray filesJson = new JsonArray(getRequestParameter(new string("archive")));
		List<File> filesToArchive = new ArrayList<>();
		
		for (int i = 0; i < filesJson.length(); i++) {
			JsonObject fileJson = (JsonObject) filesJson.get(i);
			guid fileId = fileJson.getGuid("id");
			file currentFile = Files.get(fileId);
			currentFile = Files.get(currentFile);
			File file = currentFile.toFile();
			if (file.exists() && file.isFile()) {
				filesToArchive.add(file);
			} else {
				throw new RuntimeException("Файл не найден: " + file.getAbsolutePath());
			}
		}
		File archive = createZipArchive(filesToArchive);

		String archiveRelativePath = file.getRelativePath(archive);
		writer.writeProperty(Json.source, archiveRelativePath);
		writer.writeProperty(Json.server, ApplicationServer.id);
	}
	
	private File createZipArchive(List<File> files) throws Exception {
		file tempFile = file.createTempFile(ARCHIVE_NAME, "zip");
		File archive = tempFile.toFile();
		
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archive))) {
			for (File file : files) {
				try (FileInputStream  fis = new FileInputStream(file)) {
					ZipEntry entry = new ZipEntry(file.getName());
					zos.putNextEntry(entry);

					byte[] buffer = new byte[1024];
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
				}
			}
		}

		return archive;
	}
}