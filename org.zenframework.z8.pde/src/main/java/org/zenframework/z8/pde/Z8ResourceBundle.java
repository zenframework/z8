package org.zenframework.z8.pde;

import java.util.ListResourceBundle;

public class Z8ResourceBundle extends ListResourceBundle {

	final static Object[][] contents = new Object[][] { { "ContentAssistProposal.label", "Автозамена" }, { "ContentAssistTip.label", "Автоподсказка" }, { "OpenDeclaration.label", "Перейти к определению" }, { "OpenTablesEditor.label", "Схема данных" },
			{ "FindEntryPointPaths.label", "Пути от точек входа" }, { "OpenTextHierarchy.label", "Открыть иерархию типов" }, { "OrganizeImports.label", "Организовать import'ы" }, { "OpenFormsEditor.label", "Открыть представление" },
			{ "CreateGUID.label", "Вставить уникальный идентификатор" } };

	@Override
	protected Object[][] getContents() {
		return contents;
	}

}
