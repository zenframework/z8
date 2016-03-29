package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.zenframework.z8.server.engine.Z8Context;
import org.zenframework.z8.server.utils.EmlUtils;
import org.zenframework.z8.server.utils.IOUtils;

public class FileConverter {

	private static final Log LOG = LogFactory.getLog(FileConverter.class);

	private static final String PdfExtension = ".pdf";
	private static final String TxtExtension = ".txt";

	private static final int OFFICE_PORT = 8100;
	private static final List<String> pdfExtensions = Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
			"odt", "odp", "ods", "odf", "odg", "wpd", "sxw", "sxi", "sxc", "sxd", "stw", "tif", "tiff", "vsd", "jpg");

	private static final List<String> txtExtensions = Arrays.asList("eml", "mime");

	private static OfficeManager officeManager;

	private final File storage;

	public FileConverter(File storage) {
		this.storage = storage;
	}

	public File getConvertedPdf(String relativePath, File srcFile) {
		return getConvertedFile(relativePath, srcFile, PdfExtension);
	}

	public File getConvertedTxt(String relativePath, File srcFile) {
		return getConvertedFile(relativePath, srcFile, TxtExtension);
	}

	private File getConvertedFile(String relativePath, File srcFile, String extension) {
		if (srcFile.getName().endsWith(extension))
			return srcFile;

		File convertedFile = new File(storage, relativePath + extension);

		if (!convertedFile.exists()) {
			if (TxtExtension.equalsIgnoreCase(extension))
				convertFileToTxt(srcFile, convertedFile);
			else if (PdfExtension.equalsIgnoreCase(extension))
				convertFileToPdf(srcFile, convertedFile);
		}

		return convertedFile;
	}

	public static boolean isConvertableToPdf(File file) {
		return isConvertableToPdf(file.getName());
	}

	public static boolean isConvertableToPdf(String fileName) {
		String extension = FilenameUtils.getExtension(fileName).toLowerCase();
		return pdfExtensions.contains(extension);
	}

	public static boolean isConvertableToTxt(File file) {
		return isConvertableToTxt(file.getName());
	}

	public static boolean isConvertableToTxt(String fileName) {
		String extension = FilenameUtils.getExtension(fileName).toLowerCase();
		return txtExtensions.contains(extension);
	}

	public static void startOfficeManager() {
		startOfficeManager(Z8Context.getConfig().getOfficeHome());
	}

	private static void startOfficeManager(String officeHome) {
		if (officeManager == null) {
			try {
				officeManager = new DefaultOfficeManagerConfiguration().setOfficeHome(officeHome).setPortNumber(OFFICE_PORT)
						.buildOfficeManager();
				officeManager.start();
			} catch (Throwable e) {
				LOG.error("Could not start office manager", e);
			}
		}
	}

	public static void stopOfficeManager() {
		if (officeManager != null) {
			officeManager.stop();
			officeManager = null;
		}
	}

	private void convertFileToTxt(File sourceFile, File convertedFile) {
		convertedFile.getParentFile().mkdirs();

		java.util.Properties props = new java.util.Properties();
		props.put("mail.host", "smtp.dummydomain.com");
		props.put("mail.transport.protocol", "smtp");

		Session mailSession = Session.getDefaultInstance(props, null);

		InputStream in = null;
		PrintStream out = null;

		try {
			in = new FileInputStream(sourceFile);
			out = new PrintStream(convertedFile, "UTF-8");
			MimeMessage message = new MimeMessage(mailSession, in);
			out.println("Тема : " + message.getSubject());
			out.println("Отправитель : " + message.getFrom()[0]);
			out.println("----------------------------");
			out.println("Сообщение :");
			out.print(EmlUtils.parsePartDocText(message));
		} catch (Exception e) {
			LOG.error("Can't convert EML to TXT", e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	private void convertFileToPdf(File sourceFile, File convertedFile) {
		getOfficeDocumentConverter().convert(sourceFile, convertedFile);
	}

	private OfficeDocumentConverter getOfficeDocumentConverter() {
		return new OfficeDocumentConverter(officeManager);
	}

}
