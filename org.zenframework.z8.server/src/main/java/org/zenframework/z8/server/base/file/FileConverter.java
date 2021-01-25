package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.JodConverter;
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
	
	public static final String PDF_EXTENSION = "pdf";

	public static final string Background = new string("background");
	public static final string Reset = new string("reset");

	private static OfficeManager officeManager;

	private FileConverter() {}

	public static file z8_convertToPdf(file source, file target) {
		return new file(convertToPdf(source.toFile(), target.toFile()));
	}

	public static file z8_convertToPdf(binary source, string type, file target) {
		return new file(convertToPdf(source.get(), type.get(), target.toFile(), Collections.<String, String> emptyMap()));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static file z8_convertToPdf(file source, file target, RLinkedHashMap parameters) {
		return new file(convertToPdf(source.toFile(), target.toFile(), PrimaryUtils.unwrapStringMap(parameters)));
	}

	public static file z8_convertToDocx(file source, file target) {
		return z8_convertToDocx(source.binary(), target);
	}

	public static file z8_convertToDocx(binary source, file target) {
		return new file(convertToDocx(source.get(), target.toFile()));
	}

	public static File convertToPdf(File source, File target) {
		return convertToPdf(source, target, Collections.<String, String>emptyMap());
	}

	public static File convertToPdf(File source, File target, Map<String, String> parameters) {
		try {
			return convertToPdf(new FileInputStream(source), getExtension(source.getName()), target, parameters);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static File convertToPdf(InputStream in, String extension, File target, Map<String, String> parameters) {
		boolean reset = Boolean.parseBoolean(parameters.get(Reset.get()));

		if (reset && target.exists())
			target.delete();
		if (!target.exists()) {
			target.getParentFile().mkdirs();
			try {
				if (isTextExtension(extension))
					PdfUtils.textToPdf(in, target);
				else if (isImageExtension(extension))
					PdfUtils.imageToPdf(in, extension, target);
				else if (isEmailExtension(extension))
					PdfUtils.textToPdf(EmlUtils.emailToString(in), target);
				else if (isOfficeExtension(extension))
					convertOffice(in, target);
			} catch (IOException e) {
				Trace.logError("Can't convert " + in + " to " + target, e);
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}

		String background = parameters.get(Background.get());
		File backgroundFile;
		if (background != null && (backgroundFile = new File(Folders.Base, background)).exists()) {
			File source = target;
			target = new File(source.getParentFile(), addSuffix(source.getName(), "-background"));
			if (reset && target.exists())
				target.delete();
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

	public static File convertToDocx(InputStream input, File target) {
		return convertOffice(input, target);
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

	private static File convertOffice(InputStream input, File target) {
		try {
			startOfficeManager();
			JodConverter.convert(input).to(target).execute();
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
}
