package org.zenframework.z8.pde.editor.document;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import org.zenframework.z8.pde.ColorProvider;

public class DocScanner extends RuleBasedScanner {
	static class DocWordDetector implements IWordDetector {
		@Override
		public boolean isWordStart(char c) {
			return (c == '@');
		}

		@Override
		public boolean isWordPart(char c) {
			return Character.isLetter(c);
		}
	}

	private static String[] fgKeywords = { "@author", "@deprecated", "@exception", "@param", "@return", "@see", "@serial", "@serialData", "@serialField", "@since", "@throws", "@version" };

	public DocScanner(ColorProvider provider) {
		super();
		IToken keyword = new Token(new TextAttribute(provider.getColor(ColorProvider.DOC_KEYWORD)));
		IToken tag = new Token(new TextAttribute(provider.getColor(ColorProvider.DOC_TAG)));
		IToken link = new Token(new TextAttribute(provider.getColor(ColorProvider.DOC_LINK)));
		List<IRule> list = new ArrayList<IRule>();
		// Add rule for tags.
		list.add(new SingleLineRule("<", ">", tag));
		// Add rule for links.
		list.add(new SingleLineRule("{", "}", link));
		// Add generic whitespace rule.
		list.add(new WhitespaceRule(new WhitespaceDetector()));
		// Add word rule for keywords.
		WordRule wordRule = new WordRule(new DocWordDetector());
		for(int i = 0; i < fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], keyword);
		list.add(wordRule);
		IRule[] result = new IRule[list.size()];
		list.toArray(result);
		setRules(result);
	}
}
