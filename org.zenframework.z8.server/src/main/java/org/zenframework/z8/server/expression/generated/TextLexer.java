// Generated from Text.g by ANTLR 4.13.2

package org.zenframework.z8.server.expression.generated;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class TextLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, TEXT=3;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "TEXT", "EscapeSequence", "HexDigit"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'${'", "'}'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, "TEXT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public TextLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Text.g"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u0003=\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0001\u0002\u0004\u0002\u0013\b\u0002\u000b\u0002\f\u0002"+
		"\u0014\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\u001d\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003&\b\u0003\u0001"+
		"\u0003\u0003\u0003)\b\u0003\u0001\u0003\u0003\u0003,\b\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0004\u00031\b\u0003\u000b\u0003\f\u00032\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003:\b"+
		"\u0003\u0001\u0004\u0001\u0004\u0000\u0000\u0005\u0001\u0001\u0003\u0002"+
		"\u0005\u0003\u0007\u0000\t\u0000\u0001\u0000\u0005\u0003\u0000$${{}}\u0007"+
		"\u0000\"\"\'\'\\\\bbffnnrt\u0001\u000003\u0001\u000007\u0003\u000009A"+
		"FafC\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000"+
		"\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0001\u000b\u0001\u0000\u0000"+
		"\u0000\u0003\u000e\u0001\u0000\u0000\u0000\u0005\u0012\u0001\u0000\u0000"+
		"\u0000\u00079\u0001\u0000\u0000\u0000\t;\u0001\u0000\u0000\u0000\u000b"+
		"\f\u0005$\u0000\u0000\f\r\u0005{\u0000\u0000\r\u0002\u0001\u0000\u0000"+
		"\u0000\u000e\u000f\u0005}\u0000\u0000\u000f\u0004\u0001\u0000\u0000\u0000"+
		"\u0010\u0013\b\u0000\u0000\u0000\u0011\u0013\u0003\u0007\u0003\u0000\u0012"+
		"\u0010\u0001\u0000\u0000\u0000\u0012\u0011\u0001\u0000\u0000\u0000\u0013"+
		"\u0014\u0001\u0000\u0000\u0000\u0014\u0012\u0001\u0000\u0000\u0000\u0014"+
		"\u0015\u0001\u0000\u0000\u0000\u0015\u0006\u0001\u0000\u0000\u0000\u0016"+
		"\u001c\u0005\\\u0000\u0000\u0017\u0018\u0005u\u0000\u0000\u0018\u0019"+
		"\u00050\u0000\u0000\u0019\u001a\u00050\u0000\u0000\u001a\u001b\u00055"+
		"\u0000\u0000\u001b\u001d\u0005c\u0000\u0000\u001c\u0017\u0001\u0000\u0000"+
		"\u0000\u001c\u001d\u0001\u0000\u0000\u0000\u001d\u001e\u0001\u0000\u0000"+
		"\u0000\u001e:\u0007\u0001\u0000\u0000\u001f%\u0005\\\u0000\u0000 !\u0005"+
		"u\u0000\u0000!\"\u00050\u0000\u0000\"#\u00050\u0000\u0000#$\u00055\u0000"+
		"\u0000$&\u0005c\u0000\u0000% \u0001\u0000\u0000\u0000%&\u0001\u0000\u0000"+
		"\u0000&+\u0001\u0000\u0000\u0000\')\u0007\u0002\u0000\u0000(\'\u0001\u0000"+
		"\u0000\u0000()\u0001\u0000\u0000\u0000)*\u0001\u0000\u0000\u0000*,\u0007"+
		"\u0003\u0000\u0000+(\u0001\u0000\u0000\u0000+,\u0001\u0000\u0000\u0000"+
		",-\u0001\u0000\u0000\u0000-:\u0007\u0003\u0000\u0000.0\u0005\\\u0000\u0000"+
		"/1\u0005u\u0000\u00000/\u0001\u0000\u0000\u000012\u0001\u0000\u0000\u0000"+
		"20\u0001\u0000\u0000\u000023\u0001\u0000\u0000\u000034\u0001\u0000\u0000"+
		"\u000045\u0003\t\u0004\u000056\u0003\t\u0004\u000067\u0003\t\u0004\u0000"+
		"78\u0003\t\u0004\u00008:\u0001\u0000\u0000\u00009\u0016\u0001\u0000\u0000"+
		"\u00009\u001f\u0001\u0000\u0000\u00009.\u0001\u0000\u0000\u0000:\b\u0001"+
		"\u0000\u0000\u0000;<\u0007\u0004\u0000\u0000<\n\u0001\u0000\u0000\u0000"+
		"\t\u0000\u0012\u0014\u001c%(+29\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}