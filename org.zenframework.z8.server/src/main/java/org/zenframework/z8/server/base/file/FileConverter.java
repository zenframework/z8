package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.Session;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Request;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ArrayUtils;
import org.zenframework.z8.server.utils.EmlUtils;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.PdfUtils;
import org.zenframework.z8.server.utils.PrimaryUtils;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfAppearance;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class FileConverter {

	public static final String PDF = "pdf";
	public static final String TempPrefix = "converted-pdf-";

	public static final string PDFX = new string("PDF/X");
	public static final string PDFX1A2001 = new string("PDF/X-1a:2001");
	public static final string PDFX32002 = new string("PDF/X-3:2002");
	public static final string PDFA1A = new string("PDF/A-1a");
	public static final string PDFA1B = new string("PDF/A-1b");

	public static final string Background = new string("background");
	public static final string Reset = new string("reset");
	public static final string Format = new string("format");
	public static final string Stamps = new string("stamps");

	private static final Map<String, Integer> PDF_VERSIONS = getPdfVersions();

	private static OfficeManager officeManager;

	private FileConverter() {}

	public static file z8_convertToPdf(file source) {
		return new file(convert(source.toFile(), file.createTempJavaFile(TempPrefix, PDF)));
	}
	
	public static file z8_convert(file source, file target) {
		return new file(convert(source.toFile(), target.toFile()));
	}

	public static file z8_convertToPdf(file source, RLinkedHashMap<string, string> parameters) {
		return new file(convert(source.toFile(), file.createTempJavaFile(TempPrefix, PDF), PrimaryUtils.unwrapStringMap(parameters)));
	}
	
	public static file z8_convert(file source, file target, RLinkedHashMap<string, string> parameters) {
		return new file(convert(source.toFile(), target.toFile(), PrimaryUtils.unwrapStringMap(parameters)));
	}

	public static file z8_convert(binary source, file target) {
		return new file(convert(source.get(), null, target.toFile(), Collections.<String, String>emptyMap()));
	}

	public static file z8_convert(binary source, file target, RLinkedHashMap<string, string> parameters) {
		return new file(convert(source.get(), null, target.toFile(), PrimaryUtils.unwrapStringMap(parameters)));
	}

	public static file z8_convert(binary source, string extension, file target) {
		return new file(convert(source.get(), extension.get(), target.toFile(), Collections.<String, String>emptyMap()));
	}

	public static file z8_convert(binary source, string extension, file target, RLinkedHashMap<string, string> parameters) {
		return new file(convert(source.get(), extension.get(), target.toFile(), PrimaryUtils.unwrapStringMap(parameters)));
	}

	public static File convert(File source, File target) {
		return convert(source, target, Collections.<String, String>emptyMap());
	}

	public static File convert(File source, File target, Map<String, String> parameters) {
		try {
			return convert(new FileInputStream(source), getExtension(source.getName()), target, parameters);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File convert(InputStream in, String extension, File target) {
		return convert(in, extension, target, Collections.<String, String>emptyMap());
	}

	public static File convert(InputStream in, String extension, File target, Map<String, String> parameters) {
		boolean reset = Boolean.parseBoolean(parameters.get(Reset.get()));

		if (reset && target.exists())
			target.delete();

		boolean toPdf = isPdfExtension(getExtension(target));

		if (!target.exists()) {
			target.getParentFile().mkdirs();
			try {
				if (toPdf && isPdfExtension(extension))
					IOUtils.copy(in, target);
				else if (toPdf && isTextExtension(extension))
					PdfUtils.textToPdf(in, target);
				else if (toPdf && isImageExtension(extension))
					PdfUtils.imageToPdf(in, extension, target);
				else if (toPdf && isEmailExtension(extension))
					PdfUtils.textToPdf(EmlUtils.emailToString(in), target);
				else
					convertOffice(in, extension, target, parameters);
			} catch (IOException e) {
				throw new RuntimeException("Can't convert file to " + target, e);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}

		target = appendBackground(target, parameters);
		target = appendStamps(target, parameters);

		return target;
	}

	private static File appendBackground(File file, Map<String, String> parameters) {
		boolean reset = Boolean.parseBoolean(parameters.get(Reset.get()));
		boolean toPdf = isPdfExtension(getExtension(file));
		String background = parameters.get(Background.get());
		File backgroundFile;
		if (!toPdf || background == null || !(backgroundFile = new File(Folders.Base, background)).exists())
			return file;

		File source = file;
		file = new File(source.getParentFile(), addSuffix(source.getName(), "-background"));

		if (file.exists()) {
			if (!reset)
				return file;
			file.delete();
		}

		try {
			if (isPdfExtension(getExtension(background)))
				PdfUtils.insertBackgroundPdf(source, file, backgroundFile, false);
			else
				PdfUtils.insertBackgroundImg(source, file, backgroundFile);
		} catch (IOException e) {
			throw new RuntimeException("Can't insert background " + backgroundFile + " into " + source, e);
		}

		return file;
	}

	private static File appendStamps(File file, Map<String, String> parameters) {
		boolean reset = Boolean.parseBoolean(parameters.get(Reset.get()));
		boolean toPdf = isPdfExtension(getExtension(file));
		String stampsString = parameters.get(Stamps.get());
		if (!toPdf || stampsString == null)
			return file;

		JsonArray stamps = new JsonArray(stampsString);
		if (stamps.length() == 0)
			return file;

		File source = file;

		file = new File(source.getParentFile(), addSuffix(source.getName(), "-stamps"));

		if (file.exists()) {
			if (!reset)
				return file;
			file.delete();
		}

		try {
			InputStream sourceIn = new FileInputStream(source);
			PdfReader sourceReader = new PdfReader(sourceIn);
			int pages = sourceReader.getNumberOfPages();
			Rectangle size = sourceReader.getPageSize(1);
			Trace.logEvent(size.getWidth() + "x" + size.getHeight());
			PdfStamper stamper = new PdfStamper(sourceReader, new FileOutputStream(file), '\0', true);

			ApplicationServer.setRequest(new Request(new Session(ApplicationServer.getSchema())));

			for (int i = 0; i < stamps.length(); ++i) {
				try {
					JsonObject stampInfo = stamps.getJsonObject(i);
					processStamp(stamper, stampInfo, size.getHeight(), pages);
				} catch (Exception e) {
					throw new RuntimeException("Can't insert stamp at index " + i + " into " + source, e);
				}
			}

			stamper.close();
			sourceReader.close();
			sourceIn.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			ApplicationServer.setRequest(null);
		}

		return file;
	}

	private static void processStamp(PdfStamper stamper, JsonObject stampInfo, float pageH, int pages) throws Exception {
		guid stampId = stampInfo.getGuid("id");
		file stampFile = Files.get(Files.get(stampId));
		Image signImg = Image.getInstance(ImageIO.read(stampFile.getInputStream()), null);

		Rectangle loc = getStampPosition(stampInfo, pageH);

		String pageStr = stampInfo.has("page") ? stampInfo.getString("page") : "1";
		int page = pageStr.equals("last") ? pages : Integer.parseInt(pageStr);
		if (page > pages)
			page = pages;

		addStamp(stamper, "Stamp", signImg, loc, page);
	}

	private static Rectangle getStampPosition(JsonObject stampInfo, float pageH) {
		int x = stampInfo.getInt("x");
		int y = stampInfo.getInt("y");
		int width = stampInfo.getInt("w");
		int height = stampInfo.getInt("h");

		String yStart = stampInfo.has("yStart") ? stampInfo.getString("yStart") : "bottom";
		if (!yStart.equals("bottom") && !yStart.equals("top"))
			throw new RuntimeException("Unknown 'yStart' value: " + yStart);

		float x1 = x;
		float x2 = x + width;
		float y1 = yStart.equals("top") ? pageH - y - height : y;
		float y2 = yStart.equals("top") ? pageH - y : y + height;

		return new Rectangle(x1, y1, x2, y2);
	}

	private static void addStamp(PdfStamper stamp, String name, com.lowagie.text.Image image, Rectangle location, int page) throws DocumentException {
		PdfAnnotation stampAnnot = PdfAnnotation.createStamp(stamp.getWriter(), location, null, name);
		image.setAbsolutePosition(0, 0);
		PdfContentByte cb = new PdfContentByte(stamp.getWriter());
		PdfAppearance app = cb.createAppearance(image.getScaledWidth(), image.getScaledHeight());
		app.addImage(image);
		stampAnnot.setAppearance(PdfName.N, app);
		stampAnnot.setFlags(PdfAnnotation.FLAGS_PRINT);
		stamp.addAnnotation(stampAnnot, page);
	}

	public static string z8_getExtension(file file) {
		return new string(file.extension());
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
		return PDF.equals(extension);
	}

	public static bool z8_isTextExtension(string extension) {
		return new bool(isTextExtension(extension.get()));
	}

	public static boolean isTextExtension(String extension) {
		return extension != null && ArrayUtils.contains(ServerConfig.textExtensions(), extension.toLowerCase());
	}

	public static bool z8_isImageExtension(string extension) {
		return new bool(isImageExtension(extension.get()));
	}

	public static boolean isImageExtension(String extension) {
		return extension != null && ArrayUtils.contains(ServerConfig.imageExtensions(), extension.toLowerCase());
	}

	public static bool z8_isEmailExtension(string extension) {
		return new bool(isEmailExtension(extension.get()));
	}

	public static boolean isEmailExtension(String extension) {
		return extension != null && ArrayUtils.contains(ServerConfig.emailExtensions(), extension.toLowerCase());
	}

	public static bool z8_isOfficeExtension(string extension) {
		return new bool(isOfficeExtension(extension.get()));
	}

	public static boolean isOfficeExtension(String extension) {
		return extension != null && ArrayUtils.contains(ServerConfig.officeExtensions(), extension.toLowerCase());
	}

	public static void startOfficeManager() {
		if(officeManager != null)
			return;

		// TODO Define setting for local/external manager
//		officeManager = ExternalOfficeManager.builder().install().connectOnStart(true).portNumber(OFFICE_PORT).build();
//		officeManager.start();

		try {
			officeManager = LocalOfficeManager.builder().install().officeHome(ServerConfig.officeHome()).portNumbers(ServerConfig.officePort()).build();
			officeManager.start();
			Trace.logEvent("New OpenOffice '" + ServerConfig.officeHome() + "' process created, port " + ServerConfig.officePort());
		} catch(OfficeException e1) {
			Trace.logError("Could not start OpenOffice '" + ServerConfig.officeHome() + "'", e1);
		}
	}

	public static void stopOfficeManager() {
		if(officeManager == null)
			return;
		OfficeUtils.stopQuietly(officeManager);
		officeManager = null;
	}

	private static File convertOffice(InputStream input, String extension, File target, Map<String, String> parameters) {
		try {
			startOfficeManager();

			DocumentConverter converter = LocalConverter.make(officeManager);
			DocumentFormatRegistry registry = converter.getFormatRegistry();
			DocumentFormat inputFormat = registry.getFormatByExtension(extension);
			DocumentFormat targetFormat = getDocumentFormat(parameters);
			if (targetFormat == null)
				targetFormat = registry.getFormatByExtension(new file(target).extension());

			converter.convert(input).as(inputFormat).to(target).as(targetFormat).execute();
			return target;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static String addSuffix(String filename, String suffix) {
		if (filename == null)
			return null;
		int extIndex = FilenameUtils.indexOfExtension(filename);
		if (extIndex < 0)
			return filename + suffix;
		return filename.substring(0, extIndex) + suffix + filename.substring(extIndex);
	}

	private static DocumentFormat getDocumentFormat(Map<String, String> parameters) {
		String format = parameters.get(Format.get());

		if (format == null)
			return null;

		Integer pdfVersion = PDF_VERSIONS.get(format);

		if (pdfVersion == null)
			return null;

		final Map<String, Integer> pdfOptions = new HashMap<String, Integer>();
		pdfOptions.put("SelectPdfVersion", pdfVersion);

		return DocumentFormat.builder()
				.name(format)
				.inputFamily(DocumentFamily.TEXT)
				.extension(PDF)
				.mediaType(PDF)
				.storeProperty(DocumentFamily.TEXT, "FilterData", pdfOptions)
				.storeProperty(DocumentFamily.TEXT, "FilterName", "writer_pdf_Export")
				.unmodifiable(false)
				.build();
	}

	private static Map<String, Integer> getPdfVersions() {
		Map<String, Integer> versions = new HashMap<String, Integer>();
		versions.put(PDFX.get(), 0);
		versions.put(PDFX1A2001.get(), 1);
		versions.put(PDFX32002.get(), 2);
		versions.put(PDFA1A.get(), 3);
		versions.put(PDFA1B.get(), 4);
		return versions;
	}

	// Deprecated methods

	@Deprecated
	public static file z8_convertToDocx(file source, file target) {
		return z8_convertToDocx(source.binary(), target);
	}

	@Deprecated
	public static file z8_convertToDocx(binary source, file target) {
		return z8_convert(source, target);
	}

	@Deprecated
	public static file z8_convertToPdf(file source, file target) {
		return new file(convert(source.toFile(), target.toFile()));
	}

	@Deprecated
	public static file z8_convertToPdf(binary source, string type, file target) {
		return new file(convert(source.get(), type.get(), target.toFile(), Collections.<String, String> emptyMap()));
	}

	@Deprecated
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static file z8_convertToPdf(file source, file target, RLinkedHashMap parameters) {
		return new file(convert(source.toFile(), target.toFile(), PrimaryUtils.unwrapStringMap(parameters)));
	}
}
