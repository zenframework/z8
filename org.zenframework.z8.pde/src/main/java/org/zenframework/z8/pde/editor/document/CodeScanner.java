package org.zenframework.z8.pde.editor.document;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import org.zenframework.z8.pde.ColorProvider;

public class CodeScanner extends RuleBasedScanner {
	private static String[] fgKeywords = { "auto", "break", "catch", "class", "container", "continue", "do", "else", "enum", "extends", "exception",
			"finally", "for", "if", "new", "private", "protected", "public", "records", "return", "static", "super", "this", "throw", "try", "while",
			"virtual", "operator", "import", "final", "instanceof", "null" };

	private static String[] fgAttributes = { "name", "native", "displayName", "columnHeader", "generatable", "entry", "request", "ui", "presentation", "system", 
			"description", "icon", "job", "exportable", "foreignKey" };

	private static String[] fgTypes = { "void", "binary", "bool", "date", "datetime", "datespan", "decimal", "guid", "geometry", "file", "int", "string", "sql_binary", "sql_bool", "sql_date", "sql_datetime", "sql_datespan", "sql_decimal", "sql_guid", "sql_geometry", "sql_int", "sql_string" };

	private static String[] fgConstants = { "false", "true" };

	public CodeScanner(ColorProvider provider) {
		IToken keyword = new Token(new TextAttribute(provider.getColor(ColorProvider.KEYWORD)));
		IToken attribute = new Token(new TextAttribute(provider.getColor(ColorProvider.ATTRIBUTE)));
		IToken type = new Token(new TextAttribute(provider.getColor(ColorProvider.TYPE)));
		IToken string = new Token(new TextAttribute(provider.getColor(ColorProvider.STRING)));
		IToken comment = new Token(new TextAttribute(provider.getColor(ColorProvider.SINGLE_LINE_COMMENT)));
		IToken other = new Token(new TextAttribute(provider.getColor(ColorProvider.DEFAULT)));

		List<IRule> rules = new ArrayList<IRule>();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", comment)); //$NON-NLS-1$

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
		rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new WhitespaceDetector()));

		// Add word rule for keywords, attributes, types, and constants.
		WordRule wordRule = new WordRule(new WordDetector(), other);

		for(int i = 0; i < fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], keyword);

		for(int i = 0; i < fgAttributes.length; i++)
			wordRule.addWord(fgAttributes[i], attribute);

		for(int i = 0; i < fgTypes.length; i++)
			wordRule.addWord(fgTypes[i], type);

		for(int i = 0; i < fgConstants.length; i++)
			wordRule.addWord(fgConstants[i], type);

		rules.add(wordRule);

		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);

		setRules(result);
	}
}
