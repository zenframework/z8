package org.zenframework.z8.server.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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
		InputStream in = null;
		try {
			in = new FileInputStream(sourceFile);
			textToPdf(IOUtils.readText(in, Charset.forName(IOUtils.determineEncoding(sourceFile, "UTF-8"))), convertedFile);
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
			Font font = new Font(BaseFont.createFont(
					new File(Folders.Base, "fonts/times.ttf").getCanonicalPath(), BaseFont.IDENTITY_H,
					BaseFont.EMBEDDED), 14);
			PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();
			document.add(new Paragraph(text, font));
		} catch (DocumentException e) {
			throw new IOException(e);
		} finally {
			document.close();
		}
	}

	public static void imageToPdf(File sourceFile, File convertedFile) throws IOException {
		Document document = new Document();
		try {
			PdfWriter.getInstance(document, new FileOutputStream(convertedFile));
			document.open();
			if (isTiff(sourceFile)) {
				RandomAccessFileOrArray raf = new RandomAccessFileOrArray(sourceFile.getCanonicalPath());
				int numberOfPages = TiffImage.getNumberOfPages(raf);
				for (int i = 1; i <= numberOfPages; i++) {
					addImage(document, TiffImage.getTiffImage(raf, i));
				}
			} else {
				addImage(document, Image.getInstance(sourceFile.getCanonicalPath()));
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

	public static boolean isTiff(File file) throws IOException {
		if (TIFF_EXTENSIONS.contains(FilenameUtils.getExtension(file.getName()).toLowerCase()))
			return true;
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		try {
			return TIFF_MAGIC_NUMBERS.contains(in.readInt());
		} finally {
			in.close();
		}
	}

	public static void merge(List<File> sourceFiles, File mergedFile) throws IOException {
		Document document = new Document();
		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(mergedFile));
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			for (File file : sourceFiles) {
				InputStream in = new FileInputStream(file);
				try {
					PdfReader reader = new PdfReader(in);
					for (int i = 1; i <= reader.getNumberOfPages(); i++) {
						document.newPage();
						cb.addTemplate(writer.getImportedPage(reader, i), 0, 0);
					}
				} finally {
					in.close();
				}
			}
		} catch (DocumentException e) {
			throw new IOException(e);
		} finally {
			document.close();
		}
	}

	// PDFBox converter realization

	/*public static int getPageCount(String pdfFile) throws IOException {
		return getPageCount(new File(pdfFile));
	}

	public static int getPageCount(File pdfFile) throws IOException {
		PDDocument pdfDocument = null;
		try {
			pdfDocument = PDDocument.load(pdfFile);
			return pdfDocument.getNumberOfPages();
		} finally {
			IOUtils.closeQuietly(pdfDocument);
		}
	}

	public static void textToPdf(String text, File file) throws IOException {
		PDDocument doc = new PDDocument();
		PDFont font = getFont(doc, "fonts/times.ttf");
		try {
			PDPage page = null;
			PDPageContentStream contents = null;
			for (int i = 0, pos = 0, lines = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (c == '\r')
					continue;
				if (lines == 30) {
					contents.endText();
					contents.close();
					page = null;
					lines = 0;
					pos = 0;
				}
				if (page == null) {
					page = new PDPage();
					doc.addPage(page);
					contents = new PDPageContentStream(doc, page);
					contents.beginText();
					contents.setFont(font, 14);
					contents.newLineAtOffset(100, 700);
				}
				if (c == '\n' || pos == 80) {
					contents.setLeading(21);
					contents.newLine();
					pos = 0;
					lines++;
				}
				if (c != '\n') {
					try {
						contents.showText(Character.toString(c));
						pos++;
					} catch (Throwable e) {}
				}
			}
			contents.endText();
			contents.close();
			doc.save(file);
		} finally {
			IOUtils.closeQuietly(doc);
		}
	}

	private static PDFont getFont(PDDocument doc, String path) throws IOException {
		InputStream in = new FileInputStream(new File(Z8Context.getWorkingPath(), path));
		try {
			return PDType0Font.load(doc, in);
		} finally {
			in.close();
		}
	}*/

}
