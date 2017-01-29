package org.zenframework.z8.pde.editor;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public interface IEditorActionDefinitionIds extends ITextEditorActionDefinitionIds {
	public static final String OPEN_DECLARATION = "org.eclipse.jdt.ui.edit.text.java.open.editor";
	public static final String OPEN_TABLES_EDITOR = "org.eclipse.jdt.ui.edit.text.java.open.external.javadoc";
	public static final String FIND_ENTRYPOINT_PATHS = "org.eclipse.jdt.ui.edit.text.java.open.call.hierarchy";
	public static final String OPEN_TEXT_HIERARCHY = "org.eclipse.jdt.ui.edit.text.java.open.type.hierarchy";
	public static final String ORGANIZE_IMPORTS = "org.eclipse.jdt.ui.edit.text.java.organize.imports";
	public static final String OPEN_FORMS_EDITOR = "org.eclipse.ui.edit.rename";
	public static final String CREATE_GUID = "org.zenframework.z8.pde.createguid";
}
