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
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.EmlUtils;
import org.zenframework.z8.server.utils.IOUtils;

public class FileConverter implements Properties.Listener {

	private static final String PdfExtension = ".pdf";
	private static final String TxtExtension = ".txt";

	private static final int OFFICE_PORT = 8100;
	private static final List<String> pdfExtensions = Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "odp", "ods", "odf", "odg", "wpd", "sxw", "sxi", "sxc", "sxd", "stw", "tif", "tiff", "vsd", "jpg");

	private static final List<String> txtExtensions = Arrays.asList("eml", "mime");

	private final FileStorage storage;
	
	private static File officeHome;
	private static OfficeManager officeManager;

	public FileConverter(File path) {
		super();
		this.storage = new FileStorage(path);
	}

	public FileConverter(FileStorage storage) {
		super();
		this.storage = storage;
	}

	public File getConvertedPdf(String relativePath, File srcFile) {
		return getConvertedFile(relativePath, srcFile, PdfExtension);
	}

	public File getConvertedTxt(String relativePath, File srcFile) {
		return getConvertedFile(relativePath, srcFile, TxtExtension);
	}

	private File getConvertedFile(String relativePath, File srcFile, String extension) {
		if(srcFile.getName().endsWith(extension))
			return srcFile;

		File convertedFile = storage.getFile(relativePath + extension);

		if(!convertedFile.exists()) {
			if(TxtExtension.equalsIgnoreCase(extension))
				convertFileToTxt(srcFile, convertedFile);
			else if(PdfExtension.equalsIgnoreCase(extension))
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

	public String getPath() {
		return storage.getRootPath();
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
		} catch(Exception e) {
			Trace.logError("Can't convert EML to TXT", e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	private void convertFileToPdf(File sourceFile, File convertedFile) {
		getOfficeDocumentConverter().convert(sourceFile, convertedFile);
	}

	public void close() {
		stopOfficeManager();
	}
	
	private OfficeDocumentConverter getOfficeDocumentConverter() {
		if(officeManager == null)
			startOfficeManager();
		return new OfficeDocumentConverter(officeManager);
	}

	@Override
	public void onPropertyChange(String key, String value) {
		if(ServerRuntime.LibreOfficeDirectoryProperty.equalsKey(key)) {
			officeHome = new File(value);
			close();
		}
	}

	synchronized private void startOfficeManager() {
		if(officeManager == null) {
			officeManager = new DefaultOfficeManagerConfiguration().setOfficeHome(getOfficeHome()).setPortNumber(OFFICE_PORT).buildOfficeManager();
			officeManager.start();
		}
	}

	synchronized private void stopOfficeManager() {
		if(officeManager != null) {
			officeManager.stop();
			officeManager = null;
		}
	}

	private File getOfficeHome() {
		if(officeHome == null) {
			String path = new ServerConfig().fileConverter();
			
			if(path.isEmpty())
				path = Properties.getProperty(ServerRuntime.LibreOfficeDirectoryProperty);

			officeHome = new File(path);
		}
		return officeHome;
	}
}
