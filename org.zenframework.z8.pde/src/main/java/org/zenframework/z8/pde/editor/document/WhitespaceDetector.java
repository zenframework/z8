package org.zenframework.z8.pde.editor.document;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class WhitespaceDetector implements IWhitespaceDetector {
	@Override
	public boolean isWhitespace(char character) {
		return Character.isWhitespace(character);
	}
}
