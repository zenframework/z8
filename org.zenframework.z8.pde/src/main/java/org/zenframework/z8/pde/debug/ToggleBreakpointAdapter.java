package org.zenframework.z8.pde.debug;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.debug.breakpoints.JDXLineBreakpoint;
import org.zenframework.z8.pde.debug.model.JDXDebugModel;

public class ToggleBreakpointAdapter implements IToggleBreakpointsTargetExtension {
	@Override
	public void toggleWatchpoints(final IWorkbenchPart part, final ISelection finalSelection) {
	}

	@Override
	public void toggleMethodBreakpoints(final IWorkbenchPart part, final ISelection finalSelection) {
	}

	public ToggleBreakpointAdapter() {
	}

	@Override
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		toggleLineBreakpoints(part, selection, false);
	}

	public void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection, final boolean bestMatch) {
		Job job = new Job("Toggle Line Breakpoint") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if(selection instanceof ITextSelection) {
					if(monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}

					final IEditorPart editorPart = (IEditorPart)part;
					ITextSelection textSelection = (ITextSelection)selection;
					IEditorInput editorInput = editorPart.getEditorInput();
					IDocumentProvider documentProvider = ((ITextEditor)editorPart).getDocumentProvider();

					if(documentProvider == null) {
						return Status.CANCEL_STATUS;
					}

					IDocument document = documentProvider.getDocument(editorInput);
					int lineNumber = textSelection.getStartLine();

					try {
						// statusLine.setErrorMessage(MessageFormat.format(ActionMessages.ManageBreakpointRulerAction_Breakpoints_can_only_be_created_within_the_type_associated_with_the_editor___0___1,
						// new String[] { type.getTypeQualifiedName() }));

						IResource resource = getResource(editorPart);
						CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(resource);

						String statusMessage = "Breakpoint cannot be toggled here";

						if(compilationUnit != null) {
							IType type = compilationUnit.getReconciledType();

							if(type != null) {
								// int targetLineNumber =
								// type.getCompilationUnit().getTargetLineNumber(lineNumber)
								// + 1;

								// if(targetLineNumber != 0)
								// {
								String typeName = type.getQualifiedJavaName();

								JDXLineBreakpoint existingBreakpoint = JDXDebugModel.lineBreakpointExists(resource, typeName, lineNumber + 1);
								if(existingBreakpoint != null) {
									removeBreakpoint(existingBreakpoint, true);
								} else {
									createLineBreakpoint(resource, typeName, lineNumber + 1, -1/* targetLineNumber */, -1, -1, 0, true, null, document, bestMatch, editorPart);
								}

								statusMessage = null;
								// }
							}
						}

						final String message = statusMessage;
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								IStatusLineManager statusLine = editorPart.getEditorSite().getActionBars().getStatusLineManager();
								statusLine.setErrorMessage(message);
								Display.getCurrent().beep();
							}
						});
					} catch(CoreException ce) {
						return ce.getStatus();
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private void createLineBreakpoint(IResource resource, String typeName, int lineNumber, int javaLineNumber, int charStart, int charEnd, int hitCount, boolean register, Map<String, Object> attributes, IDocument document, boolean bestMatch,
			/* IType type, */IEditorPart editorPart) throws CoreException {
		JDXDebugModel.createLineBreakpoint(resource, typeName, lineNumber, javaLineNumber, charStart, charEnd, hitCount, register, attributes);
		// new BreakpointLocationVerifierJob(document, breakpoint, lineNumber,
		// bestMatch, typeName, type, resource, editorPart).schedule();
	}

	@Override
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return selection instanceof ITextSelection;
	}

	/*
	 * public void toggleMethodBreakpoints(final IWorkbenchPart part, final
	 * ISelection finalSelection) { Job job = new
	 * Job("Toggle Method Breakpoints") { protected IStatus run(IProgressMonitor
	 * monitor) { if(monitor.isCanceled()) { return Status.CANCEL_STATUS; } try
	 * { report(null, part); ISelection selection = finalSelection; selection =
	 * translateToMembers(part, selection); if(selection instanceof
	 * ITextSelection) { ITextSelection textSelection =
	 * (ITextSelection)selection; if(selection != null) { CompilationUnit
	 * compilationUnit = parseCompilationUnit((ITextEditor)part);
	 * if(compilationUnit != null) { BreakpointMethodLocator locator = new
	 * BreakpointMethodLocator(textSelection.getOffset());
	 * compilationUnit.accept(locator); String methodName =
	 * locator.getMethodName(); if(methodName == null) {
	 * report(ActionMessages.ManageMethodBreakpointActionDelegate_CantAdd,
	 * part); //$NON-NLS-1$ return Status.OK_STATUS; } String typeName =
	 * locator.getTypeName(); String methodSignature =
	 * locator.getMethodSignature(); if(methodSignature == null) {
	 * report(ActionMessages.
	 * ManageMethodBreakpointActionDelegate_methodNonAvailable, part);
	 * //$NON-NLS-1$ return Status.OK_STATUS; } // check if this method
	 * breakpoint already // exist. If yes, remove it. final IBreakpointManager
	 * breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
	 * IBreakpoint[] breakpoints =
	 * breakpointManager.getBreakpoints(JDXDebugModel.getModelIdentifier());
	 * for(int i = 0; i < breakpoints.length; i++) { IBreakpoint breakpoint =
	 * breakpoints[i];
	 * 
	 * if(breakpoint instanceof JDXMethodBreakpoint) { final JDXMethodBreakpoint
	 * methodBreakpoint = (JDXMethodBreakpoint)breakpoint;
	 * if(typeName.equals(methodBreakpoint.getTypeName()) &&
	 * methodName.equals(methodBreakpoint.getMethodName()) &&
	 * methodSignature.equals(methodBreakpoint.getMethodSignature())) {
	 * removeBreakpoint(breakpoint, true); return Status.OK_STATUS; } } } // add
	 * the breakpoint createMethodBreakpoint(getResource((IEditorPart)part),
	 * typeName, methodName, methodSignature, true, false, false, -1, -1, -1, 0,
	 * true, new HashMap<String, Object>(10)); } } } else if(selection
	 * instanceof IStructuredSelection) F { IMethod[] members =
	 * getMethods((IStructuredSelection)selection);
	 * 
	 * if(members.length == 0) {
	 * report(ActionMessages.ToggleBreakpointAdapter_9, part); return
	 * Status.OK_STATUS; }
	 * 
	 * for(int i = 0, length = members.length; i < length; i++) { IMethod method
	 * = members[i]; JDXBreakpoint breakpoint = getBreakpoint(method);
	 * if(breakpoint == null) { int start = -1; int end = -1;
	 * 
	 * ISourceRange range = method.getNameRange();
	 * 
	 * if(range != null) { start = range.getOffset(); end = start +
	 * range.getLength(); }
	 * 
	 * Map attributes = new HashMap(10);
	 * 
	 * BreakpointUtils.addJavaBreakpointAttributes(attributes, method);
	 * 
	 * IType type = method.getDeclaringType();
	 * 
	 * String methodSignature = method.getSignature(); String methodName =
	 * method.getElementName();
	 * 
	 * if(method.isConstructor()) { methodName = "<init>"; //$NON-NLS-1$
	 * if(type.isEnum()) { methodSignature = "(Ljava.lang.String;I" +
	 * methodSignature.substring(1); //$NON-NLS-1$ } } if(!type.isBinary()) { //
	 * resolve the type names methodSignature = resolveMethodSignature(type,
	 * methodSignature); if(methodSignature == null) { return new
	 * Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(),
	 * IStatus.ERROR, "Source method signature could not be resolved", null); }
	 * } createMethodBreakpoint(BreakpointUtils.getBreakpointResource(method),
	 * type.getFullyQualifiedName(), methodName, methodSignature, true, false,
	 * false, -1, start, end, 0, true, attributes); } else { // remove
	 * breakpoint removeBreakpoint(breakpoint, true); } } } }
	 * catch(CoreException e) { return e.getStatus(); } return Status.OK_STATUS;
	 * } }; job.setSystem(true); job.schedule(); }
	 */
	private void removeBreakpoint(IBreakpoint breakpoint, boolean delete) throws CoreException {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, delete);
	}

	/*
	 * private void createMethodBreakpoint(IResource resource, String typeName,
	 * String methodName, String methodSignature, boolean entry, boolean exit,
	 * boolean nativeOnly, int lineNumber, int javaLineNumber, int charStart,
	 * int charEnd, int hitCount, boolean register, Map<String, Object>
	 * attributes) throws CoreException {
	 * JDXDebugModel.createMethodBreakpoint(resource, typeName, methodName,
	 * methodSignature, entry, exit, nativeOnly, lineNumber, javaLineNumber,
	 * charStart, charEnd, hitCount, register, attributes); }
	 */
	@Override
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		// if(selection instanceof IStructuredSelection)
		// {
		// IStructuredSelection ss = (IStructuredSelection)selection;
		// return getMethods(ss).length > 0;
		// }
		return selection instanceof ITextSelection;
	}

	/*
	 * protected IMethod[] getMethods(IStructuredSelection selection) {
	 * if(selection.isEmpty()) { return new IMethod[0]; } List methods = new
	 * ArrayList(selection.size()); Iterator iterator = selection.iterator();
	 * while(iterator.hasNext()) { Object thing = iterator.next(); try {
	 * if(thing instanceof IMethod &&
	 * !Flags.isAbstract(((IMethod)thing).getFlags())) { methods.add(thing); } }
	 * catch(JavaModelException e) { } } return (IMethod[])methods.toArray(new
	 * IMethod[methods.size()]); }
	 * 
	 * protected IField[] getFields(IStructuredSelection selection) {
	 * if(selection.isEmpty()) { return new IField[0]; } List fields = new
	 * ArrayList(selection.size()); Iterator iterator = selection.iterator();
	 * while(iterator.hasNext()) { Object thing = iterator.next(); if(thing
	 * instanceof IField) { fields.add(thing); } else if(thing instanceof
	 * IJavaFieldVariable) { IField field = getField((IJavaFieldVariable)thing);
	 * if(field != null) { fields.add(field); } } } return
	 * (IField[])fields.toArray(new IField[fields.size()]); }
	 * 
	 * private boolean isFields(IStructuredSelection selection) {
	 * if(!selection.isEmpty()) { Iterator iterator = selection.iterator();
	 * while(iterator.hasNext()) { Object thing = iterator.next(); if(!(thing
	 * instanceof IField || thing instanceof IJavaFieldVariable)) { return
	 * false; } } return true; } return false; }
	 * 
	 * public void toggleWatchpoints(final IWorkbenchPart part, final ISelection
	 * finalSelection) { Job job = new Job("Toggle Watchpoints") { protected
	 * IStatus run(IProgressMonitor monitor) { if(monitor.isCanceled()) { return
	 * Status.CANCEL_STATUS; } try { report(null, part); ISelection selection =
	 * finalSelection; selection = translateToMembers(part, selection);
	 * if(selection instanceof ITextSelection) { ITextSelection textSelection =
	 * (ITextSelection)selection; CompilationUnit compilationUnit =
	 * parseCompilationUnit((ITextEditor)part); if(compilationUnit != null) {
	 * BreakpointFieldLocator locator = new
	 * BreakpointFieldLocator(textSelection.getOffset());
	 * compilationUnit.accept(locator); String fieldName =
	 * locator.getFieldName(); if(fieldName == null) {
	 * report(ActionMessages.ManageWatchpointActionDelegate_CantAdd, part);
	 * //$NON-NLS-1$ return Status.OK_STATUS; } String typeName =
	 * locator.getTypeName(); // check if the watchpoint already exists. If yes,
	 * // remove it IBreakpointManager breakpointManager =
	 * DebugPlugin.getDefault().getBreakpointManager(); IBreakpoint[]
	 * breakpoints =
	 * breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
	 * for(int i = 0; i < breakpoints.length; i++) { IBreakpoint breakpoint =
	 * breakpoints[i]; if(breakpoint instanceof IJavaWatchpoint) {
	 * IJavaWatchpoint watchpoint = (IJavaWatchpoint)breakpoint;
	 * if(typeName.equals(watchpoint.getTypeName()) &&
	 * fieldName.equals(watchpoint.getFieldName())) {
	 * removeBreakpoint(watchpoint, true); return Status.OK_STATUS; } } } // add
	 * the watchpoint createWatchpoint(getResource((IEditorPart)part), typeName,
	 * fieldName, -1, -1, -1, 0, true, new HashMap(10)); } } else if(selection
	 * instanceof IStructuredSelection) { IField[] members =
	 * getFields((IStructuredSelection)selection); if(members.length == 0) {
	 * report(ActionMessages.ToggleBreakpointAdapter_10, part); //$NON-NLS-1$
	 * return Status.OK_STATUS; } for(int i = 0, length = members.length; i <
	 * length; i++) { IField element = members[i]; IJavaBreakpoint breakpoint =
	 * getBreakpoint(element); if(breakpoint == null) { IType type =
	 * element.getDeclaringType(); int start = -1; int end = -1; ISourceRange
	 * range = element.getNameRange(); if(range != null) { start =
	 * range.getOffset(); end = start + range.getLength(); } Map attributes =
	 * new HashMap(10); BreakpointUtils.addJavaBreakpointAttributes(attributes,
	 * element); createWatchpoint(BreakpointUtils.getBreakpointResource(type),
	 * type.getFullyQualifiedName(), element.getElementName(), -1, start, end,
	 * 0, true, attributes); } else { // remove breakpoint
	 * removeBreakpoint(breakpoint, true); } } } } catch(CoreException e) {
	 * return e.getStatus(); } return Status.OK_STATUS; } };
	 * job.setSystem(true); job.schedule(); }
	 * 
	 * private void createWatchpoint(IResource resource, String typeName, String
	 * fieldName, int lineNumber, int charStart, int charEnd, int hitCount,
	 * boolean register, Map attributes) throws CoreException {
	 * JDXDebugModel.createWatchpoint(resource, typeName, fieldName, lineNumber,
	 * charStart, charEnd, hitCount, register, attributes); }
	 * 
	 * public static String resolveMethodSignature(IType type, String
	 * methodSignature) throws JavaModelException { String[] parameterTypes =
	 * Signature.getParameterTypes(methodSignature); int length = length =
	 * parameterTypes.length; String[] resolvedParameterTypes = new
	 * String[length]; for(int i = 0; i < length; i++) {
	 * resolvedParameterTypes[i] = resolveType(type, parameterTypes[i]);
	 * if(resolvedParameterTypes[i] == null) { return null; } } String
	 * resolvedReturnType = resolveType(type,
	 * Signature.getReturnType(methodSignature)); if(resolvedReturnType == null)
	 * { return null; } return
	 * Signature.createMethodSignature(resolvedParameterTypes,
	 * resolvedReturnType); }
	 * 
	 * private static String resolveType(IType type, String typeSignature)
	 * throws JavaModelException { int count =
	 * Signature.getArrayCount(typeSignature); String elementTypeSignature =
	 * Signature.getElementType(typeSignature); if(elementTypeSignature.length()
	 * == 1) { // no need to resolve primitive types return typeSignature; }
	 * String elementTypeName = Signature.toString(elementTypeSignature);
	 * String[][] resolvedElementTypeNames = type.resolveType(elementTypeName);
	 * if(resolvedElementTypeNames == null || resolvedElementTypeNames.length !=
	 * 1) { // the type name cannot be resolved return null; } String
	 * resolvedElementTypeName =
	 * Signature.toQualifiedName(resolvedElementTypeNames[0]); String
	 * resolvedElementTypeSignature =
	 * Signature.createTypeSignature(resolvedElementTypeName, true).replace('.',
	 * '/'); return Signature.createArraySignature(resolvedElementTypeSignature,
	 * count); }
	 */
	protected static IResource getResource(IEditorPart editor) {
		IEditorInput editorInput = editor.getEditorInput();
		IResource resource = (IResource)editorInput.getAdapter(IFile.class);
		if(resource == null) {
			resource = ResourcesPlugin.getWorkspace().getRoot();
		}
		return resource;
	}

	/*
	 * protected IMethod getMethodHandle(IEditorPart editorPart, String
	 * typeName, String methodName, String signature) throws CoreException {
	 * IJavaElement element =
	 * (IJavaElement)editorPart.getEditorInput().getAdapter(IJavaElement.class);
	 * IType type = null; if(element instanceof ICompilationUnit) { IType[]
	 * types = ((ICompilationUnit)element).getAllTypes(); for(int i = 0; i <
	 * types.length; i++) {
	 * if(types[i].getFullyQualifiedName().equals(typeName)) { type = types[i];
	 * break; } } } else if(element instanceof IClassFile) { type =
	 * ((IClassFile)element).getType(); } if(type != null) { String[] sigs =
	 * Signature.getParameterTypes(signature); return type.getMethod(methodName,
	 * sigs); } return null; }
	 * 
	 * protected IJavaBreakpoint getBreakpoint(IMember element) {
	 * IBreakpointManager breakpointManager =
	 * DebugPlugin.getDefault().getBreakpointManager(); IBreakpoint[]
	 * breakpoints =
	 * breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
	 * if(element instanceof IMethod) { IMethod method = (IMethod)element;
	 * for(int i = 0; i < breakpoints.length; i++) { IBreakpoint breakpoint =
	 * breakpoints[i]; if(breakpoint instanceof IJavaMethodBreakpoint) {
	 * IJavaMethodBreakpoint methodBreakpoint =
	 * (IJavaMethodBreakpoint)breakpoint; IMember container = null; try {
	 * container = BreakpointUtils.getMember(methodBreakpoint); }
	 * catch(CoreException e) { JDIDebugUIPlugin.log(e); return null; }
	 * if(container == null) { try {
	 * if(method.getDeclaringType().getFullyQualifiedName().equals(
	 * methodBreakpoint.getTypeName()) &&
	 * method.getElementName().equals(methodBreakpoint.getMethodName()) &&
	 * method.getSignature().equals(methodBreakpoint.getMethodSignature())) {
	 * return methodBreakpoint; } } catch(CoreException e) {
	 * JDIDebugUIPlugin.log(e); } } else { if(container instanceof IMethod) {
	 * if(method.getDeclaringType().getFullyQualifiedName().equals(container.
	 * getDeclaringType().getFullyQualifiedName())) {
	 * if(method.isSimilar((IMethod)container)) { return methodBreakpoint; } } }
	 * } } } } else if(element instanceof IField) { for(int i = 0; i <
	 * breakpoints.length; i++) { IBreakpoint breakpoint = breakpoints[i];
	 * if(breakpoint instanceof IJavaWatchpoint) { try { if(equalFields(element,
	 * (IJavaWatchpoint)breakpoint)) return (IJavaBreakpoint)breakpoint; }
	 * catch(CoreException e) { JDIDebugUIPlugin.log(e); } } } } return null; }
	 * 
	 * private boolean equalFields(IMember breakpointField, IJavaWatchpoint
	 * watchpoint) throws CoreException { return
	 * (breakpointField.getElementName().equals(watchpoint.getFieldName()) &&
	 * breakpointField.getDeclaringType().getFullyQualifiedName().equals(
	 * watchpoint.getTypeName())); }
	 * 
	 * protected CompilationUnit parseCompilationUnit(ITextEditor editor) throws
	 * CoreException { IEditorInput editorInput = editor.getEditorInput();
	 * IDocumentProvider documentProvider = editor.getDocumentProvider();
	 * if(documentProvider == null) { throw new
	 * CoreException(Status.CANCEL_STATUS); } IDocument document =
	 * documentProvider.getDocument(editorInput); ASTParser parser =
	 * ASTParser.newParser(AST.JLS3);
	 * parser.setSource(document.get().toCharArray()); return
	 * (CompilationUnit)parser.createAST(null); }
	 */
	@Override
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		// if(selection instanceof IStructuredSelection)
		// {
		// IStructuredSelection ss = (IStructuredSelection)selection;
		// return isFields(ss);
		// }
		return selection instanceof ITextSelection;
	}

	/*
	 * protected ISelection translateToMembers(IWorkbenchPart part, ISelection
	 * selection) throws CoreException { if(selection instanceof ITextSelection
	 * && part instanceof ITextEditor) { ITextSelection textSelection =
	 * (ITextSelection)selection; ITextEditor editorPart = (ITextEditor)part;
	 * IEditorInput editorInput = editorPart.getEditorInput(); IDocumentProvider
	 * documentProvider = editorPart.getDocumentProvider(); if(documentProvider
	 * == null) { throw new CoreException(Status.CANCEL_STATUS); } IDocument
	 * document = documentProvider.getDocument(editorInput); int offset =
	 * textSelection.getOffset(); if(document != null) { try { IRegion region =
	 * document.getLineInformationOfOffset(offset); int end = region.getOffset()
	 * + region.getLength();
	 * while(Character.isWhitespace(document.getChar(offset)) && offset < end) {
	 * offset++; } } catch(BadLocationException e) { } } IMember m = null;
	 * IClassFile classFile =
	 * (IClassFile)editorInput.getAdapter(IClassFile.class); if(classFile !=
	 * null) { IJavaElement e = classFile.getElementAt(offset); if(e instanceof
	 * IMember) { m = (IMember)e; } } else { IWorkingCopyManager manager =
	 * JavaUI.getWorkingCopyManager(); ICompilationUnit unit =
	 * manager.getWorkingCopy(editorInput); if(unit != null) {
	 * synchronized(unit) { unit.reconcile(ICompilationUnit.NO_AST don't create
	 * ast , false don't force problem detection , null use primary owner , null
	 * no progress monitor ); } IJavaElement e = unit.getElementAt(offset); if(e
	 * instanceof IMember) { m = (IMember)e; } } } if(m != null) { return new
	 * StructuredSelection(m); } } return selection; }
	 * 
	 *//**
		 * Returns a list of matching types (IType - Java model) that correspond
		 * to the given type name in the context of the given launch.
		 */
	/*
	 * protected static List searchForTypes(String typeName, ILaunch launch) {
	 * List types = new ArrayList(); if(launch == null) { return types; }
	 * ILaunchConfiguration configuration = launch.getLaunchConfiguration();
	 * IJavaProject[] javaProjects = null; IWorkspace workspace =
	 * ResourcesPlugin.getWorkspace(); if(configuration != null) { // Launch
	 * configuration support try { String projectName =
	 * configuration.getAttribute(IJavaLaunchConfigurationConstants.
	 * ATTR_PROJECT_NAME, ""); //$NON-NLS-1$ if(projectName.length() != 0) {
	 * javaProjects = new IJavaProject[] {
	 * JavaCore.create(workspace.getRoot().getProject(projectName)) }; } else {
	 * IProject[] projects =
	 * ResourcesPlugin.getWorkspace().getRoot().getProjects(); IProject project;
	 * List projectList = new ArrayList(); for(int i = 0, numProjects =
	 * projects.length; i < numProjects; i++) { project = projects[i];
	 * if(project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
	 * projectList.add(JavaCore.create(project)); } } javaProjects = new
	 * IJavaProject[projectList.size()]; projectList.toArray(javaProjects); } }
	 * catch(CoreException e) { JDIDebugUIPlugin.log(e); } } if(javaProjects ==
	 * null) { return types; } SearchEngine engine = new SearchEngine();
	 * IJavaSearchScope scope = SearchEngine.createJavaSearchScope(javaProjects,
	 * true); ArrayList typeRefsFound = new ArrayList(3); TypeInfoRequestor
	 * requestor = new TypeInfoRequestor(typeRefsFound); try {
	 * engine.searchAllTypeNames(getPackage(typeName), getTypeName(typeName),
	 * SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
	 * IJavaSearchConstants.TYPE, scope, requestor,
	 * IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null); }
	 * catch(JavaModelException x) { JDIDebugUIPlugin.log(x); return types; }
	 * Iterator iter = typeRefsFound.iterator(); TypeInfo typeInfo = null;
	 * while(iter.hasNext()) { typeInfo = (TypeInfo)iter.next(); try {
	 * types.add(typeInfo.resolveType(scope)); } catch(JavaModelException jme) {
	 * JDIDebugUIPlugin.log(jme); } } return types; }
	 * 
	 *//**
		 * Returns the package name of the given fully qualified type name. The
		 * package name is assumed to be the dot-separated prefix of the type
		 * name.
		 */
	/*
	 * private static char[] getPackage(String fullyQualifiedName) { int index =
	 * fullyQualifiedName.lastIndexOf('.'); if(index == -1) { return new
	 * char[0]; } return fullyQualifiedName.substring(0, index).toCharArray(); }
	 * 
	 *//**
		 * Returns a simple type name from the given fully qualified type name.
		 * The type name is assumed to be the last contiguous segment of the
		 * fullyQualifiedName not containing a '.' or '$'
		 */

	/*
	 * private static char[] getTypeName(String fullyQualifiedName) { int index
	 * = fullyQualifiedName.lastIndexOf('.'); String typeName =
	 * fullyQualifiedName; if(index >= 0) { typeName =
	 * fullyQualifiedName.substring(index + 1); } index =
	 * typeName.lastIndexOf('$'); if(index >= 0) { typeName =
	 * typeName.substring(index + 1); } return typeName.toCharArray(); }
	 * 
	 *//**
		 * Return the associated IField (Java model) for the given
		 * IJavaFieldVariable (JDI model)
		 */
	/*
	 * private IField getField(IJavaFieldVariable variable) { String varName =
	 * null; try { varName = variable.getName(); } catch(DebugException x) {
	 * JDIDebugUIPlugin.log(x); return null; } IField field; String
	 * declaringType = null; try { declaringType =
	 * variable.getDeclaringType().getName(); } catch(DebugException x) {
	 * JDIDebugUIPlugin.log(x); return null; } List types =
	 * searchForTypes(declaringType, variable.getLaunch()); Iterator iter =
	 * types.iterator(); while(iter.hasNext()) { IType type =
	 * (IType)iter.next(); field = type.getField(varName); if(field.exists()) {
	 * return field; } } return null; }
	 */
	@Override
	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		toggleLineBreakpoints(part, selection, true);
	}

	@Override
	public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
		return canToggleLineBreakpoints(part, selection);
	}
}
