package org.zenframework.z8.pde;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class PluginImages {
	private static final String NAME_PREFIX = "org.zenframework.z8.pde.";
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();
	private static URL fgIconBaseURL = null;

	static {
		fgIconBaseURL = Plugin.getDefault().getBundle().getEntry("/icons/");
	}

	private static ImageRegistry fgImageRegistry = null;
	private static HashMap<String, ImageDescriptor> fgAvoidSWTErrorMap = null;

	private static final String T_OBJ = "obj16";
	private static final String T_OVR = "ovr16";
	private static final String T_WIZBAN = "wizban";
	private static final String T_OUTLINE = "outline";
	// private static final String T_DLCL = "dlcl16";
	// private static final String T_ETOOL = "etool16";
	// private static final String T_EVIEW = "eview16";

	public static final String IMG_MISC_PUBLIC = NAME_PREFIX + "methpub_obj.gif";
	public static final String IMG_MISC_PROTECTED = NAME_PREFIX + "methpro_obj.gif";
	public static final String IMG_MISC_PRIVATE = NAME_PREFIX + "methpri_obj.gif";
	public static final String IMG_MISC_DEFAULT = NAME_PREFIX + "methdef_obj.gif";

	public static final String IMG_OBJS_UNKNOWN = NAME_PREFIX + "unknown_obj.gif";

	public static final String IMG_FIELD_PUBLIC = NAME_PREFIX + "field_public_obj.gif";
	public static final String IMG_FIELD_PROTECTED = NAME_PREFIX + "field_protected_obj.gif";
	public static final String IMG_FIELD_PRIVATE = NAME_PREFIX + "field_private_obj.gif";
	public static final String IMG_FIELD_DEFAULT = NAME_PREFIX + "field_default_obj.gif";

	public static final String IMG_OBJS_ENUM = NAME_PREFIX + "enum_obj.gif";
	public static final String IMG_OBJS_ENUM_ALT = NAME_PREFIX + "enum_alt_obj.gif";

	public static final String IMG_OBJS_CLASS = NAME_PREFIX + "class_obj.gif";
	public static final String IMG_OBJS_CLASSALT = NAME_PREFIX + "classfo_obj.gif";
	public static final String IMG_OBJS_CLASS_DEFAULT = NAME_PREFIX + "class_default_obj.gif";

	public static final String IMG_OBJS_CUNIT = NAME_PREFIX + "cu_obj.gif";
	public static final String IMG_OBJS_CUNIT_RESOURCE = NAME_PREFIX + "jcu_resource_obj.gif";
	public static final String IMG_OBJS_FOLDER = NAME_PREFIX + "package_obj.gif";
	public static final String IMG_OBJS_EMPTY_FOLDER = NAME_PREFIX + "empty_pack_obj.gif";

	public static final ImageDescriptor DESC_MISC_PUBLIC = createManaged(T_OBJ, IMG_MISC_PUBLIC);
	public static final ImageDescriptor DESC_MISC_PROTECTED = createManaged(T_OBJ, IMG_MISC_PROTECTED);
	public static final ImageDescriptor DESC_MISC_PRIVATE = createManaged(T_OBJ, IMG_MISC_PRIVATE);
	public static final ImageDescriptor DESC_MISC_DEFAULT = createManaged(T_OBJ, IMG_MISC_DEFAULT);

	public static final ImageDescriptor DESC_OBJS_UNKNOWN = createManaged(T_OBJ, IMG_OBJS_UNKNOWN);

	public static final ImageDescriptor DESC_FIELD_PUBLIC = createManaged(T_OBJ, IMG_FIELD_PUBLIC);
	public static final ImageDescriptor DESC_FIELD_PROTECTED = createManaged(T_OBJ, IMG_FIELD_PROTECTED);
	public static final ImageDescriptor DESC_FIELD_PRIVATE = createManaged(T_OBJ, IMG_FIELD_PRIVATE);
	public static final ImageDescriptor DESC_FIELD_DEFAULT = createManaged(T_OBJ, IMG_FIELD_DEFAULT);

	public static final ImageDescriptor DESC_OBJS_ENUM = createManaged(T_OBJ, IMG_OBJS_ENUM);
	public static final ImageDescriptor DESC_OBJS_ENUM_ALT = createManaged(T_OBJ, IMG_OBJS_ENUM_ALT);

	public static final ImageDescriptor DESC_OBJS_CLASS = createManaged(T_OBJ, IMG_OBJS_CLASS);
	public static final ImageDescriptor DESC_OBJS_CLASSALT = createManaged(T_OBJ, IMG_OBJS_CLASSALT);
	public static final ImageDescriptor DESC_OBJS_CLASS_DEFAULT = createManaged(T_OBJ, IMG_OBJS_CLASS_DEFAULT);
	public static final ImageDescriptor DESC_OBJS_FOLDER = createManaged(T_OBJ, IMG_OBJS_FOLDER);
	public static final ImageDescriptor DESC_OBJS_EMPTY_FOLDER = createManaged(T_OBJ, IMG_OBJS_EMPTY_FOLDER);

	public static final ImageDescriptor DESC_OBJS_CUNIT = createManaged(T_OBJ, IMG_OBJS_CUNIT);
	public static final ImageDescriptor DESC_OBJS_CUNIT_RESOURCE = createManaged(T_OBJ, IMG_OBJS_CUNIT_RESOURCE);

	public static final ImageDescriptor DESC_OVR_STATIC = create(T_OVR, "static_co.gif");
	public static final ImageDescriptor DESC_OVR_FINAL = create(T_OVR, "final_co.gif");
	public static final ImageDescriptor DESC_OVR_IMPLEMENTS = create(T_OVR, "implm_co.gif");
	public static final ImageDescriptor DESC_OVR_OVERRIDES = create(T_OVR, "over_co.gif");
	public static final ImageDescriptor DESC_OVR_ERROR = create(T_OVR, "error_co.gif");
	public static final ImageDescriptor DESC_OVR_WARNING = create(T_OVR, "warning_co.gif");

	public static final ImageDescriptor DESC_WIZBAN_REFACTOR = create(T_WIZBAN, "refactor_wiz.gif");
	public static final ImageDescriptor DESC_WIZBAN_REFACTOR_TYPE = create(T_WIZBAN, "typerefact_wiz.gif");

	public static final ImageDescriptor DESC_WIZBAN_REFACTOR_CU = create(T_WIZBAN, "compunitrefact_wiz.gif");
	public static final ImageDescriptor DESC_WIZBAN_NEWFOLDER = create(T_WIZBAN, "newpack_wiz.gif");

	public static final ImageDescriptor DESC_WIZBAN_REFACTOR_FOLDER = create(T_WIZBAN, "packrefact_wiz.gif");

	public static final ImageDescriptor DESC_OUTLINE_STATIC = create(T_OUTLINE, "static_co.gif");
	public static final ImageDescriptor DESC_OUTLINE_BASE = create(T_OUTLINE, "base_co.gif");
	public static final ImageDescriptor DESC_OUTLINE_SYNCHRONIZED = create(T_OUTLINE, "synced.gif");

	/*
	 * public static final String IMG_ELCL_VIEW_MENU = NAME_PREFIX + T_ELCL +
	 * "view_menu.gif"; public static final String IMG_DLCL_VIEW_MENU =
	 * NAME_PREFIX + T_DLCL + "view_menu.gif"; public static final String
	 * IMG_OBJS_GHOST = NAME_PREFIX + "ghost.gif"; public static final String
	 * IMG_OBJS_SEARCH_TSK = NAME_PREFIX + "search_tsk.gif"; public static final
	 * String IMG_OBJS_PACKDECL = NAME_PREFIX + "packd_obj.gif"; public static
	 * final String IMG_OBJS_IMPDECL = NAME_PREFIX + "imp_obj.gif"; public
	 * static final String IMG_OBJS_IMPCONT = NAME_PREFIX + "impc_obj.gif";
	 * public static final String IMG_OBJS_JSEARCH = NAME_PREFIX +
	 * "jsearch_obj.gif"; public static final String IMG_OBJS_SEARCH_DECL =
	 * NAME_PREFIX + "search_decl_obj.gif"; public static final String
	 * IMG_OBJS_SEARCH_REF = NAME_PREFIX + "search_ref_obj.gif"; public static
	 * final String IMG_OBJS_INNER_CLASS_PUBLIC = NAME_PREFIX +
	 * "innerclass_public_obj.gif"; public static final String
	 * IMG_OBJS_INNER_CLASS_DEFAULT = NAME_PREFIX +
	 * "innerclass_default_obj.gif"; public static final String
	 * IMG_OBJS_INNER_CLASS_PROTECTED = NAME_PREFIX +
	 * "innerclass_protected_obj.gif"; public static final String
	 * IMG_OBJS_INNER_CLASS_PRIVATE = NAME_PREFIX +
	 * "innerclass_private_obj.gif"; public static final String
	 * IMG_OBJS_INTERFACE = NAME_PREFIX + "int_obj.gif"; public static final
	 * String IMG_OBJS_INTERFACEALT = NAME_PREFIX + "intf_obj.gif"; public
	 * static final String IMG_OBJS_INTERFACE_DEFAULT = NAME_PREFIX +
	 * "int_default_obj.gif"; public static final String
	 * IMG_OBJS_INNER_INTERFACE_PUBLIC = NAME_PREFIX +
	 * "innerinterface_public_obj.gif"; public static final String
	 * IMG_OBJS_INNER_INTERFACE_DEFAULT = NAME_PREFIX +
	 * "innerinterface_default_obj.gif"; public static final String
	 * IMG_OBJS_INNER_INTERFACE_PROTECTED = NAME_PREFIX +
	 * "innerinterface_protected_obj.gif"; public static final String
	 * IMG_OBJS_INNER_INTERFACE_PRIVATE = NAME_PREFIX +
	 * "innerinterface_private_obj.gif"; public static final String
	 * IMG_OBJS_ANNOTATION = NAME_PREFIX + "annotation_obj.gif"; public static
	 * final String IMG_OBJS_ANNOTATION_DEFAULT = NAME_PREFIX +
	 * "annotation_default_obj.gif"; public static final String
	 * IMG_OBJS_ANNOTATION_PROTECTED = NAME_PREFIX +
	 * "annotation_protected_obj.gif"; public static final String
	 * IMG_OBJS_ANNOTATION_PRIVATE = NAME_PREFIX + "annotation_private_obj.gif";
	 * public static final String IMG_OBJS_ANNOTATION_ALT = NAME_PREFIX +
	 * "annotation_alt_obj.gif"; public static final String
	 * IMG_OBJS_ENUM_DEFAULT = NAME_PREFIX + "enum_default_obj.gif"; public
	 * static final String IMG_OBJS_ENUM_PROTECTED = NAME_PREFIX +
	 * "enum_protected_obj.gif"; public static final String
	 * IMG_OBJS_ENUM_PRIVATE = NAME_PREFIX + "enum_private_obj.gif"; public
	 * static final String IMG_OBJS_CFILE = NAME_PREFIX + "classf_obj.gif";
	 * public static final String IMG_OBJS_CFILECLASS = NAME_PREFIX +
	 * "class_obj.gif"; public static final String IMG_OBJS_CFILEINT =
	 * NAME_PREFIX + "int_obj.gif"; public static final String
	 * IMG_OBJS_LOGICAL_PACKAGE = NAME_PREFIX +
	 * "logical_package_obj.gif";//$NON-NLS-1$ public static final String
	 * IMG_OBJS_EMPTY_LOGICAL_PACKAGE = NAME_PREFIX +
	 * "empty_logical_package_obj.gif";//$NON-NLS-1$ public static final String
	 * IMG_OBJS_EMPTY_PACK_RESOURCE = NAME_PREFIX + "empty_pack_fldr_obj.gif";
	 * public static final String IMG_OBJS_PACKFRAG_ROOT = NAME_PREFIX +
	 * "packagefolder_obj.gif"; public static final String
	 * IMG_OBJS_MISSING_PACKFRAG_ROOT = NAME_PREFIX +
	 * "packagefolder_nonexist_obj.gif"; public static final String
	 * IMG_OBJS_MISSING_JAR = NAME_PREFIX + "jar_nonexist_obj.gif"; public
	 * static final String IMG_OBJS_JAR = NAME_PREFIX + "jar_obj.gif"; public
	 * static final String IMG_OBJS_EXTJAR = NAME_PREFIX + "jar_l_obj.gif";
	 * public static final String IMG_OBJS_JAR_WSRC = NAME_PREFIX +
	 * "jar_src_obj.gif"; public static final String IMG_OBJS_EXTJAR_WSRC =
	 * NAME_PREFIX + "jar_lsrc_obj.gif"; public static final String
	 * IMG_OBJS_ENV_VAR = NAME_PREFIX + "envvar_obj.gif"; public static final
	 * String IMG_OBJS_MISSING_ENV_VAR = NAME_PREFIX +
	 * "envvar_nonexist_obj.gif"; public static final String IMG_OBJS_JAVA_MODEL
	 * = NAME_PREFIX + "java_model_obj.gif"; public static final String
	 * IMG_OBJS_LOCAL_VARIABLE = NAME_PREFIX + "localvariable_obj.gif"; public
	 * static final String IMG_OBJS_LIBRARY = NAME_PREFIX + "library_obj.gif";
	 * public static final String IMG_OBJS_JAVADOCTAG = NAME_PREFIX +
	 * "jdoc_tag_obj.gif"; public static final String IMG_OBJS_HTMLTAG =
	 * NAME_PREFIX + "html_tag_obj.gif"; public static final String
	 * IMG_OBJS_TEMPLATE = NAME_PREFIX + "template_obj.gif"; public static final
	 * String IMG_OBJS_TYPEVARIABLE = NAME_PREFIX + "typevariable_obj.gif";
	 * public static final String IMG_OBJS_EXCEPTION = NAME_PREFIX +
	 * "jexception_obj.gif"; public static final String IMG_OBJS_ERROR =
	 * NAME_PREFIX + "jrtexception_obj.gif"; public static final String
	 * IMG_OBJS_BREAKPOINT_INSTALLED = NAME_PREFIX + "brkpi_obj.gif"; public
	 * static final String IMG_OBJS_QUICK_ASSIST = NAME_PREFIX +
	 * "quickassist_obj.gif"; public static final String
	 * IMG_OBJS_FIXABLE_PROBLEM = NAME_PREFIX + "quickfix_warning_obj.gif";
	 * public static final String IMG_OBJS_FIXABLE_ERROR = NAME_PREFIX +
	 * "quickfix_error_obj.gif"; public static final String
	 * IMG_OBJS_REFACTORING_FATAL = NAME_PREFIX + "fatalerror_obj.gif"; public
	 * static final String IMG_OBJS_REFACTORING_ERROR = NAME_PREFIX +
	 * "error_obj.gif"; public static final String IMG_OBJS_REFACTORING_WARNING
	 * = NAME_PREFIX + "warning_obj.gif"; public static final String
	 * IMG_OBJS_REFACTORING_INFO = NAME_PREFIX + "info_obj.gif"; public static
	 * final String IMG_OBJS_NLS_TRANSLATE = NAME_PREFIX + "translate.gif";
	 * public static final String IMG_OBJS_NLS_NEVER_TRANSLATE = NAME_PREFIX +
	 * "never_translate.gif"; public static final String IMG_OBJS_NLS_SKIP =
	 * NAME_PREFIX + "skip.gif"; public static final String
	 * IMG_OBJS_SEARCH_READACCESS = NAME_PREFIX + "occ_read.gif"; public static
	 * final String IMG_OBJS_SEARCH_WRITEACCESS = NAME_PREFIX + "occ_write.gif";
	 * public static final String IMG_OBJS_SEARCH_OCCURRENCE = NAME_PREFIX +
	 * "occ_match.gif"; public static final String IMG_OBJS_HELP = NAME_PREFIX +
	 * "help.gif";
	 * 
	 * public static final ImageDescriptor DESC_VIEW_ERRORWARNING_TAB =
	 * create(T_EVIEW, "errorwarning_tab.gif"); public static final
	 * ImageDescriptor DESC_VIEW_CLASSFILEGENERATION_TAB = create(T_EVIEW,
	 * "classfilegeneration_tab.gif"); public static final ImageDescriptor
	 * DESC_VIEW_JDKCOMPLIANCE_TAB = create(T_EVIEW, "jdkcompliance_tab.gif");
	 * public static final ImageDescriptor DESC_ELCL_FILTER = create(T_ELCL,
	 * "filter_ps.gif"); public static final ImageDescriptor DESC_DLCL_FILTER =
	 * create(T_DLCL, "filter_ps.gif"); public static final ImageDescriptor
	 * DESC_ELCL_CODE_ASSIST = create(T_ELCL, "metharg_obj.gif"); public static
	 * final ImageDescriptor DESC_DLCL_CODE_ASSIST = create(T_DLCL,
	 * "metharg_obj.gif"); public static final ImageDescriptor
	 * DESC_ELCL_VIEW_MENU = createManaged(T_ELCL, NAME_PREFIX +
	 * "view_menu.gif", IMG_ELCL_VIEW_MENU); public static final ImageDescriptor
	 * DESC_DLCL_VIEW_MENU = createManaged(T_DLCL, NAME_PREFIX +
	 * "view_menu.gif", IMG_DLCL_VIEW_MENU); public static final ImageDescriptor
	 * DESC_MENU_SHIFT_RIGHT = create(T_ETOOL, "shift_r_edit.gif"); public
	 * static final ImageDescriptor DESC_MENU_SHIFT_LEFT = create(T_ETOOL,
	 * "shift_l_edit.gif"); public static final ImageDescriptor DESC_OBJS_GHOST
	 * = createManaged(T_OBJ, IMG_OBJS_GHOST); public static final
	 * ImageDescriptor DESC_OBJS_PACKDECL = createManaged(T_OBJ,
	 * IMG_OBJS_PACKDECL); public static final ImageDescriptor DESC_OBJS_IMPDECL
	 * = createManaged(T_OBJ, IMG_OBJS_IMPDECL); public static final
	 * ImageDescriptor DESC_OBJS_IMPCONT = createManaged(T_OBJ,
	 * IMG_OBJS_IMPCONT); public static final ImageDescriptor DESC_OBJS_JSEARCH
	 * = createManaged(T_OBJ, IMG_OBJS_JSEARCH); public static final
	 * ImageDescriptor DESC_OBJS_SEARCH_DECL = createManaged(T_OBJ,
	 * IMG_OBJS_SEARCH_DECL); public static final ImageDescriptor
	 * DESC_OBJS_SEARCH_REF = createManaged(T_OBJ, IMG_OBJS_SEARCH_REF); public
	 * static final ImageDescriptor DESC_OBJS_CFILE = createManaged(T_OBJ,
	 * IMG_OBJS_CFILE); public static final ImageDescriptor DESC_OBJS_CFILECLASS
	 * = createManaged(T_OBJ, IMG_OBJS_CFILECLASS); public static final
	 * ImageDescriptor DESC_ELCL_CLEAR = create(T_ELCL, "clear_co.gif"); public
	 * static final ImageDescriptor DESC_DLCL_CLEAR = create(T_DLCL,
	 * "clear_co.gif"); public static final ImageDescriptor DESC_OBJS_CFILEINT =
	 * createManaged(T_OBJ, IMG_OBJS_CFILEINT); public static final
	 * ImageDescriptor DESC_OBJS_EMPTY_LOGICAL_PACKAGE = createManaged(T_OBJ,
	 * IMG_OBJS_EMPTY_LOGICAL_PACKAGE); public static final ImageDescriptor
	 * DESC_OBJS_LOGICAL_PACKAGE = createManaged(T_OBJ,
	 * IMG_OBJS_LOGICAL_PACKAGE); public static final ImageDescriptor
	 * DESC_OBJS_EMPTY_PACKAGE_RESOURCES = createManaged(T_OBJ,
	 * IMG_OBJS_EMPTY_PACK_RESOURCE); public static final ImageDescriptor
	 * DESC_OBJS_PACKFRAG_ROOT = createManaged(T_OBJ, IMG_OBJS_PACKFRAG_ROOT);
	 * public static final ImageDescriptor DESC_OBJS_MISSING_PACKFRAG_ROOT =
	 * createManaged(T_OBJ, IMG_OBJS_MISSING_PACKFRAG_ROOT); public static final
	 * ImageDescriptor DESC_OBJS_JAVA_MODEL = createManaged(T_OBJ,
	 * IMG_OBJS_JAVA_MODEL); public static final ImageDescriptor
	 * DESC_OBJS_INNER_CLASS_PUBLIC = createManaged(T_OBJ,
	 * IMG_OBJS_INNER_CLASS_PUBLIC); public static final ImageDescriptor
	 * DESC_OBJS_INNER_CLASS_DEFAULT = createManaged(T_OBJ,
	 * IMG_OBJS_INNER_CLASS_DEFAULT); public static final ImageDescriptor
	 * DESC_OBJS_INNER_CLASS_PROTECTED = createManaged(T_OBJ,
	 * IMG_OBJS_INNER_CLASS_PROTECTED); public static final ImageDescriptor
	 * DESC_OBJS_INNER_CLASS_PRIVATE = createManaged(T_OBJ,
	 * IMG_OBJS_INNER_CLASS_PRIVATE); public static final ImageDescriptor
	 * DESC_OBJS_INTERFACE = createManaged(T_OBJ, IMG_OBJS_INTERFACE); public
	 * static final ImageDescriptor DESC_OBJS_INTERFACE_DEFAULT =
	 * createManaged(T_OBJ, IMG_OBJS_INTERFACE_DEFAULT); public static final
	 * ImageDescriptor DESC_OBJS_INNER_INTERFACE_PUBLIC = createManaged(T_OBJ,
	 * IMG_OBJS_INNER_INTERFACE_PUBLIC); public static final ImageDescriptor
	 * DESC_OBJS_INNER_INTERFACE_DEFAULT = createManaged(T_OBJ,
	 * IMG_OBJS_INNER_INTERFACE_DEFAULT); public static final ImageDescriptor
	 * DESC_OBJS_INNER_INTERFACE_PROTECTED = createManaged(T_OBJ,
	 * IMG_OBJS_INNER_INTERFACE_PROTECTED); public static final ImageDescriptor
	 * DESC_OBJS_INNER_INTERFACE_PRIVATE = createManaged(T_OBJ,
	 * IMG_OBJS_INNER_INTERFACE_PRIVATE); public static final ImageDescriptor
	 * DESC_OBJS_INTERFACEALT = createManaged(T_OBJ, IMG_OBJS_INTERFACEALT);
	 * public static final ImageDescriptor DESC_OBJS_ANNOTATION =
	 * createManaged(T_OBJ, IMG_OBJS_ANNOTATION); public static final
	 * ImageDescriptor DESC_OBJS_ANNOTATION_DEFAULT = createManaged(T_OBJ,
	 * IMG_OBJS_ANNOTATION_DEFAULT); public static final ImageDescriptor
	 * DESC_OBJS_ANNOTATION_PROTECTED = createManaged(T_OBJ,
	 * IMG_OBJS_ANNOTATION_PROTECTED); public static final ImageDescriptor
	 * DESC_OBJS_ANNOTATION_PRIVATE = createManaged(T_OBJ,
	 * IMG_OBJS_ANNOTATION_PRIVATE); public static final ImageDescriptor
	 * DESC_OBJS_ANNOTATION_ALT = createManaged(T_OBJ, IMG_OBJS_ANNOTATION_ALT);
	 * public static final ImageDescriptor DESC_OBJS_ENUM_DEFAULT =
	 * createManaged(T_OBJ, IMG_OBJS_ENUM_DEFAULT); public static final
	 * ImageDescriptor DESC_OBJS_ENUM_PROTECTED = createManaged(T_OBJ,
	 * IMG_OBJS_ENUM_PROTECTED); public static final ImageDescriptor
	 * DESC_OBJS_ENUM_PRIVATE = createManaged(T_OBJ, IMG_OBJS_ENUM_PRIVATE);
	 * public static final ImageDescriptor DESC_OBJS_JAR = createManaged(T_OBJ,
	 * IMG_OBJS_JAR); public static final ImageDescriptor DESC_OBJS_MISSING_JAR
	 * = createManaged(T_OBJ, IMG_OBJS_MISSING_JAR); public static final
	 * ImageDescriptor DESC_OBJS_EXTJAR = createManaged(T_OBJ, IMG_OBJS_EXTJAR);
	 * public static final ImageDescriptor DESC_OBJS_JAR_WSRC =
	 * createManaged(T_OBJ, IMG_OBJS_JAR_WSRC); public static final
	 * ImageDescriptor DESC_OBJS_EXTJAR_WSRC = createManaged(T_OBJ,
	 * IMG_OBJS_EXTJAR_WSRC); public static final ImageDescriptor
	 * DESC_OBJS_ENV_VAR = createManaged(T_OBJ, IMG_OBJS_ENV_VAR); public static
	 * final ImageDescriptor DESC_OBJS_MISSING_ENV_VAR = createManaged(T_OBJ,
	 * IMG_OBJS_MISSING_ENV_VAR); public static final ImageDescriptor
	 * DESC_OBJS_LIBRARY = createManaged(T_OBJ, IMG_OBJS_LIBRARY); public static
	 * final ImageDescriptor DESC_OBJS_JAVADOCTAG = createManaged(T_OBJ,
	 * IMG_OBJS_JAVADOCTAG); public static final ImageDescriptor
	 * DESC_OBJS_HTMLTAG = createManaged(T_OBJ, IMG_OBJS_HTMLTAG); public static
	 * final ImageDescriptor DESC_OBJS_TEMPLATE = createManaged(T_OBJ,
	 * IMG_OBJS_TEMPLATE); public static final ImageDescriptor
	 * DESC_OBJS_TYPEVARIABLE = createManaged(T_OBJ, IMG_OBJS_TYPEVARIABLE);
	 * public static final ImageDescriptor DESC_OBJS_EXCEPTION =
	 * createManaged(T_OBJ, IMG_OBJS_EXCEPTION); public static final
	 * ImageDescriptor DESC_OBJS_BREAKPOINT_INSTALLED = createManaged(T_OBJ,
	 * IMG_OBJS_BREAKPOINT_INSTALLED); public static final ImageDescriptor
	 * DESC_OBJS_ERROR = createManaged(T_OBJ, IMG_OBJS_ERROR); public static
	 * final ImageDescriptor DESC_OBJS_QUICK_ASSIST = createManaged(T_OBJ,
	 * IMG_OBJS_QUICK_ASSIST); public static final ImageDescriptor
	 * DESC_OBJS_FIXABLE_PROBLEM = createManaged(T_OBJ,
	 * IMG_OBJS_FIXABLE_PROBLEM); public static final ImageDescriptor
	 * DESC_OBJS_FIXABLE_ERROR = createManaged(T_OBJ, IMG_OBJS_FIXABLE_ERROR);
	 * // public static final ImageDescriptor DESC_OBJS_SNIPPET_EVALUATING= //
	 * createManaged(T_OBJ, IMG_OBJS_SNIPPET_EVALUATING); public static final
	 * ImageDescriptor DESC_OBJS_DEFAULT_CHANGE = create(T_OBJ, "change.gif");
	 * public static final ImageDescriptor DESC_OBJS_COMPOSITE_CHANGE =
	 * create(T_OBJ, "composite_change.gif"); public static final
	 * ImageDescriptor DESC_OBJS_CU_CHANGE = create(T_OBJ, "cu_change.gif");
	 * public static final ImageDescriptor DESC_OBJS_FILE_CHANGE = create(T_OBJ,
	 * "file_change.gif"); public static final ImageDescriptor
	 * DESC_OBJS_TEXT_EDIT = create(T_OBJ, "text_edit.gif"); public static final
	 * ImageDescriptor DESC_DLCL_TEXT_EDIT = create(T_DLCL, "text_edit.gif");
	 * public static final ImageDescriptor DESC_OBJS_EXCLUSION_FILTER_ATTRIB =
	 * create(T_OBJ, "exclusion_filter_attrib.gif"); public static final
	 * ImageDescriptor DESC_OBJS_INCLUSION_FILTER_ATTRIB = create(T_OBJ,
	 * "inclusion_filter_attrib.gif"); public static final ImageDescriptor
	 * DESC_OBJS_OUTPUT_FOLDER_ATTRIB = create(T_OBJ,
	 * "output_folder_attrib.gif"); public static final ImageDescriptor
	 * DESC_OBJS_SOURCE_ATTACH_ATTRIB = create(T_OBJ,
	 * "source_attach_attrib.gif"); public static final ImageDescriptor
	 * DESC_OBJS_JAVADOC_LOCATION_ATTRIB = create(T_OBJ,
	 * "javadoc_location_attrib.gif"); public static final ImageDescriptor
	 * DESC_OBJS_ACCESSRULES_ATTRIB = create(T_OBJ,
	 * "access_restriction_attrib.gif"); public static final ImageDescriptor
	 * DESC_OBJS_NATIVE_LIB_PATH_ATTRIB = create(T_OBJ,
	 * "native_lib_path_attrib.gif"); public static final ImageDescriptor
	 * DESC_OBJS_REFACTORING_FATAL = createManaged(T_OBJ,
	 * IMG_OBJS_REFACTORING_FATAL); public static final ImageDescriptor
	 * DESC_OBJS_REFACTORING_ERROR = createManaged(T_OBJ,
	 * IMG_OBJS_REFACTORING_ERROR); public static final ImageDescriptor
	 * DESC_OBJS_REFACTORING_WARNING = createManaged(T_OBJ,
	 * IMG_OBJS_REFACTORING_WARNING); public static final ImageDescriptor
	 * DESC_OBJS_REFACTORING_INFO = createManaged(T_OBJ,
	 * IMG_OBJS_REFACTORING_INFO); public static final ImageDescriptor
	 * DESC_OBJS_NLS_TRANSLATE = createManaged(T_OBJ, IMG_OBJS_NLS_TRANSLATE);
	 * public static final ImageDescriptor DESC_OBJS_NLS_NEVER_TRANSLATE =
	 * createManaged(T_OBJ, IMG_OBJS_NLS_NEVER_TRANSLATE); public static final
	 * ImageDescriptor DESC_OBJS_NLS_SKIP = createManaged(T_OBJ,
	 * IMG_OBJS_NLS_SKIP); public static final ImageDescriptor
	 * DESC_OBJS_TYPE_SEPARATOR = create(T_OBJ, "type_separator.gif"); public
	 * static final ImageDescriptor DESC_OBJS_SEARCH_READACCESS =
	 * createManaged(T_OBJ, IMG_OBJS_SEARCH_READACCESS); public static final
	 * ImageDescriptor DESC_OBJS_SEARCH_WRITEACCESS = createManaged(T_OBJ,
	 * IMG_OBJS_SEARCH_WRITEACCESS); public static final ImageDescriptor
	 * DESC_OBJS_SEARCH_OCCURRENCE = createManaged(T_OBJ,
	 * IMG_OBJS_SEARCH_OCCURRENCE); public static final ImageDescriptor
	 * DESC_OBJS_LOCAL_VARIABLE = createManaged(T_OBJ, IMG_OBJS_LOCAL_VARIABLE);
	 * public static final ImageDescriptor DESC_OBJS_HELP =
	 * createManaged(T_ELCL, IMG_OBJS_HELP); public static final ImageDescriptor
	 * DESC_ELCL_ADD_TO_BP = create(T_ELCL, "add_to_buildpath.gif"); public
	 * static final ImageDescriptor DESC_ELCL_REMOVE_FROM_BP = create(T_ELCL,
	 * "remove_from_buildpath.gif"); public static final ImageDescriptor
	 * DESC_ELCL_INCLUSION = create(T_ELCL, "inclusion_filter_attrib.gif");
	 * public static final ImageDescriptor DESC_ELCL_EXCLUSION = create(T_ELCL,
	 * "exclusion_filter_attrib.gif"); // public static final ImageDescriptor
	 * DESC_ELCL_INCLUSION_UNDO= // create(T_ELCL,
	 * "inclusion_filter_attrib_undo.gif"); // public static final
	 * ImageDescriptor DESC_ELCL_EXCLUSION_UNDO= // create(T_ELCL,
	 * "exclusion_filter_attrib_undo.gif"); public static final ImageDescriptor
	 * DESC_DLCL_ADD_TO_BP = create(T_DLCL, "add_to_buildpath.gif"); public
	 * static final ImageDescriptor DESC_DLCL_REMOVE_FROM_BP = create(T_DLCL,
	 * "remove_from_buildpath.gif"); public static final ImageDescriptor
	 * DESC_DLCL_INCLUSION = create(T_DLCL, "inclusion_filter_attrib.gif");
	 * public static final ImageDescriptor DESC_DLCL_EXCLUSION = create(T_DLCL,
	 * "exclusion_filter_attrib.gif"); public static final ImageDescriptor
	 * DESC_DLCL_OUTPUT_FOLDER_ATTRIB = create(T_DLCL,
	 * "output_folder_attrib.gif"); // public static final ImageDescriptor
	 * DESC_DLCL_INCLUSION_UNDO= // create(T_DLCL,
	 * "inclusion_filter_attrib_undo.gif"); // public static final
	 * ImageDescriptor DESC_DLCL_EXCLUSION_UNDO= // create(T_DLCL,
	 * "exclusion_filter_attrib_undo.gif"); public static final ImageDescriptor
	 * DESC_DLCL_ADD_LINKED_SOURCE_TO_BUILDPATH = create(T_DLCL,
	 * "add_linked_source_to_buildpath.gif"); public static final
	 * ImageDescriptor DESC_ELCL_ADD_LINKED_SOURCE_TO_BUILDPATH = create(T_ELCL,
	 * "add_linked_source_to_buildpath.gif"); public static final
	 * ImageDescriptor DESC_DLCL_CONFIGURE_BUILDPATH = create(T_DLCL,
	 * "configure_build_path.gif"); public static final ImageDescriptor
	 * DESC_ELCL_CONFIGURE_BUILDPATH = create(T_ELCL,
	 * "configure_build_path.gif"); public static final ImageDescriptor
	 * DESC_DLCL_CONFIGURE_BUILDPATH_FILTERS = create(T_DLCL,
	 * "configure_buildpath_filters.gif"); public static final ImageDescriptor
	 * DESC_ELCL_CONFIGURE_BUILDPATH_FILTERS = create(T_ELCL,
	 * "configure_buildpath_filters.gif"); public static final ImageDescriptor
	 * DESC_DLCL_CONFIGURE_OUTPUT_FOLDER = create(T_DLCL,
	 * "configure_output_folder.gif"); public static final ImageDescriptor
	 * DESC_ELCL_CONFIGURE_OUTPUT_FOLDER = create(T_ELCL,
	 * "configure_output_folder.gif"); public static final ImageDescriptor
	 * DESC_DLCL_EXCLUDE_FROM_BUILDPATH = create(T_DLCL,
	 * "exclude_from_buildpath.gif"); public static final ImageDescriptor
	 * DESC_ELCL_EXCLUDE_FROM_BUILDPATH = create(T_ELCL,
	 * "exclude_from_buildpath.gif"); public static final ImageDescriptor
	 * DESC_DLCL_INCLUDE_ON_BUILDPATH = create(T_DLCL,
	 * "include_on_buildpath.gif"); public static final ImageDescriptor
	 * DESC_ELCL_INCLUDE_ON_BUILDPATH = create(T_ELCL,
	 * "include_on_buildpath.gif"); public static final ImageDescriptor
	 * DESC_DLCL_ADD_AS_SOURCE_FOLDER = create(T_DLCL,
	 * "add_as_source_folder.gif"); public static final ImageDescriptor
	 * DESC_ELCL_ADD_AS_SOURCE_FOLDER = create(T_ELCL,
	 * "add_as_source_folder.gif"); public static final ImageDescriptor
	 * DESC_DLCL_REMOVE_AS_SOURCE_FOLDER = create(T_DLCL,
	 * "remove_as_source_folder.gif"); public static final ImageDescriptor
	 * DESC_ELCL_REMOVE_AS_SOURCE_FOLDER = create(T_ELCL,
	 * "remove_as_source_folder.gif"); public static final ImageDescriptor
	 * DESC_OBJ_IMPLEMENTS = create(T_OBJ, "implm_co.gif"); public static final
	 * ImageDescriptor DESC_OVR_ABSTRACT = create(T_OVR, "abstract_co.gif");
	 * public static final ImageDescriptor DESC_OVR_SYNCH = create(T_OVR,
	 * "synch_co.gif"); public static final ImageDescriptor DESC_OVR_RUN =
	 * create(T_OVR, "run_co.gif"); public static final ImageDescriptor
	 * DESC_OVR_SYNCH_AND_OVERRIDES = create(T_OVR, "sync_over.gif"); public
	 * static final ImageDescriptor DESC_OVR_SYNCH_AND_IMPLEMENTS =
	 * create(T_OVR, "sync_impl.gif"); public static final ImageDescriptor
	 * DESC_OVR_CONSTRUCTOR = create(T_OVR, "constr_ovr.gif"); public static
	 * final ImageDescriptor DESC_OVR_DEPRECATED = create(T_OVR,
	 * "deprecated.gif"); public static final ImageDescriptor DESC_OVR_FOCUS =
	 * create(T_OVR, "focus_ovr.gif"); // Call Hierarchy public static final
	 * ImageDescriptor DESC_OVR_RECURSIVE = create(T_OVR, "recursive_co.gif");
	 * public static final ImageDescriptor DESC_OVR_MAX_LEVEL = create(T_OVR,
	 * "maxlevel_co.gif"); public static final ImageDescriptor
	 * DESC_WIZBAN_NEWCLASS = create(T_WIZBAN, "newclass_wiz.gif"); public
	 * static final ImageDescriptor DESC_WIZBAN_NEWINT = create(T_WIZBAN,
	 * "newint_wiz.gif"); public static final ImageDescriptor
	 * DESC_WIZBAN_NEWENUM = create(T_WIZBAN, "newenum_wiz.gif"); public static
	 * final ImageDescriptor DESC_WIZBAN_NEWANNOT = create(T_WIZBAN,
	 * "newannotation_wiz.gif"); public static final ImageDescriptor
	 * DESC_WIZBAN_NEWJPRJ = create(T_WIZBAN, "newjprj_wiz.gif"); public static
	 * final ImageDescriptor DESC_WIZBAN_NEWSRCFOLDR = create(T_WIZBAN,
	 * "newsrcfldr_wiz.gif"); public static final ImageDescriptor
	 * DESC_WIZBAN_NEWMETH = create(T_WIZBAN, "newmeth_wiz.gif"); public static
	 * final ImageDescriptor DESC_WIZBAN_NEWSCRAPPAGE = create(T_WIZBAN,
	 * "newsbook_wiz.gif"); public static final ImageDescriptor
	 * DESC_WIZBAN_JAVA_LAUNCH = create(T_WIZBAN, "java_app_wiz.gif"); public
	 * static final ImageDescriptor DESC_WIZBAN_JAVA_ATTACH = create(T_WIZBAN,
	 * "java_attach_wiz.gif"); public static final ImageDescriptor
	 * DESC_WIZBAN_REFACTOR_FIELD = create(T_WIZBAN, "fieldrefact_wiz.gif");
	 * public static final ImageDescriptor DESC_WIZBAN_REFACTOR_METHOD =
	 * create(T_WIZBAN, "methrefact_wiz.gif"); public static final
	 * ImageDescriptor DESC_WIZBAN_REFACTOR_CODE = create(T_WIZBAN,
	 * "coderefact_wiz.gif"); public static final ImageDescriptor
	 * DESC_WIZBAN_REFACTOR_PULL_UP = create(T_WIZBAN, "pullup_wiz.gif"); public
	 * static final ImageDescriptor DESC_WIZBAN_JAR_PACKAGER = create(T_WIZBAN,
	 * "jar_pack_wiz.gif"); public static final ImageDescriptor
	 * DESC_WIZBAN_JAVA_WORKINGSET = create(T_WIZBAN,
	 * "java_workingset_wiz.gif");//$NON-NLS-1$ public static final
	 * ImageDescriptor DESC_WIZBAN_EXPORT_JAVADOC = create(T_WIZBAN,
	 * "export_javadoc_wiz.gif");//$NON-NLS-1$ public static final
	 * ImageDescriptor DESC_WIZBAN_EXTERNALIZE_STRINGS = create(T_WIZBAN,
	 * "extstr_wiz.gif");//$NON-NLS-1$ public static final ImageDescriptor
	 * DESC_WIZBAN_ADD_LIBRARY = create(T_WIZBAN,
	 * "addlibrary_wiz.gif");//$NON-NLS-1$ public static final ImageDescriptor
	 * DESC_TOOL_SHOW_EMPTY_PKG = create(T_ETOOL, "show_empty_pkg.gif"); public
	 * static final ImageDescriptor DESC_TOOL_SHOW_SEGMENTS = create(T_ETOOL,
	 * "segment_edit.gif"); public static final ImageDescriptor
	 * DESC_TOOL_OPENTYPE = create(T_ETOOL, "opentype.gif"); public static final
	 * ImageDescriptor DESC_TOOL_NEWPROJECT = create(T_ETOOL,
	 * "newjprj_wiz.gif"); public static final ImageDescriptor
	 * DESC_TOOL_NEWPACKAGE = create(T_ETOOL, "newpack_wiz.gif"); public static
	 * final ImageDescriptor DESC_TOOL_NEWCLASS = create(T_ETOOL,
	 * "newclass_wiz.gif"); public static final ImageDescriptor
	 * DESC_TOOL_NEWINTERFACE = create(T_ETOOL, "newint_wiz.gif"); public static
	 * final ImageDescriptor DESC_TOOL_NEWSNIPPET = create(T_ETOOL,
	 * "newsbook_wiz.gif"); public static final ImageDescriptor
	 * DESC_TOOL_NEWPACKROOT = create(T_ETOOL, "newpackfolder_wiz.gif"); public
	 * static final ImageDescriptor DESC_DLCL_NEWPACKROOT = create(T_DLCL,
	 * "newpackfolder_wiz.gif"); public static final ImageDescriptor
	 * DESC_TOOL_CLASSPATH_ORDER = create(T_OBJ, "cp_order_obj.gif"); public
	 * static final ImageDescriptor DESC_ELCL_COLLAPSEALL = create(T_ELCL,
	 * "collapseall.gif"); // Keys for correction proposal. We have to put the
	 * image into the registry // since "code assist" doesn't // have a life
	 * cycle. So no change to dispose icons. public static final String
	 * IMG_CORRECTION_CHANGE = NAME_PREFIX + "correction_change.gif"; public
	 * static final String IMG_CORRECTION_MOVE = NAME_PREFIX +
	 * "correction_move.gif"; public static final String IMG_CORRECTION_RENAME =
	 * NAME_PREFIX + "correction_rename.gif"; public static final String
	 * IMG_CORRECTION_DELETE_IMPORT = NAME_PREFIX +
	 * "correction_delete_import.gif"; public static final String
	 * IMG_CORRECTION_LOCAL = NAME_PREFIX + "localvariable_obj.gif"; public
	 * static final String IMG_CORRECTION_REMOVE = NAME_PREFIX +
	 * "remove_correction.gif"; public static final String IMG_CORRECTION_ADD =
	 * NAME_PREFIX + "add_correction.gif"; public static final String
	 * IMG_CORRECTION_CAST = NAME_PREFIX + "correction_cast.gif";
	 */

	static {
		// createManaged(T_OBJ, IMG_CORRECTION_CHANGE);
		// createManaged(T_OBJ, IMG_CORRECTION_MOVE);
		// createManaged(T_OBJ, IMG_CORRECTION_RENAME);
		// createManaged(T_OBJ, IMG_CORRECTION_DELETE_IMPORT);
		// createManaged(T_OBJ, IMG_CORRECTION_LOCAL);
		// createManaged(T_OBJ, IMG_CORRECTION_REMOVE);
		// createManaged(T_OBJ, IMG_CORRECTION_ADD);
		// createManaged(T_OBJ, IMG_CORRECTION_CAST);
	}

	public static Image get(String key) {
		return getImageRegistry().get(key);
	}

	public static ImageDescriptor getDescriptor(String key) {
		if(fgImageRegistry == null) {
			return fgAvoidSWTErrorMap.get(key);
		}
		return getImageRegistry().getDescriptor(key);
	}

	public static void setToolImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "tool16", iconName);
	}

	public static void setLocalImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "lcl16", iconName);
	}

	static ImageRegistry getImageRegistry() {
		if(fgImageRegistry == null) {
			fgImageRegistry = new ImageRegistry();
			for(String key : fgAvoidSWTErrorMap.keySet()) {
				fgImageRegistry.put(key, (ImageDescriptor)fgAvoidSWTErrorMap.get(key));
			}
			fgAvoidSWTErrorMap = null;
		}
		return fgImageRegistry;
	}

	private static void setImageDescriptors(IAction action, String type, String relPath) {
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL("d" + type, relPath));
			if(id != null)
				action.setDisabledImageDescriptor(id);
		} catch(MalformedURLException e) {
		}

		ImageDescriptor descriptor = create("e" + type, relPath);
		action.setHoverImageDescriptor(descriptor);
		action.setImageDescriptor(descriptor);
	}

	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));

			if(fgAvoidSWTErrorMap == null) {
				fgAvoidSWTErrorMap = new HashMap<String, ImageDescriptor>();
			}

			fgAvoidSWTErrorMap.put(name, result);

			if(fgImageRegistry != null) {
				Plugin.log(IStatus.ERROR, "Image registry already defined", 0, null);
			}

			return result;
		} catch(MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch(MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if(fgIconBaseURL == null)
			throw new MalformedURLException();

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(fgIconBaseURL, buffer.toString());
	}
}
