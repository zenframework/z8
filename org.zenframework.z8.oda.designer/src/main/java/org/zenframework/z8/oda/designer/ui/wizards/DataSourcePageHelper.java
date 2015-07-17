package org.zenframework.z8.oda.designer.ui.wizards;

import java.util.Properties;

import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.zenframework.z8.oda.designer.ExceptionHandler;
import org.zenframework.z8.oda.designer.plugin.Plugin;
import org.zenframework.z8.oda.driver.Constants;
import org.zenframework.z8.oda.driver.connection.Connection;

public class DataSourcePageHelper {
    private WizardPage wizardPage;
    private PreferencePage propertyPage;

    private static final String EMPTY_STRING = "";

    private Text url, userName, password;

    private Button testButton;

    private String DEFAULT_MESSAGE;

    final private static String EMPTY_URL = Plugin.getResourceString("error.emptyDatabaseUrl");

    DataSourcePageHelper(WizardPage page) {
        DEFAULT_MESSAGE = Plugin.getResourceString("wizard.message.createDataSource");
        wizardPage = page;
    }

    DataSourcePageHelper(PreferencePage page) {
        DEFAULT_MESSAGE = Plugin.getResourceString("wizard.message.editDataSource");
        propertyPage = page;
    }

    void createCustomControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.verticalSpacing = 10;
        layout.marginBottom = 10;
        content.setLayout(layout);

        GridData gridData;

        new Label(content, SWT.RIGHT).setText(Plugin.getResourceString("wizard.label.url"));

        gridData = new GridData();
        gridData.horizontalSpan = 1;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        url = new Text(content, SWT.BORDER | SWT.READ_ONLY);
        url.setLayoutData(gridData);

        new Label(content, SWT.RIGHT).setText(Plugin.getResourceString("wizard.label.username"));
        gridData = new GridData();
        gridData.horizontalSpan = 1;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        userName = new Text(content, SWT.BORDER);
        userName.setLayoutData(gridData);

        new Label(content, SWT.RIGHT).setText(Plugin.getResourceString("wizard.label.password"));

        gridData = new GridData();
        gridData.horizontalSpan = 1;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        password = new Text(content, SWT.BORDER | SWT.PASSWORD);
        password.setLayoutData(gridData);

        new Label(content, SWT.NONE);

        testButton = new Button(content, SWT.PUSH);
        testButton.setText(Plugin.getResourceString("wizard.label.testConnection"));
        testButton.setLayoutData(new GridData(GridData.CENTER));

        addControlListeners();
        updateTestButton();
        verifyProperties();
    }

    void initCustomControl(Properties profileProps) {
        String odaUrl = DataSetEditorPage.getUrl();

        if(odaUrl == null)
            odaUrl = EMPTY_STRING;

        url.setText(odaUrl);

        String odaUser = profileProps != null ? profileProps.getProperty(Constants.User) : EMPTY_STRING;

        if(odaUser == null)
            odaUser = EMPTY_STRING;

        userName.setText(odaUser);

        String odaPassword = profileProps != null ? profileProps.getProperty(Constants.Password) : EMPTY_STRING;

        if(odaPassword == null)
            odaPassword = EMPTY_STRING;

        password.setText(odaPassword);

        updateTestButton();
        verifyProperties();
    }

    Properties collectCustomProperties(Properties props) {
        if(props == null)
            props = new Properties();

        props.setProperty(Constants.User, getUser());
        props.setProperty(Constants.Password, getPassword());
        return props;
    }

    private String getUser() {
        if(userName == null)
            return EMPTY_STRING;
        return getTrimedString(userName.getText());
    }

    private String getPassword() {
        if(password == null)
            return EMPTY_STRING;
        return getTrimedString(password.getText());
    }

    private String getURL() {
        if(url == null)
            return EMPTY_STRING;
        return getTrimedString(url.getText());
    }

    private String getTrimedString(String tobeTrimed) {
        if(tobeTrimed != null)
            tobeTrimed = tobeTrimed.trim();
        return tobeTrimed;
    }

    private void addControlListeners() {
        url.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if(!url.isFocusControl() && url.getText().trim().length() == 0) {
                    return;
                }
                verifyProperties();
                updateTestButton();
            }
        });

        testButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                testButton.setEnabled(false);
                try {
                    if(testConnection()) {
                        MessageDialog.openInformation(getShell(), Plugin.getResourceString("connection.test"),
                                Plugin.getResourceString("connection.success"));
                    }
                    else {
                        OdaException ex = new OdaException(Plugin.getResourceString("connection.failed"));
                        ExceptionHandler.showException(getShell(), Plugin.getResourceString("connection.test"),
                                Plugin.getResourceString("connection.failed"), ex);
                    }
                }
                catch(Exception e1) {
                    ExceptionHandler.showException(getShell(), Plugin.getResourceString("connection.test"), e1.getMessage(),
                            e1);
                }
                testButton.setEnabled(true);
            }

        });

    }

    private boolean testConnection() throws OdaException {
        if(isURLBlank()) {
            return false;
        }

        Connection connection = null;

        try {
            connection = Connection.connect(getURL(), getUser(), getPassword());
        }
        catch(Throwable e) {
            throw new OdaException(e);
        }
        finally {
            if(connection != null) {
                Connection.disconnect(connection);
            }
        }

        return true;
    }

    private boolean isURLBlank() {
        return url == null || url.getText().trim().length() == 0;
    }

    private void updateTestButton() {
        if(isURLBlank()) {
            setMessage(EMPTY_URL, IMessageProvider.ERROR);
            testButton.setEnabled(false);
        }
        else {
            setMessage(DEFAULT_MESSAGE);
            if(!testButton.isEnabled())
                testButton.setEnabled(true);
        }
    }

    protected void resetTestButton() {
        if(isURLBlank()) {
            setMessage(EMPTY_URL, IMessageProvider.ERROR);
            testButton.setEnabled(false);
        }
        else {
            setMessage(DEFAULT_MESSAGE);
            if(!testButton.isEnabled())
                testButton.setEnabled(true);
        }

        enableParent(testButton);
    }

    private void enableParent(Control control) {
        Composite parent = control.getParent();
        if(parent == null || parent instanceof Shell) {
            return;
        }
        if(!parent.isEnabled()) {
            parent.setEnabled(true);
        }
        enableParent(parent);
    }

    private void verifyProperties() {
        setPageComplete(!isURLBlank());
    }

    private Shell getShell() {
        if(wizardPage != null)
            return wizardPage.getShell();
        else if(propertyPage != null)
            return propertyPage.getShell();
        else
            return null;
    }

    private void setPageComplete(boolean complete) {
        if(wizardPage != null)
            wizardPage.setPageComplete(complete);
        else if(propertyPage != null)
            propertyPage.setValid(complete);
    }

    private void setMessage(String message) {
        if(wizardPage != null)
            wizardPage.setMessage(message);
        else if(propertyPage != null)
            propertyPage.setMessage(message);
    }

    private void setMessage(String message, int type) {
        if(wizardPage != null)
            wizardPage.setMessage(message, type);
        else if(propertyPage != null)
            propertyPage.setMessage(message, type);
    }

    public void setDefaultMessage(String message) {
        this.DEFAULT_MESSAGE = message;
    }
}
