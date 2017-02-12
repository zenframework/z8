package org.zenframework.z8.server.reports;

import java.io.InputStream;
import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class PdfSplitter {
	static public void split(ReportOptions options, InputStream input, OutputStream output) throws Throwable {
		PdfReader reader = new PdfReader(input);

		int totalPages = reader.getNumberOfPages();

		if(totalPages < 1)
			return;

		Rectangle rtPsize = reader.getPageSizeWithRotation(1);

		float rtWidth = rtPsize.getWidth();
		float rtHeight = rtPsize.getHeight();

		Rectangle psize = reader.getPageSize(1);

		float width = psize.getWidth();
		float height = psize.getHeight();
		int rotation = reader.getPageRotation(1);

		Rectangle dstPgRect = getPageRectangle(options.getPageHeight(), options.getPageWidth());

		int hzCount = 0;

		if(rtWidth <= dstPgRect.getWidth() + 5)
			hzCount = 1;
		else
			hzCount = (int)Math.ceil((rtWidth - options.getHorizontalMargins()) / (dstPgRect.getWidth() - options.pageOverlapping));

		int vtCount = 0;

		if(rtHeight <= dstPgRect.getHeight() + 5)
			vtCount = 1;
		else
			vtCount = (int)Math.ceil((rtHeight - options.getVerticalMargins()) / (dstPgRect.getHeight() - options.getVerticalMargins() - options.pageOverlapping));

		int margin = 10;
		Document document = new Document(dstPgRect, margin, margin, margin, margin);

		try {
			PdfWriter writer = PdfWriter.getInstance(document, output);
			document.open();

			PdfContentByte cb = writer.getDirectContent();

			for(int nPage = 1; nPage < totalPages + 1; nPage++) {
				for(int nVt = 0; nVt < vtCount; nVt++) {
					for(int nHz = 0; nHz < hzCount; nHz++) {
						float rem = width - options.getHorizontalMargins() - nHz * (dstPgRect.getWidth() - options.getHorizontalMargins() - options.pageOverlapping);

						if(rem < options.pageOverlapping)
							continue;

						document.newPage();

						PdfImportedPage page = writer.getImportedPage(reader, nPage);

						if(rotation == 0)
							cb.addTemplate(page, 1, 0, 0, 1, 0 - nHz * (dstPgRect.getWidth() - options.getHorizontalMargins() - options.pageOverlapping),
									0 + nVt * (dstPgRect.getHeight() - options.pageOverlapping - options.getVerticalMargins()) - (height - dstPgRect.getHeight()));
						else
							cb.addTemplate(page, 0, -1, 1, 0, 0 - nHz * (dstPgRect.getWidth() - options.pageOverlapping - options.getHorizontalMargins()),
									0 - nVt * (dstPgRect.getHeight() - options.pageOverlapping - options.getBottomMargin() - options.getTopMargin()) + width);
					}
				}
			}
		} finally {
			document.close();
		}
	}

	private static Rectangle getPageRectangle(float m_pageHeight, float m_pageWidth) {
		return new Rectangle((float)m_pageWidth, (float)m_pageHeight);
	}
}
