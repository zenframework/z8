package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.ExternalOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ArrayUtils;
import org.zenframework.z8.server.utils.EmlUtils;
import org.zenframework.z8.server.utils.PdfUtils;
import org.zenframework.z8.server.utils.PrimaryUtils;

public class FileConverter {

	private static final int OFFICE_PORT = 8100;
	
	public static final String PDF_EXTENSION = "pdf";

	public static final string Background = new string("background");

	private static OfficeManager officeManager;

	private FileConverter() {}

	public static file z8_convertToPdf(file source, file target) {
		return new file(convertToPdf(source.toFile(), target.toFile()));
	}

	public static File convertToPdf(File source, File target) {
		return convertToPdf(source, target, Collections.emptyMap());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static file z8_convertToPdf(file source, file target, RLinkedHashMap parameters) {
		return new file(convertToPdf(source.toFile(), target.toFile(), PrimaryUtils.unwrapStringMap(parameters)));
	}

	public static File convertToPdf(File source, File target, Map<String, String> parameters) {
		String extension = getExtension(source);

		if (!target.exists()) {
			target.getParentFile().mkdirs();
			try {
				if (isTextExtension(extension))
					PdfUtils.textToPdf(source, target);
				else if (isImageExtension(extension))
					PdfUtils.imageToPdf(source, target);
				else if (isEmailExtension(extension))
					PdfUtils.textToPdf(EmlUtils.emailToString(source), target);
				else if (isOfficeExtension(extension))
					convertOfficeToPdf(source, target);
			} catch (IOException e) {
				Trace.logError("Can't convert " + source + " to " + target, e);
			}
		}

		String background = parameters.get(Background.get());
		File backgroundFile;
		if (background != null && (backgroundFile = new File(Folders.Base, background)).exists()) {
			source = target;
			target = new File(source.getParentFile(), addSuffix(source.getName(), "-background"));
			if (!target.exists()) {
				extension = FilenameUtils.getExtension(background).toLowerCase();
				try {
					if (isPdfExtension(extension))
						PdfUtils.insertBackgroundPdf(source, target, backgroundFile, false);
					else
						PdfUtils.insertBackgroundImg(source, target, backgroundFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return target;
	}

	public static string z8_getExtension(file file) {
		return new string(getExtension(file.baseName()));
	}

	public static String getExtension(File file) {
		return FilenameUtils.getExtension(file.getName()).toLowerCase();
	}

	public static string z8_getExtension(string fileName) {
		return new string(getExtension(fileName.get()));
	}

	public static String getExtension(String fileName) {
		return FilenameUtils.getExtension(fileName).toLowerCase();
	}

	public static bool z8_isConvertableToPdf(string extension) {
		return new bool(isConvertableToPdf(extension.get()));
	}

	public static boolean isConvertableToPdf(String extension) {
		return isPdfExtension(extension) || isTextExtension(extension)
				|| isImageExtension(extension) || isOfficeExtension(extension)
				|| isEmailExtension(extension);
	}

	public static bool z8_isPdfExtension(string extension) {
		return new bool(isPdfExtension(extension.get()));
	}

	public static boolean isPdfExtension(String extension) {
		return PDF_EXTENSION.equals(extension);
	}

	public static bool z8_isTextExtension(string extension) {
		return new bool(isTextExtension(extension.get()));
	}

	public static boolean isTextExtension(String extension) {
		return ArrayUtils.contains(ServerConfig.textExtensions(), extension.toLowerCase());
	}

	public static bool z8_isImageExtension(string extension) {
		return new bool(isImageExtension(extension.get()));
	}

	public static boolean isImageExtension(String extension) {
		return ArrayUtils.contains(ServerConfig.imageExtensions(), extension.toLowerCase());
	}

	public static bool z8_isEmailExtension(string extension) {
		return new bool(isEmailExtension(extension.get()));
	}

	public static boolean isEmailExtension(String extension) {
		return ArrayUtils.contains(ServerConfig.emailExtensions(), extension.toLowerCase());
	}

	public static bool z8_isOfficeExtension(string extension) {
		return new bool(isOfficeExtension(extension.get()));
	}

	public static boolean isOfficeExtension(String extension) {
		return ArrayUtils.contains(ServerConfig.officeExtensions(), extension.toLowerCase());
	}

	public static void startOfficeManager() {
		if (officeManager != null)
			return;

		try {
			// Try to connect to an existing instance of openoffice
			ExternalOfficeManagerConfiguration externalProcessOfficeManager = new ExternalOfficeManagerConfiguration();
			externalProcessOfficeManager.setConnectOnStart(true);
			externalProcessOfficeManager.setPortNumber(OFFICE_PORT);
			officeManager = externalProcessOfficeManager.buildOfficeManager();
			officeManager.start();
			Trace.logEvent("Connected to an existing OpenOffice process, port " + OFFICE_PORT);
		} catch (OfficeException e) {
			String officeHome = ServerConfig.officeHome();

			try {
				Trace.logEvent("Can't connect to an existing OpenOffice process: " + e.getMessage());
				//Start a new openoffice instance
				officeManager = new DefaultOfficeManagerConfiguration()
						.setOfficeHome(officeHome).setPortNumber(OFFICE_PORT)
						.buildOfficeManager();
				officeManager.start();
				Trace.logEvent("New OpenOffice '" + officeHome + "' process created, port "
						+ OFFICE_PORT);
			} catch (Throwable e1) {
				Trace.logError("Could not start OpenOffice '" + officeHome + "'", e1);
				officeManager = null;
			}
		}
	}

	public static void stopOfficeManager() {
		if (officeManager == null)
			return;
		officeManager.stop();
		officeManager = null;
	}

	private static void convertOfficeToPdf(File sourceFile, File convertedFile) throws IOException {
		try {
			getOfficeDocumentConverter().convert(sourceFile, convertedFile);
		} catch (NullPointerException e) {
			throw new IOException("OpenOffice process is not started", e);
		}
	}

	private static OfficeDocumentConverter getOfficeDocumentConverter() {
		startOfficeManager();
		return new OfficeDocumentConverter(officeManager);
	}

	private static String addSuffix(String filename, String suffix) {
		if (filename == null)
			return null;
		int extIndex = FilenameUtils.indexOfExtension(filename);
		if (extIndex < 0)
			return filename + suffix;
		return filename.substring(0, extIndex) + suffix + filename.substring(extIndex);
	}
}
