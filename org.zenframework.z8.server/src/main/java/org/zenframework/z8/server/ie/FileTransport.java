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
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.FilesFactory;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.IOUtils;

public class FileTransport extends AbstractTransport implements Properties.Listener {

	public static final String PROTOCOL = "file";

	private static final Log LOG = LogFactory.getLog(FileTransport.class);

	private static final String IN = "in";
	private static final String OUT = "out";
	private static final String IN_ARCH = "in_arch";
	private static final String EXPORT_ENTRY = "export-entry.xml";
	private static final String PROPERTIES = "message.properties";

	private static final String PROP_CLASS_ID = "z8.ie.message.classId";

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
		for (FileInfo file : message.getFiles()) {
			if (file.file != null) {
				outFile = new File(messageFolder, file.id.toString());
				try {
					IOUtils.copy(file.getInputStream(), new FileOutputStream(outFile));
				} catch (IOException e) {
					Trace.logError("Can't write file '" + outFile + "'", e);
				}
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
	public boolean isSynchronousRequestSupported() {
		return false;
	}

	@Override
	public FileInfo readFileSynchronously(FileInfo fileInfo, String transportAddress) throws TransportException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProtocol() {
		return PROTOCOL;
	}

	@Override
	public Message receive() {
		File[] files = in.listFiles();
		Arrays.sort(files, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);

		if (files == null)
			throw new RuntimeException("FileTransport.receive(): '" + in.getPath() + "' is not a directory.");

		for (File addresseeFolder : files) {
			if (addresseeFolder.isDirectory()) {
				for (File messageFolder : addresseeFolder.listFiles()) {
					if (messageFolder.isDirectory()) {
						try {
							Message message = getMessage(messageFolder);
							File entryFile = new File(messageFolder, EXPORT_ENTRY);
							if (entryFile.exists()) {
								Reader in = null;
								try {
									in = new FileReader(entryFile);
									ExportEntry entry = IeUtil.unmarshalExportEntry(in);
									message.setExportEntry(entry);
									// add files
									Collection<FileInfo> fileInfos = IeUtil.filesToFileInfos(entry.getFiles().getFile(), false);
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

	private Message getMessage(File messageFolder) {
		String classId = null;

		File propsFile = new File(messageFolder, PROPERTIES);
		if (propsFile.exists()) {
			try {
				java.util.Properties props = new java.util.Properties();
				InputStream in = new FileInputStream(propsFile);
				try {
					props.loadFromXML(in);
				} finally {
					IOUtils.closeQuietly(in);
				}
				classId = props.getProperty(PROP_CLASS_ID);
			} catch (Throwable e) {
				LOG.warn("Can't get " + PROP_CLASS_ID + " from '" + messageFolder + "'", e);
			}
		}

		if (classId == null)
			classId = Message.class.getCanonicalName();

		Message message = (Message) Loader.getInstance(classId);
		String[] folderName = messageFolder.getName().split("_");
		message.setId(UUID.fromString(folderName.length < 2 ? folderName[0] : folderName[1]));
		message.setAddress(context.getProperty(TransportContext.SelfAddressProperty));
		message.setSender(messageFolder.getParentFile().getName());

		return message;
	}

}
