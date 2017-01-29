package org.zenframework.z8.pde;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.zenframework.z8.pde.debug.model.JDXDebugOptionsManager;
import org.zenframework.z8.pde.editor.document.DocScanner;

public class Plugin extends AbstractUIPlugin {
	static final public String PLUGIN_ID = "org.zenframework.z8.pde";

	static private Plugin m_instance;
	static private ColorProvider fColorProvider;

	private boolean fShuttingDown = false;

	private ResourceBundle m_resourceBundle;
	private ImageDescriptorRegistry m_imageDescriptorRegistry;

	static private Map<MessageConsole, IOConsoleOutputStream> console = new HashMap<MessageConsole, IOConsoleOutputStream>();

	private DocScanner m_docScanner;

	private String m_userName = "";
	private String m_userPassword = "";

	private static boolean m_userLoggedOn = false;

	public boolean isUserLoggedOn() {
		return m_userLoggedOn;
	}

	public void setUserLoggedOn(boolean loggedOn) {
		m_userLoggedOn = loggedOn;
	}

	public String getUserName() {
		return m_userName;
	}

	public void setUserName(String name) {
		m_userName = name;
	}

	public String getUserPassword() {
		return m_userPassword;
	}

	public void setUserPassword(String password) {
		m_userPassword = password;
	}

	public Plugin() {
		super();

		m_instance = this;

		try {
			m_resourceBundle = ResourceBundle.getBundle("org.zenframework.z8.pde.PluginResources");

		} catch(MissingResourceException x) {
			m_resourceBundle = null;
		}

		WorkspaceInitializer.run();

		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {

			@Override
			public void resourceChanged(IResourceChangeEvent arg0) {
				try {
					int type = arg0.getType();
					if(type == IResourceChangeEvent.PRE_CLOSE || type == IResourceChangeEvent.PRE_DELETE) {
						IResource res = arg0.getResource();
						if(res instanceof IProject) {
							IProject project = (IProject)res;
							if(!project.hasNature(Z8ProjectNature.Id))
								return;
							for(IWorkbenchWindow win : PlatformUI.getWorkbench().getWorkbenchWindows()) {
								for(final IWorkbenchPage page : win.getPages()) {
									for(IEditorReference ref : page.getEditorReferences()) {
										IEditorPart part = ref.getEditor(false);
										if(part instanceof MyMultiEditor) {
											final MyMultiEditor editor = (MyMultiEditor)part;
											if(((FileEditorInput)editor.getEditorInput()).getFile().getProject().equals(project)) {
												Display.getDefault().asyncExec(new Runnable() {

													@Override
													public void run() {
														page.closeEditor(editor, true);

													}

												});
											}
										}
									}
								}
							}
						}
					}
				} catch(Exception e) {
					Plugin.log(e);
				}
			}

		});
	}

	public static boolean getPreferenceBoolean(String name) {
		return getDefault().getPreferenceStore().getBoolean(name);
	}

	public static double getPreferenceDouble(String name) {
		return getDefault().getPreferenceStore().getDouble(name);
	}

	public static float getPreferenceFloat(String name) {
		return getDefault().getPreferenceStore().getFloat(name);
	}

	public static int getPreferenceInt(String name) {
		return getDefault().getPreferenceStore().getInt(name);
	}

	public static long getPreferenceLong(String name) {
		return getDefault().getPreferenceStore().getLong(name);
	}

	public static String getPreferenceString(String name) {
		return getDefault().getPreferenceStore().getString(name);
	}

	public static void setPreference(String name, double value) {
		getDefault().getPreferenceStore().setValue(name, value);
	}

	public static void setPreference(String name, float value) {
		getDefault().getPreferenceStore().setValue(name, value);
	}

	public static void setPreference(String name, int value) {
		getDefault().getPreferenceStore().setValue(name, value);
	}

	public static void setPreference(String name, long value) {
		getDefault().getPreferenceStore().setValue(name, value);
	}

	public static void setPreference(String name, String value) {
		getDefault().getPreferenceStore().setValue(name, value);
	}

	public static void setPreference(String name, boolean value) {
		getDefault().getPreferenceStore().setValue(name, value);
	}

	protected PropertyResourceBundle pdeProperties;
	static protected PropertyResourceBundle pluginProperties;

	public boolean isShuttingDown() {
		return fShuttingDown;
	}

	private void setShuttingDown(boolean value) {
		fShuttingDown = value;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		JDXDebugOptionsManager.getDefault().startup();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if(m_imageDescriptorRegistry != null)
			m_imageDescriptorRegistry.dispose();

		setShuttingDown(true);
		JDXDebugOptionsManager.getDefault().shutdown();
		shutdownAllLaunched();
		super.stop(context);

	}

	public void shutdownAllLaunched() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for(int i = 0; i < launches.length; i++) {
			if(launches[i].getLaunchMode().equals(ILaunchManager.RUN_MODE)) {
				IProcess[] processes = launches[i].getProcesses();
				for(int rp = 0; rp < processes.length; rp++) {
					if(processes[rp].canTerminate()) {
						launches[i].removeProcess(processes[rp]);
					}
				}
			} else if(launches[i].getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
				IDebugTarget[] debugTargets = launches[i].getDebugTargets();
				for(int dp = 0; dp < debugTargets.length; dp++) {
					if(debugTargets[dp].canTerminate()) {
						launches[i].removeDebugTarget(debugTargets[dp]);
					}
				}
			}
		}
	}

	public static Plugin getDefault() {
		return m_instance;
	}

	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}

	public ResourceBundle getResourceBundle() {
		return m_resourceBundle;
	}

	public static String getResourceString(String key) {
		ResourceBundle bundle = Plugin.getDefault().getResourceBundle();

		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch(MissingResourceException e) {
			return key;
		}
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		return getDefault().internalGetImageDescriptorRegistry();
	}

	private synchronized ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
		if(m_imageDescriptorRegistry == null) {
			m_imageDescriptorRegistry = new ImageDescriptorRegistry();
		}
		return m_imageDescriptorRegistry;
	}

	private IOConsoleOutputStream createConsole(String message) {
		MessageConsole messageConsole = null;
		IOConsoleOutputStream m_stream = null;

		messageConsole = new MessageConsole(message, null, false);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { messageConsole });

		m_stream = messageConsole.newOutputStream();
		m_stream.setActivateOnWrite(true);

		console.put(messageConsole, m_stream);
		return m_stream;
	}

	public OutputStream openConsoleStream(String msg) {
		for(MessageConsole messageConsole : console.keySet()) {
			if(messageConsole.getName().equals(msg)) {
				return console.get(messageConsole);
			}
		}

		return createConsole(msg);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(int statusCode, String message, int code, Throwable throwable) {
		message = message == null ? "" : message;
		Status status = new Status(statusCode, getDefault().getBundle().getSymbolicName(), code, message, throwable);
		log(status);
	}

	public static void log(int code, Throwable throwable) {
		log(IStatus.ERROR, throwable.getMessage(), code, throwable);
	}

	public static void log(Throwable throwable) {
		log(IStatus.ERROR, throwable.getMessage(), IStatus.ERROR, throwable);
	}

	public static void info(Throwable throwable) {
		log(IStatus.INFO, throwable.getMessage(), 0, null);
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace(); // .getRoot().getFullPath();
	}

	public static boolean projectExists(IProject project) {
		try {
			return project != null && project.exists() && project.isOpen() && project.hasNature(Z8ProjectNature.Id);
		} catch(CoreException e) {
			Plugin.log(e);
			return false;
		}
	}

	public static IProject findProject(String name) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		for(IProject project : projects) {
			if(projectExists(project) && project.getName().equals(name)) {
				return project;
			}
		}
		return null;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();

		if(window != null) {
			return window.getActivePage();
		}

		return null;
	}

	public static IEditorPart getActiveEditor() {
		IWorkbenchPage page = getActivePage();

		if(page != null) {
			return page.getActiveEditor();
		}

		return null;
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		return window != null ? window.getShell() : null;
	}

	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();

		if(display == null) {
			return Display.getDefault();
		}

		return display;
	}

	static public ColorProvider getColorProvider() {
		if(fColorProvider == null) {
			fColorProvider = new ColorProvider();
		}
		return fColorProvider;
	}

	public RuleBasedScanner getDocScanner() {
		if(m_docScanner == null) {
			m_docScanner = new DocScanner(fColorProvider);
		}
		return m_docScanner;
	}

	public static IEditorPart[] getDirtyEditors(/*
												 * boolean
												 * skipNonResourceEditors =
												 * false
												 */) {
		Set<IEditorInput> inputs = new HashSet<IEditorInput>();
		List<IEditorPart> result = new ArrayList<IEditorPart>(0);

		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();

		for(int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for(int x = 0; x < pages.length; x++) {
				IEditorPart[] editors = pages[x].getDirtyEditors();
				for(int z = 0; z < editors.length; z++) {
					IEditorPart ep = editors[z];
					IEditorInput input = ep.getEditorInput();
					if(inputs.add(input)) {
						if(true)// /*!skipNonResourceEditors*/ ||
								// isResourceEditorInput(input))
						{
							result.add(ep);
						}
					}
				}
			}
		}
		return result.toArray(new IEditorPart[result.size()]);
	}

	@SuppressWarnings("unused")
	private static boolean isResourceEditorInput(IEditorInput input) {
		if(input instanceof MultiEditorInput) {
			IEditorInput[] inputs = ((MultiEditorInput)input).getInput();
			for(int i = 0; i < inputs.length; i++) {
				if(inputs[i].getAdapter(IResource.class) != null) {
					return true;
				}
			}
		} else if(input.getAdapter(IResource.class) != null) {
			return true;
		}
		return false;
	}
}
