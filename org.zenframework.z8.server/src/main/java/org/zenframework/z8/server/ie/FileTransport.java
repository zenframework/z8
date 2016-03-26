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

	private File root = new File(Properties.getProperty(ServerRuntime.FileFolderProperty));
	private File in = new File(root, IN);
	private File out = new File(root, OUT);
	private File inArch = new File(root, IN_ARCH);

	private Message lastReceived = null;

	public FileTransport(TransportContext context) {
		super(context);
		initFolders(Properties.getProperty(ServerRuntime.FileFolderProperty));
		Properties.addListener(this);
	}

	@Override
	public void onPropertyChange(String key, String value) {
		if (ServerRuntime.FileFolderProperty.equalsKey(key)) {
			initFolders(value);
		}
	}

	public boolean canReceive() {
		return true;
	}

	@Override
	public void connect() throws TransportException {}

	@Override
	public void close() {}

	@Override
	public void send(Message message, String transportAddress) {
		File messageFolder = getMessageFolderOut(out, transportAddress, message.getId());
		messageFolder.mkdirs();
		// write export-entry.xml
		File outFile = new File(messageFolder, EXPORT_ENTRY);
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outFile, IeUtil.XML_ENCODING);
			IeUtil.marshalExportEntry(message.getExportEntry(), writer);
		} catch (Exception e) {
			Trace.logError("Can't export entries to '" + transportAddress + "'", e);
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
				IOUtils.copy(in, out);
			} catch (IOException e) {
				Trace.logError("Can't write file '" + outFile + "'", e);
			}
		}
	}

	@Override
	public void commit() throws TransportException {
		if (lastReceived != null) {
			File inFile = getMessageFolderIn(in, lastReceived);
			File archFile = getMessageFolderIn(inArch, lastReceived);
			archFile.getParentFile().mkdirs();
			if (IOUtils.moveFolder(inFile, archFile, true))
				lastReceived = null;
			else
				throw new TransportException("Committing incoming message '" + lastReceived.getId()
						+ "' failed: can't move folder '" + inFile + "' to '" + archFile + "'");
		}
	}

	@Override
	public void rollback() throws TransportException {}

	@Override
	public String getProtocol() {
		return PROTOCOL;
	}

	@Override
	public Message receive() {
		File[] files = in.listFiles();

		if (files == null)
			throw new RuntimeException("FileTransport.receive(): '" + in.getPath() + "' is not a directory.");

		for (File addresseeFolder : files) {
			if (addresseeFolder.isDirectory()) {
				for (File messageFolder : addresseeFolder.listFiles()) {
					if (messageFolder.isDirectory()) {
						try {
							Message message = Message.instance(UUID.fromString(messageFolder.getName()));
							message.setAddress(context.getProperty(TransportContext.SelfAddressProperty));
							message.setSender(messageFolder.getParentFile().getName());
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

										try {
											InputStream is = new FileInputStream(file);
											OutputStream os = fileInfo.file.getOutputStream();
											IOUtils.copy(is, os);
											message.getFiles().add(fileInfo);
										} catch (IOException e) {
											Trace.logError("Can't import attachment " + fileInfo, e);
										}
									}
									lastReceived = message;
									return message;
								} catch (Exception e) {
									Trace.logError("Can't unmarshal file '" + entryFile + "'", e);
								} finally {
									IOUtils.closeQuietly(in);
								}
							}
						} catch (Exception e) {
							Trace.logError("Can't import entry from '" + messageFolder + "'", e);
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

	private static File getMessageFolderIn(File parent, Message message) {
		return new File(parent, message.getSender() + File.separatorChar + message.getId().toString());
	}

	private static File getMessageFolderOut(File parent, String address, UUID id) {
		return new File(parent, address + File.separatorChar + id.toString());
	}

}
