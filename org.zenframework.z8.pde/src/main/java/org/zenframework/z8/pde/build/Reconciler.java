package org.zenframework.z8.pde.build;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.editor.Z8Editor;

public class Reconciler extends MonoReconciler {
	private class PartListener implements IPartListener {
		@Override
		public void partActivated(IWorkbenchPart part) {
			if(part == m_editor && isWaitingForReconciling()) {
				forceReconciling();
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
			if(part == m_editor) {
			}
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}
	}

	private class ActivationListener extends ShellAdapter {
		private Control fControl;

		public ActivationListener(Control control) {
			fControl = control;
		}

		@Override
		public void shellActivated(ShellEvent e) {
			if(!fControl.isDisposed() && fControl.isVisible() && isWaitingForReconciling()) {
				forceReconciling();
			}
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			if(!fControl.isDisposed() && fControl.isVisible()) {
			}
		}
	}

	private class ResourceChangeListener implements IResourceChangeListener {
		@Override
		public void resourceChanged(IResourceChangeEvent e) {
			IResourceDelta delta = e.getDelta();

			IResource resource = m_editor.getResource();

			if(delta != null && resource != null) {
				if(delta.findMember(resource.getFullPath()) != null) {
					forceReconciling();
				}
			}
		}
	}

	private Z8Editor m_editor;

	private IPartListener m_partListener;
	private ShellListener m_activationListener;
	private IResourceChangeListener m_resourceChangeListener;

	private Object fMutex;

	private boolean m_ininitalProcessDone;

	public Reconciler(Z8Editor editor, ReconcilingStrategy strategy, boolean isIncremental) {
		super(strategy, isIncremental);
		m_editor = editor;
		fMutex = new Object();
	}

	@Override
	public void install(ITextViewer textViewer) {
		super.install(textViewer);

		m_partListener = new PartListener();
		IWorkbenchPartSite site = m_editor.getSite();
		IWorkbenchWindow window = site.getWorkbenchWindow();
		window.getPartService().addPartListener(m_partListener);

		m_activationListener = new ActivationListener(textViewer.getTextWidget());
		Shell shell = window.getShell();
		shell.addShellListener(m_activationListener);

		m_resourceChangeListener = new ResourceChangeListener();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(m_resourceChangeListener);
	}

	@Override
	public void uninstall() {
		IWorkbenchPartSite site = m_editor.getSite();
		IWorkbenchWindow window = site.getWorkbenchWindow();
		window.getPartService().removePartListener(m_partListener);
		m_partListener = null;

		Shell shell = window.getShell();
		if(shell != null && !shell.isDisposed())
			shell.removeShellListener(m_activationListener);
		m_activationListener = null;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(m_resourceChangeListener);
		m_resourceChangeListener = null;

		super.uninstall();
	}

	@Override
	protected void forceReconciling() {
		if(!m_ininitalProcessDone)
			return;

		super.forceReconciling();

		ReconcilingStrategy strategy = (ReconcilingStrategy)getReconcilingStrategy(IDocument.DEFAULT_CONTENT_TYPE);
		strategy.notifyListeners(false);
	}

	@Override
	protected void aboutToBeReconciled() {
		ReconcilingStrategy strategy = (ReconcilingStrategy)getReconcilingStrategy(IDocument.DEFAULT_CONTENT_TYPE);
		strategy.aboutToBeReconciled();
	}

	@Override
	protected void reconcilerReset() {
		super.reconcilerReset();
		ReconcilingStrategy strategy = (ReconcilingStrategy)getReconcilingStrategy(IDocument.DEFAULT_CONTENT_TYPE);
		strategy.notifyListeners(true);
	}

	@Override
	protected void initialProcess() {
		synchronized(fMutex) {
			super.initialProcess();
		}
		m_ininitalProcessDone = true;
	}

	@Override
	protected void process(DirtyRegion dirtyRegion) {
		synchronized(fMutex) {
			super.process(dirtyRegion);
		}
	}

	protected boolean isWaitingForReconciling() {
		CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(m_editor.getResource());

		if(compilationUnit != null) {
			return true; // compilationUnit.isBuildPending();
		}
		return false;
	}
}
