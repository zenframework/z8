package org.zenframework.z8.compiler.core;

public interface IToken {
	// literal types
	public static final int NOTHING = 0;
	public static final int EOF = 0;

	public static final int WHITESPACE = 257;
	public static final int LINEBREAK = 258;
	public static final int COMMENT = 259;

	// operators
	public static final int NOT = 260;
	public static final int MUL = 261;
	public static final int DIV = 262;
	public static final int MOD = 263;
	public static final int PLUS = 264;
	public static final int MINUS = 265;
	public static final int CARET = 266;
	public static final int MUL_CARET = 267;
	public static final int CARET_MUL = 268;
	public static final int MUL_CARET_MUL = 269;

	public static final int EQU = 270;
	public static final int NOT_EQU = 271;
	public static final int LESS = 272;
	public static final int MORE = 273;
	public static final int LESS_EQU = 274;
	public static final int MORE_EQU = 275;
	public static final int AND = 276;
	public static final int OR = 277;
	public static final int GROUP = 278;

	public static final int ADD_ASSIGN = 279;
	public static final int SUB_ASSIGN = 280;
	public static final int MUL_ASSIGN = 281;
	public static final int DIV_ASSIGN = 282;
	public static final int MOD_ASSIGN = 283;
	public static final int CARET_ASSIGN = 284;
	public static final int MUL_CARET_ASSIGN = 285;
	public static final int CARET_MUL_ASSIGN = 286;
	public static final int MUL_CARET_MUL_ASSIGN = 287;

	public static final int LBRACE = 288;
	public static final int RBRACE = 289;
	public static final int LBRACKET = 290;
	public static final int RBRACKET = 291;
	public static final int LCBRACE = 292;
	public static final int RCBRACE = 293;
	public static final int BRACKETS = 294;
	public static final int COLON = 295;
	public static final int SEMICOLON = 296;
	public static final int ASSIGN = 297;
	public static final int QUESTION = 298;
	public static final int COMMA = 299;
	public static final int DOT = 300;

	public static final int CONSTANT = 301;
	public static final int IDENTIFIER = 302;

	// keywords
	public static final int OPERATOR = 303;

	public static final int IF = 304;
	public static final int ELSE = 305;

	public static final int DO = 306;
	public static final int FOR = 307;
	public static final int WHILE = 308;

	public static final int BREAK = 309;
	public static final int RETURN = 310;
	public static final int CONTINUE = 311;

	public static final int THIS = 312;
	public static final int SUPER = 313;
	public static final int CONTAINER = 314;

	public static final int NULL = 315;

	public static final int IMPORT = 316;
	public static final int CLASS = 317;
	public static final int PUBLIC = 318;
	public static final int PROTECTED = 319;
	public static final int PRIVATE = 320;
	public static final int EXTENDS = 321;

	public static final int ENUM = 322;
	public static final int RECORDS = 323;

	public static final int AUTO = 324;

	public static final int NEW = 325;
	public static final int STATIC = 326;

	public static final int TRY = 327;
	public static final int CATCH = 328;
	public static final int FINALLY = 329;
	public static final int THROW = 330;

	public static final int VIRTUAL = 331;
	public static final int FINAL = 332;

	public int getId();

	public IPosition getPosition();

	public String getRawText();
}
