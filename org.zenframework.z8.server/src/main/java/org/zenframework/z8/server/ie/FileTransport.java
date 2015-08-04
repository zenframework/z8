package org.zenframework.z8.server.ie;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collection;
import java.util.UUID;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.FilesFactory;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.IOUtils;

public class FileTransport extends AbstractTransport implements Properties.Listener {

    public static final String PROTOCOL = "file";

    private static final String IN = "in";
    private static final String OUT = "out";
    private static final String IN_ARCH = "in_arch";
    private static final String EXPORT_ENTRY = "export-entry.xml";

    private File root = new File(Properties.getProperty(ServerRuntime.FolderProperty));
    private File in = new File(root, IN);
    private File out = new File(root, OUT);
    private File inArch = new File(root, IN_ARCH);
    
    private Message lastReceived = null;

    public FileTransport(TransportContext context) {
        super(context);
        initFolders(Properties.getProperty(ServerRuntime.FolderProperty));
        Properties.addListener(this);
    }

    @Override
    public void onPropertyChange(String key, String value) {
        if (ServerRuntime.FolderProperty.equalsKey(key)) {
            initFolders(value);
        }
    }

    @Override
    public void connect() throws TransportException {}

    @Override
    public void close() {}

    @Override
    public void send(Message message) {
        File messageFolder = getMessageFolder(out, message);
        messageFolder.mkdirs();
        // write export-entry.xml
        File outFile = new File(messageFolder, EXPORT_ENTRY);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(outFile, IeUtil.XML_ENCODING);
            IeUtil.marshalExportEntry(message.getExportEntry(), writer);
        } catch (Exception e) {
            Trace.logError("Can't export entries to '" + message.getAddress() + "'", e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        // write files
        for (ExportEntry.Files.File file : message.getExportEntry().getFiles().getFile()) {
            outFile = new File(messageFolder, file.getId());
            try {
                InputStream in = Files.getInputStream(IeUtil.fileToFileInfo(file));
                OutputStream out = new FileOutputStream(outFile);
                try {
                    IOUtils.copy(in, out);
                } finally {
                    in.close();
                    out.close();
                }
            } catch (IOException e) {
                Trace.logError("Can't write file '" + outFile + "'", e);
            }
        }
    }

    @Override
    public void commit() {
        File archFile = getMessageFolder(inArch, lastReceived);
        archFile.getParentFile().mkdirs();
        IOUtils.moveFolder(getMessageFolder(in, lastReceived), archFile, true);
    }

    @Override
    public void rollback() throws TransportException {}

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public boolean usePersistency() {
        return false;
    }

    @Override
    public Message receive() {
        for (File addresseeFolder : in.listFiles()) {
            if (addresseeFolder.isDirectory()) {
                for (File messageFolder : addresseeFolder.listFiles()) {
                    if (messageFolder.isDirectory()) {
                        try {
                            Message message = Message.instance(UUID.fromString(messageFolder.getName()));
                            message.setAddress(messageFolder.getParentFile().getName());
                            File entryFile = new File(messageFolder, EXPORT_ENTRY);
                            if (entryFile.exists()) {
                                Reader in = null;
                                try {
                                    in = new FileReader(entryFile);
                                    ExportEntry entry = IeUtil.unmarshalExportEntry(in);
                                    message.setExportEntry(entry);
                                    // add files
                                    Collection<FileInfo> fileInfos = IeUtil.filesToFileInfos(entry.getFiles().getFile());
                                    for (FileInfo fileInfo : fileInfos) {
                                        File file = new File(messageFolder, fileInfo.id.toString());
                                        fileInfo.file = FilesFactory.createFileItem(fileInfo.name.get());
                                        InputStream is = null;
                                        OutputStream os = null;
                                        try {
                                            is = new FileInputStream(file);
                                            os = fileInfo.file.getOutputStream();
                                            IOUtils.copy(is, os);
                                            message.getFiles().add(fileInfo);
                                        } catch (IOException e) {
                                            Trace.logError("Can't import attachment " + fileInfo, e);
                                        } finally {
                                            if (is != null) {
                                                try {
                                                    is.close();
                                                } catch (IOException e) {
                                                    Trace.logError("Can't close input stream from file " + file, e);
                                                }
                                            }
                                            if (os != null) {
                                                try {
                                                    os.close();
                                                } catch (IOException e) {
                                                    Trace.logError("Can't close output stream to file item " + fileInfo.file, e);
                                                }
                                            }
                                        }
                                    }
                                    lastReceived = message;
                                    return message;
                                } catch (Exception e) {
                                    Trace.logError("Can't unmarshal file '" + entryFile + "'", e);
                                } finally {
                                    if (in != null) {
                                        try {
                                            in.close();
                                        } catch (IOException e) {
                                            Trace.logError("Can't close file '" + entryFile + "'", e);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Trace.logError("Can't import entry from '" + messageFolder + "'", e);
                        } finally {
                            IOUtils.moveFolder(messageFolder, new File(inArch, addresseeFolder.getName() + '/'
                                    + messageFolder.getName()), true);
                        }
                    }
                }
            }
        }
        return null;
    }

    private void initFolders(String root) {
        this.root = new File(root);
        in = new File(root, IN);
        out = new File(root, OUT);
        inArch = new File(root, IN_ARCH);
    }

    private static File getMessageFolder(File parent, Message message) {
        return new File(parent, message.getAddress() + '/' + message.getId().toString());
    }

}
