package org.zenframework.z8.pde;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Workspace;

public class EditorTestAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow m_window;

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
		m_window = window;
	}

	@Override
	public void run(IAction action) {
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					int count = 0;
					for(Project p : Workspace.getInstance().getProjects()) {
						count += p.getCompilationUnits().length;
					}
					monitor.beginTask("Editor test", count);
					for(final Project p : Workspace.getInstance().getProjects()) {
						for(final CompilationUnit u : p.getCompilationUnits()) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									try {
										IEditorPart editor = IDE.openEditor(m_window.getActivePage(), new FileEditorInput((IFile)u.getResource()), "org.zenframework.z8.forms.editors.MultiEditor", false);
										m_window.getActivePage().closeEditor(editor, false);
									} catch(Exception e) {
										Plugin.log(e);
									} finally {
										monitor.worked(1);
									}
								}
							});
						}
					}
					monitor.done();
				}

			});
		} catch(Exception e) {
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
