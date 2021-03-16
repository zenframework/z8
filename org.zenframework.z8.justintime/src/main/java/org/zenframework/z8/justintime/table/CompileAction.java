package org.zenframework.z8.justintime.table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.zenframework.z8.compiler.cmd.Main;
import org.zenframework.z8.compiler.workspace.ProjectProperties;
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.form.action.Action;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.db.sql.functions.InVector;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.IOUtils;

public class CompileAction extends Action {

	static public class strings {
		public final static String Title = "CompileAction.title";
	}

	static public class displayNames {
		public final static String Title = Resources.get(strings.Title);
	}

	public static class CLASS<T extends CompileAction> extends Action.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(CompileAction.class);
			setDisplayName(displayNames.Title);
		}

		@Override
		public Object newObject(IObject container) {
			return new CompileAction(container);
		}
	}

	private static final String PROJECT_NAME = "JustInTime";
	private static final String BL_SOURCES = "bl";
	private static final String JAVA_SOURCES = "java";
	private static final String JAVA_CLASSES = "classes";

	private final File workspace = new File(Folders.Base, "just-in-time");
	private final File blSources = new File(workspace, BL_SOURCES);

	public CompileAction(IObject container) {
		super(container);
	}

	@Override
	public void execute(Collection<guid> records, Query context, Collection<guid> selected, Query query) {
		Source source = (Source) query;
		cleanWorkspace();
		prepareWorkspace(source);
		compileBl();
		compileJava();
	}

	private void cleanWorkspace() {
		try {
			if (workspace.exists())
				FileUtils.cleanDirectory(workspace);
			else
				workspace.mkdirs();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void prepareWorkspace(Source source) {
		source.read(Arrays.asList(source.name.get(), source.source.get()), new InVector(source.typeId.get(), Arrays.asList(SourceType.BL, SourceType.NLS)));
		while (source.next()) {
			File file = new File(blSources, source.name.get().string().get().replace('.', '/'));
			file.getParentFile().mkdirs();
			Writer writer = null;
			try {
				writer = new OutputStreamWriter(new FileOutputStream(file));
				writer.write(source.source.get().string().get());
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(writer);
			}
		}
	}

	private void compileBl() {
		ProjectProperties properties = new ProjectProperties(workspace.toString());
		properties.setProjectName(getProjectName());
		properties.setSourcePaths(BL_SOURCES);
		properties.setOutputPath(JAVA_SOURCES);
		properties.setRequiredPaths("/opt/prog/projects/zfw/z8-1.3.0/org.zenframework.z8.lang/target/libs/org.zenframework.z8.lang-1.3.0.zip");
		try {
			Main.compile(properties);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void compileJava() {
		
	}

	private static String getProjectName() {
		return ApplicationServer.getSchema() + "-jst";
	}

}
