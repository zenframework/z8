package org.zenframework.z8.pde.debug.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.preferences.PreferencePage;
import org.zenframework.z8.pde.preferences.PreferencePageConsts;

public class LaunchMainTab extends AbstractLaunchConfigurationTab {
	private Label lbl_tomcatpath_warning;
	private Label lbl_project;
	private Button btn_project;
	private Text m_project;

	private Button btn_checkButton_CreateAndUseJarFile;
	private Button btn_checkButton_RunTomcatWithSecurity;

	@Override
	public void createControl(Composite parent) {
		GridData gd = null;
		Font font = parent.getFont();
		final Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout(3, false);
		topLayout.marginTop = 10;
		comp.setLayout(topLayout);
		comp.setFont(font);

		gd = new GridData(GridData.BEGINNING);
		lbl_project = new Label(comp, SWT.NONE);
		lbl_project.setText("&Project:");
		lbl_project.setLayoutData(gd);
		lbl_project.setFont(font);
		m_project = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);

		m_project.setLayoutData(gd);
		m_project.setFont(font);
		m_project.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		btn_project = createPushButton(comp, "Browse", null);
		btn_project.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ListDialog dialog = new ListDialog(comp.getShell());
				dialog.setContentProvider(new IStructuredContentProvider() {
					@Override
					public Object[] getElements(Object inputElement) {
						return (Object[])inputElement;
					}

					@Override
					public void dispose() {
					}

					@Override
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}
				});

				dialog.setLabelProvider(new LabelProvider() {
					@Override
					public String getText(Object element) {
						return element == null ? "" : ((IProject)element).getName();
					}

				});

				IProject[] projects = getProjects();

				dialog.setInput(projects);
				dialog.setTitle("Select a project");
				dialog.setMessage("Select a project");

				dialog.setInitialSelections(new Object[] { getLaunchProject() });

				if(dialog.open() != Dialog.CANCEL) {
					Object[] result = dialog.getResult();

					if(result.length == 1) {
						m_project.setText(((IProject)result[0]).getName());
					}
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		lbl_tomcatpath_warning = new Label(comp, SWT.WRAP);
		lbl_tomcatpath_warning.setText("* for properly run set the \"Tomcat home\" path parameter in a appropriate Preference Page !");
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 3;
		lbl_tomcatpath_warning.setLayoutData(gd);
		lbl_tomcatpath_warning.setFont(font);
		lbl_tomcatpath_warning.setForeground(new Color(null, 250, 20, 20));
		if(new PreferencePage().getAttribute(PreferencePageConsts.ATTR_TOMCAT_HOME, "") == "")
			lbl_tomcatpath_warning.setVisible(true);
		else
			lbl_tomcatpath_warning.setVisible(false);

		gd = new GridData(GridData.BEGINNING); // �������� ��� 3-� ������
		new Label(comp, SWT.NONE).setLayoutData(gd);

		// ATTR_CREATE_USE_JAR
		btn_checkButton_CreateAndUseJarFile = new Button(comp, SWT.CHECK); // $NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		btn_checkButton_CreateAndUseJarFile.setLayoutData(gd);
		btn_checkButton_CreateAndUseJarFile.setText("&Create and use jar file with version info");
		btn_checkButton_CreateAndUseJarFile.setToolTipText("when this option switched on launching goes with creating one executable file with jar extension that has version information, but it extends launching time period on slower computer !");
		btn_checkButton_CreateAndUseJarFile.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		// ATTR_RUN_TOMCAT_WITH_SECURITY
		btn_checkButton_RunTomcatWithSecurity = new Button(comp, SWT.CHECK); // $NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		btn_checkButton_RunTomcatWithSecurity.setLayoutData(gd);
		btn_checkButton_RunTomcatWithSecurity.setText("&Run tomcat with security manager");
		btn_checkButton_RunTomcatWithSecurity.setToolTipText("needs to run executable files properly from tomcat java machine. This option uses file conf\\catalina.policy in Tomcat home dir.");
		btn_checkButton_RunTomcatWithSecurity.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			btn_checkButton_CreateAndUseJarFile.setSelection(configuration.getAttribute(LaunchConstants.ID_CREATE_USE_JAR, false));
			btn_checkButton_RunTomcatWithSecurity.setSelection(configuration.getAttribute(LaunchConstants.ID_RUN_TOMCAT_WITH_SECURITY, false));

			initializeProject(configuration);
		} catch(CoreException e) {
			setErrorMessage(e.getMessage());
		}
	}

	private void initializeProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(LaunchConstants.ID_PROJECT, "");
		m_project.setText(projectName);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConstants.ID_PROJECT, m_project.getText());

		configuration.setAttribute(LaunchConstants.ID_CREATE_USE_JAR, btn_checkButton_CreateAndUseJarFile.getSelection());
		configuration.setAttribute(LaunchConstants.ID_RUN_TOMCAT_WITH_SECURITY, btn_checkButton_RunTomcatWithSecurity.getSelection());
	}

	@Override
	public String getName() {
		return "Main";
	}

	protected String browseDirectory() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		// dialog.setText("Choose a directory");
		return dialog.open();
	}

	protected String browseFile() {
		FileDialog dialog = new FileDialog(getShell());
		// dialog.setText("Choose a file");
		dialog.setFilterExtensions(new String[] { "*.exe" });
		return dialog.open();
	}

	private boolean validateProject() {
		String projectName = m_project.getText();

		if(projectName.length() == 0) {
			setErrorMessage("Select a project");
			return false;
		}

		IProject[] projects = getProjects();

		for(IProject project : projects) {
			if(project.getName().equals(projectName)) {
				return true;
			}
		}

		setErrorMessage("Project " + projectName + " does not exist or is not open");
		return false;
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		super.isValid(config);
		setMessage(null);
		setErrorMessage(null);
		return validateProject();
	}

	private IProject[] getProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		List<IProject> result = new ArrayList<IProject>();

		for(IProject project : projects) {
			if(Plugin.projectExists(project)) {
				result.add(project);
			}
		}
		return result.toArray(new IProject[result.size()]);
	}

	public IProject getLaunchProject() {
		return Plugin.findProject(m_project.getText());
	}

	public String getLaunchProjectName() {
		IProject project = getLaunchProject();
		return project != null ? project.getName() : null;
	}
}
