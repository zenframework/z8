package org.zenframework.z8.pde.debug;

import org.zenframework.z8.pde.Plugin;

public interface JDXPreferences {
	public static final String PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS = Plugin.PLUGIN_ID + "javaDebug.SuspendOnUncaughtExceptions";
	/**
	 * Boolean preference controlling whether to suspend execution when a
	 * compilation error is encountered (while debugging).
	 */
	public static final String PREF_SUSPEND_ON_COMPILATION_ERRORS = Plugin.PLUGIN_ID + ".suspend_on_compilation_errors";
	/**
	 * Boolean preference controlling whether synthetic methods are to be
	 * filtered when stepping (and step filters are enabled).
	 */
	public static final String PREF_FILTER_SYNTHETICS = Plugin.PLUGIN_ID + ".filter_synthetics";
	/**
	 * Boolean preference controlling whether static initializers are to be
	 * filtered when stepping (and step filters are enabled).
	 */
	public static final String PREF_FILTER_STATIC_INITIALIZERS = Plugin.PLUGIN_ID + ".filter_statics";
	/**
	 * Boolean preference controlling whether constructors are to be filtered
	 * when stepping (and step filters are enabled).
	 */
	public static final String PREF_FILTER_CONSTRUCTORS = Plugin.PLUGIN_ID + ".filter_constructors";
	/**
	 * List of active step filters. A String containing a comma separated list
	 * of fully qualified type names/patterns.
	 */
	public static final String PREF_ACTIVE_FILTERS_LIST = Plugin.PLUGIN_ID + ".active_filters";
	/**
	 * List of inactive step filters. A String containing a comma separated list
	 * of fully qualified type names/patterns.
	 */
	public static final String PREF_INACTIVE_FILTERS_LIST = Plugin.PLUGIN_ID + ".inactive_filters";
	/**
	 * Boolean preference controlling whether to alert with a dialog when hot
	 * code replace fails.
	 */
	public static final String PREF_ALERT_HCR_FAILED = Plugin.PLUGIN_ID + ".javaDebug.alertHCRFailed";
	/**
	 * Boolean preference controlling whether to alert with a dialog when hot
	 * code replace is not supported.
	 */
	public static final String PREF_ALERT_HCR_NOT_SUPPORTED = Plugin.PLUGIN_ID + ".javaDebug.alertHCRNotSupported";
	/**
	 * Boolean preference controlling whether to alert with a dialog when hot
	 * code replace results in obsolete methods.
	 */
	public static final String PREF_ALERT_OBSOLETE_METHODS = Plugin.PLUGIN_ID + "javaDebug.alertObsoleteMethods";
	/**
	 * Boolean preference controlling whether the debugger shows qualifed names.
	 * When <code>true</code> the debugger will show qualified names in newly
	 * opened views.
	 * 
	 * @since 2.0
	 */
	public static final String PREF_SHOW_QUALIFIED_NAMES = Plugin.PLUGIN_ID + ".show_qualified_names";
	/**
	 * List of defined detail formatters.A String containing a comma separated
	 * list of fully qualified type names, the associated code snippet and an
	 * 'enabled' flag.
	 */
	public static final String PREF_DETAIL_FORMATTERS_LIST = Plugin.PLUGIN_ID + ".detail_formatters";
	/**
	 * Boolean preference indicating whether (non-final) static varibles should
	 * be shown in variable views. A view may over-ride this preference, and if
	 * so, stores its preference, prefixed by view id.
	 * 
	 * @since 2.1
	 */
	public static final String PREF_SHOW_STATIC_VARIALBES = Plugin.PLUGIN_ID + ".show_static_variables";
	/**
	 * Boolean preference indicating whether constant (final static) varibles
	 * should be shown in variable views. A view may over-ride this preference,
	 * and if so, stores its preference, prefixed by view id.
	 * 
	 * @since 2.1
	 */
	public static final String PREF_SHOW_CONSTANTS = Plugin.PLUGIN_ID + ".show_constants";
	/**
	 * Boolean preference indicating whether null array entries should be shown
	 * in variable views. A view may over-ride this preference, and if so,
	 * stores its preference, prefixed by view id.
	 * 
	 * @since 3.0
	 */
	public static final String PREF_SHOW_NULL_ARRAY_ENTRIES = Plugin.PLUGIN_ID + ".show_null_entries";
	/**
	 * Boolean preference indicating whether hex values should be shown for
	 * primitives in variable views. A view may over-ride this preference, and
	 * if so, stores its preference, prefixed by view id.
	 * 
	 * @since 2.1
	 */
	public static final String PREF_SHOW_HEX = Plugin.PLUGIN_ID + ".show_hex";
	/**
	 * Boolean preference indicating whether char values should be shown for
	 * primitives in variable views. A view may over-ride this preference, and
	 * if so, stores its preference, prefixed by view id.
	 * 
	 * @since 2.1
	 */
	public static final String PREF_SHOW_CHAR = Plugin.PLUGIN_ID + ".show_char";
	/**
	 * Boolean preference indicating whether unsigned values should be shown for
	 * primitives in variable views. A view may over-ride this preference, and
	 * if so, stores its preference, prefixed by view id.
	 * 
	 * @since 2.1
	 */
	public static final String PREF_SHOW_UNSIGNED = Plugin.PLUGIN_ID + ".show_unsigned";
	/**
	 * Boolean preference indicating whether system threads should appear
	 * visible in the debug launch view. A view may over-ride this preference,
	 * and if so, stores its preference, prefixed by view id.
	 * 
	 * @since 3.0
	 */
	public static final String PREF_SHOW_SYSTEM_THREADS = Plugin.PLUGIN_ID + ".show_system_threads";
	/**
	 * Boolean preference indicating whether the monitor and thread info should
	 * be displayed in the debug launch view.
	 * 
	 * @since 3.1
	 */
	public static final String PREF_SHOW_MONITOR_THREAD_INFO = Plugin.PLUGIN_ID + ".show_monitor_thread_info";
	/**
	 * String preference indication when and where variable details should
	 * appear. Valid values include:
	 * <ul>
	 * <li><code>INLINE_ALL</code> to show inline details for all variables
	 * <li><code>INLINE_FORMATTERS</code> to show inline details for variables
	 * with formatters
	 * <li><code>DETAIL_PANE</code> to show details only in the detail pane
	 * </ul>
	 */
	public static final String PREF_SHOW_DETAILS = Plugin.PLUGIN_ID + ".show_details";
	/**
	 * "Show detail" preference values.
	 */
	public static final String INLINE_ALL = "INLINE_ALL";
	public static final String INLINE_FORMATTERS = "INLINE_FORMATTERS";
	public static final String DETAIL_PANE = "DETAIL_PANE";
	/**
	 * Common dialog settings
	 */
	public static final String DIALOG_ORIGIN_X = "DIALOG_ORIGIN_X";
	public static final String DIALOG_ORIGIN_Y = "DIALOG_ORIGIN_Y";
	public static final String DIALOG_WIDTH = "DIALOG_WIDTH";
	public static final String DIALOG_HEIGHT = "DIALOG_HEIGHT";
	/**
	 * Boolean preference controlling whether to alert with a dialog when unable
	 * to install a breakpoint (line info not available, ...)
	 */
	public static final String PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT = Plugin.PLUGIN_ID + ".prompt_unable_to_install_breakpoint";
	public static final String PREF_THREAD_MONITOR_IN_DEADLOCK_COLOR = "org.eclipse.jdt.debug.ui.InDeadlockColor";
}
