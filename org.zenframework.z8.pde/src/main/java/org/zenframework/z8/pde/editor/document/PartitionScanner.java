package org.zenframework.z8.pde.editor.document;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class PartitionScanner extends RuleBasedPartitionScanner {
	public final static String MULTILINE_COMMENT = "org.zenframework.z8.pde.editor.document.multiline.comment";
	public final static String DOCUMENT = "org.zenframework.z8.pde.editor.document";
	public final static String[] PARTITION_TYPES = { MULTILINE_COMMENT, DOCUMENT };

	static class EmptyCommentDetector implements IWordDetector {
		@Override
		public boolean isWordStart(char c) {
			return (c == '/');
		}

		@Override
		public boolean isWordPart(char c) {
			return (c == '*' || c == '/');
		}
	}

	static class WordPredicateRule extends WordRule implements IPredicateRule {
		private IToken fSuccessToken;

		public WordPredicateRule(IToken successToken) {
			super(new EmptyCommentDetector());
			fSuccessToken = successToken;
			addWord("/**/", fSuccessToken); //$NON-NLS-1$
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			return super.evaluate(scanner);
		}

		@Override
		public IToken getSuccessToken() {
			return fSuccessToken;
		}
	}

	public PartitionScanner() {
		super();
		IToken doc = new Token(DOCUMENT);
		IToken comment = new Token(MULTILINE_COMMENT);
		List<IRule> rules = new ArrayList<IRule>();
		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", Token.UNDEFINED)); //$NON-NLS-1$
		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		rules.add(new SingleLineRule("'", "'", Token.UNDEFINED, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		// Add special case word rule.
		rules.add(new WordPredicateRule(comment));
		// Add rules for multi-line comments and doc.
		rules.add(new MultiLineRule("/**", "*/", doc, (char)0, true)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MultiLineRule("/*", "*/", comment, (char)0, true)); //$NON-NLS-1$ //$NON-NLS-2$
		IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
