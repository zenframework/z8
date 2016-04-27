package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.OutputStream;
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

import org.apache.commons.codec.binary.Base64;
import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.ie.xml.ExportEntry.Records;
import org.zenframework.z8.ie.xml.ObjectFactory;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.FilesFactory;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.AttachmentField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.security.BuiltinUsers;
import org.zenframework.z8.server.types.guid;

public class IeUtil {

	public static final String XML_ENCODING = "utf-8";

	private static final String RECORD_ID = "recordId";
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

	private IeUtil() {/* Hide constructor*/}

	public static String getUrl(String protocol, String address) {
		return new StringBuilder(100).append(protocol).append(':').append(address).toString();
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

	public static ExportEntry.Records.Record getRecord(String tableClass, guid recordId, Collection<Field> fields,
			RecordsetExportRules exportRules) {
		ImportPolicy defaultImportPolicy = exportRules.getDefaultImportPolicy(recordId);
		Records.Record record = new Records.Record();
		record.setTable(tableClass);
		record.setRecordId(recordId.toString());
		record.setPolicy(defaultImportPolicy.name());
		for (Field f : fields) {
			if (!RECORD_ID.equals(f.id())
					&& f.exportable()
					&& (exportRules.isExportAttachments(recordId, f) || !(AttachmentField.class.isAssignableFrom(f
							.getClass())))) {
				Records.Record.Field field = new Records.Record.Field();
				field.setId(f.id());
				field.setValue(f.get().toString());
				ImportPolicy fieldImportPolicy = exportRules.getImportPolicy(recordId, f);
				if (fieldImportPolicy != defaultImportPolicy)
					field.setPolicy(fieldImportPolicy.name());
				record.getField().add(field);
			}
		}
		return record;
	}

	public static ExportEntry.Files.File fileInfoToFile(FileInfo fileInfo, ImportPolicy policy) {
		ExportEntry.Files.File file = new ExportEntry.Files.File();
		file.setName(fileInfo.name.get());
		file.setType(fileInfo.type.get());
		file.setPath(fileInfo.path.get());
		file.setId(fileInfo.id.toString());
		if (policy != null) {
			file.setPolicy(policy.name());
		}
		return file;
	}

	public static FileInfo fileToFileInfo(ExportEntry.Files.File file) {
		return fileToFileInfoCLASS(file).get();
	}

	public static FileInfo.CLASS<FileInfo> fileToFileInfoCLASS(ExportEntry.Files.File file) {
		FileInfo.CLASS<FileInfo> fileInfo = new FileInfo.CLASS<FileInfo>();
		fileInfo.get().name.set(file.getName());
		fileInfo.get().type.set(file.getType());
		fileInfo.get().path.set(file.getPath());
		fileInfo.get().id.set(file.getId());
		return fileInfo;
	}

	public static List<ExportEntry.Files.File> fileInfosToFiles(List<FileInfo> fileInfos, ImportPolicy policy) {
		List<ExportEntry.Files.File> files = new ArrayList<ExportEntry.Files.File>(fileInfos.size());
		for (FileInfo fileInfo : fileInfos) {
			files.add(fileInfoToFile(fileInfo, policy));
		}
		return files;
	}

	public static List<FileInfo> filesToFileInfos(List<ExportEntry.Files.File> files) {
		List<FileInfo> fileInfos = new ArrayList<FileInfo>(files.size());
		for (ExportEntry.Files.File file : files) {
			fileInfos.add(fileToFileInfo(file));
		}
		return fileInfos;
	}

	public static ExportEntry.Files fileInfosToXmlFiles(List<FileInfo> fileInfos) {
		ExportEntry.Files files = new ExportEntry.Files();
		while (!fileInfos.isEmpty()) {
			FileInfo fileInfo = fileInfos.remove(0);
			ExportEntry.Files.File file = new ExportEntry.Files.File();
			file.setId(fileInfo.id.toString());
			file.setName(fileInfo.name.get());
			file.setType(fileInfo.type.get());
			file.setPath(fileInfo.path.get());
			file.setValue(Base64.encodeBase64String(fileInfo.file.get()));
			files.getFile().add(file);
		}
		return files;
	}

	public static List<FileInfo> xmlFilesToFileInfos(ExportEntry.Files files) throws IOException {
		List<FileInfo> fileInfos = new ArrayList<FileInfo>(files.getFile().size());
		for (ExportEntry.Files.File file : files.getFile()) {
			FileInfo fileInfo = new FileInfo();
			fileInfo.name.set(file.getName());
			fileInfo.type.set(file.getType());
			fileInfo.path.set(file.getPath());
			fileInfo.id.set(file.getId());
			fileInfo.file = FilesFactory.createFileItem(file.getName());
			OutputStream out = fileInfo.file.getOutputStream();
			try {
				out.write(Base64.decodeBase64(file.getValue()));
				file.setValue("");
			} finally {
				out.close();
			}
			fileInfos.add(fileInfo);
		}
		return fileInfos;
	}

	/*public static boolean isExportable(StringField.CLASS<? extends StringField> fieldClass) {
	    if (fieldClass != null && fieldClass.exportable()) {
	        StringField field = fieldClass.get();
	        return !field.isNull() && field.string().get().length() > 0;
	    } else {
	        return false;
	    }
	}

	public static boolean isExportable(GuidField.CLASS<? extends GuidField> fieldClass) {
	    if (fieldClass != null && fieldClass.exportable()) {
	        GuidField field = fieldClass.get();
	        return field != null && !field.isNull() && field.exportable();
	    } else {
	        return false;
	    }
	}

	public static boolean isExportable(DatetimeField.CLASS<? extends DatetimeField> fieldClass) {
	    if (fieldClass != null && fieldClass.exportable()) {
	        DatetimeField field = fieldClass.get();
	        return field != null && !field.isNull() && field.exportable();
	    } else {
	        return false;
	    }
	}

	public static boolean isExportable(BoolExpression.CLASS<? extends BoolExpression> fieldClass) {
	    if (fieldClass != null && fieldClass.exportable()) {
	        BoolExpression field = fieldClass.get();
	        return field != null && !field.isNull() && field.exportable();
	    } else {
	        return false;
	    }
	}*/

	public static ExportEntry.Records.Record.Field findField(ExportEntry.Records.Record record, String id) {
		for (ExportEntry.Records.Record.Field field : record.getField()) {
			if (field.getId() != null && field.getId().equals(id)) {
				return field;
			}
		}
		return null;
	}

	public static boolean isBuiltinRecord(Table table, guid recordId) {
		// TODO Разобраться, почему для таблицы-наследника Users не работает
		return guid.NULL.equals(recordId)
				|| /*table instanceof Users
					&&*/(BuiltinUsers.System.guid().equals(recordId) || BuiltinUsers.Administrator.guid().equals(recordId));
	}

	public static void marshalExportEntry(ExportEntry entry, Writer out) throws JAXBException {
		Marshaller marshaller = getMarshaller(JAXB_CONTEXT);
		marshaller.marshal(entry, out);
	}

	public static String marshalExportEntry(ExportEntry entry) throws JAXBException {
		StringWriter out = new StringWriter();
		marshalExportEntry(entry, out);
		return out.toString();
	}

	public static ExportEntry unmarshalExportEntry(Reader in) throws JAXBException {
		Unmarshaller unmarshaller = getUnmarshaller(JAXB_CONTEXT);
		Object result = unmarshaller.unmarshal(in);
		if (result instanceof JAXBElement) {
			result = ((JAXBElement<?>) result).getValue();
		}
		if (result instanceof ExportEntry) {
			return (ExportEntry) result;
		} else {
			throw new JAXBException("Incorrect ExportEntry class: " + result.getClass());
		}
	}

	public static ExportEntry unmarshalExportEntry(String str) throws JAXBException {
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
