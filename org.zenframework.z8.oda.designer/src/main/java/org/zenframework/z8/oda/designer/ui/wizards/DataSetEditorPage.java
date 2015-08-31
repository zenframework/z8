package org.zenframework.z8.oda.designer.ui.wizards;

import java.io.File;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.design.DataSetDesign;
import org.eclipse.datatools.connectivity.oda.design.DataSetParameters;
import org.eclipse.datatools.connectivity.oda.design.DesignFactory;
import org.eclipse.datatools.connectivity.oda.design.ParameterDefinition;
import org.eclipse.datatools.connectivity.oda.design.ParameterMode;
import org.eclipse.datatools.connectivity.oda.design.ResultSetColumns;
import org.eclipse.datatools.connectivity.oda.design.ResultSetDefinition;
import org.eclipse.datatools.connectivity.oda.design.ui.designsession.DesignSessionUtil;
import org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.zenframework.z8.oda.designer.ExceptionHandler;
import org.zenframework.z8.oda.designer.plugin.Plugin;
import org.zenframework.z8.oda.driver.OdaQuery;
import org.zenframework.z8.oda.driver.RuntimeLoader;
import org.zenframework.z8.server.base.form.Desktop;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.IValue;
import org.zenframework.z8.server.runtime.CLASS;

public class DataSetEditorPage extends DataSetWizardPage {
    private static String ROOT_ICON = DataSetEditorPage.class.getName() + ".RootIcon";
    private static String DESKTOP_ICON = DataSetEditorPage.class.getName() + ".DesktopIcon";
    private static String TABLE_ICON = DataSetEditorPage.class.getName() + ".TableIcon";
    private static String VIEW_ICON = DataSetEditorPage.class.getName() + ".ViewIcon";
    private static String COLUMN_ICON = DataSetEditorPage.class.getName() + ".ColumnIcon";

    static {
        ImageRegistry registry = JFaceResources.getImageRegistry();

        registry.put(ROOT_ICON, ImageDescriptor.createFromFile(Plugin.class, "icons/data_source.gif"));
        registry.put(DESKTOP_ICON, ImageDescriptor.createFromFile(Plugin.class, "icons/desktop.gif"));
        registry.put(TABLE_ICON, ImageDescriptor.createFromFile(Plugin.class, "icons/table.gif"));
        registry.put(VIEW_ICON, ImageDescriptor.createFromFile(Plugin.class, "icons/view.gif"));
        registry.put(COLUMN_ICON, ImageDescriptor.createFromFile(Plugin.class, "icons/column.gif"));
    }

    private String dataSetClassName;
    private DataSetDesign dataSetDesign = null;
    
    private Tree tables = null;
    private Table fields = null;

    private static String DEFAULT_MESSAGE = Plugin.getResourceString("dataset.select.data.object");
    private static String ERROR_MESSAGE = Plugin.getResourceString("dataset.no.data.object.selected");

    public DataSetEditorPage(String pageName) {
        super(pageName);
    }

    private void readPreferences() {}

    private void prepareConnection(DataSetDesign dataSetDesign) {
    }

    private File getUrl() {
        File path = Plugin.getWebInfPath();

        if(path.toString().isEmpty())
            throw new RuntimeException("WEB-INF path is not set. See Preferences - Z8 Property Page");

        return path;
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
        layout.numColumns = 2;
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
        // Tables
        Composite tablesComposite = new Composite(parent, SWT.NONE);
        tablesComposite.setLayout(new GridLayout());

        GridData tablesCompositeData = new GridData(GridData.FILL_BOTH);
        tablesCompositeData.grabExcessVerticalSpace = true;
        tablesCompositeData.grabExcessHorizontalSpace = true;
        tablesComposite.setLayoutData(tablesCompositeData);

        Label tablesLabel = new Label(tablesComposite, SWT.LEFT);
        tablesLabel.setText(Plugin.getResourceString("tablepage.label.availableTables"));
        GridData tablesLabelData = new GridData();
        tablesLabel.setLayoutData(tablesLabelData);

        tables = new Tree(tablesComposite, SWT.BORDER | SWT.SINGLE);
        GridData tablesData = new GridData(GridData.FILL_BOTH);
        tablesData.grabExcessHorizontalSpace = true;
        tablesData.grabExcessVerticalSpace = true;
        tablesData.heightHint = 150;
        tables.setLayoutData(tablesData);

        // Fields
        Composite fieldsComposite = new Composite(parent, SWT.NONE);
        fieldsComposite.setLayout(new GridLayout());

        GridData fieldsCompositeData = new GridData(GridData.FILL_BOTH);
        fieldsCompositeData.grabExcessVerticalSpace = true;
        fieldsCompositeData.grabExcessHorizontalSpace = true;
        fieldsComposite.setLayoutData(tablesCompositeData);

        Label fieldsLabel = new Label(fieldsComposite, SWT.LEFT);
        fieldsLabel.setText(Plugin.getResourceString("tablepage.label.availableFields"));
        GridData fieldsLabelData = new GridData();
        fieldsLabel.setLayoutData(fieldsLabelData);

        fields = new Table(fieldsComposite, SWT.BORDER);
        GridData fieldsData = new GridData(GridData.FILL_BOTH);
        fieldsData.grabExcessHorizontalSpace = true;
        fieldsData.grabExcessVerticalSpace = true;
        fieldsData.heightHint = 150;
        fields.setLayoutData(fieldsData);

        addFetchDbObjectListener();

        initializeTree();

        return tablesComposite;
    }

    public Image getImage(String name) {
        return JFaceResources.getImageRegistry().get(name);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Desktop.CLASS[] getDesktops() throws Throwable {
        List<Desktop.CLASS> desktops = new ArrayList<Desktop.CLASS>();

        for(CLASS cls : RuntimeLoader.getRuntime(getUrl()).entries()) {
            if(cls != null && cls.instanceOf(Desktop.class))
                desktops.add((Desktop.CLASS)cls);
        }
            
        Desktop.CLASS[] result = desktops.toArray(new Desktop.CLASS[0]);

        Comparator<Desktop.CLASS> comparator = new Comparator<Desktop.CLASS>() {
            @Override
            public int compare(Desktop.CLASS o1, Desktop.CLASS o2) {
                return o1.displayName().compareTo(o2.displayName());
            }
        };

        Arrays.sort(result, comparator);
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private void initializeTree() {
        TreeItem root = new TreeItem(tables, SWT.NONE);
        root.setText(getDataSetDesign().getDataSourceDesign().getName());
        root.setImage(getImage(ROOT_ICON));

        try {
            Desktop.CLASS[] desktops = getDesktops();

            for(Desktop.CLASS desktop : desktops) {
                initializeDesktop(root, desktop);
            }
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    class DesktopTreeData {
        private CLASS<? extends Desktop> dataClass;

        DesktopTreeData(CLASS<? extends Desktop> dataClass) {
            this.dataClass = dataClass;
        }

        CLASS<? extends Desktop> get() {
            return dataClass;
        }
    }

    class DataSetTreeData {
        private CLASS<? extends Query> dataClass;

        DataSetTreeData(CLASS<? extends Query> dataClass) {
            this.dataClass = dataClass;
        }

        CLASS<? extends Query> get() {
            return dataClass;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void initializeDesktop(TreeItem parent, Desktop.CLASS desktopClass) {
        TreeItem item = new TreeItem(parent, SWT.NONE);
        item.setText(desktopClass.displayName());
        item.setImage(getImage(DESKTOP_ICON));
        item.setData(new DesktopTreeData(desktopClass));

        Desktop desktop = (Desktop)desktopClass.get();
        String currentClass = getCurrentDataSetClassName(); 
        
        
        Collection<Desktop.CLASS> desktopClasses = desktop.getSubDesktops();
        Collection<Query.CLASS> dataSetClasses = desktop.getDataSets();

        for(Desktop.CLASS subDesktopClass : desktopClasses) {
            initializeDesktop(item, subDesktopClass);
        }

        for(CLASS<? extends Query> dataSetClass : dataSetClasses) {
            TreeItem dataSetItem = new TreeItem(item, SWT.NONE);
            dataSetItem.setText(dataSetClass.displayName());
            dataSetItem.setImage(getImage(TABLE_ICON));
            dataSetItem.setData(new DataSetTreeData(dataSetClass));
            dataSetItem.setExpanded(true);
            
            if(dataSetClass.classId().equals(currentClass))
                dataSetItem.getParent().select(dataSetItem);
        }
    }

    private String getCurrentDataSetClassName() {
        String text = getDataSetDesign().getQueryText();
        return text.split(";")[0];
    }
    
    @Override
    protected DataSetDesign collectDataSetDesign(DataSetDesign design) {
        dataSetClassName = getSelectedDataSetClassName();

        design.setQueryText(dataSetClassName + ";" + getUrl());
        MetaDataRetriever retriever = new MetaDataRetriever(design);
        IResultSetMetaData resultsetMeta = retriever.getResultSetMetaData();
        IParameterMetaData paramMeta = retriever.getParameterMetaData();
        saveDataSetDesign(design, resultsetMeta, paramMeta);
        retriever.close();

        return design;
    }

    @SuppressWarnings({ "rawtypes" })
    public void saveDataSetDesign(DataSetDesign design, IResultSetMetaData meta, IParameterMetaData paramMeta) {
        try {
            if(paramMeta != null && design != null) {
                DataSetParameters dataSetParameter = DesignSessionUtil.toDataSetParametersDesign(paramMeta,
                        ParameterMode.IN_LITERAL);

                if(dataSetParameter != null) {
                    Iterator iter = dataSetParameter.getParameterDefinitions().iterator();

                    while(iter.hasNext()) {
                        ParameterDefinition defn = (ParameterDefinition)iter.next();

                        if(defn.getAttributes().getNativeDataTypeCode() == Types.NULL) {
                            defn.getAttributes().setNativeDataTypeCode(Types.CHAR);
                        }
                    }
                }

                design.setParameters(dataSetParameter);
            }

            ResultSetColumns columns = DesignSessionUtil.toResultSetColumnsDesign(meta);

            if(columns != null) {
                ResultSetDefinition resultSetDefn = DesignFactory.eINSTANCE.createResultSetDefinition();
                resultSetDefn.setResultSetColumns(columns);
                design.setPrimaryResultSet(resultSetDefn);
                design.getResultSets().setDerivedMetaData(true);
            }
            else {
                design.setResultSets(null);
            }
        }
        catch(OdaException e) {
            design.setResultSets(null);
        }
    }

    private void addFetchDbObjectListener() {
        tables.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if(canLeave()) {
                            initFields((TreeItem)event.item);
                            setMessage(DEFAULT_MESSAGE, IMessageProvider.NONE);
                        }
                        else {
                            fields.removeAll();
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

    private void initFields(TreeItem item) {
        try {
            fields.removeAll();

            Object data = item.getData();

            if(data != null && data instanceof DataSetTreeData) {
                IValue[] queryFields = OdaQuery.getColumns(((DataSetTreeData)data).get().get());

                if(queryFields != null) {
                    for(IValue field : queryFields) {
                        String name = field.displayName();

                        if(name != null) {
                            TableItem root = new TableItem(fields, SWT.NONE);
                            root.setText(name);
                            root.setImage(getImage(COLUMN_ICON));
                            root.setData(field);
                        }
                    }
                }
            }
        }
        catch(Throwable e) {
            ExceptionHandler.showException(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                    Plugin.getResourceString("exceptionHandler.title.error"), e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected boolean canLeave() {
        return !getSelectedDataSetClassName().isEmpty(); //getSelection() instanceof DataSetTreeData;
    }

    private Object getSelection() {
        TreeItem[] selection = tables != null ? tables.getSelection() : null;

        if(selection != null && selection.length > 0) {
            return selection[0].getData();
        }

        return null;
    }

    private String getSelectedDataSetClassName() {
        Object data = getSelection();

        if(data instanceof DataSetTreeData) {
            return ((DataSetTreeData)data).get().classId();
        }

        return getCurrentDataSetClassName();
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