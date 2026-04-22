package org.zenframework.z8.compiler.core;

public interface IToken {
	// literal types
	public static final int Nothing = 0;
	public static final int Eof = 0;

	public static final int Whitespace = 257;
	public static final int Linebreak = 258;
	public static final int Comment = 259;

	// operators
	public static final int Not = 260;
	public static final int BitwiseNot = 261;

	public static final int Mul = 262;
	public static final int Div = 263;
	public static final int Mod = 264;
	public static final int Plus = 265;
	public static final int Minus = 266;

	public static final int Equ = 267;
	public static final int NotEqu = 268;
	public static final int Less = 269;
	public static final int More = 270;
	public static final int LessEqu = 271;
	public static final int MoreEqu = 272;

	public static final int BitwiseAnd = 273;
	public static final int BitwiseOr = 274;
	public static final int BitwiseXor = 275;

	public static final int And = 276;
	public static final int Or = 277;

	public static final int AddAssign = 278;
	public static final int SubAssign = 279;
	public static final int MulAssign = 280;
	public static final int DivAssign = 281;
	public static final int ModAssign = 282;
	public static final int BitwiseAndAssign = 283;
	public static final int BitwiseOrAssign = 284;
	public static final int BitwiseXorAssign = 285;

	public static final int LBrace = 286;
	public static final int RBrace = 287;
	public static final int LBracket = 288;
	public static final int RBracket = 289;
	public static final int LCBrace = 290;
	public static final int RCBrace = 291;
	public static final int Brackets = 292;
	public static final int Colon = 293;
	public static final int Semicolon = 294;
	public static final int Assign = 295;
	public static final int Question = 296;
	public static final int Comma = 297;
	public static final int Elvis = 298;
	public static final int Dot = 299;

	public static final int Constant = 300;
	public static final int Identifier = 301;

	// keywords
	public static final int Operator = 302;

	public static final int If = 303;
	public static final int Else = 304;

	public static final int Do = 305;
	public static final int For = 306;
	public static final int While = 307;

	public static final int Break = 308;
	public static final int Return = 309;
	public static final int Continue = 310;

	public static final int This = 311;
	public static final int Super = 312;
	public static final int Container = 313;

	public static final int Null = 314;

	public static final int Import = 315;
	public static final int Class = 316;
	public static final int Public = 317;
	public static final int Protected = 318;
	public static final int Private = 319;
	public static final int Extends = 320;

	public static final int Enum = 321;
	public static final int Records = 322;

	public static final int Auto = 323;

	public static final int New = 324;
	public static final int Static = 325;

	public static final int Try = 326;
	public static final int Catch = 327;
	public static final int Finally = 328;
	public static final int Throw = 329;

	public static final int Virtual = 330;
	public static final int Final = 331;
	public static final int InstanceOf = 332;

	public int getId();
	public boolean is(int id);

	public IPosition getPosition();

	public String getRawText();
}
