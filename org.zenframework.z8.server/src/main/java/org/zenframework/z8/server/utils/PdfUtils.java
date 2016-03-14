package org.zenframework.z8.server.utils;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

public class PdfUtils {
    static public int getPageCount(String pdfFile) throws IOException {
    	return getPageCount(new File(pdfFile));
    }
    
    static public int getPageCount(File pdfFile)  throws IOException {
		PDDocument pdfDocument = null;
		
		try {
			pdfDocument = PDDocument.load(pdfFile);
			return pdfDocument.getNumberOfPages();
		} finally {
			if(pdfDocument != null)
				pdfDocument.close();
		}
    }
}
