package org.zenframework.z8.pde.refactoring.action;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.editor.Z8Editor;

public class SelectionConverter {
    private static final ILanguageElement[] EMPTY_RESULT = new ILanguageElement[0];

    private SelectionConverter() {}

    public static IStructuredSelection getStructuredSelection(IWorkbenchPart part) {
        if(part instanceof Z8Editor) {
            return new StructuredSelection(codeResolve((Z8Editor)part));
        }

        ISelectionProvider provider = part.getSite().getSelectionProvider();

        if(provider != null) {
            ISelection selection = provider.getSelection();
            if(selection instanceof IStructuredSelection)
                return (IStructuredSelection)selection;
        }

        return StructuredSelection.EMPTY;
    }

    @SuppressWarnings("rawtypes")
    public static ILanguageElement[] getElements(IStructuredSelection selection) {
        if(!selection.isEmpty()) {
            ILanguageElement[] result = new ILanguageElement[selection.size()];

            int i = 0;

            for(Iterator iter = selection.iterator(); iter.hasNext(); i++) {
                Object element = iter.next();

                if(!(element instanceof ILanguageElement))
                    return EMPTY_RESULT;

                result[i] = (ILanguageElement)element;
            }

            return result;
        }
        return EMPTY_RESULT;
    }

    public static boolean canOperateOn(Z8Editor editor) {
        if(editor == null)
            return false;

        return getCompilationUnit(editor) != null;
    }

    public static ILanguageElement[] codeResolve(Z8Editor editor) {
        return codeResolve(editor, true);
    }

    public static ILanguageElement[] codeResolve(Z8Editor editor, boolean primaryOnly) {
        return codeResolve(getCompilationUnit(editor), (ITextSelection)editor.getSelectionProvider().getSelection());
    }

    public static ILanguageElement[] codeResolveForked(Z8Editor editor) throws InvocationTargetException,
            InterruptedException {
        return performForkedCodeResolve(getCompilationUnit(editor), (ITextSelection)editor.getSelectionProvider()
                .getSelection());
    }

    private static ILanguageElement getElementAtOffset(Z8Editor editor) {
        return getElementAtOffset(getCompilationUnit(editor), (ITextSelection)editor.getSelectionProvider().getSelection());
    }

    public static IType getTypeAtOffset(Z8Editor editor) {
        ILanguageElement element = SelectionConverter.getElementAtOffset(editor);

        if(element != null) {
            return element.getCompilationUnit().getReconciledType();
        }
        return null;
    }

    public static CompilationUnit getCompilationUnit(Z8Editor editor) {
        return Workspace.getInstance().getCompilationUnit(editor.getResource());
    }

    private static ILanguageElement[] performForkedCodeResolve(final CompilationUnit compilationUnit,
            final ITextSelection selection) throws InvocationTargetException, InterruptedException {
        final class CodeResolveRunnable implements IRunnableWithProgress {
            ILanguageElement[] result;

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    result = codeResolve(compilationUnit, selection);
                }
                catch(Exception e) {
                    throw new InvocationTargetException(e);
                }
            }
        }

        CodeResolveRunnable runnable = new CodeResolveRunnable();
        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
        return runnable.result;
    }

    public static ILanguageElement[] codeResolve(CompilationUnit compilationUnit, ITextSelection selection) {
        return EMPTY_RESULT;
    }

    public static ILanguageElement getElementAtOffset(CompilationUnit compilationUnit, ITextSelection selection) {
        //		----		compilationUnit.reconcile();
        return compilationUnit.getElementAt(selection.getOffset());
    }
}
