package org.zenframework.z8.server.converter;

import java.io.File;
import java.io.IOException;
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
import org.zenframework.z8.server.utils.ArrayUtils;
import org.zenframework.z8.server.utils.EmlUtils;
import org.zenframework.z8.server.utils.PdfUtils;

public class FileConverter {

	private static final int OFFICE_PORT = 8100;
	
	public static final String PDF_EXTENSION = "pdf";

	public static final String PARAM_BACKGROUND = "background";

	private static OfficeManager officeManager;

	private final File storage;

	public FileConverter(File storage) {
		this.storage = storage;
	}

	public File getConvertedPdf(String relativePath, File srcFile, Map<String, String> parameters) {
		String extension = FilenameUtils.getExtension(srcFile.getName()).toLowerCase();
		if (isPdfExtension(extension))
			return srcFile;

		File convertedFile = new File(storage, relativePath + '.' + PDF_EXTENSION);

		if (!convertedFile.exists()) {
			convertedFile.getParentFile().mkdirs();
			try {
				if (isTextExtension(extension))
					PdfUtils.textToPdf(srcFile, convertedFile);
				else if (isImageExtension(extension))
					PdfUtils.imageToPdf(srcFile, convertedFile);
				else if (isEmailExtension(extension))
					PdfUtils.textToPdf(EmlUtils.emailToString(srcFile), convertedFile);
				else if (isOfficeExtension(extension))
					convertOfficeToPdf(srcFile, convertedFile);
			} catch (IOException e) {
				Trace.logError("Can't convert " + srcFile + " to " + convertedFile, e);
			}
		}

		String background = parameters.get(PARAM_BACKGROUND);
		if (background != null) {
			srcFile = convertedFile;
			convertedFile = new File(srcFile.getParentFile(), addSuffix(srcFile.getName(), "-background"));
			if (!convertedFile.exists()) {
				File backgroundFile = new File(Folders.Base, background);
				extension = FilenameUtils.getExtension(background).toLowerCase();
				try {
					if (isPdfExtension(extension))
						PdfUtils.insertBackgroundPdf(srcFile, convertedFile, backgroundFile, false);
					else
						PdfUtils.insertBackgroundImg(srcFile, convertedFile, backgroundFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		return convertedFile;
	}

	public static boolean isConvertableToPdf(File file) {
		return isConvertableToPdf(file.getName());
	}

	public static boolean isConvertableToPdf(String fileName) {
		String extension = FilenameUtils.getExtension(fileName).toLowerCase();
		return PDF_EXTENSION.equals(extension) || isTextExtension(extension)
				|| isImageExtension(extension) || isOfficeExtension(extension)
				|| isEmailExtension(extension);
	}

	private static void startOfficeManager() {
		if (officeManager == null) {
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
	}

	public static void stopOfficeManager() {
		if (officeManager != null) {
			officeManager.stop();
			officeManager = null;
		}
	}

	private void convertOfficeToPdf(File sourceFile, File convertedFile) throws IOException {
		try {
			getOfficeDocumentConverter().convert(sourceFile, convertedFile);
		} catch (NullPointerException e) {
			throw new IOException("OpenOffice process is not started", e);
		}
	}

	private OfficeDocumentConverter getOfficeDocumentConverter() {
		startOfficeManager();
		return new OfficeDocumentConverter(officeManager);
	}

	private static boolean isPdfExtension(String extension) {
		return PDF_EXTENSION.equals(extension);
	}

	private static boolean isTextExtension(String extension) {
		return ArrayUtils.contains(ServerConfig.textExtensions(), extension.toLowerCase());
	}

	private static boolean isImageExtension(String extension) {
		return ArrayUtils.contains(ServerConfig.imageExtensions(), extension.toLowerCase());
	}

	private static boolean isEmailExtension(String extension) {
		return ArrayUtils.contains(ServerConfig.emailExtensions(), extension.toLowerCase());
	}

	private static boolean isOfficeExtension(String extension) {
		return ArrayUtils.contains(ServerConfig.officeExtensions(), extension.toLowerCase());
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
