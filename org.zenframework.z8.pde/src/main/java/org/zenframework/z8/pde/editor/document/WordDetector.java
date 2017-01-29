package org.zenframework.z8.pde.editor.document;

import org.eclipse.jface.text.rules.IWordDetector;

public class WordDetector implements IWordDetector {
	@Override
	public boolean isWordPart(char character) {
		return Character.isJavaIdentifierPart(character);
	}

	@Override
	public boolean isWordStart(char character) {
		return Character.isJavaIdentifierStart(character);
	}
}
