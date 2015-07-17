package org.zenframework.z8.oda.designer.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.zenframework.z8.oda.designer.plugin.Plugin;

@SuppressWarnings("deprecation")
public class DataSetPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    private IntegerFieldEditor maxDisplaySchemaEditor;
    private IntegerFieldEditor maxDisplayTableEditor;
    private Button schemasPrefetchConfigCheckbox;

    public static final int DEFAULT_MAX_NUM_OF_SCHEMA = 20;

    public static final int DEFAULT_MAX_NUM_OF_TABLE_EACH_SCHEMA = 100;

    private static final int MAX_MAX_ROW = Integer.MAX_VALUE;

    public static final String ENABLED = "YES";
    public static final String DISABLED = "NO";

    public static final String USER_MAXROW = "user_maxrow";
    public static final String SCHEMAS_PREFETCH_CONFIG = "shemas_prefetch_config";
    public static final String USER_MAX_NUM_OF_SCHEMA = "user_max_num_of_schema";
    public static final String USER_MAX_NUM_OF_TABLE_EACH_SCHEMA = "user_max_num_of_table_each_schema";

    @Override
    protected Control createContents(Composite parent) {
        Composite mainComposite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);

        data.horizontalSpan = 2;
        data.verticalSpan = 6;
        // mainComposite.setLayoutData( data );
        GridLayout layout = new GridLayout();
        mainComposite.setLayout(layout);

        final Group sqlDataSetGroup = new Group(mainComposite, SWT.NONE);

        sqlDataSetGroup.setLayout(layout);
        sqlDataSetGroup
                .setText(Plugin.getResourceString("designer.preview.preference.resultset.sqldatasetpage.group.title"));
        sqlDataSetGroup.setLayoutData(data);

        sqlDataSetGroup.setEnabled(true);

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;

        Label invisibleRow = new Label(sqlDataSetGroup, SWT.NONE);
        invisibleRow.setLayoutData(data);

        schemasPrefetchConfigCheckbox = new Button(sqlDataSetGroup, SWT.CHECK);
        schemasPrefetchConfigCheckbox.setLayoutData(data);

        schemasPrefetchConfigCheckbox.setText(Plugin
                .getResourceString("designer.preview.preference.resultset.schemasPrefetchCheckbox.description"));

        initializeSchemaPrefetchConfig();

        maxDisplaySchemaEditor = new IntegerFieldEditor(USER_MAX_NUM_OF_SCHEMA, "", sqlDataSetGroup);

        Label lab = maxDisplaySchemaEditor.getLabelControl(sqlDataSetGroup);
        lab.setText(Plugin.getResourceString("designer.preview.preference.resultset.maxNoOfSchema.description"));

        maxDisplaySchemaEditor.setPage(this);
        maxDisplaySchemaEditor.setTextLimit(Integer.toString(MAX_MAX_ROW).length());

        maxDisplaySchemaEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        maxDisplaySchemaEditor.setValidRange(0, MAX_MAX_ROW);

        maxDisplaySchemaEditor.setPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if(event.getProperty().equals(FieldEditor.IS_VALID))
                    setValid(maxDisplaySchemaEditor.isValid());
            }
        });

        maxDisplaySchemaEditor.setErrorMessage(Plugin.getFormattedString(
                "designer.preview.preference.resultset.maxNoOfSchema.errormessage",
                new Object[] { new Integer(MAX_MAX_ROW) }));

        String defaultMaxSchema = Plugin.getDefault().getPluginPreferences().getString(USER_MAX_NUM_OF_SCHEMA);
        if(defaultMaxSchema == null || defaultMaxSchema.trim().length() <= 0) {
            defaultMaxSchema = String.valueOf(DEFAULT_MAX_NUM_OF_SCHEMA);
        }
        maxDisplaySchemaEditor.setStringValue(defaultMaxSchema);

        // Set up the maximum number of tables in each schema
        maxDisplayTableEditor = new IntegerFieldEditor(USER_MAX_NUM_OF_TABLE_EACH_SCHEMA, "", sqlDataSetGroup);

        lab = maxDisplayTableEditor.getLabelControl(sqlDataSetGroup);
        lab.setText(Plugin.getResourceString("designer.preview.preference.resultset.maxNoOfTable.description"));

        maxDisplayTableEditor.setPage(this);
        maxDisplayTableEditor.setTextLimit(Integer.toString(MAX_MAX_ROW).length());

        maxDisplayTableEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        maxDisplayTableEditor.setValidRange(0, MAX_MAX_ROW);

        maxDisplayTableEditor.setPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if(event.getProperty().equals(FieldEditor.IS_VALID))
                    setValid(maxDisplayTableEditor.isValid());
            }
        });

        maxDisplayTableEditor.setErrorMessage(Plugin
                .getFormattedString("designer.preview.preference.resultset.maxNoOfTable.errormessage",
                        new Object[] { new Integer(MAX_MAX_ROW) }));

        String defaultMaxTable = Plugin.getDefault().getPluginPreferences().getString(USER_MAX_NUM_OF_TABLE_EACH_SCHEMA);
        if(defaultMaxTable == null || defaultMaxTable.trim().length() <= 0) {
            defaultMaxTable = String.valueOf(DEFAULT_MAX_NUM_OF_TABLE_EACH_SCHEMA);
        }
        maxDisplayTableEditor.setStringValue(defaultMaxTable);

        return mainComposite;
    }

    private void initializeSchemaPrefetchConfig() {
        if(Plugin.getDefault().getPluginPreferences().contains(SCHEMAS_PREFETCH_CONFIG)) {
            String selection = Plugin.getDefault().getPluginPreferences().getString(SCHEMAS_PREFETCH_CONFIG);

            schemasPrefetchConfigCheckbox.setSelection(selection.equals(ENABLED) ? true : false);
        }
        else {
            Plugin.getDefault().getPluginPreferences().setValue(SCHEMAS_PREFETCH_CONFIG, ENABLED);
            schemasPrefetchConfigCheckbox.setSelection(true);
        }
    }

    @Override
    public void init(IWorkbench workbench) {}

    @Override
    protected void performDefaults() {
        maxDisplaySchemaEditor.setStringValue(String.valueOf(DEFAULT_MAX_NUM_OF_SCHEMA));
        maxDisplayTableEditor.setStringValue(String.valueOf(DEFAULT_MAX_NUM_OF_TABLE_EACH_SCHEMA));
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        Preferences preferences = Plugin.getDefault().getPluginPreferences();

        preferences.setValue(SCHEMAS_PREFETCH_CONFIG, schemasPrefetchConfigCheckbox.getSelection() ? ENABLED : DISABLED);

        preferences.setValue(USER_MAX_NUM_OF_SCHEMA, maxDisplaySchemaEditor.getStringValue());
        preferences.setValue(USER_MAX_NUM_OF_TABLE_EACH_SCHEMA, maxDisplayTableEditor.getStringValue());

        Plugin.getDefault().savePluginPreferences();

        return true;
    }

}