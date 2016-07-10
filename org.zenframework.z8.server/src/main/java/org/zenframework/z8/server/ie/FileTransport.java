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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.FilesFactory;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.IOUtils;

public class FileTransport extends AbstractTransport implements Properties.Listener {

	public static final String PROTOCOL = "file";

	private static final Log LOG = LogFactory.getLog(FileTransport.class);

	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("YYYYMMdd-HHmmss-SSS");

	private static final String IN = "in";
	private static final String OUT = "out";
	private static final String IN_ARCH = "in_arch";
	private static final String TEMP = "temp";
	private static final String EXPORT_ENTRY = "export-entry.xml";
	private static final String PROPERTIES = "message.properties";

	private static final String PROP_CLASS_ID = "z8.ie.message.classId";
	private static final String PROP_TIME = "z8.ie.message.time";

	private File in;
	private File out;
	private File inArch;
	private File temp;

	private File lastReceived = null;
	private File lastSent = null;

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
	public void send(Message message, String transportAddress) throws TransportException {
		File messageFolder = getMessageFolderTemp(temp, message);
		messageFolder.mkdirs();

		// write properties
		File propsFile = new File(messageFolder, PROPERTIES);
		java.util.Properties props = new java.util.Properties();
		props.setProperty(PROP_CLASS_ID, message.getClass().getCanonicalName());
		props.setProperty(PROP_TIME, message.getTime().format(TIME_FORMAT));
		try {
			OutputStream out = new FileOutputStream(propsFile);
			try {
				props.storeToXML(out, "Z8 file message properties", "UTF-8");
			} finally {
				IOUtils.closeQuietly(out);
			}
		} catch (Throwable e) {
			LOG.warn("Can't save message properties " + props + " to '" + messageFolder + "'", e);
		}

		// write export-entry.xml
		File outFile = new File(messageFolder, EXPORT_ENTRY);
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outFile, IeUtil.XML_ENCODING);
			IeUtil.marshalExportEntry(message.getExportEntry(), writer);
		} catch (Exception e) {
			throw new TransportException("Can't export entries to '" + message.getAddress() + "'", e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		// write files
		for (file file : message.getFiles()) {
			if (file.get() != null) {
				outFile = new File(messageFolder, file.id.toString());
				try {
					IOUtils.copy(file.getInputStream(), new FileOutputStream(outFile));
				} catch (IOException e) {
					throw new TransportException("Can't write file '" + outFile + "'", e);
				}
			}
		}
		lastSent = messageFolder;
	}

	@Override
	public void commit() throws TransportException {
		if (lastReceived != null) {
			File archive = new File(inArch, lastReceived.getParentFile().getName() + File.separatorChar
					+ lastReceived.getName());
			archive.getParentFile().mkdirs();
			if (IOUtils.moveFolder(lastReceived, archive, true))
				lastReceived = null;
			else
				throw new TransportException("Committing incoming message '" + lastReceived.getName()
						+ "' failed: can't move folder '" + lastReceived + "' to '" + archive + "'");
		}
		if (lastSent != null) {
			File outFolder = new File(out, lastSent.getParentFile().getName() + File.separatorChar + lastSent.getName());
			outFolder.getParentFile().mkdirs();
			if (IOUtils.moveFolder(lastSent, outFolder, true))
				lastSent = null;
			else
				throw new TransportException("Committing outgoing message '" + lastSent.getName()
						+ "' failed: can't move folder '" + lastSent + "' to '" + outFolder + "'");
		}
	}

	@Override
	public void rollback() {
		lastReceived = null;
		if (lastSent != null) {
			if (IOUtils.deleteFolder(lastSent, true))
				lastSent = null;
			else
				LOG.error("Rolling back sent message '" + lastSent.getName() + "' failed: can't delete folder '" + lastSent
						+ "'");
		}
	}

	@Override
	public boolean isSynchronousRequestSupported() {
		return false;
	}

	@Override
	public file readFileSynchronously(file file, String transportAddress) throws TransportException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProtocol() {
		return PROTOCOL;
	}

	@Override
	public Message receive() throws TransportException {
		File[] addresseeFolders = in.listFiles();

		if (addresseeFolders == null)
			return null;

		for (File addresseeFolder : addresseeFolders) {
			if (addresseeFolder.isDirectory()) {
				File[] messageFolders = addresseeFolder.listFiles();
				Arrays.sort(messageFolders, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
				for (File messageFolder : messageFolders) {
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
									Collection<file> fileInfos = IeUtil.filesToFileInfos(entry.getFiles().getFile(),
											false);
									for (file fileInfo : fileInfos) {
										File file = new File(messageFolder, fileInfo.id.toString());
										if (file.exists()) {
											fileInfo.set(FilesFactory.createFileItem(fileInfo.name.get()));
											InputStream is = new FileInputStream(file);
											OutputStream os = fileInfo.getOutputStream();
											IOUtils.copy(is, os);
											message.getFiles().add(fileInfo);
										}
									}
									lastReceived = messageFolder;
									return message;
								} finally {
									IOUtils.closeQuietly(in);
								}
							}
						} catch (IOException e) {
							throw new TransportException("Can't import entry from '" + messageFolder + "'", e);
						}
					}
				}
			}
		}
		return null;
	}

	private void initFolders(String root) {
		root = root + File.separatorChar + context.getProperty(TransportContext.SelfAddressProperty);
		in = new File(root, IN);
		out = new File(root, OUT);
		inArch = new File(root, IN_ARCH);
		temp = new File(root, TEMP);
	}

	private static File getMessageFolderTemp(File parent, Message message) {
		return new File(parent, message.getAddress() + File.separatorChar + message.getTime().format(TIME_FORMAT) + '_'
				+ message.getId().toString());
	}

	private Message getMessage(File messageFolder) {
		String classId = null;
		datetime time = null;

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
				time = new datetime(TIME_FORMAT.parse(props.getProperty(PROP_TIME)));
			} catch (Throwable e) {
				LOG.warn("Can't get " + PROP_CLASS_ID + " from '" + messageFolder + "'", e);
			}
		}

		if (classId == null)
			classId = Message.class.getCanonicalName();

		Message message = (Message) Loader.getInstance(classId);
		String[] folderName = messageFolder.getName().split("_");
		message.setId(new guid(folderName.length < 2 ? folderName[0] : folderName[1]));
		message.setTime(time);
		message.setAddress(context.getProperty(TransportContext.SelfAddressProperty));
		message.setSender(messageFolder.getParentFile().getName());

		return message;
	}

}
