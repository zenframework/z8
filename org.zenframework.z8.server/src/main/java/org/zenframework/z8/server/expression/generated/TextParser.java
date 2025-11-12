// Generated from Text.g by ANTLR 4.13.2

package org.zenframework.z8.server.expression.generated;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class TextParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, TEXT=3;
	public static final int
		RULE_text = 0, RULE_textPart = 1;
	private static String[] makeRuleNames() {
		return new String[] {
			"text", "textPart"
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

	@Override
	public String getGrammarFileName() { return "Text.g"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TextParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TextContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(TextParser.EOF, 0); }
		public List<TextPartContext> textPart() {
			return getRuleContexts(TextPartContext.class);
		}
		public TextPartContext textPart(int i) {
			return getRuleContext(TextPartContext.class,i);
		}
		public TextContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_text; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TextVisitor ) return ((TextVisitor<? extends T>)visitor).visitText(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TextContext text() throws RecognitionException {
		TextContext _localctx = new TextContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_text);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(7);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0 || _la==TEXT) {
				{
				{
				setState(4);
				textPart();
				}
				}
				setState(9);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(10);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TextPartContext extends ParserRuleContext {
		public TextPartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_textPart; }
	 
		public TextPartContext() { }
		public void copyFrom(TextPartContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PlainTextPartContext extends TextPartContext {
		public TerminalNode TEXT() { return getToken(TextParser.TEXT, 0); }
		public PlainTextPartContext(TextPartContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TextVisitor ) return ((TextVisitor<? extends T>)visitor).visitPlainTextPart(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionTextPartContext extends TextPartContext {
		public TerminalNode TEXT() { return getToken(TextParser.TEXT, 0); }
		public ExpressionTextPartContext(TextPartContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TextVisitor ) return ((TextVisitor<? extends T>)visitor).visitExpressionTextPart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TextPartContext textPart() throws RecognitionException {
		TextPartContext _localctx = new TextPartContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_textPart);
		try {
			setState(16);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TEXT:
				_localctx = new PlainTextPartContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(12);
				match(TEXT);
				}
				break;
			case T__0:
				_localctx = new ExpressionTextPartContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(13);
				match(T__0);
				setState(14);
				match(TEXT);
				setState(15);
				match(T__1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0003\u0013\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0001\u0000\u0005\u0000\u0006\b\u0000\n\u0000\f\u0000\t\t\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"+
		"\u0001\u0011\b\u0001\u0001\u0001\u0000\u0000\u0002\u0000\u0002\u0000\u0000"+
		"\u0012\u0000\u0007\u0001\u0000\u0000\u0000\u0002\u0010\u0001\u0000\u0000"+
		"\u0000\u0004\u0006\u0003\u0002\u0001\u0000\u0005\u0004\u0001\u0000\u0000"+
		"\u0000\u0006\t\u0001\u0000\u0000\u0000\u0007\u0005\u0001\u0000\u0000\u0000"+
		"\u0007\b\u0001\u0000\u0000\u0000\b\n\u0001\u0000\u0000\u0000\t\u0007\u0001"+
		"\u0000\u0000\u0000\n\u000b\u0005\u0000\u0000\u0001\u000b\u0001\u0001\u0000"+
		"\u0000\u0000\f\u0011\u0005\u0003\u0000\u0000\r\u000e\u0005\u0001\u0000"+
		"\u0000\u000e\u000f\u0005\u0003\u0000\u0000\u000f\u0011\u0005\u0002\u0000"+
		"\u0000\u0010\f\u0001\u0000\u0000\u0000\u0010\r\u0001\u0000\u0000\u0000"+
		"\u0011\u0003\u0001\u0000\u0000\u0000\u0002\u0007\u0010";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}