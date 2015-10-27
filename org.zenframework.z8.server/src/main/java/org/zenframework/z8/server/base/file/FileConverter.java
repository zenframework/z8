package org.zenframework.z8.server.base.file;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

import com.google.common.io.Files;

public class FileConverter extends OBJECT implements Properties.Listener {

    private static final String PDF_EXT = ".pdf";

    public static class CLASS<T extends FileConverter> extends OBJECT.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(FileConverter.class);
            setAttribute(Native, FileConverter.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new FileConverter(container);
        }
    }

    private static final int OFFICE_PORT = 8100;
    private final static List<String> convertableExtensions = Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt",
            "pptx", "odt", "odp", "ods", "odf", "odg", "wpd", "sxw", "sxi", "sxc", "sxd", "stw", "tif", "tiff", "vsd");

    private final FilesStorage pdfStorage;
    private volatile File officeHome;
    private volatile OfficeManager officeManager;
    private volatile OfficeDocumentConverter pdfConverter;

    public FileConverter(File pdfStorageRoot) {
        super();
        pdfStorage = new FilesStorage(pdfStorageRoot);
    }

    public FileConverter() {
        this(Files.createTempDir());
    }

    public FileConverter(IObject container) {
        super(container);

        pdfStorage = new FilesStorage(new File(file.BaseFolder, file.CacheFolderName));
    }

    public File getConvertedPDF(String relativePath, File srcFile) {
        if (srcFile.getName().endsWith(PDF_EXT))
            return srcFile;

        File convertedFile = pdfStorage.getFile(relativePath + PDF_EXT);
        if (!convertedFile.exists()) {
            convertFileToPDF(srcFile, convertedFile);
        }
        return convertedFile;
    }

    public File getConvertedPDF(File srcFile) {
        return getConvertedPDF(srcFile.getName(), srcFile);
    }

    public static boolean isConvertableToPDFFileExtension(File file) {
        return isConvertableToPDFFileExtension(file.getName());
    }

    public static boolean isConvertableToPDFFileExtension(String fileName) {
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        return convertableExtensions.contains(extension);
    }

    public int getPagesCount(File srcFile) throws IOException {
        return getPagesCount(srcFile.getName(), srcFile);
    }

    public int getPagesCount(String relativePath, File srcFile) throws IOException {
        if (!isConvertableToPDFFileExtension(srcFile))
            return 1;

        PDDocument doc = null;
        try {
            File pdfFile = getConvertedPDF(relativePath, srcFile);
            doc = PDDocument.load(pdfFile);
            return doc.getNumberOfPages();
        } finally {
            if (doc != null)
                doc.close();
        }
    }

    public String getPath() {
        return pdfStorage.getRootPath();
    }

    public static bool z8_isConvertableToPDFFileExtension(string fileName) {
        return new bool(isConvertableToPDFFileExtension(fileName.get()));
    }

    public file z8_getConvertedPDF(file srcFile) {
        return new file(getConvertedPDF(srcFile.get()));
    }

    public integer z8_getPagesCount(file srcFile) throws IOException {
        return new integer(getPagesCount(srcFile.get()));
    }

    public integer z8_getAttachmentsPagesCount(guid recordId,
            AttachmentField.CLASS<? extends AttachmentField> attachments) {
        try {
            AttachmentProcessor processor = attachments.get().getAttachmentProcessor();
            Collection<FileInfo> fileInfos = processor.read(recordId);

            int result = 0;
            File unconvertedDir = new File(file.BaseFolder, file.UnconvertedFolderName);
            if (unconvertedDir.exists() && !unconvertedDir.isDirectory())
                unconvertedDir.delete();
            if (!unconvertedDir.exists())
                unconvertedDir.mkdirs();
            unconvertedDir.deleteOnExit();

            for (FileInfo fileInfo : fileInfos) {
                fileInfo = org.zenframework.z8.server.base.table.system.Files.getFile(fileInfo);
                FileItem fileItem = fileInfo.file;

                File tempFile = new File(unconvertedDir, fileInfo.id.get().toString() + "-" + fileInfo.name.get());
                if (!tempFile.exists()) {
                    tempFile.deleteOnExit();
                    fileItem.write(tempFile);
                }

                result += getPagesCount(tempFile);
            }

            return new integer(result);
        } catch (Exception e) {
            throw new exception("Не удалось выполнить подсчёт листов", e);
        }
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
