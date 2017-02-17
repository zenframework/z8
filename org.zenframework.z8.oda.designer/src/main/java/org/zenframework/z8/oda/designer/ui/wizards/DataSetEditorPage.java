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
import org.eclipse.datatools.connectivity.oda.design.ResultSetColumns;
import org.eclipse.datatools.connectivity.oda.design.ResultSetDefinition;
import org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage;
import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.ILink;
import org.zenframework.z8.server.json.JsonWriter;

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

	private org.eclipse.swt.widgets.Table queries = null;
	private org.eclipse.swt.widgets.Tree structure = null;
	private org.eclipse.swt.widgets.Table fields = null;

	private static String DEFAULT_MESSAGE = Plugin.getResourceString("dataset.select.data.object");
	private static String ERROR_MESSAGE = Plugin.getResourceString("dataset.no.data.object.selected");

	public DataSetEditorPage(String pageName) {
		super(pageName);
	}

	private void readPreferences() {
	}

	private void prepareConnection(DataSetDesign dataSetDesign) {
	}

	private DataSetDesign getDataSetDesign() {
		return dataSetDesign == null ? getInitializationDesign() : dataSetDesign;
	}

	@Override
	public void createPageCustomControl(Composite parent) {
		readPreferences();
		prepareConnection(getDataSetDesign());
		setControl(createPageControl(parent));
		initializeControl();
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

		Control control = createDBMetaDataSelectionComposite(pageContainer);

		setWidthHints(pageContainer, control);
		return pageContainer;
	}

	private void setWidthHints(Composite pageContainer, Control control) {
		int totalWidth = pageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		GridData data = (GridData)control.getLayoutData();
		data.widthHint = totalWidth;
	}

	private void initializeControl() {
		setMessage(DEFAULT_MESSAGE, IMessageProvider.NONE);
	}

	private Control createDBMetaDataSelectionComposite(Composite parent) {
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

		queries = new org.eclipse.swt.widgets.Table(queriesComposite, SWT.BORDER);
		queries.setLayoutData(controlData);

		// Structure
		Composite structureComposite = new Composite(parent, SWT.NONE);
		structureComposite.setLayout(new GridLayout());
		structureComposite.setLayoutData(compositeData);

		Label structureLabel = new Label(structureComposite, SWT.LEFT);
		structureLabel.setText(Plugin.getResourceString("tablepage.label.structure"));
		structureLabel.setLayoutData(new GridData());

		structure = new org.eclipse.swt.widgets.Tree(structureComposite, SWT.BORDER | SWT.SINGLE);
		structure.setLayoutData(controlData);

		// Content
		Composite fieldsComposite = new Composite(parent, SWT.NONE);
		fieldsComposite.setLayout(new GridLayout());
		fieldsComposite.setLayoutData(compositeData);

		Label fieldsLabel = new Label(fieldsComposite, SWT.LEFT);
		fieldsLabel.setText(Plugin.getResourceString("tablepage.label.selectedFields"));
		fieldsLabel.setLayoutData(new GridData());

		fields = new org.eclipse.swt.widgets.Table(fieldsComposite, SWT.BORDER);
		fields.setLayoutData(controlData);

		installQuerySelectionListener();
		installStructureDblClickListener();
		installFieldsDblClickListener();

		initializeTables();

		return queriesComposite;
	}

	public Image getImage(String name) {
		return JFaceResources.getImageRegistry().get(name);
	}

	private Collection<Table> getTables() throws Throwable {
		List<Table> tables = new ArrayList<Table>();

		for(Table.CLASS<? extends Table> cls : RuntimeLoader.getRuntime().tables())
			tables.add(cls.newInstance());

		Comparator<Table> comparator = new Comparator<Table>() {
			@Override
			public int compare(Table o1, Table o2) {
				return o1.displayName().compareTo(o2.displayName());
			}
		};

		Collections.sort(tables, comparator);
		return tables;
	}

	private void initializeTables() {
		try {
			for(Table table : getTables()) {
				TableItem item = new TableItem(queries, SWT.NONE);
				item.setText(table.displayName());
				item.setImage(getImage(TableIcon));
				item.setData(table);
			}
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private Collection<Table> getLinkedTables(Table table) {
		Collection<Table> tables = new ArrayList<Table>();

		for(Field.CLASS<Field> field : table.getLinks()) {
			ILink link = (ILink)field.get();
			Query.CLASS<Query> query = link.query();
			if(query != null)
				tables.add((Table)query.get());
		}

		return tables;
	}

	private void initializeTree(Table table) {
		structure.removeAll();
		fields.removeAll();
		initializeTableItem(null, table);
	}

	private boolean checkCicle(TreeItem item, Table table) {
		TreeItem parent;

		while((parent = item.getParentItem()) != null) {
			Table t = (Table)parent.getData();
			if(t != null && t.classId().equals(table.classId()))
				return false;
			item = parent;
		}

		return true;
	}

	private void initializeTableItem(TreeItem parent, Table table) {
/*
		String currentClass = getCurrentDataSetClassName();
*/
		for(Table linkedTable : getLinkedTables(table)) {
			TreeItem item = parent == null ? new TreeItem(structure, SWT.NONE) : new TreeItem(parent, SWT.NONE);
			item.setText(linkedTable.displayName() + " (" + linkedTable.index() + ")");
			item.setImage(getImage(TableIcon));
			item.setData(linkedTable);

			if(checkCicle(item, linkedTable))
				initializeTableItem(item, linkedTable);
		}

		for(Field field : table.getDataFields()) {
			if(!field.system()) {
				TreeItem fieldItem = parent == null ? new TreeItem(structure, SWT.NONE) : new TreeItem(parent, SWT.NONE);
				fieldItem.setText(field.displayName() + " (" + field.index() + ")");
				fieldItem.setImage(getImage(ColumnIcon));
				fieldItem.setData(field);
			}
/*
			if(dataSetClass.classId().equals(currentClass))
				dataSetItem.getParent().select(dataSetItem);
*/
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

	private String getDatasetInfo() {
		if(queries == null)
			return getDataSetDesign().getQueryText();

		if(queries.getSelectionCount() == 0)
			return null;

		Table query = (Table)queries.getSelection()[0].getData();

		JsonWriter writer = new JsonWriter();
		query.writeReportMeta(writer, getSelectedFields());

		return writer.toString();
	}

	@Override
	protected DataSetDesign collectDataSetDesign(DataSetDesign design) {
		design.setQueryText(getDatasetInfo());

		ResultSetDefinition resultSet = DesignFactory.eINSTANCE.createResultSetDefinition();
		ResultSetColumns columns = DesignFactory.eINSTANCE.createResultSetColumns();

		for(Field field : getSelectedFields()) {
			ColumnDefinition column = DesignFactory.eINSTANCE.createColumnDefinition();
			DataElementAttributes attributes = DesignFactory.eINSTANCE.createDataElementAttributes();
			attributes.setName(field.id());
			attributes.setUiDisplayName(field.displayName() + " (" + field.id() + ")");
			column.setAttributes(attributes);
			columns.getResultColumnDefinitions().add(column);
		}

		resultSet.setResultSetColumns(columns);
		design.setPrimaryResultSet(resultSet);

		return design;
	}

	private void installQuerySelectionListener() {
		queries.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if(canLeave()) {
							initializeTree((Table)event.item.getData());
							setMessage(DEFAULT_MESSAGE, IMessageProvider.NONE);
						} else {
							structure.removeAll();
							setMessage(ERROR_MESSAGE, IMessageProvider.ERROR);
						}

						if(getEditorContainer() != null) {
							getEditorContainer().updateButtons();
						}
					}
				});
			}
		});
	}

	private void installStructureDblClickListener() {
		structure.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(final MouseEvent event) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						Point point = new Point(event.x, event.y);
						TreeItem item = structure.getItem(point);
						if(item == null)
							return;

						Object data = item.getData();
						if(data instanceof Field) {
							Field field = (Field)data;
							TableItem fieldItem = new TableItem(fields, SWT.NONE);
							fieldItem.setText(field.displayName() + " (" + field.id() + ")");
							fieldItem.setImage(getImage(ColumnIcon));
							fieldItem.setData(field);
						}
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
		return true;
	}

	@Override
	protected void refresh(DataSetDesign dataSetDesign) {
		this.dataSetDesign = dataSetDesign;
		initializeControl();
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