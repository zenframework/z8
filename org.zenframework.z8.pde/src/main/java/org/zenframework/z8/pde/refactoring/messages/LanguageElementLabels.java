package org.zenframework.z8.pde.refactoring.messages;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.type.members.MemberInit;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;

public class LanguageElementLabels {
	/**
	 * Method names contain parameter types. e.g. <code>foo(int)</code>
	 */
	public final static long M_PARAMETER_TYPES = 1L << 0;
	/**
	 * Method names contain parameter names. e.g. <code>foo(index)</code>
	 */
	public final static long M_PARAMETER_NAMES = 1L << 1;
	/**
	 * Method names contain type parameters prepended. e.g.
	 * <code><A> foo(A index)</code>
	 */
	public final static long M_PRE_TYPE_PARAMETERS = 1L << 2;
	/**
	 * Method names contain type parameters appended. e.g.
	 * <code>foo(A index) <A></code>
	 */
	public final static long M_APP_TYPE_PARAMETERS = 1L << 3;
	/**
	 * Method names contain thrown exceptions. e.g.
	 * <code>foo throws IOException</code>
	 */
	public final static long M_EXCEPTIONS = 1L << 4;
	/**
	 * Method names contain return type (appended) e.g. <code>foo : int</code>
	 */
	public final static long M_APP_RETURNTYPE = 1L << 5;
	/**
	 * Method names contain return type (appended) e.g. <code>int foo</code>
	 */
	public final static long M_PRE_RETURNTYPE = 1L << 6;
	/**
	 * Method names are fully qualified. e.g. <code>java.util.Vector.size</code>
	 */
	public final static long M_FULLY_QUALIFIED = 1L << 7;
	/**
	 * Method names are post qualified. e.g.
	 * <code>size - java.util.Vector</code>
	 */
	public final static long M_POST_QUALIFIED = 1L << 8;
	/**
	 * Initializer names are fully qualified. e.g.
	 * <code>java.util.Vector.{ ... }</code>
	 */
	public final static long I_FULLY_QUALIFIED = 1L << 10;
	/**
	 * Type names are post qualified. e.g. <code>{ ... } - java.util.Map</code>
	 */
	public final static long I_POST_QUALIFIED = 1L << 11;
	/**
	 * Field names contain the declared type (appended) e.g.
	 * <code>fHello : int</code>
	 */
	public final static long F_APP_TYPE_SIGNATURE = 1L << 14;
	/**
	 * Field names contain the declared type (prepended) e.g.
	 * <code>int fHello</code>
	 */
	public final static long F_PRE_TYPE_SIGNATURE = 1L << 15;
	/**
	 * Fields names are fully qualified. e.g. <code>java.lang.System.out</code>
	 */
	public final static long F_FULLY_QUALIFIED = 1L << 16;
	/**
	 * Fields names are post qualified. e.g. <code>out - java.lang.System</code>
	 */
	public final static long F_POST_QUALIFIED = 1L << 17;
	/**
	 * Type names are fully qualified. e.g. <code>java.util.Map.MapEntry</code>
	 */
	public final static long T_FULLY_QUALIFIED = 1L << 18;
	/**
	 * Type names are type container qualified. e.g. <code>Map.MapEntry</code>
	 */
	public final static long T_CONTAINER_QUALIFIED = 1L << 19;
	/**
	 * Type names are post qualified. e.g. <code>MapEntry - java.util.Map</code>
	 */
	public final static long T_POST_QUALIFIED = 1L << 20;
	/**
	 * Type names contain type parameters. e.g. <code>Map&lt;S, T&gt;</code>
	 */
	public final static long T_TYPE_PARAMETERS = 1L << 21;
	/**
	 * Declarations (import container / declaration, package declaration) are
	 * qualified. e.g. <code>java.util.Vector.class/import container</code>
	 */
	public final static long D_QUALIFIED = 1L << 24;
	/**
	 * Declarations (import container / declaration, package declaration) are
	 * post qualified. e.g.
	 * <code>import container - java.util.Vector.class</code>
	 */
	public final static long D_POST_QUALIFIED = 1L << 25;
	/**
	 * Class file names are fully qualified. e.g.
	 * <code>java.util.Vector.class</code>
	 */
	public final static long CF_QUALIFIED = 1L << 27;
	/**
	 * Class file names are post qualified. e.g.
	 * <code>Vector.class - java.util</code>
	 */
	public final static long CF_POST_QUALIFIED = 1L << 28;
	/**
	 * Compilation unit names are fully qualified. e.g.
	 * <code>java.util.Vector.java</code>
	 */
	public final static long CU_QUALIFIED = 1L << 31;
	/**
	 * Compilation unit names are post qualified. e.g.
	 * <code>Vector.java - java.util</code>
	 */
	public final static long CU_POST_QUALIFIED = 1L << 32;
	/**
	 * Package names are qualified. e.g. <code>MyProject/src/java.util</code>
	 */
	public final static long P_QUALIFIED = 1L << 35;
	/**
	 * Package names are post qualified. e.g.
	 * <code>java.util - MyProject/src</code>
	 */
	public final static long P_POST_QUALIFIED = 1L << 36;
	/**
	 * Package names are compressed. e.g. <code>o*.e*.search</code>
	 */
	public final static long P_COMPRESSED = 1L << 37;
	/**
	 * Package Fragment Roots contain variable name if from a variable. e.g.
	 * <code>JRE_LIB - c:\java\lib\rt.jar</code>
	 */
	public final static long ROOT_VARIABLE = 1L << 40;
	/**
	 * Package Fragment Roots contain the project name if not an archive
	 * (prepended). e.g. <code>MyProject/src</code>
	 */
	public final static long ROOT_QUALIFIED = 1L << 41;
	/**
	 * Package Fragment Roots contain the project name if not an archive
	 * (appended). e.g. <code>src - MyProject</code>
	 */
	public final static long ROOT_POST_QUALIFIED = 1L << 42;
	/**
	 * Add root path to all elements except Package Fragment Roots and Java
	 * projects. e.g. <code>java.lang.Vector - c:\java\lib\rt.jar</code> Option
	 * only applies to getElementLabel
	 */
	public final static long APPEND_ROOT_PATH = 1L << 43;
	/**
	 * Add root path to all elements except Package Fragment Roots and Java
	 * projects. e.g. <code>java.lang.Vector - c:\java\lib\rt.jar</code> Option
	 * only applies to getElementLabel
	 */
	public final static long PREPEND_ROOT_PATH = 1L << 44;
	/**
	 * Post qualify referenced package fragment roots. For example
	 * <code>jdt.jar - org.eclipse.jdt.ui</code> if the jar is referenced from
	 * another project.
	 */
	public final static long REFERENCED_ROOT_POST_QUALIFIED = 1L << 45;
	/**
	 * Specified to use the resolved information of a IType, IMethod or IField.
	 * See {@link IType#isResolved()}. If resolved information is available,
	 * types will be rendered with type parameters of the instantiated type.
	 * Resolved method render with the parameter types of the method instance.
	 * <code>Vector<String>.get(String)</code>
	 */
	public final static long USE_RESOLVED = 1L << 48;
	/**
	 * Prepend first category (if any) to field.
	 * 
	 * @since 3.2
	 */
	public final static long F_CATEGORY = 1L << 49;
	/**
	 * Prepend first category (if any) to method.
	 * 
	 * @since 3.2
	 */
	public final static long M_CATEGORY = 1L << 50;
	/**
	 * Prepend first category (if any) to type.
	 * 
	 * @since 3.2
	 */
	public final static long T_CATEGORY = 1L << 51;
	/**
	 * Show category for all elements.
	 * 
	 * @since 3.2
	 */
	public final static long ALL_CATEGORY = new Long(F_CATEGORY | M_CATEGORY | T_CATEGORY).longValue();
	/**
	 * Qualify all elements
	 */
	public final static long ALL_FULLY_QUALIFIED = new Long(F_FULLY_QUALIFIED | M_FULLY_QUALIFIED | I_FULLY_QUALIFIED | T_FULLY_QUALIFIED | D_QUALIFIED | CF_QUALIFIED | CU_QUALIFIED | P_QUALIFIED | ROOT_QUALIFIED).longValue();
	/**
	 * Post qualify all elements
	 */
	public final static long ALL_POST_QUALIFIED = new Long(F_POST_QUALIFIED | M_POST_QUALIFIED | I_POST_QUALIFIED | T_POST_QUALIFIED | D_POST_QUALIFIED | CF_POST_QUALIFIED | CU_POST_QUALIFIED | P_POST_QUALIFIED | ROOT_POST_QUALIFIED).longValue();
	/**
	 * Default options (M_PARAMETER_TYPES, M_APP_TYPE_PARAMETERS &
	 * T_TYPE_PARAMETERS enabled)
	 */
	public final static long ALL_DEFAULT = new Long(M_PARAMETER_TYPES | M_APP_TYPE_PARAMETERS | T_TYPE_PARAMETERS).longValue();
	/**
	 * Default qualify options (All except Root and Package)
	 */
	public final static long DEFAULT_QUALIFIED = new Long(F_FULLY_QUALIFIED | M_FULLY_QUALIFIED | I_FULLY_QUALIFIED | T_FULLY_QUALIFIED | D_QUALIFIED | CF_QUALIFIED | CU_QUALIFIED).longValue();
	/**
	 * Default post qualify options (All except Root and Package)
	 */
	public final static long DEFAULT_POST_QUALIFIED = new Long(F_POST_QUALIFIED | M_POST_QUALIFIED | I_POST_QUALIFIED | T_POST_QUALIFIED | D_POST_QUALIFIED | CF_POST_QUALIFIED | CU_POST_QUALIFIED).longValue();
	/**
	 * User-readable string for separating post qualified names (e.g. " - ").
	 */
	public final static String CONCAT_STRING = RefactoringMessages.LanguageElementLabels_concat_string;
	/**
	 * User-readable string for separating list items (e.g. ", ").
	 */
	public final static String COMMA_STRING = RefactoringMessages.LanguageElementLabels_comma_string;
	/**
	 * User-readable string for separating the return type (e.g. " : ").
	 */
	public final static String DECL_STRING = RefactoringMessages.LanguageElementLabels_declseparator_string;
	/**
	 * User-readable string for ellipsis ("...").
	 */
	public final static String ELLIPSIS_STRING = "...";
	/**
	 * User-readable string for the default package name (e.g. "(default
	 * package)").
	 */
	private final static long QUALIFIER_FLAGS = P_COMPRESSED | USE_RESOLVED;

	private LanguageElementLabels() {
	}

	private static final boolean getFlag(long flags, long flag) {
		return (flags & flag) != 0;
	}

	public static String getTextLabel(Object obj, long flags) {
		if(obj instanceof ILanguageElement) {
			return getElementLabel((ILanguageElement)obj, flags);
		} else if(obj instanceof IAdaptable) {
			IWorkbenchAdapter wbadapter = (IWorkbenchAdapter)((IAdaptable)obj).getAdapter(IWorkbenchAdapter.class);
			if(wbadapter != null) {
				return wbadapter.getLabel(obj);
			}
		}
		return "";
	}

	public static String getElementLabel(ILanguageElement element, long flags) {
		StringBuffer buf = new StringBuffer(60);
		getElementLabel(element, flags, buf);
		return buf.toString();
	}

	public static void getElementLabel(ILanguageElement element, long flags, StringBuffer buf) {
		Folder root = null;

		if(element instanceof Project || element instanceof Folder) {
			root = (Folder)element;
		}

		if(root != null && getFlag(flags, PREPEND_ROOT_PATH)) {
			getFolderLabel1(root, ROOT_QUALIFIED, buf);
			buf.append(CONCAT_STRING);
		}

		if(element instanceof IMethod) {
			getMethodLabel((IMethod)element, flags, buf);
		} else if(element instanceof IMember) {
			getFieldLabel((IMember)element, flags, buf);
		} else if(element instanceof MemberInit) {
			getInitializerLabel((MemberInit)element, flags, buf);
		} else if(element instanceof IType) {
			getTypeLabel((IType)element, flags, buf);
		} else if(element instanceof CompilationUnit) {
			getCompilationUnitLabel((CompilationUnit)element, flags, buf);
		} else if(element instanceof Project) {
			buf.append(((Project)element).getName());
		} else if(element instanceof Folder) {
			getFolderLabel((Folder)element, flags, buf);
		}

		if(root != null && getFlag(flags, APPEND_ROOT_PATH)) {
			buf.append(CONCAT_STRING);
			getFolderLabel1(root, ROOT_QUALIFIED, buf);
		}
	}

	public static void getMethodLabel(IMethod method, long flags, StringBuffer buf) {
		if(getFlag(flags, M_PRE_RETURNTYPE)) {
			buf.append(method.getVariableType().getSignature());
			buf.append(' ');
		}

		if(getFlag(flags, M_FULLY_QUALIFIED)) {
			getTypeLabel(method.getDeclaringType(), T_FULLY_QUALIFIED | (flags & QUALIFIER_FLAGS), buf);
			buf.append('.');
		}

		buf.append(method.getName());

		buf.append('(');

		IVariableType[] types = getFlag(flags, M_PARAMETER_TYPES) ? method.getParameterTypes() : null;
		String[] names = method.getParameterNames();
		int nParams = method.getParametersCount();

		if(getFlag(flags, M_PARAMETER_TYPES | M_PARAMETER_NAMES)) {

			for(int i = 0; i < nParams; i++) {
				if(i > 0) {
					buf.append(COMMA_STRING);
				}

				if(types != null) {
					buf.append(types[i].getSignature());
				}

				if(names != null) {
					if(types != null) {
						buf.append(' ');
					}
					buf.append(names[i]);
				}
			}
		} else {
			if(nParams > 0) {
				buf.append(ELLIPSIS_STRING);
			}
		}

		buf.append(')');

		if(getFlag(flags, M_APP_RETURNTYPE)) {
			buf.append(DECL_STRING);
			buf.append(method.getVariableType().getSignature());
		}

		// post qualification
		if(getFlag(flags, M_POST_QUALIFIED)) {
			buf.append(CONCAT_STRING);
			getTypeLabel(method.getDeclaringType(), T_FULLY_QUALIFIED | (flags & QUALIFIER_FLAGS), buf);
		}
	}

	public static void getFieldLabel(IMember member, long flags, StringBuffer buf) {
		if(getFlag(flags, F_PRE_TYPE_SIGNATURE) && !member.getVariableType().isEnum()) {
			buf.append(member.getVariableType().getSignature());
			buf.append(' ');
		}

		if(getFlag(flags, F_FULLY_QUALIFIED)) {
			getTypeLabel(member.getDeclaringType(), T_FULLY_QUALIFIED | (flags & QUALIFIER_FLAGS), buf);
			buf.append('.');

		}
		buf.append(member.getName());

		if(getFlag(flags, F_APP_TYPE_SIGNATURE) && !member.getVariableType().isEnum()) {
			buf.append(DECL_STRING);
			buf.append(member.getVariableType().getSignature());
		}

		if(getFlag(flags, F_POST_QUALIFIED)) {
			buf.append(CONCAT_STRING);
			getTypeLabel(member.getDeclaringType(), T_FULLY_QUALIFIED | (flags & QUALIFIER_FLAGS), buf);
		}
	}

	public static void getLocalVariableLabel(IVariable localVariable, long flags, StringBuffer buf) {
		if(getFlag(flags, F_PRE_TYPE_SIGNATURE)) {
			buf.append(localVariable.getVariableType().getSignature());
			buf.append(' ');
		}

		buf.append(localVariable.getName());

		if(getFlag(flags, F_APP_TYPE_SIGNATURE)) {
			buf.append(DECL_STRING);
			buf.append(localVariable.getVariableType().getSignature());
		}
	}

	public static void getInitializerLabel(MemberInit initializer, long flags, StringBuffer buf) {
		// qualification
		if(getFlag(flags, I_FULLY_QUALIFIED)) {
			getTypeLabel(initializer.getDeclaringType(), T_FULLY_QUALIFIED | (flags & QUALIFIER_FLAGS), buf);
			buf.append('.');
		}

		buf.append(RefactoringMessages.LanguageElementLabels_initializer);

		// post qualification
		if(getFlag(flags, I_POST_QUALIFIED)) {
			buf.append(CONCAT_STRING);
			getTypeLabel(initializer.getDeclaringType(), T_FULLY_QUALIFIED | (flags & QUALIFIER_FLAGS), buf);
		}
	}

	public static void getTypeLabel(IType type, long flags, StringBuffer buf) {
		if(getFlag(flags, T_FULLY_QUALIFIED)) {
			Folder pack = type.getCompilationUnit().getFolder();
			getFolderLabel(pack, (flags & QUALIFIER_FLAGS), buf);
			buf.append('.');
		}

		if(getFlag(flags, T_FULLY_QUALIFIED | T_CONTAINER_QUALIFIED)) {
			IType containerType = type.getContainerType();

			if(containerType != null) {
				getTypeLabel(containerType, T_CONTAINER_QUALIFIED | (flags & QUALIFIER_FLAGS), buf);
				buf.append('.');
			}

		}

		String typeName = type.getUserName();

		if(typeName.length() == 0) {
			typeName = '{' + ELLIPSIS_STRING + '}';
		}

		buf.append(typeName);

		if(getFlag(flags, T_POST_QUALIFIED)) {
			buf.append(CONCAT_STRING);

			IType containerType = type.getContainerType();

			if(containerType != null) {
				getTypeLabel(containerType, T_FULLY_QUALIFIED | (flags & QUALIFIER_FLAGS), buf);
			} else {
				getFolderLabel(type.getCompilationUnit().getFolder(), flags & QUALIFIER_FLAGS, buf);
			}
		}
	}

	public static void getCompilationUnitLabel(CompilationUnit cu, long flags, StringBuffer buf) {
		if(getFlag(flags, CU_QUALIFIED)) {
			Folder pack = cu.getFolder();

			getFolderLabel(pack, (flags & QUALIFIER_FLAGS), buf);
			buf.append('.');
		}

		buf.append(cu.getName());

		if(getFlag(flags, CU_POST_QUALIFIED)) {
			buf.append(CONCAT_STRING);
			getFolderLabel1(cu.getFolder(), flags & QUALIFIER_FLAGS, buf);
		}
	}

	public static void getFolderLabel(Folder pack, long flags, StringBuffer buf) {
		if(getFlag(flags, P_QUALIFIED)) {
			getFolderLabel1(pack.getFolder(), ROOT_QUALIFIED, buf);
			buf.append('/');
		}

		buf.append(pack.getName());

		if(getFlag(flags, P_POST_QUALIFIED)) {
			buf.append(CONCAT_STRING);
			getFolderLabel1(pack.getFolder(), ROOT_QUALIFIED, buf);
		}
	}

	private static void getFolderLabel1(Folder root, long flags, StringBuffer buf) {
		IResource resource = root.getResource();

		boolean rootQualified = getFlag(flags, ROOT_QUALIFIED);
		boolean referencedQualified = getFlag(flags, REFERENCED_ROOT_POST_QUALIFIED);

		if(rootQualified) {
			buf.append(root.getPath().makeRelative().toString());
		} else {
			if(resource != null)
				buf.append(resource.getProjectRelativePath().toString());
			else
				buf.append(root.getName());

			if(referencedQualified) {
				buf.append(CONCAT_STRING);
				buf.append(resource.getProject().getName());
			} else if(getFlag(flags, ROOT_POST_QUALIFIED)) {
				buf.append(CONCAT_STRING);
				buf.append(root.getFolder().getName());
			}
		}
	}
}
