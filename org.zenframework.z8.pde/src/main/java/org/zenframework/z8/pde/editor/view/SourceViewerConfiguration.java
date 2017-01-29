package org.zenframework.z8.pde.editor.view;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;

import org.zenframework.z8.pde.ColorProvider;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.build.Reconciler;
import org.zenframework.z8.pde.build.ReconcilingStrategy;
import org.zenframework.z8.pde.editor.DoubleClickSelector;
import org.zenframework.z8.pde.editor.Z8Editor;
import org.zenframework.z8.pde.editor.contentassist.CompletionProcessor;
import org.zenframework.z8.pde.editor.contentassist.DocCompletionProcessor;
import org.zenframework.z8.pde.editor.document.AutoIndentStrategy;
import org.zenframework.z8.pde.editor.document.DocumentSetupParticipant;
import org.zenframework.z8.pde.editor.document.PartitionScanner;

public class SourceViewerConfiguration extends org.eclipse.jface.text.source.SourceViewerConfiguration {
	static class SingleTokenScanner extends BufferedRuleBasedScanner {
		public SingleTokenScanner(TextAttribute attribute) {
			setDefaultReturnToken(new Token(attribute));
		}
	}

	private Z8Editor m_editor;

	public SourceViewerConfiguration(Z8Editor editor) {
		m_editor = editor;
	}

	public Z8Editor getEditor() {
		return m_editor;
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new AnnotationHover();
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		if(IDocument.DEFAULT_CONTENT_TYPE.equals(contentType))
			return new IAutoEditStrategy[] { new AutoIndentStrategy((SourceViewer)sourceViewer) };
		else
			return new IAutoEditStrategy[] { new DefaultIndentLineAutoEditStrategy() };
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return DocumentSetupParticipant.PARTITIONING;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, PartitionScanner.DOCUMENT, PartitionScanner.MULTILINE_COMMENT };
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		assistant.setContentAssistProcessor(new CompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(new DocCompletionProcessor(), PartitionScanner.DOCUMENT);
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		return assistant;
	}

	public String getDefaultPrefix(ISourceViewer sourceViewer, String contentType) {
		return (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType) ? "//" : null); //$NON-NLS-1$
	}

	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		return new DoubleClickSelector();
	}

	@Override
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "\t", "    " };
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		ColorProvider provider = Plugin.getColorProvider();

		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(Z8Editor.getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(provider.getColor(ColorProvider.DOC_DEFAULT))));
		reconciler.setDamager(dr, PartitionScanner.DOCUMENT);
		reconciler.setRepairer(dr, PartitionScanner.DOCUMENT);

		dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(provider.getColor(ColorProvider.MULTI_LINE_COMMENT))));
		reconciler.setDamager(dr, PartitionScanner.MULTILINE_COMMENT);
		reconciler.setRepairer(dr, PartitionScanner.MULTILINE_COMMENT);

		return reconciler;
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {

		final Z8Editor editor = getEditor();

		if(editor != null && editor.isEditable()) {
			ReconcilingStrategy strategy = new ReconcilingStrategy(editor);
			Reconciler reconciler = new Reconciler(editor, strategy, false);
			reconciler.setIsIncrementalReconciler(false);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			reconciler.setDelay(500);

			return reconciler;
		}
		return null;
	}

	@Override
	public int getTabWidth(ISourceViewer sourceViewer) {
		return 4;
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new TextHover();
	}

	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		int len = super.getHyperlinkDetectors(sourceViewer).length;
		IHyperlinkDetector a[] = new IHyperlinkDetector[len + 1];
		for(int i = 0; i < len; i++) {
			a[i] = super.getHyperlinkDetectors(sourceViewer)[i];
		}
		a[len] = new HyperlinkDetector();
		return a;
	}

}
