package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
//  импорт не проходит так как нет либы в z8
//import org.jodconverter.core.office.OfficeException;
//import org.jodconverter.core.office.OfficeManager;
//import org.jodconverter.core.office.OfficeUtils;
//import org.jodconverter.local.JodConverter;
//import org.jodconverter.local.office.ExternalOfficeManager;
//import org.jodconverter.local.office.LocalOfficeManager;

import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.RLinkedHashMap;
import org.zenframework.z8.server.types.binary;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.*;

public class FileConverter {
	private static final int OFFICE_PORT = 8100;

	public static final String PDF_EXTENSION = "pdf";

	public static final string Background = new string("background");
	public static final string Reset = new string("reset");

	private static OfficeManager officeManager;

	private FileConverter() {
	}

	public static file z8_convertToPdf(binary source, string type, file target) {
		return new file(convertToPdf(source.get(), type.get(), target.toFile(), Collections.<String, String> emptyMap()));
	}

	public static file z8_convertToPdf(file source, file target) {
		return new file(convertToPdf(source.toFile(), target.toFile()));
	}

	public static File convertToPdf(File source, File target) {
		return convertToPdf(source, target, Collections.<String, String> emptyMap());
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

	public static File convertToPdf(File source, File target, Map<String, String> parameters) {
		try {
			String type = FilenameUtils.getExtension(source.getName()).toLowerCase();
			return convertToPdf(new FileInputStream(source), type, target, parameters);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static File convertToPdf(InputStream in, String type, File target, Map<String, String> parameters) {
		boolean reset = Boolean.parseBoolean(parameters.get(Reset.get()));

		if(reset && target.exists())
			target.delete();

		if(!target.exists()) {
			target.getParentFile().mkdirs();
			try {
				if(isTextExtension(type))
					PdfUtils.textToPdf(in, target);
				else if(isImageExtension(type))
					throw new UnsupportedOperationException();
				else if(isEmailExtension(type))
					PdfUtils.textToPdf(EmlUtils.emailToString(in), target);
				else if(isOfficeExtension(type))
					convertAction(in, target);
			} catch(IOException e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}

		String background = parameters.get(Background.get());
		File backgroundFile;
		if(background != null && (backgroundFile = new File(Folders.Base, background)).exists()) {
			File source = target;
			target = new File(source.getParentFile(), addSuffix(source.getName(), "-background"));
			if(reset && target.exists())
				target.delete();
			if(!target.exists()) {
				type = FilenameUtils.getExtension(background).toLowerCase();
				try {
					if(isPdfExtension(type))
						PdfUtils.insertBackgroundPdf(source, target, backgroundFile, false);
					else
						PdfUtils.insertBackgroundImg(source, target, backgroundFile);
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return target;
	}

	public static File convertToDocx(InputStream input, File target) {
		return convertAction(input, target);
	}

	public static bool z8_isConvertableToPdf(string extension) {
		return new bool(isConvertableToPdf(extension.get()));
	}

	public static boolean isConvertableToPdf(String extension) {
		return isPdfExtension(extension) || isTextExtension(extension) || isImageExtension(extension) || isOfficeExtension(extension) || isEmailExtension(extension);
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

		try {
			officeManager = ExternalOfficeManager.builder().install().connectOnStart(true).portNumber(OFFICE_PORT).build();
			officeManager.start();
		} catch(OfficeException e) {
			try {
				Trace.logEvent("Can't connect to an existing OpenOffice process: " + e.getMessage());
				officeManager = LocalOfficeManager.builder().install().officeHome(ServerConfig.officeHome()).portNumbers(OFFICE_PORT).build();
				officeManager.start();
				Trace.logEvent("New OpenOffice '" + ServerConfig.officeHome() + "' process created, port " + OFFICE_PORT);
			} catch(OfficeException e1) {
				Trace.logError("Could not start OpenOffice '" + ServerConfig.officeHome() + "'", e1);
			}
		}

	}

	public static void stopOfficeManager() {
		if(officeManager == null)
			return;
		OfficeUtils.stopQuietly(officeManager);
		officeManager = null;
	}

	private static File convertAction(InputStream input, File target) {
		try {
			startOfficeManager();
			JodConverter.convert(input).to(target).execute();
			return target;
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}


	private static String addSuffix(String filename, String suffix) {
		if(filename == null)
			return null;
		int extIndex = FilenameUtils.indexOfExtension(filename);
		if(extIndex < 0)
			return filename + suffix;
		return filename.substring(0, extIndex) + suffix + filename.substring(extIndex);
	}
}
