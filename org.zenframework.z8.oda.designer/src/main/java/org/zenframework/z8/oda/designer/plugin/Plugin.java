package org.zenframework.z8.oda.designer.plugin;

import java.io.File;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.zenframework.z8.pde.preferences.PreferencePageConsts;

public class Plugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.zenframework.z8.oda.designer";

	private static Plugin plugin;

	private ResourceBundle resourceBundle = null;

	static public File getWebInfPath() {
		return new File(org.zenframework.z8.pde.Plugin.getPreferenceString(PreferencePageConsts.ATTR_WEB_INF_PATH));
	}

	public Plugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		try {
			resourceBundle = ResourceBundle.getBundle("org.zenframework.z8.oda.designer.plugin.Plugin");
		} catch(MissingResourceException x) {
			resourceBundle = null;
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		resourceBundle = null;
		plugin = null;
		super.stop(context);
	}

	public static Plugin getDefault() {
		return plugin;
	}

	public static String getResourceString(String key) {
		ResourceBundle bundle = getDefault().getResourceBundle();
		try {
			return bundle != null ? bundle.getString(key) : key;
		} catch(MissingResourceException e) {
			return key;
		}
	}

	public static String getFormattedString(String key, Object[] arguments) {
		return MessageFormat.format(getResourceString(key), arguments);
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
