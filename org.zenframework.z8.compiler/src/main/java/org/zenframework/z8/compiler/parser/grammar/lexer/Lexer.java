package org.zenframework.z8.compiler.parser.grammar.lexer;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.file.FileException;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.BinaryToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.BooleanToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.DateToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.DatespanToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.DecimalToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.GuidToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.IntegerToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.KeywordToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.OperatorToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.StringToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.Token;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.TokenException;
import org.zenframework.z8.compiler.util.Binary;
import org.zenframework.z8.compiler.util.Date;
import org.zenframework.z8.compiler.util.Datespan;
import org.zenframework.z8.compiler.util.Set;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Lexer {
	private boolean skipSpaces;

	private IPosition position;
	private FileBuffer buffer;

	private Token currentToken = new Token();
	private Token previousToken;

	public Lexer() {
	}

	public Lexer(char[] content) throws FileException, UnsupportedEncodingException {
		initialize(null, content);
	}

	public Lexer(CompilationUnit compilationUnit, char[] content) throws FileException, UnsupportedEncodingException {
		initialize(compilationUnit, content);
	}

	public void setSource(char[] content) {
		initialize(content);
	}

	public char[] getContent() {
		return buffer.getContent();
	}

	protected void initialize(char[] content) {
		buffer = new FileBuffer(content);
		position = new Position();
		skipSpaces = true;
	}

	protected void initialize(CompilationUnit compilationUnit, char[] content) throws FileException, UnsupportedEncodingException {
		buffer = new FileBuffer(compilationUnit, content);
		position = new Position();
		skipSpaces = true;
	}

	@Override
	public int hashCode() {
		return buffer.hashCode();
	}

	public boolean isEOF(IPosition position) {
		return buffer.isEOF(position);
	}

	public char charAt(IPosition position) {
		return buffer.charAt(position);
	}

	public char nextCharAt(IPosition position) {
		return buffer.nextCharAt(position);
	}

	public char nextCharAt(IPosition position, int offset) {
		return buffer.nextCharAt(position, offset);
	}

	public String getString(IPosition position) {
		return buffer.getString(position);
	}

	public IToken previousToken() {
		return previousToken;
	}

	public void unread() {
		position = new Position(currentToken.getPosition(), 0);
	}

	public Token nextToken() throws TokenException {
		Token token;

		while(true) {
			if(isEOF(position)) {
				previousToken = currentToken;
				currentToken = new Token();
				return currentToken;
			}

			char chr = charAt(position);

			if(ABC.isWhiteSpace(chr)) {
				token = getWhiteSpace(position);
			} else if(ABC.isLineBreak(chr)) {
				token = getLineBreak(position);
			} else if(ABC.isPunctuator(chr)) {
				token = getPunctuator(position);
			} else if(ABC.isDigit(chr)) {
				token = getDigit(position);
			} else if(!ABC.isAllowed(chr)) {
				throw new TokenException("not allowed character found : " + chr + "", new Position(position, 1));
			} else {
				token = getIdentifier(position);
			}

			if(skipSpaces) {
				if(token.getId() == IToken.COMMENT || token.getId() == IToken.WHITESPACE || token.getId() == IToken.LINEBREAK) {
					continue;
				}

				break;
			}

			break;
		}

		previousToken = currentToken;
		currentToken = token;
		return token;
	}

	private boolean skipWhiteSpaces(IPosition position) {
		while(!isEOF(position) && ABC.isWhiteSpace(charAt(position))) {
			buffer.advance(position);
		}
		return !isEOF(position);
	}

	public boolean skipLineBreaks(IPosition position) {
		while(!isEOF(position) && ABC.isLineBreak(charAt(position))) {
			int advance = 1;

			if(charAt(position) == '\r' && nextCharAt(position) == '\n') {
				advance = 2;
			}

			buffer.advance(position, advance);
			buffer.breakLine(position);
		}

		return !isEOF(position);
	}

	public boolean skipNonLetters(IPosition position) {
		while(!isEOF(position)) {
			char chr = charAt(position);

			if(ABC.isWhiteSpace(chr)) {
				skipWhiteSpaces(position);
			} else if(ABC.isLineBreak(chr)) {
				skipLineBreaks(position);
			} else {
				break;
			}
		}

		return !isEOF(position);
	}

	Token getWhiteSpace(IPosition position) throws TokenException {
		Position start = new Position(position);

		skipWhiteSpaces(position);

		return new Token(IToken.WHITESPACE, new Position(start, position));
	}

	Token getLineBreak(IPosition position) throws TokenException {
		Position start = new Position(position);

		skipLineBreaks(position);

		return new Token(IToken.LINEBREAK, new Position(start, position));
	}

	char getEscape(IPosition position) throws TokenException {
		Position start = new Position(position);

		buffer.advance(position);

		if(isEOF(position)) {
			throw new TokenException("escape sequence expected", new Position(start, 1));
		}

		char chr = charAt(position);

		buffer.advance(position);

		switch(chr) {
		case 'b':
			return '\b';
		case 'f':
			return '\f';
		case 'n':
			return '\n';
		case 'r':
			return '\r';
		case 't':
			return '\t';
		case '"':
			return '\"';
		case '\\':
			return '\\';
		case '\'':
			return '\'';
		default:
			throw new TokenException("bad escape sequence " + '\\' + chr, new Position(start, 2));
		}
	}

	Token getDigit(IPosition position) throws TokenException {
		Position start = new Position(position);

		try {
			String number = "";

			char sign = charAt(position);

			if(sign == '-' || sign == '+') {
				buffer.advance(position);

				number += sign;

				skipWhiteSpaces(position);
			}

			// cannot be EOF - checked with 'isNumberSign'

			char chr = charAt(position);
			char nextChr = nextCharAt(position);

			if(chr == '0' && (nextChr == 'x' || nextChr == 'X')) {
				buffer.advance(position, 2);

				number += getDigitSequence(position, 16);
				return new IntegerToken(Long.parseLong(number, 16), new Position(start, position));
			}

			if(chr != '.') {
				number += getDigitSequence(position, 10);
			}

			if(charAt(position) == '.') {
				number += getMantissa(position);
				return new DecimalToken(new BigDecimal(number), new Position(start, position));
			}

			return new IntegerToken(Long.parseLong(number), new Position(start, position));
		} catch(NumberFormatException e) {
			throw new TokenException(e.getMessage(), new Position(start, position));
		}
	}

	private Token getLiteral(IPosition position) throws TokenException {
		Position start = new Position(position);

		buffer.advance(position);

		String string = "";

		while(!isEOF(position)) {
			char chr = charAt(position);

			switch(chr) {
			case '"':
				buffer.advance(position);
				return new StringToken(string, new Position(start, position));
			case '\\': {
				string += getEscape(position);
				break;
			}
			default:
				if(ABC.isPrintable(chr) || ABC.isWhiteSpace(chr) || ABC.isLineBreak(chr))
					string += chr;
				buffer.advance(position);
			}
		}

		throw new TokenException("end of string not found", new Position(start, position));
	}

	Token getComment(IPosition position) throws TokenException {
		Position start = new Position(position);

		boolean singleLine = nextCharAt(position) == '/';

		buffer.advance(position, 2);

		if(singleLine) {
			while(!isEOF(position)) {
				if(charAt(position) == '\r' || charAt(position) == '\n')
					break;

				buffer.advance(position);
			}

			return new Token(IToken.COMMENT, new Position(start, position));
		}
		int level = 0;

		while(!isEOF(position)) {
			char chr = charAt(position);
			char nextChr = nextCharAt(position);

			if(chr == '*' && nextChr == '/') {
				if(level == 0) {
					buffer.advance(position, 2);
					return new Token(IToken.COMMENT, new Position(start, position));
				}

				level--;
				buffer.advance(position, 2);
			} else if(chr == '/' && nextChr == '*') {
				level++;
				buffer.advance(position, 2);
			} else if(ABC.isLineBreak(chr)) {
				skipLineBreaks(position);
			} else
				buffer.advance(position);
		}

		throw new TokenException("end of comment not found", new Position(start, position));
	}

	static Map<Character, Integer> charToId;

	static {
		charToId = new HashMap<Character, Integer>();
		charToId.put('%', IToken.MOD);
		charToId.put('&', IToken.AND);
		charToId.put('(', IToken.LBRACE);
		charToId.put(')', IToken.RBRACE);
		charToId.put('*', IToken.MUL);
		charToId.put('/', IToken.DIV);
		charToId.put('+', IToken.PLUS);
		charToId.put('-', IToken.MINUS);
		charToId.put('^', IToken.CARET);
		charToId.put(',', IToken.COMMA);
		charToId.put('.', IToken.DOT);
		charToId.put(';', IToken.SEMICOLON);
		charToId.put('?', IToken.QUESTION);
		charToId.put('[', IToken.LBRACKET);
		charToId.put(']', IToken.RBRACKET);
		charToId.put('|', IToken.OR);
		charToId.put('{', IToken.LCBRACE);
		charToId.put('}', IToken.RCBRACE);
		charToId.put('!', IToken.NOT);
		charToId.put('=', IToken.ASSIGN);
		charToId.put('<', IToken.LESS);
		charToId.put('>', IToken.MORE);
		charToId.put(':', IToken.COLON);
	}

	Token getPunctuator(IPosition position) throws TokenException {
		Position start = new Position(position);

		char first = charAt(position);
		char second = nextCharAt(position, 1);
		char third = nextCharAt(position, 2);
		char fourth = nextCharAt(position, 3);

		if(first == '/' && (second == '*' || second == '/')) {
			return getComment(position);
		} else if(first == '"') {
			return getLiteral(position);
		} else if(first == '_') {
			return getIdentifier(position);
		} else if(isNumberSign(position)) {
			return getDigit(position);
		} else if(first == '.' && ABC.isDigit(second)) {
			return getDigit(position);
		} else if(first == '\'') {
			return getSpecialConst(position);
		} else if(first == '!' && second == '=') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.NOT_EQU, new Position(start, 2));
		} else if(first == '=' && second == '=') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.EQU, new Position(start, 2));
		} else if(first == '<' && second == '=') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.LESS_EQU, new Position(start, 2));
		} else if(first == '>' && second == '=') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.MORE_EQU, new Position(start, 2));
		} else if(first == '+' && second == '=') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.ADD_ASSIGN, new Position(start, 2));
		} else if(first == '-' && second == '=') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.SUB_ASSIGN, new Position(start, 2));
		} else if(first == '*' && second == '=') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.MUL_ASSIGN, new Position(start, 2));
		} else if(first == '/' && second == '=') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.DIV_ASSIGN, new Position(start, 2));
		} else if(first == '%' && second == '=') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.MOD_ASSIGN, new Position(start, 2));
		} else if(first == '&' && second == '&') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.AND, new Position(start, 2));
		} else if(first == '|' && second == '|') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.OR, new Position(start, 2));
		} else if(first == '^' && second == '=') {
			buffer.advance(position, 2);
			return new OperatorToken(IToken.CARET_ASSIGN, new Position(start, 2));
		} else if(first == '^' && second == '*') {
			if(third == '=') {
				buffer.advance(position, 3);
				return new OperatorToken(IToken.CARET_MUL_ASSIGN, new Position(start, 3));
			}

			buffer.advance(position, 2);
			return new OperatorToken(IToken.CARET_MUL, new Position(start, 2));
		} else if(first == '*' && second == '^') {
			if(third == '*') {
				if(fourth == '=') {
					buffer.advance(position, 4);
					return new OperatorToken(IToken.MUL_CARET_MUL_ASSIGN, new Position(start, 4));
				}
				buffer.advance(position, 3);
				return new OperatorToken(IToken.MUL_CARET_MUL, new Position(start, 3));
			}

			if(third == '=') {
				buffer.advance(position, 3);
				return new OperatorToken(IToken.MUL_CARET_ASSIGN, new Position(start, 3));
			}

			buffer.advance(position, 2);
			return new OperatorToken(IToken.MUL_CARET, new Position(start, 2));
		} else {
			Integer id = charToId.get(first);

			if(id != null) {
				buffer.advance(position);
				return new OperatorToken(id, new Position(start, 1));
			}
		}

		throw new UnsupportedOperationException();
	}

	Token getIdentifier(IPosition position) {
		Position start = new Position(position);

		while(!isEOF(position)) {
			char chr = charAt(position);
			if(ABC.isAlnum(chr) || chr == '_')
				buffer.advance(position);
			else
				break;
		}

		return getKeyword(start, position);
	}

	static Map<String, Integer> keywords = new HashMap<String, Integer>();
	static Set<String> reserved = new Set<String>();

	static {
		reserved.add("boolean");
		reserved.add("byte");
		reserved.add("case");
		reserved.add("char");
		reserved.add("const");
		reserved.add("default");
		reserved.add("double");
		reserved.add("float");
		reserved.add("goto");
		reserved.add("implements");
		reserved.add("instanceof");
		reserved.add("interface");
		reserved.add("long");
		reserved.add("package");
		reserved.add("short");
		reserved.add("strictfp");
		reserved.add("switch");
		reserved.add("synchronized");
		reserved.add("throws");
		reserved.add("transient");
		reserved.add("volatile");

		keywords.put("operator", IToken.OPERATOR);

		keywords.put("do", IToken.DO);
		keywords.put("for", IToken.FOR);
		keywords.put("while", IToken.WHILE);

		keywords.put("if", IToken.IF);
		keywords.put("else", IToken.ELSE);

		keywords.put("break", IToken.BREAK);
		keywords.put("continue", IToken.CONTINUE);
		keywords.put("return", IToken.RETURN);

		keywords.put("this", IToken.THIS);
		keywords.put("super", IToken.SUPER);
		keywords.put("container", IToken.CONTAINER);

		keywords.put("null", IToken.NULL);

		keywords.put("import", IToken.IMPORT);
		keywords.put("class", IToken.CLASS);
		keywords.put("public", IToken.PUBLIC);
		keywords.put("protected", IToken.PROTECTED);
		keywords.put("private", IToken.PRIVATE);
		keywords.put("extends", IToken.EXTENDS);

		keywords.put("enum", IToken.ENUM);
		keywords.put("records", IToken.RECORDS);

		keywords.put("auto", IToken.AUTO);
		keywords.put("new", IToken.NEW);
		keywords.put("static", IToken.STATIC);

		keywords.put("try", IToken.TRY);
		keywords.put("catch", IToken.CATCH);
		keywords.put("finally", IToken.FINALLY);
		keywords.put("throw", IToken.THROW);
		keywords.put("virtual", IToken.VIRTUAL);
		keywords.put("final", IToken.FINAL);
	}

	public static boolean checkIdentifier(String name) {
		return !(name.equals("native") || name.equals("void") || name.equals("int") || name.equals("abstract"));
	}

	Token getKeyword(IPosition begin, IPosition end) {
		Position position = new Position(begin, end);

		String name = getString(position);

		Integer id = keywords.get(name);

		if(id != null)
			return new KeywordToken(id, position);

		if(name.equals("true"))
			return new BooleanToken(true, position);

		if(name.equals("false"))
			return new BooleanToken(false, position);

		if(reserved.get(name) != null) {
			String error = "token '" + name + "' is reserved and cannot be used";
			throw new TokenException(error, position);
		}

		return new Token(IToken.IDENTIFIER, position, name);
	}

	String getMantissa(IPosition position) throws TokenException {
		char next = nextCharAt(position);

		buffer.advance(position);

		String mantissa = ".";

		if(ABC.isDigit(next)) {
			mantissa += getDigitSequence(position, 10);
		}

		if(charAt(position) == 'e' || charAt(position) == 'E') {
			mantissa += getDegree(position);
		}

		return mantissa;
	}

	String getDegree(IPosition position) throws TokenException {
		Position start = new Position(position);

		char sign = nextCharAt(position);

		if(sign != '+' && sign != '-') {
			String error = "expected exponent sign ('+' or '-')";

			if(!isEOF(position))
				error += ", not " + sign;

			throw new TokenException(error, new Position(start, position));
		}

		buffer.advance(position, 2);

		return 'e' + sign + getDigitSequence(position, 10);
	}

	String getDigitSequence(IPosition position, int base) throws TokenException {
		Position start = new Position(position);

		if(isEOF(position) || !ABC.isDigit(charAt(position), base)) {
			String error = "expected " + (base == 10 ? "decimal" : "hexadecimal") + " digit";

			if(!isEOF(position))
				error += ", not " + charAt(position);

			throw new TokenException(error, new Position(start, position));
		}

		String digits = "";

		while(!isEOF(position)) {
			char digit = charAt(position);

			if(!ABC.isDigit(digit, base))
				break;

			digits += digit;
			buffer.advance(position);
		}
		return digits;
	}

	Token getSpecialConst(IPosition position) throws TokenException {
		Token token;

		position.saveState();

		try {
			token = getDateTime(position);
		} catch(TokenException e) {
			try {
				position.restoreState();
				token = getGUID(position);
			} catch(TokenException e1) {
				try {
					position.restoreState();
					token = getBinary(position);
				} catch(TokenException e2) {
					String error = "bad 'constant' format;\n" + "	proper formats are:\n" + "		date : 'dd/mm/yyyy' where dd = 01...31; mm = 01...12; yyyy = 1899...4712\n"
							+ "		datetime : 'dd/MM/yyyy hh:mm:ss' where dd = 01...31; MM = 01...12; yyyy = 1899...4712; hh = 00...23; mm = 00...59; ss = 00...59\n" + "		timespan : 'dd hh:mm:ss' where hh = 00...23; mm = 00...59; ss = 00...59\n"
							+ "		binary : 'n, n, n, ..., n' where n = 0...255\n" + "		guid : 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx' where x is any hexadecimal digit";

					throw new TokenException(error, e2.getPosition());
				}
			}
		}

		return token;
	}

	Token getDateTime(IPosition position) {
		Position start = new Position(position);

		buffer.advance(position);

		try {
			if(!skipWhiteSpaces(position))
				throw new TokenException(new Position(start, position));

			boolean bHasSpace;
			String day = "", month = "", year = "", hour = "", minute = "", second = "";
			int nDay, nMonth, nYear, nHour, nMinute, nSecond;
			nDay = nMonth = nYear = nHour = nMinute = nSecond = 0;

			boolean bHasDate = false;
			boolean bHasTime = false;
			boolean bHasDay = false;

			day = getDigitSequence(position, 10);

			switch(charAt(position)) {
			case '/':
				bHasDate = true;
				if(day.length() > 2)
					throw new TokenException(new Position(start, position));
				break;
			case ':':
				bHasTime = true;
				hour = day;
				day = "";
				if(hour.length() > 2)
					throw new TokenException(new Position(start, position));
				break;
			default:
				if(!ABC.isWhiteSpace(charAt(position))) {
					throw new TokenException(new Position(start, position));
				}

				bHasDay = true;
			}

			buffer.advance(position);

			if(bHasDate) {
				month = getDigitSequence(position, 10);

				if(month.length() > 2 || charAt(position) != '/')
					throw new TokenException(new Position(start, position));

				buffer.advance(position);

				year = getDigitSequence(position, 10);

				if(year.length() > 4)
					throw new TokenException(new Position(start, position));

				bHasSpace = ABC.isWhiteSpace(charAt(position));

				if(!skipWhiteSpaces(position))
					throw new TokenException(new Position(start, position));
			} else {
				bHasSpace = true;
			}

			if(charAt(position) != '\'') {
				if(!bHasSpace)
					throw new TokenException(new Position(start, position));

				if(!bHasTime) {
					hour = getDigitSequence(position, 10);

					if(hour.length() > 2 || charAt(position) != ':')
						throw new TokenException(new Position(start, position));
				}

				bHasTime = true;

				buffer.advance(position);

				minute = getDigitSequence(position, 10);

				if(minute.length() > 2 || charAt(position) != ':')
					throw new TokenException(new Position(start, position));

				buffer.advance(position);

				second = getDigitSequence(position, 10);

				if(second.length() > 2)
					throw new TokenException(new Position(start, position));

				if(!skipWhiteSpaces(position))
					throw new TokenException(new Position(start, position));

				if(charAt(position) != '\'')
					throw new TokenException(new Position(start, position));
			}

			if(bHasDate) {
				nYear = Integer.parseInt(year);
				nMonth = Integer.parseInt(month);
				nDay = Integer.parseInt(day);
				if(nYear < 1 || nYear > 9999 || nMonth < 1 || nMonth > 12 || nDay < 1 || nDay > Date.daysInMonth(nYear, nMonth))
					throw new TokenException(new Position(start, position));
			}

			if(bHasTime) {
				nHour = Integer.parseInt(hour);
				nMinute = Integer.parseInt(minute);
				nSecond = Integer.parseInt(second);
				if(nHour < 0 || nHour > 23 || nMinute < 0 || nMinute > 59 || nSecond < 0 || nSecond > 59)
					throw new TokenException(new Position(start, position));
			}

			buffer.advance(position);

			if(bHasDate) {
				if(bHasTime)
					return new DateToken(new Date(nYear, nMonth, nDay, nHour, nMinute, nSecond), new Position(start, position));

				return new DateToken(new Date(nYear, nMonth, nDay, nHour, nMinute, nSecond), new Position(start, position));
			}

			if(bHasDay) {
				nDay = Integer.parseInt(day);
				if(nDay < 0)
					throw new TokenException(new Position(start, position));

				return new DatespanToken(new Datespan(nDay, nHour, nMinute, nSecond, 0), new Position(start, position));
			}

			return new DateToken(new Date(), new Position(start, position));
		} catch(NumberFormatException e) {
			throw new TokenException(new Position(start, position));
		}
	}

	Token getBinary(IPosition position) {
		LinkedList<Byte> bytes = new LinkedList<Byte>();

		Position start = new Position(position);

		buffer.advance(position);

		if(!skipNonLetters(position))
			throw new TokenException(new Position(start, position));

		try {
			while(true) {
				String number = "";

				char chr = charAt(position);
				char nextChr = nextCharAt(position);

				int nByte = 0;
				int radix = 10;

				if(chr == '0' && (nextChr == 'x' || nextChr == 'X')) {
					buffer.advance(position, 2);
					radix = 16;
				}

				number += getDigitSequence(position, radix);

				nByte = Integer.parseInt(number, radix);

				if(nByte < 0 || nByte > 255)
					throw new TokenException(new Position(start, position));

				bytes.add((byte)nByte);

				if(!skipNonLetters(position))
					throw new TokenException(new Position(start, position));

				if(charAt(position) == '\'')
					break;

				if(!skipNonLetters(position))
					throw new TokenException(new Position(start, position));

				if(charAt(position) != ',')
					throw new TokenException(new Position(start, position));

				buffer.advance(position);

				if(!skipNonLetters(position))
					throw new TokenException(new Position(start, position));
			}

			buffer.advance(position);
		} catch(NumberFormatException e) {
			throw new TokenException(new Position(start, position));
		}

		byte[] array = new byte[bytes.size()];

		for(int i = 0; i < bytes.size(); i++)
			array[i] = bytes.get(i);

		return new BinaryToken(new Binary(array), new Position(start, position));
	}

	Token getGUID(IPosition position) {
		// {1601767A-6DAE-4e27-A765-9789FF1875D6}

		Position start = new Position(position);

		buffer.advance(position);

		String guid = getDigitSequence(position, 16);

		if(guid.length() != 8 || charAt(position) != '-')
			throw new TokenException(new Position(start, position));

		buffer.advance(position);

		guid += '-' + getDigitSequence(position, 16);

		if(guid.length() != 13 || charAt(position) != '-')
			throw new TokenException(new Position(start, position));

		buffer.advance(position);

		guid += '-' + getDigitSequence(position, 16);

		if(guid.length() != 18 || charAt(position) != '-')
			throw new TokenException(new Position(start, position));

		buffer.advance(position);

		guid += '-' + getDigitSequence(position, 16);

		if(guid.length() != 23 || charAt(position) != '-')
			throw new TokenException(new Position(start, position));

		buffer.advance(position);

		guid += '-' + getDigitSequence(position, 16);

		if(guid.length() != 36)
			throw new TokenException(new Position(start, position));

		if(charAt(position) != '\'')
			throw new TokenException(new Position(start, position));

		buffer.advance(position);

		try {
			return new GuidToken(UUID.fromString(guid), new Position(start, position));
		} catch(IllegalArgumentException e) {
			throw new TokenException(new Position(start, position));
		}
	}

	boolean isNumberSign(IPosition position) {
		if(charAt(position) != '-' && charAt(position) != '+')
			return false;

		if(currentToken.getId() != IToken.NOTHING && !(currentToken instanceof KeywordToken) || currentToken.getId() == IToken.RBRACE)
			return false;

		Position next = new Position(position);

		buffer.advance(next);

		if(!skipWhiteSpaces(next))
			return false;

		char chr = charAt(next);

		return ABC.isDigit(chr) || chr == '.';
	}
}
