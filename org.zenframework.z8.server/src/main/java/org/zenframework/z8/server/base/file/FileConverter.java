package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.runtime.ServerRuntime;

import com.google.common.io.Files;

public class FileConverter implements Properties.Listener {
    private static final int OFFICE_PORT = 8100;
    private final static List<String> convertableExtensions = Arrays.asList("doc", "docx", "xls", "xlsx", "ppt",
            "pptx",
            "odt", "odp", "ods", "odf", "odg", "wpd", "sxw", "sxi", "sxc", "sxd", "stw", "tif", "tiff", "vsd");

    private final FilesStorage pdfStorage;
    private volatile File officeHome;
    private volatile OfficeManager officeManager;
    private volatile OfficeDocumentConverter pdfConverter;

    public FileConverter(File pdfStorageRoot) {
        pdfStorage = new FilesStorage(pdfStorageRoot);
    }

    public FileConverter() {
        this(Files.createTempDir());
    }

    public File getConvertedPDF(File srcFile) {
        File convertedFile = pdfStorage.getFile(srcFile.getName() + ".pdf");
        if (!convertedFile.exists()) {
            convertFileToPDF(srcFile, convertedFile);
        }
        return convertedFile;
    }

    public boolean isConvertableToPDFFileExtension(File file) {
        return isConvertableToPDFFileExtension(file.getName());
    }

    public boolean isConvertableToPDFFileExtension(String fileName) {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        return convertableExtensions.contains(extension);
    }

    public int getPagesCount(File srcFile) throws IOException {
        if (!isConvertableToPDFFileExtension(srcFile))
            return 1;

        File pdfFile = getConvertedPDF(srcFile);
        PDDocument doc = PDDocument.load(pdfFile);
        return doc.getNumberOfPages();
    }

    public String getPath() {
        return pdfStorage.getRootPath();
    }

    private void convertFileToPDF(File sourceFile, File convertedPDF) {
        initJODConverter();
        pdfConverter.convert(sourceFile, convertedPDF);
    }

    private void initJODConverter() {
        if (officeManager != null)
            return;

        synchronized (this) {
            startOfficeManager();
        }
    }

    @Override
    public void onPropertyChange(String key, String value) {
        if (ServerRuntime.LibreOfficeDirectoryProperty.equalsKey(key)) {
            officeHome = new File(value);
            stopOfficeManager();
        }
    }

    private void startOfficeManager() {
        if (officeManager == null) {
            officeManager = new DefaultOfficeManagerConfiguration().setOfficeHome(getOfficeHome())
                    .setPortNumber(OFFICE_PORT).buildOfficeManager();
            officeManager.start();
            pdfConverter = new OfficeDocumentConverter(officeManager);
        }
    }

    public void stopOfficeManager() {
        if (officeManager != null) {
            officeManager.stop();
            officeManager = null;
            pdfConverter = null;
        }
    }

    private File getOfficeHome() {
        if (officeHome == null)
            officeHome = new File(Properties.getProperty(ServerRuntime.LibreOfficeDirectoryProperty));
        return officeHome;
    }
}
