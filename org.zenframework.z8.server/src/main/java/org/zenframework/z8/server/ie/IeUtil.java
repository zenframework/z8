package org.zenframework.z8.server.ie;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.ie.xml.ObjectFactory;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;

public class IeUtil {

	public static final String XML_ENCODING = "utf-8";

	private static final JAXBContext JAXB_CONTEXT;

	private static final List<String> TO_STRING_FIELDS = Arrays.asList("createdAt", "modifiedAt", "createdBy", "modifiedBy",
			"id", "id1", "name", "description", "locked");

	static {
		try {
			JAXB_CONTEXT = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	private IeUtil() {
	}

	public static String toString(ExportEntry.Records.Record record) {
		StringBuilder str = new StringBuilder(1024);
		str.append(record.getTable()).append(" [").append("recordId: ").append(record.getRecordId());
		String policy = record.getPolicy();
		if (policy != null && !policy.isEmpty())
			str.append(" (").append(policy).append(')');
		for (String id : TO_STRING_FIELDS) {
			ExportEntry.Records.Record.Field field = findField(record, id);
			if (field != null) {
				str.append(", ").append(id).append(": ").append(field.getValue());
				policy = field.getPolicy();
				if (policy != null && !policy.isEmpty())
					str.append(" (").append(policy).append(')');
			}
		}
		str.append(']');
		return str.toString();
	}

	public static ExportEntry.Files.File fileInfoToFile(file fileInfo) {
		ExportEntry.Files.File file = new ExportEntry.Files.File();
		file.setName(fileInfo.name.get());
		file.setTime(fileInfo.time.getTicks());
		file.setPath(fileInfo.path.get());
		file.setId(fileInfo.id.toString());
		return file;
	}

	public static file fileToFileInfo(ExportEntry.Files.File file) {
		return new file(new guid(file.getId()), file.getName(), file.getInstanceId(), file.getPath(), 0, new datetime(file.getTime()));
	}

	public static List<ExportEntry.Files.File> fileInfosToFiles(Collection<file> fileInfos) {
		List<ExportEntry.Files.File> files = new ArrayList<ExportEntry.Files.File>(fileInfos.size());

		for (file fileInfo : fileInfos)
			files.add(fileInfoToFile(fileInfo));

		return files;
	}

	public static ExportEntry.Records.Record.Field findField(ExportEntry.Records.Record record, String id) {
		for (ExportEntry.Records.Record.Field field : record.getField()) {
			if (field.getId().equals(id))
				return field;
		}
		return null;
	}

	public static boolean isBuiltinRecord(Table table, guid recordId) {
		return guid.NULL.equals(recordId)
				|| (BuiltinUsers.System.guid().equals(recordId) || BuiltinUsers.Administrator.guid().equals(recordId));
	}

	public static void marshalExportEntry(ExportEntry entry, Writer out) throws JAXBException {
		Marshaller marshaller = getMarshaller(JAXB_CONTEXT);
		marshaller.marshal(entry, out);
	}

	public static String marshalExportEntry(ExportEntry entry) {
		try {
			StringWriter out = new StringWriter();
			marshalExportEntry(entry, out);
			return out.toString();
		} catch(JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public static ExportEntry unmarshalExportEntry(Reader in) {
		try {
			Unmarshaller unmarshaller = getUnmarshaller(JAXB_CONTEXT);
			Object result = unmarshaller.unmarshal(in);
		
			if (result instanceof JAXBElement)
				result = ((JAXBElement<?>) result).getValue();

			if (result instanceof ExportEntry)
				return (ExportEntry) result;

			throw new RuntimeException("Incorrect ExportEntry class: " + result.getClass());
		} catch(JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public static ExportEntry unmarshalExportEntry(String str) {
		return unmarshalExportEntry(new StringReader(str));
	}

	public static Marshaller getMarshaller(JAXBContext jaxbContext) throws JAXBException {
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, XML_ENCODING);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		return marshaller;
	}

	public static Unmarshaller getUnmarshaller(JAXBContext jaxbContext) throws JAXBException {
		return jaxbContext.createUnmarshaller();
	}

}
