package org.zenframework.z8.pde.editor.view;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.part.FileEditorInput;

import org.zenframework.z8.compiler.content.Hyperlink;
import org.zenframework.z8.compiler.content.LabelEntry;
import org.zenframework.z8.compiler.content.LabelProvider;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.ConstantToken;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Workspace;

public class HyperlinkDetector implements IHyperlinkDetector {
	public HyperlinkDetector() {
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		int pos = region.getOffset();

		IResource resource = ((FileEditorInput)((SourceViewer)textViewer).getEditor().getEditorInput()).getFile();
		CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(resource);

		IPosition position = compilationUnit.getHyperlinkPosition(pos);
		Hyperlink hyperlink = compilationUnit.getHyperlink(position);

		if(hyperlink != null) {
			return new IHyperlink[] { new EditorHyperlink(position, hyperlink) };
		}

		ConstantToken[] tokens = compilationUnit.getNLSStrings();

		for(ConstantToken token : tokens) {
			String value = token.getValueString();
			String key = value.substring(1, value.length() - 1);

			int offset = token.getPosition().getOffset();
			int length = token.getPosition().getLength();

			if(pos >= offset && pos <= offset + length) {
				LabelEntry entry = LabelProvider.getEntry(compilationUnit.getProject(), "ru", key);

				if(entry != null) {
					return new IHyperlink[] { new ResourceHyperlink(token.getPosition(), key, entry) };
				}
			}
		}

		return null;
	}
}
