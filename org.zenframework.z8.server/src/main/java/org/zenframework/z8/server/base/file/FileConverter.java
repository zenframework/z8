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

	private static final int OFFICE_PORT = 8100;

	private static final List<String> imageExtensions = Arrays.asList("tif", "tiff", "jpg", "jpeg");
	private static final List<String> officeExtensions = Arrays.asList("pdf", "txt", "doc", "docx", "xls", "xlsx", "ppt",
			"pptx", "odt", "odp", "ods", "odf", "odg", "wpd", "sxw", "sxi", "sxc", "sxd", "stw", "vsd");
	private static final List<String> emailExtensions = Arrays.asList("eml", "mime");

	private static OfficeManager officeManager;

	private final File storage;

	public FileConverter(File storage) {
		this.storage = storage;
	}

	public File getConvertedPdf(String relativePath, File srcFile) {
		if (srcFile.getName().endsWith(PdfExtension))
			return srcFile;

		String extension = FilenameUtils.getExtension(srcFile.getName()).toLowerCase();
		File convertedFile = new File(storage, relativePath + PdfExtension);

		if (!convertedFile.exists()) {
			if (imageExtensions.contains(extension))
				convertImageToPdf(srcFile, convertedFile);
			else if (emailExtensions.contains(extension))
				convertEmailToPdf(srcFile, convertedFile);
			else if (officeExtensions.contains(extension))
				convertOfficeToPdf(srcFile, convertedFile);
		}

		return convertedFile;
	}

	public static boolean isConvertableToPdf(File file) {
		return isConvertableToPdf(file.getName());
	}

	public static boolean isConvertableToPdf(String fileName) {
		String extension = FilenameUtils.getExtension(fileName).toLowerCase();
		return imageExtensions.contains(extension) || officeExtensions.contains(extension)
				|| emailExtensions.contains(extension);
	}

	private static void startOfficeManager() {
		if (officeManager == null) {
			try {
				officeManager = new DefaultOfficeManagerConfiguration().setOfficeHome(Z8Context.getConfig().getOfficeHome())
						.setPortNumber(OFFICE_PORT).buildOfficeManager();
				officeManager.start();
			} catch (Throwable e) {
				LOG.error("Could not start office manager", e);
				officeManager = null;
			}
		}
	}

	public static void stopOfficeManager() {
		if (officeManager != null) {
			officeManager.stop();
			officeManager = null;
		}
	}

	private void convertImageToPdf(File sourceFile, File convertedFile) {

	}

	private void convertEmailToPdf(File sourceFile, File convertedFile) {
		convertedFile.getParentFile().mkdirs();

		java.util.Properties props = new java.util.Properties();
		props.put("mail.host", "smtp.dummydomain.com");
		props.put("mail.transport.protocol", "smtp");

		Session mailSession = Session.getDefaultInstance(props, null);

		File tempFile = null;
		InputStream in = null;
		PrintStream out = null;

		try {
			tempFile = File.createTempFile("z8-temp-", '-' + sourceFile.getName() + ".");
			in = new FileInputStream(sourceFile);
			out = new PrintStream(tempFile, "UTF-8");
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

		if (tempFile != null && tempFile.exists()) {
			convertOfficeToPdf(tempFile, convertedFile);
			tempFile.delete();
		}
	}

	/*private void convertEmailToTxt(File sourceFile, File convertedFile) {
		// Create a document and add a page to it
	    PDDocument document = new PDDocument();
	    PDPage page1 = new PDPage(PDRectangle.A4);
	        // PDRectangle.LETTER and others are also possible
	    PDRectangle rect = page1.getMediaBox();
	        // rect can be used to get the page width and height
	    document.addPage(page1);
	
	    // Create a new font object selecting one of the PDF base fonts
	    PDFont fontPlain = PDType1Font.HELVETICA;
	    PDFont fontBold = PDType1Font.HELVETICA_BOLD;
	    PDFont fontItalic = PDType1Font.HELVETICA_OBLIQUE;
	    PDFont fontMono = PDType1Font.COURIER;
	
	    // Start a new content stream which will "hold" the to be created content
	    PDPageContentStream cos = new PDPageContentStream(document, page1);
	
	    int line = 0;
	
	    // Define a text content stream using the selected font, move the cursor and draw some text
	    cos.beginText();
	    cos.setFont(fontPlain, 12);
	    cos.newLineAtOffset(100, rect.getHeight() - 50*(++line));
	    cos.showText("Hello World");
	    cos.endText();
	    cos.close();
	}*/

	private void convertOfficeToPdf(File sourceFile, File convertedFile) {
		try {
			getOfficeDocumentConverter().convert(sourceFile, convertedFile);
		} catch (NullPointerException e) {
			throw new RuntimeException("Can't start office process '" + Z8Context.getConfig().getOfficeHome() + "'", e);
		}
	}

	private OfficeDocumentConverter getOfficeDocumentConverter() {
		startOfficeManager();
		return new OfficeDocumentConverter(officeManager);
	}

}
