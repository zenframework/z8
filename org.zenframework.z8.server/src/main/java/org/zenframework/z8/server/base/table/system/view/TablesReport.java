package org.zenframework.z8.server.base.table.system.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.form.report.Report;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.Tables;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.IOUtils;

import net.sf.jooreports.templates.DocumentTemplate;
import net.sf.jooreports.templates.DocumentTemplateException;
import net.sf.jooreports.templates.DocumentTemplateFactory;

public class TablesReport extends Report {

	private static final File TemplateFile = new File(Folders.ReportsOutput, "system/Tables.odt");

	public static class CLASS<T extends Report> extends Report.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(TablesReport.class);
			setAttribute("icon", "fa-file-o");
			setAttribute("displayName", "LibreOffice (*.odt)");
		}

		@Override
		public Object newObject(IObject container) {
			return new TablesReport(container);
		}
	}

	public TablesReport(IObject container) {
		super(container);
	}

	@Override
	protected file execute(guid recordId) {
		InputStream template = null;
		OutputStream out = null;
		file fileOut = file.createTempFile("Tables_", "odt");
		try {
			template = new FileInputStream(TemplateFile);
			out = fileOut.getBinaryOutputStream();
			Map<String, Object> params = new HashMap<String, Object>();

			List<Object> tablesList = new ArrayList<Object>();

			Tables tables = new Tables.CLASS<Tables>(null).get();
			Fields fields = new Fields.CLASS<Fields>(null).get();
			tables.read(Arrays.asList(tables.recordId.get(), tables.name.get(), tables.description.get(), tables.classId.get()));
			while (tables.next()) {
				List<Object> fieldList = new ArrayList<Object>();

				Map<String, Object> tableMap = new HashMap<String, Object>();
				tableMap.put("name", tables.name.get().string().get());
				tableMap.put("description", tables.description.get().string().get());
				tableMap.put("classId", tables.classId.get().string().get());

				int counter = 0;
				fields.read(Arrays.asList(fields.table.get(), fields.name.get(), fields.type.get(), fields.description.get()),
							fields.table.get().sql_guid().operatorEqu(tables.recordId.get().get().sql_guid()));
				while (fields.next()) {
					counter += 1;
					Map<String, String> fieldMap = new HashMap<String, String>();
					fieldMap.put("sequence", String.valueOf(counter));
					fieldMap.put("name", fields.name.get().string().get());
					fieldMap.put("type", fields.type.get().string().get());
					fieldMap.put("description", fields.description.get().string().get());
					fieldList.add(fieldMap);
				}
				tableMap.put("fields", fieldList);
				tablesList.add(tableMap);
			}
			params.put("tables", tablesList);
			buildDocument(template, out, params);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(template);
			IOUtils.closeQuietly(out);
		}
		return fileOut;
	}

	protected static void buildDocument(InputStream template, OutputStream out, Map<String, Object> params) throws IOException {
		DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
		DocumentTemplate docTemplate = documentTemplateFactory.getTemplate(template);
		try {
			docTemplate.createDocument(params, out);
		} catch (DocumentTemplateException e) {
			throw new IOException(e);
		}
	}
}
