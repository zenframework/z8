package org.zenframework.z8.pde.build;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.error.BuildError;
import org.zenframework.z8.compiler.error.BuildMessage;
import org.zenframework.z8.compiler.error.DefaultBuildMessageConsumer;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.pde.MyMultiEditor;
import org.zenframework.z8.pde.editor.Z8Editor;

public class ReconcileMessageConsumer extends DefaultBuildMessageConsumer {
	private abstract class AbstractConsumerJob extends UIJob {
		private IResource m_resource;

		public AbstractConsumerJob(IResource resource) {
			super("");
			m_resource = resource;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			IEditorReference[] editors;

			try {
				editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
			} catch(NullPointerException e) {
				// Workbench is shutting down or something like that
				return Status.OK_STATUS;
			}

			for(IEditorReference editorReference : editors) {
				IEditorPart editorPart = editorReference.getEditor(false);

				if(editorPart instanceof MyMultiEditor) {
					MyMultiEditor multi = (MyMultiEditor)editorPart;
					editorPart = multi.getEditor(0);
				}

				if(editorPart instanceof Z8Editor) {
					Z8Editor editor = (Z8Editor)editorPart;

					if(m_resource.equals(editor.getResource())) {
						AnnotationModel model = editor.getAnnotationModel();

						if(model != null) {
							doAction(editor.getAnnotationModel());
						}
						return Status.OK_STATUS;
					}
				}
			}

			return Status.OK_STATUS;
		}

		abstract void doAction(AnnotationModel model);
	}

	private class ConsumeJob extends AbstractConsumerJob {
		private BuildMessage m_message;

		public ConsumeJob(BuildMessage message) {
			super(message.getResource());
			m_message = message;
		}

		@Override
		public void doAction(AnnotationModel model) {
			doConsume(model, m_message);
		}
	}

	private class ClearMessagesJob extends AbstractConsumerJob {
		public ClearMessagesJob(IResource resource) {
			super(resource);
		}

		@Override
		@SuppressWarnings({ "rawtypes" })
		public void doAction(AnnotationModel model) {
			Iterator iterator = model.getAnnotationIterator();
			while(iterator.hasNext()) {
				Annotation annotation = (Annotation)iterator.next();
				String type = annotation.getType();
				if(type.equals("org.eclipse.ui.workbench.texteditor.error") || type.equals("org.eclipse.ui.workbench.texteditor.warning")) {
					model.removeAnnotation(annotation);
				}
			}
		}
	}

	public ReconcileMessageConsumer() {
		super();
	}

	@Override
	public void clearMessages(Resource resource) {
		ClearMessagesJob job = new ClearMessagesJob(resource.getResource());
		job.schedule();
	}

	@Override
	public void consume(BuildMessage message) {
		super.consume(message);

		ConsumeJob job = new ConsumeJob(message);
		job.schedule();
	}

	private void doConsume(AnnotationModel model, BuildMessage message) {
		String text = message.getDescription();

		IPosition position = message.getPosition();

		int nCharStart = position != null ? position.getOffset() : -1;
		int nLength = position != null ? position.getLength() : 0;

		Annotation annotation = new Annotation(message instanceof BuildError ? "org.eclipse.ui.workbench.texteditor.error" : "org.eclipse.ui.workbench.texteditor.warning", false, text);
		model.addAnnotation(annotation, new Position(nCharStart, nLength));
	}
}
