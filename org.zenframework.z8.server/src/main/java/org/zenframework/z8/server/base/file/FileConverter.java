package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.ArrayUtils;
import org.zenframework.z8.server.utils.EmlUtils;
import org.zenframework.z8.server.utils.IOUtils;
import org.zenframework.z8.server.utils.PdfUtils;
import org.zenframework.z8.server.utils.PrimaryUtils;

public class FileConverter {

	private static final int OFFICE_PORT = 8100;

	public static final String PDF = "pdf";

	public static final string PDFX = new string("PDF/X");
	public static final string PDFX1A2001 = new string("PDF/X-1a:2001");
	public static final string PDFX32002 = new string("PDF/X-3:2002");
	public static final string PDFA1A = new string("PDF/A-1a");
	public static final string PDFA1B = new string("PDF/A-1b");

	public static final string Background = new string("background");
	public static final string Reset = new string("reset");
	public static final string Format = new string("format");

	private static final Map<String, Integer> PDF_VERSIONS = getPdfVersions();

	private static OfficeManager officeManager;

	private FileConverter() {}

	public static file z8_convert(file source, file target) {
		return new file(convert(source.toFile(), target.toFile()));
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
				if (toPdf && isTextExtension(extension))
					PdfUtils.textToPdf(in, target);
				else if (toPdf && isImageExtension(extension))
					PdfUtils.imageToPdf(in, extension, target);
				else if (toPdf && isEmailExtension(extension))
					PdfUtils.textToPdf(EmlUtils.emailToString(in), target);
				else
					convertOffice(in, target, parameters);
			} catch (IOException e) {
				throw new RuntimeException("Can't convert file to " + target, e);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}

		String background = parameters.get(Background.get());
		File backgroundFile;
		if (!toPdf || background == null || !(backgroundFile = new File(Folders.Base, background)).exists())
			return target;

		File source = target;
		target = new File(source.getParentFile(), addSuffix(source.getName(), "-background"));

		if (target.exists()) {
			if (!reset)
				return target;
			target.delete();
		}

		try {
			if (isPdfExtension(getExtension(background)))
				PdfUtils.insertBackgroundPdf(source, target, backgroundFile, false);
			else
				PdfUtils.insertBackgroundImg(source, target, backgroundFile);
		} catch (IOException e) {
			throw new RuntimeException("Can't insert background " + backgroundFile + " into " + source, e);
		}

		return target;
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
			officeManager = LocalOfficeManager.builder().install().officeHome(ServerConfig.officeHome()).portNumbers(OFFICE_PORT).build();
			officeManager.start();
			Trace.logEvent("New OpenOffice '" + ServerConfig.officeHome() + "' process created, port " + OFFICE_PORT);
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

	private static File convertOffice(InputStream input, File target, Map<String, String> parameters) {
		try {
			startOfficeManager();

			DocumentConverter converter = LocalConverter.make(officeManager);
			DocumentFormatRegistry registry = converter.getFormatRegistry();
			//DocumentFormat inputFormat = registry.getFormatByExtension(extension);
			DocumentFormat targetFormat = getDocumentFormat(parameters);
			if (targetFormat == null)
				targetFormat = registry.getFormatByExtension(new file(target).extension());

			converter.convert(input)/*.as(inputFormat)*/.to(target).as(targetFormat).execute();
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
