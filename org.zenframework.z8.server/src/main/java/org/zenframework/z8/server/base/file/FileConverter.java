package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.ExternalOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.table.system.Property;
import org.zenframework.z8.server.engine.Z8Context;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.EmlUtils;
import org.zenframework.z8.server.utils.PdfUtils;

public class FileConverter {
	private static class ConverterListener implements Properties.Listener {
		@Override
		public void onPropertyChange(String key, String value) {
			if (ServerRuntime.FileConverterTextExtensionsProperty.equalsKey(key))
				textExtensions.clear();
			if (ServerRuntime.FileConverterImageExtensionsProperty.equalsKey(key))
				imageExtensions.clear();
			if (ServerRuntime.FileConverterEmailExtensionsProperty.equalsKey(key))
				emailExtensions.clear();
			if (ServerRuntime.FileConverterOfficeExtensionsProperty.equalsKey(key))
				officeExtensions.clear();
		}

	}

	static {
		Properties.addListener(new ConverterListener());
	}

	private static final int OFFICE_PORT = 8100;

	private static final List<String> textExtensions = Collections.synchronizedList(new LinkedList<String>());
	private static final List<String> imageExtensions = Collections.synchronizedList(new LinkedList<String>());
	private static final List<String> emailExtensions = Collections.synchronizedList(new LinkedList<String>());
	private static final List<String> officeExtensions = Collections.synchronizedList(new LinkedList<String>());

	public static final String PDF_EXTENSION = "pdf";

	private static OfficeManager officeManager;

	private final File storage;

	public FileConverter(File storage) {
		this.storage = storage;
	}

	public File getConvertedPdf(String relativePath, File srcFile) {
		if (srcFile.getName().endsWith('.' + PDF_EXTENSION))
			return srcFile;

		File convertedFile = new File(storage, relativePath + '.' + PDF_EXTENSION);

		if (!convertedFile.exists()) {
			convertedFile.getParentFile().mkdirs();
			String extension = FilenameUtils.getExtension(srcFile.getName()).toLowerCase();
			try {
				if (getTextExtensions().contains(extension))
					PdfUtils.textToPdf(srcFile, convertedFile);
				else if (getImageExtensions().contains(extension))
					PdfUtils.imageToPdf(srcFile, convertedFile);
				else if (getEmailExtensions().contains(extension))
					PdfUtils.textToPdf(EmlUtils.emailToString(srcFile), convertedFile);
				else if (getOfficeExtensions().contains(extension))
					convertOfficeToPdf(srcFile, convertedFile);
			} catch (IOException e) {
				Trace.logError("Can't convert " + srcFile + " to " + convertedFile, e);
			}
		}

		return convertedFile;
	}

	public static boolean isConvertableToPdf(File file) {
		return isConvertableToPdf(file.getName());
	}

	public static boolean isConvertableToPdf(String fileName) {
		String extension = FilenameUtils.getExtension(fileName).toLowerCase();
		return PDF_EXTENSION.equals(extension) || getTextExtensions().contains(extension)
				|| getImageExtensions().contains(extension) || getOfficeExtensions().contains(extension)
				|| getEmailExtensions().contains(extension);
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
				try {
					Trace.logEvent("Can't connect to an existing OpenOffice process: " + e.getMessage());
					//Start a new openoffice instance
					officeManager = new DefaultOfficeManagerConfiguration()
							.setOfficeHome(Z8Context.getConfig().getOfficeHome()).setPortNumber(OFFICE_PORT)
							.buildOfficeManager();
					officeManager.start();
					Trace.logEvent("New OpenOffice '" + Z8Context.getConfig().getOfficeHome() + "' process created, port "
							+ OFFICE_PORT);
				} catch (Throwable e1) {
					Trace.logError("Could not start OpenOffice '" + Z8Context.getConfig().getOfficeHome() + "'", e1);
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

	private static List<String> getTextExtensions() {
		if (textExtensions.isEmpty())
			textExtensions.addAll(getExtensions(ServerRuntime.FileConverterTextExtensionsProperty));
		return textExtensions;
	}

	private static List<String> getImageExtensions() {
		if (imageExtensions.isEmpty())
			imageExtensions.addAll(getExtensions(ServerRuntime.FileConverterImageExtensionsProperty));
		return imageExtensions;
	}

	private static List<String> getEmailExtensions() {
		if (emailExtensions.isEmpty())
			emailExtensions.addAll(getExtensions(ServerRuntime.FileConverterEmailExtensionsProperty));
		return emailExtensions;
	}

	private static List<String> getOfficeExtensions() {
		if (officeExtensions.isEmpty())
			officeExtensions.addAll(getExtensions(ServerRuntime.FileConverterOfficeExtensionsProperty));
		return officeExtensions;
	}

	private static final List<String> getExtensions(Property extensionsProperty) {
		String[] extensionsArray = Properties.getProperty(extensionsProperty).split("\\,");
		List<String> extensionsList = new ArrayList<String>(extensionsArray.length);
		for (String ext : extensionsArray) {
			ext = ext.trim().toLowerCase();
			if (!ext.isEmpty())
				extensionsList.add(ext);
		}
		return extensionsList;
	}

}
