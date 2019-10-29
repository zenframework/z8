package org.zenframework.z8.oda.designer.ui.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.design.ColumnDefinition;
import org.eclipse.datatools.connectivity.oda.design.DataElementAttributes;
import org.eclipse.datatools.connectivity.oda.design.DataSetDesign;
import org.eclipse.datatools.connectivity.oda.design.DesignFactory;
import org.eclipse.datatools.connectivity.oda.design.OutputElementAttributes;
import org.eclipse.datatools.connectivity.oda.design.ResultSetColumns;
import org.eclipse.datatools.connectivity.oda.design.ResultSetDefinition;
import org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.zenframework.z8.oda.designer.plugin.Plugin;
import org.zenframework.z8.oda.driver.ResultSetMetaData;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.IObject;

public class DataSetEditorPage extends DataSetWizardPage {
	private static String RootIcon = DataSetEditorPage.class.getName() + ".RootIcon";
	private static String DesktopIcon = DataSetEditorPage.class.getName() + ".DesktopIcon";
	private static String TableIcon = DataSetEditorPage.class.getName() + ".TableIcon";
	private static String ViewIcon = DataSetEditorPage.class.getName() + ".ViewIcon";
	private static String ColumnIcon = DataSetEditorPage.class.getName() + ".ColumnIcon";

	static {
		ImageRegistry registry = JFaceResources.getImageRegistry();

		registry.put(RootIcon, ImageDescriptor.createFromFile(Plugin.class, "icons/data_source.gif"));
		registry.put(DesktopIcon, ImageDescriptor.createFromFile(Plugin.class, "icons/desktop.gif"));
		registry.put(TableIcon, ImageDescriptor.createFromFile(Plugin.class, "icons/table.gif"));
		registry.put(ViewIcon, ImageDescriptor.createFromFile(Plugin.class, "icons/view.gif"));
		registry.put(ColumnIcon, ImageDescriptor.createFromFile(Plugin.class, "icons/column.gif"));
	}

	private DataSetDesign dataSetDesign = null;

	private org.eclipse.swt.widgets.Table tables = null;
	private org.eclipse.swt.widgets.Tree tableTree = null;
	private org.eclipse.swt.widgets.Table fields = null;

	static private String SelectTableMessage = Plugin.getResourceString("dataset.select.table");
	static private String SelectFieldsMessage = Plugin.getResourceString("dataset.select.fields");

	public DataSetEditorPage(String pageName) {
		super(pageName);
	}

	private DataSetDesign getDataSetDesign() {
		return dataSetDesign == null ? getInitializationDesign() : dataSetDesign;
	}

	private String getQueryText() {
		return getDataSetDesign().getQueryText();
	}

	private String getCurrentTable() {
		String json = getQueryText();

		if(json == null || json.isEmpty())
			return null;

		JsonObject query = new JsonObject(json);
		return query.getString(query.has(Json.id) ? Json.id : Json.request);
	}

	private Collection<String> getCurrentFields() {
		String json = getQueryText();

		if(json == null || json.isEmpty())
			return null;

		JsonObject query = new JsonObject(json);
		JsonArray fields = query.getJsonArray(Json.fields);

		Collection<String> result = new ArrayList<String>();
		for(int index = 0; index < fields.length(); index++) {
			JsonObject field = fields.getJsonObject(index);
			result.add(field.getString(Json.id));
		}

		return result;
	}

	@Override
	public void createPageCustomControl(Composite parent) {
		setControl(createPageControl(parent));
	}

	private Control createPageControl(Composite parent) {
		Composite pageContainer = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 2;
		pageContainer.setLayout(layout);
		pageContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		Control control = createControls(pageContainer);

		setWidthHints(pageContainer, control);
		return pageContainer;
	}

	private void setWidthHints(Composite pageContainer, Control control) {
		int totalWidth = pageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		GridData data = (GridData)control.getLayoutData();
		data.widthHint = totalWidth;
	}

	private Control createControls(Composite parent) {
		// Sources
		GridData compositeData = new GridData(GridData.FILL_BOTH);
		compositeData.grabExcessVerticalSpace = true;
		compositeData.grabExcessHorizontalSpace = true;

		GridData controlData = new GridData(GridData.FILL_BOTH);
		controlData.grabExcessHorizontalSpace = true;
		controlData.grabExcessVerticalSpace = true;
		controlData.heightHint = 150;

		Composite queriesComposite = new Composite(parent, SWT.NONE);
		queriesComposite.setLayout(new GridLayout());
		queriesComposite.setLayoutData(compositeData);

		Label queriesLabel = new Label(queriesComposite, SWT.LEFT);
		queriesLabel.setText(Plugin.getResourceString("tablepage.label.queries"));
		queriesLabel.setLayoutData(new GridData());

		tables = new org.eclipse.swt.widgets.Table(queriesComposite, SWT.BORDER);
		tables.setLayoutData(controlData);

		// Structure
		Composite structureComposite = new Composite(parent, SWT.NONE);
		structureComposite.setLayout(new GridLayout());
		structureComposite.setLayoutData(compositeData);

		Label structureLabel = new Label(structureComposite, SWT.LEFT);
		structureLabel.setText(Plugin.getResourceString("tablepage.label.structure"));
		structureLabel.setLayoutData(new GridData());

		tableTree = new org.eclipse.swt.widgets.Tree(structureComposite, SWT.BORDER | SWT.SINGLE);
		tableTree.setLayoutData(controlData);

		// Content
		Composite fieldsComposite = new Composite(parent, SWT.NONE);
		fieldsComposite.setLayout(new GridLayout());
		fieldsComposite.setLayoutData(compositeData);

		Label fieldsLabel = new Label(fieldsComposite, SWT.LEFT);
		fieldsLabel.setText(Plugin.getResourceString("tablepage.label.selectedFields"));
		fieldsLabel.setLayoutData(new GridData());

		fields = new org.eclipse.swt.widgets.Table(fieldsComposite, SWT.BORDER);
		fields.setLayoutData(controlData);

		installTableSelectionListener();
		installStructureDblClickListener();
		installFieldsDblClickListener();

		initializeTables();

		return queriesComposite;
	}

	private void updateButtons() {
		IPreferencePageContainer container = getEditorContainer();
		if(container != null)
			container.updateButtons();
	}

	public Image getImage(String name) {
		return JFaceResources.getImageRegistry().get(name);
	}

	private String displayName(Table.CLASS<Table> table) {
		String name = table.displayName();
		return name == null ? table.name() : name;
	}

	private String index(Table.CLASS<Table> table) {
		IObject container = table.getContainer();

		if(container != null)
			container.getCLASS().get();

		return table.index();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<Table.CLASS<Table>> getTables() throws Throwable {
		List<Table.CLASS<Table>> tables = new ArrayList<Table.CLASS<Table>>();

		for(Table.CLASS<? extends Table> cls : RuntimeLoader.getRuntime().tables())
			tables.add((Table.CLASS)cls);

		Comparator<Table.CLASS<Table>> comparator = new Comparator<Table.CLASS<Table>>() {
			@Override
			public int compare(Table.CLASS<Table> o1, Table.CLASS<Table> o2) {
				return displayName(o1).compareTo(displayName(o2));
			}
		};

		Collections.sort(tables, comparator);
		return tables;
	}

	private void initializeTables() {
		String currentTable = getCurrentTable();
		Collection<String> currentFields = getCurrentFields();
		try {
			boolean selected = false;
			for(Table.CLASS<Table> table : getTables()) {
				TableItem item = new TableItem(tables, SWT.NONE);
				item.setText(displayName(table));
				item.setImage(getImage(TableIcon));
				item.setData(table);
				if(table.classId().equals(currentTable)) {
					tables.setSelection(item);
					initializeTableTree(table, currentFields);
					selected = true;
				}
			}

			if(!selected)
				setMessage(SelectTableMessage, IMessageProvider.ERROR);

			updateButtons();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Collection<Table.CLASS<Table>> getLinkedTables(Table.CLASS<Table> table) {
		Collection<Table.CLASS<Table>> tables = new ArrayList<Table.CLASS<Table>>();

		for(Field.CLASS<Field> field : table.get(CLASS.Constructor1).getLinks()) {
			ILink link = (ILink)field.get(CLASS.Constructor1);
			Table.CLASS<Table> linkedTable = (Table.CLASS)link.query();
			if(linkedTable != null)
				tables.add(linkedTable);
		}

		return tables;
	}

	private void initializeTableTree(Table.CLASS<Table> table, Collection<String> fields) {
		this.tableTree.removeAll();
		this.fields.removeAll();
		initializeTableTreeItem(null, table, fields);
	}

	@SuppressWarnings("unchecked")
	private boolean checkCicle(TreeItem item, Table.CLASS<Table> table) {
		TreeItem parent;

		while((parent = item.getParentItem()) != null) {
			Table.CLASS<Table> t = (Table.CLASS<Table>)parent.getData();
			if(t != null) {
				if(t.classId().equals(table.classId()) || t.instanceOf(table.getJavaClass()) || table.instanceOf(t.getJavaClass()))
					return false;
			}
			item = parent;
		}

		return true;
	}

	private void initializeTableTreeItem(TreeItem parent, Table.CLASS<Table> table, Collection<String> fields) {
		for(Table.CLASS<Table> linkedTable : getLinkedTables(table)) {
			TreeItem item = parent == null ? new TreeItem(tableTree, SWT.NONE) : new TreeItem(parent, SWT.NONE);
			item.setText(displayName(linkedTable) + " (" + index(linkedTable) + ")");
			item.setImage(getImage(TableIcon));
			item.setData(linkedTable);

			if(checkCicle(item, linkedTable))
				initializeTableTreeItem(item, linkedTable, fields);
		}

		for(Field field : table.get().getDataFields()) {
			if(!field.system()) {
				TreeItem fieldItem = parent == null ? new TreeItem(tableTree, SWT.NONE) : new TreeItem(parent, SWT.NONE);
				fieldItem.setText(field.displayName() + " (" + field.index() + ")");
				fieldItem.setImage(getImage(ColumnIcon));
				fieldItem.setData(field);

				if(fields != null && fields.contains(field.id()))
					addField(field);
			}
		}
	}

	private Collection<Field> getSelectedFields() {
		Collection<Field> result = new ArrayList<Field>();

		if(fields == null)
			return result;

		for(TableItem item : fields.getItems())
			result.add((Field)item.getData());

		return result;
	}

	@SuppressWarnings("unchecked")
	private String buildQueryString(String name) {
		if(tables.getSelectionCount() == 0)
			throw new RuntimeException("No table selected");

		Table.CLASS<Table> query = (Table.CLASS<Table>)tables.getSelection()[0].getData();

		JsonWriter writer = new JsonWriter();
		query.get().writeReportMeta(writer, name, getSelectedFields());

		return writer.toString();
	}

	@Override
	protected DataSetDesign collectDataSetDesign(DataSetDesign design) {
		if(this.tables == null)
			return design;

		design.setQueryText(buildQueryString(design.getName()));

		ResultSetDefinition resultSet = DesignFactory.eINSTANCE.createResultSetDefinition();
		ResultSetColumns columns = DesignFactory.eINSTANCE.createResultSetColumns();

		for(Field field : getSelectedFields()) {
			ColumnDefinition column = DesignFactory.eINSTANCE.createColumnDefinition();

			DataElementAttributes attributes = DesignFactory.eINSTANCE.createDataElementAttributes();
			attributes.setName(field.id());
			attributes.setUiDisplayName(field.displayName() + " (" + field.id() + ")");
			attributes.setNativeDataTypeCode(ResultSetMetaData.getNativeTypeCode(field));

			OutputElementAttributes outputAttributes = DesignFactory.eINSTANCE.createOutputElementAttributes();
			attributes.setName(field.id());
			attributes.setUiDisplayName(field.displayName() + " (" + field.id() + ")");

			column.setAttributes(attributes);
			column.setUsageHints(outputAttributes);

			columns.getResultColumnDefinitions().add(column);
		}

		resultSet.setResultSetColumns(columns);
		design.setPrimaryResultSet(resultSet);

		return design;
	}

	private void installTableSelectionListener() {
		tables.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					@SuppressWarnings("unchecked")
					public void run() {
						tableTree.removeAll();
						initializeTableTree((Table.CLASS<Table>)event.item.getData(), null);
						if(getSelectedFields().isEmpty())
							setMessage(SelectFieldsMessage, IMessageProvider.ERROR);
						if(getEditorContainer() != null)
							getEditorContainer().updateButtons();
					}
				});
			}
		});
	}

	private void installStructureDblClickListener() {
		tableTree.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(final MouseEvent event) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						Point point = new Point(event.x, event.y);
						TreeItem item = tableTree.getItem(point);
						if(item == null)
							return;

						Object data = item.getData();
						if(data instanceof Field)
							addField((Field)data);
					}
				});
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		});
	}

	private void addField(Field field) {
		TableItem fieldItem = new TableItem(fields, SWT.NONE);
		fieldItem.setText(field.displayName() + " (" + field.id() + ")");
		fieldItem.setImage(getImage(ColumnIcon));
		fieldItem.setData(field);

		setMessage("", IMessageProvider.NONE);
	}

	private void installFieldsDblClickListener() {
		fields.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(final MouseEvent event) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						Point point = new Point(event.x, event.y);
						TableItem item = fields.getItem(point);
						if(item != null)
							item.dispose();
					}
				});
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		});
	}

	@Override
	protected boolean canLeave() {
		return fields == null || fields.getItemCount() != 0;
	}

	@Override
	protected void refresh(DataSetDesign dataSetDesign) {
		this.dataSetDesign = dataSetDesign;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		getControl().setFocus();
	}

	@Override
	protected void cleanup() {
		dataSetDesign = null;
	}
}