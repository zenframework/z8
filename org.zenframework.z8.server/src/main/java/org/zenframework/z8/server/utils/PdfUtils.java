package org.zenframework.z8.server.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.zenframework.z8.server.base.file.Folders;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;

public class PdfUtils {

	private static List<String> TIFF_EXTENSIONS = Arrays.asList("tif", "tiff");
	private static List<Integer> TIFF_MAGIC_NUMBERS = Arrays.asList(0x49492A00, 0x4D4D002A);

	public static int getPageCount(File file) throws IOException {
		return getPageCount(file.getCanonicalPath());
	}

	public static int getPageCount(String file) throws IOException {
		PdfReader reader = new PdfReader(new RandomAccessFileOrArray(file, false, true), new byte[0]);
		try {
			return reader.getNumberOfPages();
		} finally {
			reader.close();
		}
	}

	public static void textToPdf(File sourceFile, File convertedFile) throws IOException {
		textToPdf(new FileInputStream(sourceFile), convertedFile);
	}

	public static void textToPdf(InputStream in, File convertedFile) throws IOException {
		try {
			textToPdf(IOUtils.readText(in, Charset.forName(IOUtils.determineEncoding(in, "UTF-8"))), convertedFile);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public static void textToPdf(String text, File file) throws IOException {
		// If text is empty, itext generates incorrect pdf
		if (text == null || text.isEmpty())
			text = " ";
		Document document = new Document();
		try {
			PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();
			document.add(new Paragraph(text, getFont()));
		} catch (DocumentException e) {
			throw new IOException(e);
		} finally {
			document.close();
		}
	}

	public static void imageToPdf(File sourceFile, File convertedFile) throws IOException {
		imageToPdf(new FileInputStream(sourceFile), FilenameUtils.getExtension(sourceFile.getName()), convertedFile);
	}

	public static void imageToPdf(InputStream in, String extension, File convertedFile) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		IOUtils.copy(in, buf);
		imageToPdf(buf.toByteArray(), extension, convertedFile);
	}

	public static void imageToPdf(byte[] data, String extension, File convertedFile) throws IOException {
		Document document = new Document();
		OutputStream out = new FileOutputStream(convertedFile);
		try {
			PdfWriter.getInstance(document, out);
			document.open();
			if (isTiff(data, extension)) {
				RandomAccessFileOrArray raf = new RandomAccessFileOrArray(data);
				int numberOfPages = TiffImage.getNumberOfPages(raf);
				for (int i = 1; i <= numberOfPages; i++) {
					addImage(document, TiffImage.getTiffImage(raf, i));
				}
			} else {
				addImage(document, Image.getInstance(data));
			}
		} catch (DocumentException e) {
			throw new IOException(e);
		} finally {
			document.close();
		}
	}

	private static void addImage(Document document, Image image) throws DocumentException {
		int indentation = 0;
		float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - indentation) / image
				.getWidth()) * 100;
		image.scalePercent(scaler);
		document.add(image);
	}

	public static boolean isTiff(byte[] data, String extension) throws IOException {
		if (TIFF_EXTENSIONS.contains(extension.toLowerCase()))
			return true;
		return TIFF_MAGIC_NUMBERS.contains(new DataInputStream(new ByteArrayInputStream(data)).readInt());
	}

	public static void merge(List<File> sourceFiles, File mergedFile, String comment) throws IOException {
		Document document = new Document();
		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(mergedFile));
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			for (File file : sourceFiles) {
				InputStream in = new FileInputStream(file);
				PdfReader reader = new PdfReader(in);
				try {
					for (int i = 1; i <= reader.getNumberOfPages(); i++) {
						document.newPage();
						cb.addTemplate(writer.getImportedPage(reader, i), 0, 0);
					}
				} finally {
					reader.close();
					in.close();
				}
			}
			if (comment != null && !comment.isEmpty()) {
				document.newPage();
				document.add(new Paragraph(comment, getFont()));
			}
		} catch (DocumentException e) {
			throw new IOException(e);
		} finally {
			document.close();
		}
	}

	public static void insertBackgroundImg(File pdfFile, File backFile) throws IOException {
		File tmp = new File(pdfFile.getParentFile(), pdfFile.getName() + ".tmp");
		Files.move(pdfFile.toPath(), tmp.toPath());
		try {
			insertBackgroundImg(tmp, pdfFile, backFile);
			tmp.delete();
		} catch (Throwable e) {
			Files.move(tmp.toPath(), pdfFile.toPath());
		}
	}

	public static void insertBackgroundImg(File sourcePdfFile, File targetPdfFile, File backFile) throws IOException {
		Document document = new Document();
		InputStream sourceIn = new FileInputStream(sourcePdfFile);
		PdfReader sourceReader = new PdfReader(sourceIn);
		try {
			Image background = Image.getInstance(backFile.getAbsolutePath());
			float width = document.getPageSize().getWidth();
			float height = document.getPageSize().getHeight();
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(targetPdfFile));
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			for (int i = 1; i <= sourceReader.getNumberOfPages(); i++) {
				document.newPage();
				cb.addTemplate(writer.getImportedPage(sourceReader, i), 0, 0);
				writer.getDirectContentUnder().addImage(background, width, 0, 0, height, 0, 0);
			}
		} catch (DocumentException e) {
			throw new IOException(e);
		} finally {
			sourceReader.close();
			sourceIn.close();
			document.close();
		}
	}

	public static void insertBackgroundPdf(File pdfFile, File backFile) throws IOException {
		File tmp = new File(pdfFile.getParentFile(), pdfFile.getName() + ".tmp");
		Files.move(pdfFile.toPath(), tmp.toPath());
		try {
			insertBackgroundPdf(tmp, pdfFile, backFile, false);
			tmp.delete();
		} catch (Throwable e) {
			Files.move(tmp.toPath(), pdfFile.toPath());
		}
	}

	public static void insertBackgroundPdf(File sourcePdfFile, File targetPdfFile, File backFile, boolean repeate) throws IOException {
		Document document = new Document();
		InputStream sourceIn = new FileInputStream(sourcePdfFile);
		PdfReader sourceReader = new PdfReader(sourceIn);
		InputStream backIn = new FileInputStream(backFile);
		PdfReader backReader = new PdfReader(backIn);
		int sourcePages = sourceReader.getNumberOfPages();
		int backPages = backReader.getNumberOfPages();
		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(targetPdfFile));
			document.open();
			for (int i = 1; i <= sourcePages; i++) {
				document.newPage();
				writer.getDirectContent().addTemplate(writer.getImportedPage(sourceReader, i), 0, 0);
				if (i <= backPages || repeate)
					writer.getDirectContentUnder().addTemplate(writer.getImportedPage(backReader, ((i - 1) % backPages) + 1), 0, 0);
			}
		} catch (DocumentException e) {
			throw new IOException(e);
		} finally {
			backReader.close();
			backIn.close();
			sourceReader.close();
			sourceIn.close();
			document.close();
		}
	}

	private static Font getFont() throws DocumentException, IOException {
		return new Font(BaseFont.createFont(new File(Folders.Base, "fonts/times.ttf").getCanonicalPath(),
				BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 14);
	}
}
