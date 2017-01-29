package org.zenframework.z8.pde.build;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.editor.Z8Editor;

public class ReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
	protected Z8Editor m_editor;
	protected IDocument m_document;

	protected IProgressMonitor fProgressMonitor;

	public ReconcilingStrategy(Z8Editor editor) {
		m_editor = editor;
	}

	private void reconcile(final boolean initialReconcile) {
		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void run() {
				try {
					IResource resource = m_editor.getResource();
					Project project = Workspace.getInstance().getProject(resource.getProject());

					if(project != null && resource != null) {
						char[] content = m_document.get().toCharArray();
						ReconcileMessageConsumer consumer = new ReconcileMessageConsumer();
						project.reconcile(resource, content, consumer);
					}
				} catch(OperationCanceledException ex) {
					assert (fProgressMonitor == null || fProgressMonitor.isCanceled());
				} catch(Throwable throwable) {
					Plugin.log(throwable);
				}

			}

			@Override
			public void handleException(Throwable throwable) {
				Plugin.log(throwable);
			}
		});
	}

	@Override
	public void reconcile(IRegion partition) {
		reconcile(false);
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		reconcile(false);
	}

	@Override
	public void setDocument(IDocument document) {
		m_document = document;
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor = monitor;
	}

	@Override
	public void initialReconcile() {
		reconcile(true);
	}

	public void aboutToBeReconciled() {
	}

	public void notifyListeners(boolean state) {
	}
}
