package org.zenframework.z8.pde.debug.launch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.Z8ProjectNature;
import org.zenframework.z8.pde.build.JavaBuilder;
import org.zenframework.z8.pde.build.JavaBuilderException;
import org.zenframework.z8.pde.build.RepositoryLocalChangesException;
import org.zenframework.z8.pde.build.Z8ProjectBuilder;
import org.zenframework.z8.pde.debug.model.JDXDebugTarget;
import org.zenframework.z8.pde.preferences.PreferencePage;
import org.zenframework.z8.pde.preferences.PreferencePageConsts;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;

public class LaunchDelegate extends LaunchConfigurationDelegate {
	private boolean m_debugMode;
	private static String m_memoryBound;
	private static String m_tomcatHome;
	private boolean m_useJar;
	private boolean m_tomcatSecurity;
	private IProject m_project;

	private PreferencePage m_preferences;

	private int messageDialogResult;

	private static String Java_exe_path = null;

	public LaunchDelegate() {
		m_preferences = new PreferencePage();
	}

	protected static void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, Plugin.getUniqueIdentifier(), code, message, exception));
	}

	private void validateLaunch(ILaunchConfiguration configuration) throws CoreException {
		validateProject(configuration);
		validateTomcat(configuration);
		validateProjectXML(configuration);
	}

	private void validateProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(LaunchConstants.ID_PROJECT, (String)null);

		if(projectName == null || projectName.length() == 0) {
			abort("This launch configuration does not refer to a startup project", null, 1);
		}

		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(projectName);

		if(resource == null || !(resource instanceof IProject)) {
			abort("This launch configuration is referencing to non-existing project " + projectName, null, 2);
		}

		// определяем проект заданный в конфигурации запуска
		IProject project = (IProject)resource;

		if(!project.exists()) {
			abort("This launch configuration is referencing to non-existing project " + projectName, null, 2);
		}

		if(!project.isOpen()) {
			abort("This launch configuration is referencing to closed project " + projectName, null, 2);
		}

		if(!project.hasNature(Z8ProjectNature.Id)) {
			abort("This launch configuration is referencing to non-Z8 Project " + projectName, null, 2);
		}

		m_project = project;
	}

	protected void validateTomcat(ILaunchConfiguration configuration) throws CoreException {
		m_tomcatHome = m_preferences.getAttribute(PreferencePageConsts.ATTR_TOMCAT_HOME, "");
		if(m_tomcatHome.length() == 0) {
			abort("'Tomcat home' parameter is not set. It can be found on Z8 section in Preferences!", null, 2);
		}

		validateWebInf(configuration);
		final IPath path = new Path(m_tomcatHome).append("conf").append("Catalina").append("localhost").append(".xml");
		final File context_file = new File(path.toOSString());
		String result = "";
		PreferencePage page = new PreferencePage();
		String webInfPath = page.getAttribute(PreferencePageConsts.ATTR_WEB_INF_PATH, "");
		IPath _webInfPath = new Path(webInfPath);
		IPath _docBasePath = _webInfPath.removeLastSegments(1);
		IPath _workDirPath = _webInfPath.append("work");
		String docBasePath = _docBasePath.toOSString();
		String workDirPath = _workDirPath.toOSString();

		try {
			FileReader fr = new FileReader(context_file);
			char[] chars = new char[1024];
			while(fr.ready()) {
				int read = fr.read(chars);
				if(read == -1)
					break;
				result += new String(chars, 0, read);
			}
			fr.close();

			int index = result.indexOf("<Context path=\"\"");
			if(index == -1)
				throw new Exception("String <Context path=\"\" not found in the context file (" + path.toOSString() + ")!");
			int docBaseIndex = result.indexOf("docBase=", index);
			if(docBaseIndex == -1)
				throw new Exception("String docBase not found in the context file (" + path.toOSString() + ")!");
			int workDirIndex = result.indexOf("workDir=", index);
			if(workDirIndex == -1)
				throw new Exception("String workDir not found in the context file (" + path.toOSString() + ")!");
			int docBaseQuote1, docBaseQuote2, workDirQuote1, workDirQuote2;
			docBaseQuote1 = result.indexOf('"', docBaseIndex) + 1;
			docBaseQuote2 = result.indexOf('"', docBaseQuote1);
			workDirQuote1 = result.indexOf('"', workDirIndex) + 1;
			workDirQuote2 = result.indexOf('"', workDirQuote1);

			String docBaseOldPath = result.substring(docBaseQuote1, docBaseQuote2);
			String workDirOldPath = result.substring(workDirQuote1, workDirQuote2);

			if(!docBaseOldPath.equals(docBasePath) || !workDirOldPath.equals(workDirPath)) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), "Путь к серверу приложений", null, "Неправильный путь к серверу приложений в настройках tomcat.\r\nИсправить?", MessageDialog.QUESTION, new String[] { "Да", "Нет" }, 0);
						messageDialogResult = dialog.open();
					}

				});

				if(messageDialogResult != 0)
					abort("Неправильный путь к серверу приложений в настройках tomcat.", null, 2);
				else {
					if(docBaseQuote1 > workDirQuote1)
						result = result.substring(0, workDirQuote1) + workDirPath + result.substring(workDirQuote2, docBaseQuote1) + docBasePath + result.substring(docBaseQuote2);
					else
						result = result.substring(0, docBaseQuote1) + docBasePath + result.substring(docBaseQuote2, workDirQuote1) + workDirPath + result.substring(workDirQuote2);

					FileWriter fw = new FileWriter(context_file);
					fw.write(result);
					fw.close();
				}
			}
		} catch(CoreException e) {
			throw e;
		} catch(FileNotFoundException e1) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), "Отсутствует файл контекста tomcat", null, "Для запуска необходимо иметь созданным файл контекста tomcat:\r\n" + path.toOSString() + "\r\nСоздать его?", MessageDialog.QUESTION,
							new String[] { "Да", "Нет" }, 0);
					messageDialogResult = dialog.open();
				}

			});

			if(messageDialogResult != 0)
				abort("Запуск невозможен без файла контекста tomcat.", null, 2);
			else {
				FileWriter fw;
				result = "<Context path=\"\" reloadable=\"false\" docBase=\"" + docBasePath + "\" workDir=\"" + workDirPath + "\" />";
				try {
					fw = new FileWriter(context_file);
					fw.write(result);
					fw.close();
				} catch(IOException e) {
					e.printStackTrace();
				}

			}
		} catch(Exception e2) {
			Plugin.log(e2);
			abort("Ошибка чтения, записи или разбора файла контекста tomcat", e2, 2);
		}

	}

	protected void validateProjectXML(ILaunchConfiguration configuration) throws CoreException {
		String webinf = m_preferences.getAttribute(PreferencePageConsts.ATTR_WEB_INF_PATH, "");
		IPath p = new Path(webinf);
		p = p.append("project.xml");
		final String xMLpath = p.toOSString();

		ProjectXMLTab tab = new ProjectXMLTab();
		try {
			tab.import_config(xMLpath);
			List<String> keys = new ArrayList<String>();
			List<String> values = new ArrayList<String>();
			List<String> comments = new ArrayList<String>();
			List<String> enabled = new ArrayList<String>();
			List<String> EMPTY = new ArrayList<String>();

			keys.addAll(configuration.getAttribute(ProjectXMLTab.PROJECT_XML_KEYS, EMPTY));
			values.addAll(configuration.getAttribute(ProjectXMLTab.PROJECT_XML_VALUES, EMPTY));
			comments.addAll(configuration.getAttribute(ProjectXMLTab.PROJECT_XML_COMMENTS, EMPTY));
			enabled.addAll(configuration.getAttribute(ProjectXMLTab.PROJECT_XML_ENABLED, EMPTY));

			if(!(keys.equals(tab.keys) && values.equals(tab.values) && comments.equals(tab.comments) && enabled.equals(tab.enabled))) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), "Настройки project.xml", null, "Не совпадают настройки project.xml.\r\nИсправить?\r\nНастройки из конфигурации будут экспортированы в файл " + xMLpath, MessageDialog.QUESTION,
								new String[] { "Да", "Нет" }, 0);
						messageDialogResult = dialog.open();
					}

				});

				if(messageDialogResult != 0)
					abort("Не совпадают настройки project.xml.", null, 2);
				else {
					tab.keys = keys;
					tab.values = values;
					tab.comments = comments;
					tab.enabled = enabled;
					String result = tab.export();
					FileWriter fw = new FileWriter(xMLpath);
					fw.write(result);
					fw.close();
				}
			}
			if(tab.keys.size() == 0) {
				abort("Отсутствуют настройки project.xml.", null, 2);
			}
		}

		catch(CoreException e) {
			throw e;
		} catch(Exception e) {
			Plugin.log(e);
			// abort("Ошибка при чтении, записи или разборе файла project.xml",
			// null, 2);
		}

	}

	protected boolean validateDataSchema(ILaunchConfiguration configuration) {
		return true;
	}

	protected void validateWebInf(ILaunchConfiguration configuration) throws CoreException {
		String webinf = m_preferences.getAttribute(PreferencePageConsts.ATTR_WEB_INF_PATH, "");

		if(webinf.length() == 0) {
			abort("'WEB-INF path' parameter is not set. It can be found on the Z8 Preferences page", null, 2);
		}
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		String launchProjectName = configuration.getAttribute(LaunchConstants.ID_PROJECT, "");
		IProject launchProject = (IProject)ResourcesPlugin.getWorkspace().getRoot().findMember(launchProjectName);

		monitor.beginTask("Launching project '" + launchProjectName + "' ...", 4);
		try {
			determineVM();

			if(validateDataSchema(configuration) && canLaunch(launchProject, monitor)) {
				monitor.subTask("Shutting down previous launched processes...");
				Plugin.getDefault().shutdownAllLaunched();

				monitor.worked(1);
				if(monitor.isCanceled())
					return;

				monitor.subTask("Running process...");

				if(m_debugMode) {
					VirtualMachine vm = debug(m_memoryBound, m_tomcatHome);

					try {
						IDebugTarget target = newDebugTarget(launch, vm, "Z8 Debug Target", true, true, true);
						launch.addDebugTarget(target);
					} catch(CoreException e) {
						vm.dispose();
					}
				} else {
					Process tomcat_process = tomcat_exec(m_memoryBound, m_tomcatHome, enStartStopAction.actionStart, m_tomcatSecurity);

					if(tomcat_process != null) {
						newProcess(launch, tomcat_process, "Z8 Run Target", null);
					}
				}

				monitor.worked(1);
			} else
				Z8ProjectBuilder.focusOnProblemsView();
		} finally {
			monitor.done();
		}
	}

	private static void determineVM() {

		IVMInstall vmDefault = JavaRuntime.getDefaultVMInstall();
		File vmLocation = vmDefault.getInstallLocation();
		try {
			Java_exe_path = new File(vmLocation, "\\bin\\java.exe").getCanonicalPath();
		} catch(IOException e) {
			Plugin.log(e);
		}
	}

	public IDebugTarget newDebugTarget(final ILaunch launch, final VirtualMachine vm, final String name, final boolean allowTerminate, final boolean allowDisconnect, final boolean resume) throws CoreException {
		final JDXDebugTarget[] target = new JDXDebugTarget[1];

		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor m) {
				IProcess process = newProcess(launch, vm.process(), "Z8 Process", null);
				target[0] = new JDXDebugTarget(launch, vm, name, allowTerminate, allowDisconnect, process, resume);
			}
		};

		ResourcesPlugin.getWorkspace().run(r, null, 0, null);

		return target[0];
	}

	protected static IProcess newProcess(ILaunch launch, Process process, String label, Map<String, String> attributes) {
		return DebugPlugin.newProcess(launch, process, label, attributes);
	}

	private VirtualMachine debug(String memoryBound, String classPath) throws CoreException {
		LaunchingConnector connector = findLaunchingConnector();

		Map<String, Connector.Argument> arguments = connectorArguments(connector, memoryBound, classPath, true, m_tomcatSecurity);

		try {
			return connector.launch(arguments);
		} catch(Throwable e) {
			abort("Unable to launch target VM", e, 0);
		}

		return null;
	}

	private static Process tomcat_exec(String memoryBound, String classPath, enStartStopAction action, boolean withSecuriy) throws CoreException {
		try {
			return Runtime.getRuntime().exec(tomcatExecArguments(memoryBound, classPath, action, withSecuriy));
		} catch(Throwable e) {
			abort("Unable to launch target VM", e, 0);
		}
		return null;
	}

	/*
	 * private static Process rmi_exec(String classPath, long port) throws
	 * CoreException { List<String> envir = new ArrayList<String>();
	 * envir.add("classpath=" + classPath); try { return
	 * Runtime.getRuntime().exec(rmiExecArguments(port)); // ,envir.toArray(new
	 * String[envir.size()]) } catch(Throwable e) {
	 * abort("Unable to launch rmiregistry", e, 0); } return null; }
	 */
	private LaunchingConnector findLaunchingConnector() throws CoreException {
		VirtualMachineManager vmManager = Bootstrap.virtualMachineManager();
		List<Connector> connectors = vmManager.allConnectors();

		Iterator<Connector> iter = connectors.iterator();

		while(iter.hasNext()) {
			Connector connector = iter.next();

			if(connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
				return (LaunchingConnector)connector;
			}
		}

		abort("Unable to launch target VM : no launching connector found", null, 0);
		return null;
	}

	private boolean checkProjectsOnErrors(Project[] depends) {
		boolean ret = false;
		if(depends.length > 0)
			for(Project proj : depends)
				ret = ret || proj.containsError() || checkProjectsOnErrors(proj.getReferencedProjects());
		return ret;
	}

	private boolean canLaunch(IProject launchedProject, IProgressMonitor progressmonitor) throws CoreException {
		boolean ret = false;
		Project project = Workspace.getInstance().getProject(launchedProject);
		progressmonitor.subTask("Checking on errors...");

		ret = !(project.containsError() || checkProjectsOnErrors(project.getReferencedProjects()));
		progressmonitor.worked(1);
		return ret;
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		// validateLaunch(configuration);

		m_debugMode = mode.equals(ILaunchManager.DEBUG_MODE);
		m_memoryBound = m_preferences.getAttribute(PreferencePageConsts.ATTR_TOMCAT_MEMORY_BOUND, 256).toString();
		m_useJar = configuration.getAttribute(LaunchConstants.ID_CREATE_USE_JAR, false);
		m_tomcatSecurity = configuration.getAttribute(LaunchConstants.ID_RUN_TOMCAT_WITH_SECURITY, false);

		return super.buildForLaunch(configuration, mode, monitor);
	}

	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		validateLaunch(configuration);

		assert (m_project != null);

		Project project = Workspace.getInstance().getProject(m_project);

		Project[] projects = project.getReferencedProjects();

		IProject[] target = new IProject[projects.length + 1];

		for(int i = 0; i < projects.length; i++) {
			target[i] = (IProject)projects[i].getResource();
		}

		target[target.length - 1] = m_project;

		return target;
	}

	@Override
	protected void buildProjects(IProject[] projects, IProgressMonitor monitor) throws CoreException {

		boolean hasErrors = false;

		for(IProject iProject : projects) {
			iProject.setSessionProperty(Z8ProjectBuilder.PrelaunchBuild, new Object());
			iProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
			iProject.setSessionProperty(Z8ProjectBuilder.PrelaunchBuild, null);

			Project project = Workspace.getInstance().getProject(iProject);

			hasErrors |= project.containsError();
		}

		if(!hasErrors) {
			try {
				JavaBuilder.run(m_project, m_debugMode, m_memoryBound, m_useJar);
			} catch(IOException e) {
				abort("Java build failed", e, 0);
			} catch(JavaBuilderException e) {
				abort("Java build failed", e, 0);
			} catch(RepositoryLocalChangesException e) {
				// не станем запускать при согласии пользователя, т.к. есть не
				// скинутые в репозитарий локальные изменения!
				throw new CoreException(new Status(IStatus.INFO, Plugin.getUniqueIdentifier(), 0, e.getMessage(), e));
			}

		}

		monitor.done();
	}

	Map<String, Connector.Argument> connectorArguments(LaunchingConnector connector, String memoryBound, String classPath, boolean startOrStop, boolean withSecurity) {
		Map<String, Connector.Argument> arguments = connector.defaultArguments();

		Connector.Argument main = arguments.get("main");
		Connector.Argument options = arguments.get("options");

		assert (main != null && options != null);

		main.setValue("org.apache.catalina.startup.Bootstrap -config \"" + classPath + "/conf/server.xml\" " + (startOrStop ? "start" : "stop"));

		String optionsValue = "-Xmx" + memoryBound + "m " + "-classpath \"" + classPath + "/bin/bootstrap.jar;" + classPath + "/bin/tomcat-juli.jar;" + classPath + "\" " + "-Djava.endorsed.dirs=\"" + classPath + "/common/endorsed\" " + "-Dcatalina.base=\"" + classPath + "\" "
				+ "-Dcatalina.home=\"" + classPath + "\"" + (withSecurity ? " -Djava.security.manager -Djava.security.policy=\"" + classPath + "/conf/catalina.policy\"" : "");
		options.setValue(optionsValue);
		return arguments;
	}

	public static String[] tomcatExecArguments(String memoryBound, String classPath, enStartStopAction action, boolean withSecurity) {
		List<String> args = new ArrayList<String>();

		args.add(Java_exe_path);
		args.add("-Xmx" + memoryBound + "m");
		args.add("-classpath");
		args.add("\"" + classPath + "/bin/bootstrap.jar;" + classPath + "/bin/tomcat-juli.jar;" + classPath + "\"");
		args.add("-Djava.endorsed.dirs=\"" + classPath + "/common/endorsed\"");
		args.add("-Dcatalina.base=\"" + classPath + "\"");
		args.add("-Dcatalina.home=\"" + classPath + "\"");
		if(withSecurity) {
			args.add("-Djava.security.manager");
			args.add("-Djava.security.policy=\"" + classPath + "/conf/catalina.policy\"");
		}
		args.add("org.apache.catalina.startup.Bootstrap");
		args.add("-config");
		args.add("\"" + classPath + "/conf/server.xml\"");
		switch(action) {
		case actionStart:
			args.add("start");
			break;
		case actionStop:
			args.add("stop");
			break;
		}

		return args.toArray(new String[args.size()]);
	}

	public enum enStartStopAction {
		actionStart,
		actionStop
	}
}
