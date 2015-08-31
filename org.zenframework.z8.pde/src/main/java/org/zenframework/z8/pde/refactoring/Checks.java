package org.zenframework.z8.pde.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.pde.debug.util.Signature;
import org.zenframework.z8.pde.refactoring.changes.RenameResourceChange;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

// Îøèáêà Access restriction
//import com.sun.org.apache.xpath.internal.Expression;

public class Checks {
    private Checks() {}

    public static final int IS_RVALUE = 0;
    public static final int NOT_RVALUE_MISC = 1;
    public static final int NOT_RVALUE_VOID = 2;

    public static RefactoringStatus checkFieldName(String name) {
        return checkName(name, LanguageConventions.validateFieldName(name));
    }

    public static RefactoringStatus checkTypeParameterName(String name) {
        return checkName(name, LanguageConventions.validateTypeVariableName(name));
    }

    public static RefactoringStatus checkIdentifier(String name) {
        return checkName(name, LanguageConventions.validateIdentifier(name));
    }

    public static RefactoringStatus checkMethodName(String name) {
        RefactoringStatus status = checkName(name, LanguageConventions.validateMethodName(name));

        if(status.isOK() && startsWithUpperCase(name)) {
            return RefactoringStatus.createWarningStatus(RefactoringMessages.Checks_method_names_lowercase);
        }

        return status;
    }

    public static RefactoringStatus checkTypeName(String name) {
        if(name.indexOf(".") != -1) {
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.Checks_no_dot);
        }

        return checkName(name, LanguageConventions.validateTypeName(name));
    }

    public static RefactoringStatus checkFolderName(String name) {
        return checkName(name, LanguageConventions.validateFolderName(name));
    }

    public static RefactoringStatus checkCompilationUnitName(String name) {
        return checkName(name, LanguageConventions.validateCompilationUnitName(name));
    }

    public static RefactoringStatus checkCompilationUnitNewName(CompilationUnit cu, String newName) {
        if(resourceExists(RenameResourceChange.renamedResourcePath(cu.getResource().getFullPath(), newName + ".bl"))) {
            return RefactoringStatus.createFatalErrorStatus(Messages
                    .format(RefactoringMessages.Checks_cu_name_used, newName));
        }

        return new RefactoringStatus();
    }

    private static boolean startsWithUpperCase(String s) {
        if(s == null) {
            return false;
        }
        else if("".equals(s)) {
            return false;
        }

        return s.charAt(0) == Character.toUpperCase(s.charAt(0));
    }

    public static boolean startsWithLowerCase(String s) {
        if(s == null) {
            return false;
        }
        else if("".equals(s)) {
            return false;
        }

        return s.charAt(0) == Character.toLowerCase(s.charAt(0));
    }

    public static boolean resourceExists(IPath resourcePath) {
        return ResourcesPlugin.getWorkspace().getRoot().findMember(resourcePath) != null;
    }

    public static boolean isTopLevel(IType type) {
        return type.getContainerType() == null;
    }

    public static boolean isAlreadyNamed(CompilationUnit compilationUnit, String name) {
        return name.equals(compilationUnit.getName());
    }

    public static boolean isAlreadyNamed(ILanguageElement element, String name) {
        if(element instanceof IType) {
            IType type = (IType)element;
            return name.equals(type.getUserName());
        }
        return false;
    }

    public static RefactoringStatus checkForNativeMethods(IType type) throws CoreException {
        RefactoringStatus result = new RefactoringStatus();
        result.merge(checkForNativeMethods(type.getMethods()));
        return result;
    }

    public static RefactoringStatus checkForNativeMethods(CompilationUnit cu) throws CoreException {
        return checkForNativeMethods(cu.getReconciledType());
    }

    private static RefactoringStatus checkForNativeMethods(IMethod[] methods) throws CoreException {
        if(methods == null)
            return null;

        RefactoringStatus result = new RefactoringStatus();

        for(IMethod method : methods) {
            if(method.isNative()) {
                String msg = Messages.format(RefactoringMessages.Checks_method_native, new String[] {
                        method.getDeclaringType().getQualifiedUserName(), method.getSignature(), "UnsatisfiedLinkError" });
                result.addError(msg);
            }
        }
        return result;
    }

    public static RefactoringStatus checkMethodInType(IType type, String methodName, IVariable[] parameters, Project scope) {
        RefactoringStatus result = new RefactoringStatus();

        //		IMethod method = null;//org.eclipse.jdt.internal.corext.dom.Bindings.findMethodInType(type, methodName, parameters);

        /*		if(method != null)
        		{
        			String message = Messages.format(RefactoringMessages.Checks_methodName_exists, new Object[] { methodName, type.getUserName() });
        			result.addError(message);
        		}
        */
        return result;
    }

    /*
    	public static RefactoringStatus checkMethodInHierarchy(IType type, String methodName, IVariableType returnType, IVariable[] parameters, Project scope)
    	{
    		RefactoringStatus result = new RefactoringStatus();
    		
    		IMethod method = Bindings.findMethodInHierarchy(type, methodName, parameters);
    		
    		if(method != null)
    		{
    			boolean returnTypeClash = false;
    			ITypeBinding methodReturnType = method.getReturnType();
    			if(returnType != null && methodReturnType != null)
    			{
    				String returnTypeKey = returnType.getKey();
    				String methodReturnTypeKey = methodReturnType.getKey();
    				if(returnTypeKey == null && methodReturnTypeKey == null)
    				{
    					returnTypeClash = returnType != methodReturnType;
    				}
    				else if(returnTypeKey != null && methodReturnTypeKey != null)
    				{
    					returnTypeClash = !returnTypeKey.equals(methodReturnTypeKey);
    				}
    			}
    			ITypeBinding dc = method.getDeclaringClass();
    			if(returnTypeClash)
    			{
    				result.addError(Messages.format(RefactoringCoreMessages.Checks_methodName_returnTypeClash, new Object[]
    				{
    					methodName, dc.getName() }), JavaStatusContext.create(method, scope));
    			}
    			else
    			{
    				result.addError(Messages.format(RefactoringCoreMessages.Checks_methodName_overrides, new Object[]
    				{
    					methodName, dc.getName() }), JavaStatusContext.create(method, scope));
    			}
    		}
    		return result;
    	}
    */

    private static RefactoringStatus checkName(String name, IStatus status) {
        RefactoringStatus result = new RefactoringStatus();

        if("".equals(name)) {
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.Checks_Choose_name);
        }

        if(status.isOK()) {
            return result;
        }

        switch(status.getSeverity()) {
        case IStatus.ERROR:
            return RefactoringStatus.createFatalErrorStatus(status.getMessage());
        case IStatus.WARNING:
            return RefactoringStatus.createWarningStatus(status.getMessage());
        case IStatus.INFO:
            return RefactoringStatus.createInfoStatus(status.getMessage());
        default:
            return new RefactoringStatus();
        }
    }

    public static IMethod findMethod(String name, int parameterCount, boolean isConstructor, IType type) {
        return findMethod(name, parameterCount, type.getMethods());
    }

    public static IMethod findMethod(IMethod method, IType type) {
        return findMethod(method, type.getMethods());
    }

    public static IMethod findMethod(IMethod method, IMethod[] methods) {
        return findMethod(method.getName(), method.getParametersCount(), methods);
    }

    public static IMethod findMethod(String name, int parameters, IMethod[] methods) {
        for(int i = methods.length - 1; i >= 0; i--) {
            IMethod curr = methods[i];

            if(name.equals(curr.getName())) {
                if(parameters == curr.getParametersCount()) {
                    return curr;
                }
            }
        }

        return null;
    }

    public static IMethod findSimilarMethod(IMethod method, IType type) {
        return findSimilarMethod(method, type.getMethods());
    }

    public static IMethod findSimilarMethod(IMethod method, IMethod[] methods) {
        for(IMethod m : methods) {
            if(method.getSignature().equals(m.getSignature())) {
                return m;
            }
        }
        return null;
    }

    public static boolean compareParamTypes(String[] paramTypes1, String[] paramTypes2) {
        if(paramTypes1.length == paramTypes2.length) {
            int i = 0;

            while(i < paramTypes1.length) {
                String t1 = Signature.getSimpleName(Signature.toString(paramTypes1[i]));
                String t2 = Signature.getSimpleName(Signature.toString(paramTypes2[i]));
                if(!t1.equals(t2)) {
                    return false;
                }
                i++;
            }
            return true;
        }
        return false;
    }

    public static RefactoringStatus checkIfCuBroken(ILanguageElement element) throws CoreException {
        CompilationUnit cu = element.getCompilationUnit();

        if(cu == null) {
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.Checks_cu_not_created);
        }
        else if(cu.containsParseError()) {
            return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.Checks_cu_not_parsed);
        }
        return new RefactoringStatus();
    }

    public static CompilationUnit[] excludeCompilationUnits(CompilationUnit[] compilationUnits, RefactoringStatus status)
            throws CoreException {
        List<CompilationUnit> result = new ArrayList<CompilationUnit>();

        boolean wasEmpty = compilationUnits.length == 0;

        for(CompilationUnit compilationUnit : compilationUnits) {
            if(compilationUnit.containsParseError()) {
                String path = Checks.getFullPath(compilationUnit);
                status.addError(Messages.format(RefactoringMessages.Checks_cannot_be_parsed, path));
                continue;
            }

            result.add(compilationUnit);
        }

        if((!wasEmpty) && result.isEmpty()) {
            status.addFatalError(RefactoringMessages.Checks_all_excluded);
        }

        return result.toArray(new CompilationUnit[0]);
    }

    private static final String getFullPath(CompilationUnit compilationUnit) {
        return compilationUnit.getResource().getFullPath().toString();
    }

    public static RefactoringStatus checkCompileErrorsInAffectedFiles(CompilationUnit[] compilationUnits)
            throws CoreException {
        RefactoringStatus result = new RefactoringStatus();

        for(CompilationUnit compilationUnit : compilationUnits) {
            checkCompileErrorsInAffectedFile(result, compilationUnit);
        }

        return result;
    }

    public static void checkCompileErrorsInAffectedFile(RefactoringStatus result, CompilationUnit compilationUnit)
            throws CoreException {
        if(compilationUnit.containsError()) {
            result.addWarning(Messages.format(RefactoringMessages.Checks_cu_has_compile_errors, compilationUnit
                    .getResource().getFullPath().makeRelative()));
        }
    }

    public static RefactoringStatus checkCompileErrorsInAffectedFiles(CompilationUnit[] compilationUnits,
            CompilationUnit declaring) throws CoreException {
        RefactoringStatus result = new RefactoringStatus();

        for(CompilationUnit unit : compilationUnits) {
            if(unit == declaring)
                declaring = null;

            checkCompileErrorsInAffectedFile(result, declaring);
        }

        if(declaring != null)
            checkCompileErrorsInAffectedFile(result, declaring);

        return result;
    }

    public static boolean isReadOnly(IResource res) throws CoreException {
        ResourceAttributes attributes = res.getResourceAttributes();

        if(attributes != null && attributes.isReadOnly())
            return true;

        if(!(res instanceof IContainer))
            return false;

        IContainer container = (IContainer)res;

        IResource[] children = container.members();
        for(int i = 0; i < children.length; i++) {
            if(isReadOnly(children[i]))
                return true;
        }
        return false;
    }

    public static RefactoringStatus validateModifiesFiles(IFile[] filesToModify, Object context) {
        RefactoringStatus result = new RefactoringStatus();

        IStatus status = Resources.checkInSync(filesToModify);

        if(!status.isOK())
            result.merge(RefactoringStatus.create(status));

        status = Resources.makeCommittable(filesToModify, context);

        if(!status.isOK()) {
            result.merge(RefactoringStatus.create(status));
            if(!result.hasFatalError()) {
                result.addFatalError(RefactoringMessages.Checks_validateEdit);
            }
        }
        return result;
    }

    public static RefactoringStatus validateEdit(CompilationUnit unit, Object context) {
        IResource resource = unit.getResource();

        RefactoringStatus result = new RefactoringStatus();

        if(resource == null)
            return result;

        IStatus status = Resources.checkInSync(resource);

        if(!status.isOK())
            result.merge(RefactoringStatus.create(status));

        status = Resources.makeCommittable(resource, context);

        if(!status.isOK()) {
            result.merge(RefactoringStatus.create(status));

            if(!result.hasFatalError()) {
                result.addFatalError(RefactoringMessages.Checks_validateEdit);
            }
        }
        return result;
    }

    public static RefactoringStatus checkAvailability(ILanguageElement element) throws CoreException {
        RefactoringStatus result = new RefactoringStatus();

        CompilationUnit compilationUnit = element.getCompilationUnit();

        IResource resource = compilationUnit.getResource();

        if(!resource.exists())
            result.addFatalError(Messages.format(RefactoringMessages.Refactoring_not_in_model, resource.getName()));

        if(resource.isAccessible())
            result.addFatalError(Messages.format(RefactoringMessages.Refactoring_read_only, resource.getName()));

        if(compilationUnit.containsParseError())
            result.addFatalError(Messages.format(RefactoringMessages.Refactoring_unknown_structure, resource.getName()));

        return result;
    }

    public static boolean isAvailable(ILanguageElement element) throws CoreException {
        if(element == null)
            return false;

        CompilationUnit compilationUnit = element.getCompilationUnit();

        IResource resource = compilationUnit.getResource();

        if(!resource.exists())
            return false;

        if(resource.isAccessible())
            return false;

        if(compilationUnit.containsParseError())
            return false;

        return true;
    }

    public static IType findTypeFolder(Folder folder, String name) throws CoreException {
        for(CompilationUnit compilationUnit : folder.getCompilationUnits()) {
            IType type = compilationUnit.getReconciledType();

            if(type != null && type.getUserName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    public static RefactoringStatus checkTempName(String newName) {
        RefactoringStatus result = Checks.checkIdentifier(newName);

        if(result.hasFatalError())
            return result;

        if(!Checks.startsWithLowerCase(newName))
            result.addWarning(RefactoringMessages.ExtractTempRefactoring_convention);
        return result;
    }

    public static RefactoringStatus checkEnumConstantName(String newName) {
        RefactoringStatus result = Checks.checkFieldName(newName);

        if(result.hasFatalError())
            return result;

        for(int i = 0; i < newName.length(); i++) {
            char c = newName.charAt(i);

            if(Character.isLetter(c) && !Character.isUpperCase(c)) {
                result.addWarning(RefactoringMessages.RenameEnumConstRefactoring_convention);
                break;
            }
        }
        return result;
    }

    public static RefactoringStatus checkConstantName(String newName) {
        RefactoringStatus result = Checks.checkFieldName(newName);

        if(result.hasFatalError())
            return result;

        for(int i = 0; i < newName.length(); i++) {
            char c = newName.charAt(i);

            if(Character.isLetter(c) && !Character.isUpperCase(c)) {
                result.addWarning(RefactoringMessages.ExtractConstantRefactoring_convention);
                break;
            }
        }
        return result;
    }

    public static boolean isException(IType type, IProgressMonitor pm) throws CoreException {
        try {
            IType baseType = type.getBaseType();

            while(baseType != null) {
                if(baseType.getUserName().equals(Primary.Exception)) {
                    return true;
                }
                baseType = type.getBaseType();
            }

            return false;
        }
        finally {
            pm.done();
        }
    }

    //public static int checkExpressionIsRValue(Expression e) {
        /*
         		if(e instanceof Name)
        		{
        			if(!(((Name)e).resolveBinding() instanceof IVariableBinding))
        			{
        				return NOT_RVALUE_MISC;
        			}
        		}
        		ITypeBinding tb = e.resolveTypeBinding();
        		if(tb == null)
        			return NOT_RVALUE_MISC;
        		else if(tb.getName().equals("void")) //$NON-NLS-1$
        			return NOT_RVALUE_VOID;
        		return IS_RVALUE;
        */
    //    return NOT_RVALUE_VOID;
    //}
}
