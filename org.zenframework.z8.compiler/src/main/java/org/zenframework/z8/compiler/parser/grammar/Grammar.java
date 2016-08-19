
package org.zenframework.z8.compiler.parser.grammar;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.Token;

public class Grammar {
	int WHITESPACE = 257;
	int LINEBREAK = 258;
	int COMMENT = 259;
	int NOT = 260;
	int MUL = 261;
	int DIV = 262;
	int MOD = 263;
	int ADD = 264;
	int SUB = 265;
	int CARET = 266;
	int MUL_CARET = 267;
	int CARET_MUL = 268;
	int MUL_CARET_MUL = 269;
	int EQU = 270;
	int NOT_EQU = 271;
	int LESS = 272;
	int MORE = 273;
	int LESS_EQU = 274;
	int MORE_EQU = 275;
	int AND = 276;
	int OR = 277;
	int GROUP = 278;
	int ADD_ASSIGN = 279;
	int SUB_ASSIGN = 280;
	int MUL_ASSIGN = 281;
	int DIV_ASSIGN = 282;
	int MOD_ASSIGN = 283;
	int CARET_ASSIGN = 284;
	int MUL_CARET_ASSIGN = 285;
	int CARET_MUL_ASSIGN = 286;
	int MUL_CARET_MUL_ASSIGN = 287;
	int LBRACE = 288;
	int RBRACE = 289;
	int LBRACKET = 290;
	int RBRACKET = 291;
	int LCBRACE = 292;
	int RCBRACE = 293;
	int BRACKETS = 294;
	int COLON = 295;
	int SEMICOLON = 296;
	int ASSIGN = 297;
	int QUESTION = 298;
	int COMMA = 299;
	int DOT = 300;
	int CONSTANT = 301;
	int IDENTIFIER = 302;
	int OPERATOR = 303;
	int IF = 304;
	int ELSE = 305;
	int DO = 306;
	int FOR = 307;
	int WHILE = 308;
	int BREAK = 309;
	int RETURN = 310;
	int CONTINUE = 311;
	int THIS = 312;
	int SUPER = 313;
	int CONTAINER = 314;
	int NULL = 315;
	int IMPORT = 316;
	int CLASS = 317;
	int PUBLIC = 318;
	int PROTECTED = 319;
	int PRIVATE = 320;
	int EXTENDS = 321;
	int ENUM = 322;
	int RECORDS = 323;
	int AUTO = 324;
	int NEW = 325;
	int STATIC = 326;
	int TRY = 327;
	int CATCH = 328;
	int FINALLY = 329;
	int THROW = 330;
	int VIRTUAL = 331;
	int FINAL = 332;
	int YYERRCODE = 256;

	int yyexca[] = { -1, 1, 0, -1, -2, 0, -1, 71, 302, 94, 303, 94, -2, 104, -1, 243, 302, 96, -2, 265, -1, 244, 302, 97, -2, 266, 0, };

	int YYNPROD = 287;
	int YYLAST = 1254;
	int yyact[] = { 111, 48, 319, 220, 285, 35, 53, 110, 9, 283, 343, 344, 35, 425, 438, 151, 152, 35, 84, 156, 30, 287, 35, 198, 309, 193, 217, 141, 190, 116, 86, 201, 252, 154, 54, 78, 101, 56, 52, 80, 99, 100, 50, 430, 103, 193, 79, 404, 24, 26, 25, 222, 78, 405, 28, 141, 27,
			56, 188, 80, 150, 29, 80, 80, 106, 185, 79, 80, 205, 79, 79, 229, 141, 312, 79, 102, 55, 181, 35, 80, 56, 438, 56, 436, 402, 400, 79, 438, 107, 108, 113, 159, 231, 22, 403, 399, 70, 370, 145, 132, 451, 114, 143, 446, 445, 145, 441, 135, 23, 326, 423, 163, 136, 137,
			138, 139, 411, 232, 141, 241, 242, 240, 408, 80, 142, 134, 89, 80, 56, 407, 79, 65, 162, 101, 79, 368, 362, 99, 100, 361, 239, 197, 196, 228, 195, 244, 194, 401, 187, 245, 81, 65, 243, 57, 65, 65, 80, 145, 66, 128, 39, 106, 149, 79, 43, 323, 161, 160, 141, 426, 455,
			452, 448, 443, 181, 35, 312, 106, 189, 186, 106, 409, 156, 366, 164, 107, 108, 113, 175, 176, 181, 35, 249, 68, 220, 177, 178, 179, 114, 133, 247, 107, 108, 113, 203, 78, 203, 203, 203, 203, 203, 203, 92, 214, 114, 128, 314, 244, 192, 128, 230, 366, 244, 221, 243,
			334, 225, 191, 140, 243, 175, 176, 232, 93, 289, 104, 321, 83, 171, 172, 173, 174, 244, 325, 342, 111, 59, 69, 105, 243, 109, 110, 286, 167, 168, 169, 170, 165, 166, 98, 130, 132, 144, 91, 315, 90, 5, 14, 365, 235, 32, 341, 329, 337, 61, 441, 203, 358, 427, 101, 112,
			45, 234, 99, 100, 364, 162, 167, 168, 169, 170, 363, 230, 233, 223, 12, 63, 230, 32, 5, 347, 226, 184, 227, 216, 117, 102, 106, 360, 46, 144, 145, 320, 14, 131, 332, 130, 230, 318, 204, 181, 35, 155, 232, 68, 241, 242, 240, 237, 236, 238, 107, 108, 113, 284, 6, 120,
			124, 366, 72, 33, 74, 12, 248, 114, 3, 239, 251, 31, 228, 246, 78, 241, 436, 147, 148, 146, 73, 121, 244, 60, 192, 206, 64, 59, 244, 243, 244, 36, 6, 429, 58, 243, 37, 243, 219, 290, 3, 96, 69, 412, 221, 38, 409, 133, 422, 184, 204, 200, 239, 10, 424, 358, 15, 406,
			62, 410, 288, 291, 61, 80, 85, 47, 240, 431, 51, 41, 79, 435, 425, 80, 82, 42, 206, 40, 344, 11, 79, 63, 236, 203, 413, 414, 415, 416, 417, 418, 419, 420, 421, 184, 49, 322, 446, 230, 140, 285, 321, 319, 244, 230, 17, 230, 369, 405, 333, 243, 367, 340, 364, 450, 438,
			19, 244, 451, 453, 244, 220, 20, 428, 243, 4, 443, 243, 8, 444, 308, 7, 442, 311, 439, 452, 346, 432, 201, 433, 2, 203, 348, 454, 1, 455, 456, 217, 103, 350, 351, 352, 353, 354, 355, 356, 357, 401, 0, 449, 286, 115, 440, 345, 224, 222, 0, 92, 403, 405, 437, 34, 203,
			0, 0, 30, 436, 447, 230, 330, 338, 0, 0, 229, 0, 231, 230, 35, 76, 93, 83, 230, 230, 0, 0, 230, 38, 101, 70, 0, 71, 99, 100, 24, 26, 25, 0, 0, 77, 28, 0, 27, 0, 0, 0, 218, 29, 78, 302, 303, 71, 221, 0, 71, 71, 106, 249, 0, 71, 145, 324, 225, 247, 155, 0, 316, 0, 0,
			181, 35, 158, 232, 0, 241, 242, 240, 237, 236, 238, 107, 108, 113, 0, 188, 101, 342, 112, 0, 99, 100, 328, 336, 114, 0, 239, 0, 0, 228, 0, 78, 0, 0, 349, 350, 351, 352, 353, 354, 355, 356, 357, 341, 106, 141, 71, 0, 145, 215, 71, 221, 348, 0, 0, 185, 0, 181, 35, 223,
			232, 30, 241, 242, 240, 237, 236, 238, 107, 108, 113, 0, 101, 35, 76, 94, 99, 100, 0, 158, 0, 114, 115, 239, 16, 0, 228, 0, 78, 24, 26, 25, 0, 0, 0, 28, 0, 27, 44, 0, 106, 0, 29, 78, 145, 0, 0, 176, 96, 307, 0, 36, 310, 181, 35, 0, 232, 0, 241, 242, 240, 237, 236,
			238, 107, 108, 113, 0, 30, 16, 221, 62, 0, 0, 118, 0, 0, 114, 219, 239, 35, 76, 228, 0, 78, 30, 0, 331, 339, 0, 0, 44, 0, 0, 30, 0, 24, 26, 25, 0, 123, 0, 28, 0, 27, 0, 35, 0, 0, 29, 78, 9, 22, 24, 26, 25, 30, 21, 0, 28, 0, 27, 24, 26, 25, 30, 29, 23, 28, 0, 27, 0, 0,
			119, 125, 29, 78, 35, 327, 335, 0, 0, 0, 22, 24, 26, 25, 44, 21, 0, 28, 44, 27, 24, 26, 25, 0, 29, 23, 28, 101, 27, 0, 0, 99, 100, 29, 78, 101, 153, 0, 188, 99, 100, 399, 101, 0, 0, 0, 99, 100, 292, 293, 0, 0, 0, 0, 95, 322, 0, 101, 62, 205, 434, 99, 100, 106, 62,
			221, 0, 205, 181, 35, 322, 0, 0, 224, 205, 317, 181, 35, 0, 107, 108, 113, 0, 181, 35, 106, 0, 107, 108, 113, 0, 0, 114, 359, 107, 108, 113, 101, 181, 35, 114, 99, 100, 0, 0, 0, 0, 114, 0, 107, 108, 113, 221, 101, 0, 0, 0, 99, 100, 101, 218, 199, 114, 99, 100, 106,
			313, 0, 101, 0, 158, 0, 99, 100, 0, 0, 0, 0, 181, 35, 158, 106, 44, 97, 213, 0, 0, 106, 0, 107, 108, 113, 0, 0, 181, 35, 106, 0, 0, 87, 181, 35, 114, 0, 0, 107, 108, 113, 0, 88, 35, 107, 108, 113, 0, 166, 94, 0, 114, 0, 107, 108, 113, 0, 114, 0, 0, 0, 253, 0, 0, 282,
			0, 114, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 13, 273, 274, 275, 276, 277, 278, 279, 280, 281, 294, 295, 296, 297, 298, 299, 300, 301, 0, 272, 0, 202, 18, 0, 18, 18, 0, 0, 0, 0, 0, 180, 182, 183, 0, 18, 0, 0, 0, 66,
			202, 67, 0, 0, 207, 208, 209, 210, 211, 212, 0, 0, 0, 18, 0, 0, 0, 0, 0, 66, 0, 0, 122, 126, 0, 0, 0, 61, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 154, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 387, 388, 389, 390, 391, 392, 393, 394, 395,
			396, 397, 398, 254, 129, 304, 305, 306, 100, 0, 101, 177, 127, 135, 136, 137, 138, 139, 199, 62, 0, 0, 0, 148, 0, 0, 0, 0, 0, 0, 0, 0, 0, 168, 169, 170, 171, 172, 173, 174, 95, 0, 0, 0, 0, 250, 0, 0, 0, 0, 0, 75, 0, 0, 0, 0, 0, 0, 0, 0, 129, 0, 0, 0, 129, 0, 0, 67, 0,
			0, 0, 75, 0, 0, 75, 0, 0, 0, 0, 75, 0, 0, 0, 0, 0, 0, 0, 0, 127, 0, 0, 157, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 153, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 178, 179, 97,
			75, 0, 0, 0, 0, 0, 0, 0, 250, 0, 0, 0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 122, };

	int yypact[] = { 429, -1000, 460, 460, -308, -1000, -1000, -1000, -1000, -285, -132, -128, -1000, 460, -1000, -320, -1000, -260, -264, -1000, -1000, -1000, -1000, -311, -1000, -1000, -1000, -1000, -1000, -1000, -268, 460, -1000, -1000, -220, -1000, -1000, -140, 221, -1000,
			-1000, -143, -284, -1000, -1000, -1000, -1000, -272, -1000, -1000, -1000, -1000, -1000, -1000, 635, -1000, -273, -1000, 221, -1000, -1000, 412, 438, -1000, -1000, -1000, 221, -1000, -1000, -1000, -198, -172, -194, -130, -240, -287, -269, -1000, -285, -1000, -1000,
			-1000, -208, -1000, -1000, -1000, -1000, -1000, -124, -125, -1000, -166, -92, -1000, -13, -34, -66, -1000, -1000, 626, 626, 626, -235, -111, -1000, -242, 626, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -274, -263, -1000, -1000, -1000, -150, -152, -1000, 345,
			-1000, -154, -155, 469, -279, -1000, -1000, -1000, -1000, -224, -122, -224, -224, -224, -224, -224, -224, -1000, 620, -1000, -1000, 330, -1000, -1000, -144, -270, -1000, -1000, -106, 701, -1000, -106, -280, -1000, -281, -218, -284, -1000, -1000, 626, 626, 626, 626,
			626, 626, 626, 626, 626, 626, 626, 626, 626, 626, 626, 626, 626, 626, -1000, -1000, -1000, -1000, -122, -285, -235, -1000, -290, -113, -1000, -1000, 604, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -132, -1000, 549, -1000, -1000, -1000, -1000,
			-1000, -1000, -1000, -1000, -1000, -126, -1000, 273, -1000, -1000, -1000, -187, 19, -1000, -1000, -1000, -1000, -318, -318, 626, -1000, 329, -1000, -108, -1000, -1000, -1000, 564, -157, -160, -135, -108, 386, -105, -1000, -1000, -1000, -270, -1000, -161, -1000, -270,
			-200, -1000, -1000, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -106, -1000, -1000, -204, -1000, -1000, -1000, -1000, -148, -92, -1000, 21, 21, -76,
			-76, -76, -76, -76, -76, -76, -76, -66, -66, -1000, -1000, -1000, -243, -1000, -1000, -243, -1000, -1000, -1000, -205, -1000, -1000, -1000, -246, -1000, -1000, -1000, 626, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -167, -174, -1000, -1000,
			-1000, -1000, -1000, -1000, -1000, -1000, -107, -135, -1000, -1000, -180, 542, 626, 626, 626, 626, 626, 626, 626, 626, 626, 386, -1000, -186, -1000, -1000, -1000, 386, -295, -127, -1000, -1000, -1000, -258, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000,
			-1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -297, -1000, 626, -1000, 626, -1000, 534, -216, -1000, -1000, -297, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000,
			-1000, -1000, -1000, -1000, -291, -1000, -1000, -108, 626, -190, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, 542, -116, 386, -192, -193, 626, -117, -135, -1000, -1000, 626, -196, -1000, -1000, -118, 626, 386, -119, -1000, 386, -1000, };

	int yypgo[] = { 0, 480, 476, 345, 266, 467, 464, 461, 335, 497, 458, 71, 452, 658, 979, 441, 431, 267, 416, 414, 412, 237, 411, 406, 1000, 405, 402, 401, 393, 295, 390, 368, 388, 382, 373, 371, 274, 92, 705, 1020, 296, 193, 363, 247, 246, 360, 357, 356, 355, 354, 350, 347,
			192, 343, 200, 7, 0, 199, 1095, 96, 341, 888, 339, 803, 51, 4, 334, 322, 31, 319, 318, 2, 312, 310, 304, 26, 223, 551, 375, 3, 294, 500, 226, 303, 271, 244, 301, 44, 293, 282, 278, 269, 75, 265, 263, 212, 233, 649, 821, 378, 910, 259, 250, 248, 235, 280, 228, 227,
			218, 216, };

	int yyr1[] = { 0, 1, 1, 1, 1, 3, 3, 4, 4, 2, 7, 7, 8, 10, 10, 10, 12, 12, 12, 12, 12, 12, 13, 13, 14, 14, 9, 9, 9, 15, 16, 17, 18, 18, 6, 6, 20, 21, 22, 22, 22, 23, 23, 19, 19, 24, 24, 25, 26, 27, 28, 29, 29, 30, 30, 5, 32, 33, 34, 34, 31, 36, 36, 38, 38, 40, 40, 42, 42, 44,
			45, 45, 45, 45, 45, 45, 45, 45, 45, 35, 35, 46, 43, 48, 49, 49, 47, 51, 52, 53, 53, 54, 50, 50, 55, 56, 58, 58, 59, 59, 60, 60, 37, 37, 39, 39, 39, 39, 39, 39, 39, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62,
			62, 62, 62, 62, 62, 41, 41, 65, 66, 66, 67, 63, 63, 61, 61, 69, 68, 68, 68, 68, 70, 70, 71, 71, 72, 73, 64, 64, 74, 74, 76, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 75, 78, 83, 84, 85, 86, 86, 77, 77, 77, 80, 80, 88, 89, 90, 90,
			91, 91, 81, 81, 81, 82, 82, 82, 82, 79, 79, 79, 79, 79, 79, 79, 79, 79, 79, 79, 11, 93, 93, 94, 94, 95, 95, 96, 96, 96, 97, 97, 97, 97, 97, 97, 97, 97, 97, 98, 98, 98, 99, 99, 99, 99, 100, 100, 100, 100, 101, 101, 102, 103, 103, 87, 104, 104, 104, 104, 104, 104, 104,
			104, 106, 106, 57, 57, 92, 92, 92, 92, 92, 92, 92, 92, 92, 105, 108, 107, 107, 109, 109, };

	int yyr2[] = { 2, 0, 1, 1, 2, 1, 2, 1, 1, 1, 1, 2, 3, 3, 4, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 3, 2, 1, 1, 2, 1, 2, 1, 2, 1, 1, 1, 3, 2, 1, 2, 1, 2, 1, 2, 1, 1, 1, 2, 1, 3, 1, 2, 2, 2, 1, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 2, 3, 3, 3, 3, 2, 1, 1, 1, 2, 1, 2, 1, 1, 2, 2,
			1, 3, 1, 2, 2, 1, 2, 1, 2, 1, 1, 1, 2, 2, 3, 1, 3, 1, 3, 3, 3, 3, 3, 3, 3, 2, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 2, 2, 1, 1, 3, 1, 2, 3, 1, 1, 1, 1, 2, 3, 4, 1, 3, 1, 1, 5, 1, 2, 3, 1, 2, 1, 1, 1, 2, 2, 3, 3, 1, 1,
			1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 2, 5, 2, 2, 2, 1, 2, 2, 3, 5, 3, 5, 1, 1, 8, 9, 1, 1, 1, 2, 3, 2, 2, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 5, 1, 3, 1, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 1, 2, 2, 2, 1, 2, 2, 1, 3, 3, 1, 1, 1, 1, 1,
			1, 1, 1, 2, 3, 1, 2, 1, 1, 3, 3, 2, 2, 3, 3, 2, 2, 1, 2, 3, 1, 3, };

	int yychk[] = { -1000, -1, -2, -3, -7, -4, -8, -5, -6, 316, -30, -18, -29, -14, -17, -28, -13, -15, -24, -12, -10, 322, 317, 332, 318, 320, 319, 326, 324, 331, 290, -3, -4, -8, -9, 302, -31, -34, -33, 292, -19, -23, -20, 292, -13, -17, -29, -26, 321, -16, 302, -25, 302, 317,
			302, 296, 300, 293, -35, -44, -45, -36, -38, -40, -42, -37, -14, -39, -41, -43, -59, -9, -62, -46, -60, -58, 303, 323, 332, -55, -56, 293, -22, -21, 302, -27, 302, 291, 301, -11, -93, -94, -95, -96, -97, -98, -99, -100, -101, 264, 265, 260, -92, -87, -104, -103, 288,
			312, 313, -102, -55, -56, -105, 314, 325, -9, 302, -44, 296, -38, -36, -40, -14, 296, -36, -38, -14, -58, -37, -39, -41, -43, 297, -57, 297, 279, 284, 285, 286, 287, -106, 290, -64, 296, -73, 292, -47, -49, -48, 292, 300, 302, 303, -63, 302, -67, 288, -58, -9, 299,
			291, 291, 298, 277, 276, 270, 271, 266, 267, 268, 269, 272, 273, 274, 275, 264, 265, 261, 262, 263, -100, 301, -100, -100, -57, 300, -92, -87, 300, -11, 302, -107, -108, 288, 296, 296, 296, 296, 302, -61, -32, -68, -24, -11, -69, 292, -106, -61, -61, -61, -61, -61,
			-61, 291, -11, 293, -74, -75, -77, -78, -79, -76, -64, -80, -81, -82, -86, -83, 330, -11, -92, -37, 304, -88, -89, -91, 310, 309, 311, 327, 308, 306, 307, -55, -56, 293, -50, -54, -53, -52, -14, -51, 302, -63, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270,
			271, 272, 273, 274, 275, 276, 277, 297, 279, 280, 281, 282, 283, 284, 285, 286, 287, -63, 289, -66, -65, -59, 302, -21, -11, -95, -96, -97, -97, -98, -98, -98, -98, -98, -98, -98, -98, -99, -99, -100, -100, -100, -9, -105, 314, -9, -105, 289, 289, -109, -11, -31, 293,
			-70, -71, -72, -68, 288, 291, 293, -75, 296, -77, -78, -64, -80, -81, 296, -76, -79, -77, -78, -64, -80, -81, -82, -84, -85, 328, 329, -84, -85, -11, 297, 279, 280, 281, 282, 283, 284, 285, 286, 287, -87, 296, -11, 296, 296, -64, -87, -75, 288, -54, 296, -52, 297,
			-63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, -63, 299, 289, 295, 289, 299, 293, 299, -11, 296, 296, 288, -64, 296, -68, -11, -11, -11, -11, -11, -11, -11, -11, -11, -75, 296,
			-75, 308, 296, -90, -37, -79, 301, -65, -11, -11, 293, -71, 299, -59, 305, -87, -11, 296, -68, 289, -75, 296, 296, -11, 289, -64, -79, 296, 289, -79, -75, 289, -75, };

	int yydef[] = { 1, -2, 2, 3, 9, 5, 10, 7, 8, 0, 0, 34, 53, 0, 32, 51, 24, 0, 0, 22, 23, 29, 45, 0, 16, 17, 18, 19, 20, 21, 0, 4, 6, 11, 0, 26, 55, 0, 58, 57, 35, 43, 41, 36, 25, 33, 54, 0, 48, 31, 30, 50, 47, 46, 0, 12, 28, 60, 59, 79, 69, 0, 0, 77, 78, 61, 0, 63, 65, 67,
			102, -2, 0, 0, 98, 0, 0, 81, 0, 96, 97, 44, 42, 38, 37, 52, 49, 13, 262, 0, 224, 225, 227, 229, 231, 234, 243, 246, 250, 0, 0, 0, 254, 264, 272, 273, 0, 260, 261, 263, 265, 266, 267, 257, 0, 94, 27, 80, 70, 0, 0, 76, 0, 71, 0, 0, 0, 0, 62, 64, 66, 68, 0, 95, 0, 0, 0,
			0, 0, 0, 270, 0, 142, 143, 0, 162, 82, 0, 84, 83, 99, 100, 0, 112, 0, 0, 147, 0, 94, 40, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 251, 262, 252, 253, 277, 280, 255, 264, 276, 0, 256, 281, 0, 282, 72, 73, 74, 75, 100, 103, 150, 151, 0, 153, 0, 152,
			271, 105, 106, 107, 108, 109, 110, 268, 0, 163, 0, 165, 168, 169, 0, 0, 174, 175, 176, 177, 195, 0, 0, 213, 254, 167, 0, 206, 207, 208, 0, 0, 0, 0, 0, 0, 0, -2, -2, 86, 85, 92, 0, 89, 0, 0, 87, 111, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 113, 148, 0, 145, 144, 101, 39, 0, 228, 230, 232, 233, 239, 240, 241, 242, 235, 236, 237, 238, 244, 245, 247, 248, 249, 278, 279, 258, 274, 275, 259, 283, 0, 285, 56, 154, 0, 157, 159, 160, 0, 269, 164, 166, 170, 178, 179, 180, 181, 182, 171, 0, 0, 183,
			184, 185, 186, 187, 188, 194, 197, 0, 0, 193, 196, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 209, 0, 211, 212, 190, 0, 0, 0, 93, 91, 90, 0, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140,
			141, 0, 149, 0, 284, 0, 155, 0, 0, 172, 173, 0, 192, 189, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 198, 210, 200, 0, 0, 0, 202, 203, 88, 146, 226, 286, 156, 158, 0, 0, 0, 0, 0, 0, 0, 0, 199, 201, 0, 0, 161, 191, 0, 0, 0, 0, 204, 0, 205, };

	int yyxi;

	final int endStack = 100;
	final int newState = 101;

	final int YYMAXDEPTH = 1000;
	final int YYREDMAX = 1000;
	final int PCYYFLAG = -1000;
	final int WAS0ERR = 0;
	final int WAS1ERR = 1;
	final int WAS2ERR = 2;
	final int WAS3ERR = 3;

	Token yylval = new Token();
	Token yyval = new Token();

	Token[] s = new Token[YYMAXDEPTH]; /* value stack */
	int pcyyerrfl = 0; /* error flag */
	int pcyyerrct = 0; /* error count */

	int[] redseq = new int[YYREDMAX];
	int redcnt = 0;
	int pcyytoken = -1; /* input token */

	int[] statestack = new int[YYMAXDEPTH]; /* state stack */
	int j, m; /* working index */
	int s1;
	int tmpstate = 0, tmptoken = -1, n;

	int ssPos = -1;
	int sp = -1;

	Parser parser;

	Lexer getLexer() {
		return parser.getLexer();
	}

	void recover() {
		if(yylval.getId() != 0) {
			pcyytoken = -1;
			pcyyerrfl = WAS0ERR;
		}
	}

	void error(IPosition position, String message) {
		parser.getCompilationUnit().error(position, message);
	}

	void error() {
		error(yylval);
	}

	void error(IToken token) {
		error(token, null);
	}

	void error(String message) {
		error(getLexer().previousToken(), message);
	}

	void error(IToken token, String message) {
		if(token != null && token.getPosition() != null) // empty or fully
															// commented file
		{
			if(message == null) {
				message = "', delete this token";
			} else {
				message = "', " + message;
			}

			parser.getCompilationUnit().error(token.getPosition(), "Syntax error on token '" + getLexer().getString(token.getPosition()) + message);
		} else if(token != null && token.getId() == 0) {
			token = getLexer().previousToken();

			if(token.getPosition() != null) {
				parser.getCompilationUnit().error(token.getPosition(), "Syntax error on token '" + getLexer().getString(token.getPosition()) + "', unexpected end of file");
			}
		}
	}

	int lex() {
		yylval = getLexer().nextToken();
		return yylval.getId();
	}

	int parse(Parser parser) {
		this.parser = parser;

		int nResult = endStack;

		while(true) {

			// endStack:
			if(nResult == endStack) {
				/* push stack */
				if(++ssPos - YYMAXDEPTH > 0) {
					error(); // "pcyacc internal stack overflow"
					return (1);
				}
				statestack[ssPos] = tmpstate;
				++sp;
				s[sp] = yyval;
			}

			// newState:
			n = yypact[tmpstate];
			if(n <= PCYYFLAG) {
				nResult = doDefault();
				if(nResult < endStack)
					return nResult;
				continue; // defaultact; /* a simple state */
			}

			if(pcyytoken < 0) {
				if((pcyytoken = lex()) < 0)
					pcyytoken = 0;
			}

			if((n += pcyytoken) < 0 || n >= YYLAST) {
				nResult = doDefault();
				if(nResult < endStack)
					return nResult;
				continue; // defaultact;
			}

			if(yychk[n = yyact[n]] == pcyytoken) { /* a shift */
				pcyytoken = -1;
				yyval = yylval;
				tmpstate = n;
				if(pcyyerrfl > 0)
					--pcyyerrfl;
				continue; // enstack;
			}

			nResult = doDefault();

			if(nResult < endStack)
				return nResult;
		}
	}

	int doDefault() {
		if((n = yydef[tmpstate]) == -2) {
			if(pcyytoken < 0) {
				if((pcyytoken = lex()) < 0)
					pcyytoken = 0;
			}
			for(yyxi = 0; (yyexca[yyxi] != -1) || (yyexca[yyxi + 1] != tmpstate); yyxi += 2) {
			}
			while(yyexca[yyxi += 2] >= 0)
				if(yyexca[yyxi] == pcyytoken)
					break;
			if((n = yyexca[yyxi + 1]) < 0) { /* an accept action */
				return (0);
			}
		}

		if(n == 0) {
			/* error situation */
			switch(pcyyerrfl) {
			case WAS0ERR: /* an error just occurred */
				error();
				++pcyyerrct;
			case WAS1ERR:
			case WAS2ERR: /* try again */
				pcyyerrfl = WAS3ERR;
				/* find a state for a legal shift action */
				while(ssPos >= 0) {
					n = yypact[statestack[ssPos]] + YYERRCODE;
					if(n >= 0 && n < YYLAST && yychk[yyact[n]] == YYERRCODE) {
						tmpstate = yyact[n]; /* simulate a shift of "error" */
						return endStack;// break enstack;
					}
					n = yypact[statestack[ssPos]];

					/* the current yyps has no shift on "error", pop stack */
					--ssPos;
					--sp;
				}
				return (1);
			case WAS3ERR: /* clobber input char */
				if(pcyytoken == 0)
					return 1; /* quit */
				pcyytoken = -1;
				return newState;
			} /* switch */
		} /* if */

		/* reduction, given a production n */
		ssPos -= yyr2[n];
		s1 = sp;
		sp -= yyr2[n];
		yyval = s[sp + 1];
		m = n;
		/* find next state from goto table */
		n = yyr1[n];
		j = yypgo[n] + statestack[ssPos] + 1;
		if(j >= YYLAST || yychk[tmpstate = yyact[j]] != -n)
			tmpstate = yyact[yypgo[n]];

		switch(m) {

		case 9: {
			parser.onImport();
		}
			break;
		case 10: {
			parser.onImportList(true);
		}
			break;
		case 11: {
			parser.onImportList(false);
		}
			break;
		case 12: {
			parser.onImportElement(s[s1 - 2], s[s1 - 0]);
		}
			break;
		case 13: {
			parser.onAttribute(s[s1 - 2], s[s1 - 1], null, s[s1 - 0]);
		}
			break;
		case 14: {
			parser.onAttribute(s[s1 - 3], s[s1 - 2], s[s1 - 1], s[s1 - 0]);
		}
			break;
		case 15: {
			parser.onAttribute(s[s1 - 3], s[s1 - 2], s[s1 - 0]);
		}
			break;
		case 16: {
			parser.onToken(s[s1 - 0]);
		}
			break;
		case 17: {
			parser.onToken(s[s1 - 0]);
		}
			break;
		case 18: {
			parser.onToken(s[s1 - 0]);
		}
			break;
		case 19: {
			parser.onToken(s[s1 - 0]);
		}
			break;
		case 20: {
			parser.onToken(s[s1 - 0]);
		}
			break;
		case 21: {
			parser.onToken(s[s1 - 0]);
		}
			break;
		case 24: {
			parser.onModifiers(true);
		}
			break;
		case 25: {
			parser.onModifiers(false);
		}
			break;
		case 26: {
			parser.onQualifiedName(true, s[s1 - 0]);
		}
			break;
		case 27: {
			parser.onQualifiedName(false, s[s1 - 0]);
		}
			break;
		case 28: {
			error(s[s1 - 0]);
		}
			break;
		case 29: {
			parser.startEnum(s[s1 - 0]);
		}
			break;
		case 30: {
			parser.setEnumName(s[s1 - 0]);
		}
			break;
		case 33: {
			parser.applyModifiers();
		}
			break;
		case 34: {
			error("EnumBody is missing");
		}
			break;
		case 36: {
			parser.startEnumBody(s[s1 - 0]);
		}
			break;
		case 37: {
			parser.addEnumMember(s[s1 - 0]);
		}
			break;
		case 43: {
			error("'}' expected after this token");
		}
			break;
		case 44: {
			parser.finishEnumBody(s[s1 - 0]);
		}
			break;
		case 45: {
			parser.startClass(null, s[s1 - 0]);
		}
			break;
		case 46: {
			parser.startClass(s[s1 - 1], s[s1 - 0]);
		}
			break;
		case 47: {
			parser.setClassName(s[s1 - 0]);
		}
			break;
		case 49: {
			parser.setClassBase(s[s1 - 0]);
		}
			break;
		case 54: {
			parser.applyModifiers();
		}
			break;
		case 55: {
			parser.finishClass();
		}
			break;
		case 56: {
			parser.finishClass();
		}
			break;
		case 57: {
			parser.startClassBody(s[s1 - 0]);
		}
			break;
		case 60: {
			parser.finishClassBody(s[s1 - 0]);
		}
			break;
		case 62: {
			parser.applyModifiers();
		}
			break;
		case 64: {
			parser.applyModifiers();
		}
			break;
		case 66: {
			parser.applyModifiers();
		}
			break;
		case 68: {
			parser.applyModifiers();
		}
			break;
		case 69: {
			parser.addClassMember();
		}
			break;
		case 72: {
			parser.onMissingSemicolon();
		}
			break;
		case 73: {
			parser.onMissingSemicolon();
		}
			break;
		case 74: {
			parser.onMissingSemicolon();
		}
			break;
		case 75: {
			parser.onMissingSemicolon();
		}
			break;
		case 76: {
			parser.onMissingSemicolon();
		}
			break;
		case 81: {
			parser.startRecords(s[s1 - 0]);
		}
			break;
		case 82: {
			parser.finishRecords();
		}
			break;
		case 83: {
			parser.startRecordsBody(s[s1 - 0]);
		}
			break;
		case 86: {
			parser.finishRecordsBody(s[s1 - 0]);
		}
			break;
		case 87: {
			parser.startRecord(s[s1 - 0]);
		}
			break;
		case 88: {
			parser.setRecordValue(s[s1 - 0]);
		}
			break;
		case 90: {
			parser.applyModifiers();
		}
			break;
		case 91: {
			parser.addRecord();
		}
			break;
		case 94: {
			parser.onVariableType(false);
		}
			break;
		case 95: {
			parser.onVariableType(true);
		}
			break;
		case 99: {
			error(s[s1 - 0]);
		}
			break;
		case 100: {
			parser.onVariableDeclarator(s[s1 - 0]);
		}
			break;
		case 101: {
			parser.onVariableDeclarator(s[s1 - 2], s[s1 - 0]);
		}
			break;
		case 103: {
			parser.onVariableDeclaratorInit(s[s1 - 1]);
		}
			break;
		case 104: {
			parser.onVariableInit(null);
		}
			break;
		case 105: {
			parser.onVariableInit(s[s1 - 1]);
		}
			break;
		case 106: {
			parser.onVariableInit(s[s1 - 1]);
		}
			break;
		case 107: {
			parser.onVariableInit(s[s1 - 1]);
		}
			break;
		case 108: {
			parser.onVariableInit(s[s1 - 1]);
		}
			break;
		case 109: {
			parser.onVariableInit(s[s1 - 1]);
		}
			break;
		case 110: {
			parser.onVariableInit(s[s1 - 1]);
		}
			break;
		case 111: {
			parser.createMethod(s[s1 - 1]);
		}
			break;
		case 112: {
			parser.createPriorityOperator(s[s1 - 1]);
		}
			break;
		case 113: {
			parser.createCastOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 114: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 115: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 116: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 117: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 118: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 119: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 120: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 121: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 122: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 123: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 124: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 125: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 126: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 127: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 128: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 129: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 130: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 131: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 132: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 133: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 134: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 135: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 136: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 137: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 138: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 139: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 140: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 141: {
			parser.createOperator(s[s1 - 2], s[s1 - 1]);
		}
			break;
		case 142: {
			parser.setMethodBody();
		}
			break;
		case 144: {
			parser.createParameter();
		}
			break;
		case 147: {
			parser.startParameters(s[s1 - 0]);
		}
			break;
		case 148: {
			parser.finishParameters(s[s1 - 0]);
		}
			break;
		case 149: {
			parser.finishParameters(s[s1 - 0]);
		}
			break;
		case 152: {
			parser.startArrayInitializer(s[s1 - 0]);
		}
			break;
		case 155: {
			parser.finishArrayInitializer(s[s1 - 0]);
		}
			break;
		case 156: {
			parser.finishArrayInitializer(s[s1 - 0]);
		}
			break;
		case 159: {
			parser.addArrayInitializer();
		}
			break;
		case 160: {
			parser.addArrayInitializer();
		}
			break;
		case 161: {
			parser.onMapElement(s[s1 - 4], s[s1 - 0]);
		}
			break;
		case 162: {
			parser.startCompound(s[s1 - 0]);
		}
			break;
		case 163: {
			parser.finishCompound(s[s1 - 0]);
		}
			break;
		case 164: {
			parser.finishCompound(s[s1 - 0]);
		}
			break;
		case 165: {
			parser.addStatement();
		}
			break;
		case 166: {
			parser.addStatement();
		}
			break;
		case 167: {
			parser.onDeclarator();
		}
			break;
		case 170: {
			parser.onStatement();
		}
			break;
		case 171: {
			parser.onStatement();
		}
			break;
		case 172: {
			parser.onStatement2();
		}
			break;
		case 173: {
			parser.onStatement2();
		}
			break;
		case 178: {
			parser.onStatement1();
		}
			break;
		case 179: {
			parser.onStatement1();
		}
			break;
		case 180: {
			parser.onStatement1();
		}
			break;
		case 181: {
			parser.onStatement1();
		}
			break;
		case 182: {
			parser.onStatement1();
		}
			break;
		case 183: {
			parser.onStatement1();
		}
			break;
		case 184: {
			parser.onStatement1();
		}
			break;
		case 185: {
			parser.onStatement1();
		}
			break;
		case 186: {
			parser.onStatement1();
		}
			break;
		case 187: {
			parser.onStatement1();
		}
			break;
		case 188: {
			parser.onStatement1();
		}
			break;
		case 189: {
			parser.onThrowStatement(s[s1 - 2]);
		}
			break;
		case 190: {
			parser.onTryStatement(s[s1 - 1]);
		}
			break;
		case 191: {
			parser.onCatchClause(s[s1 - 4]);
		}
			break;
		case 192: {
			parser.onFinallyStatement(s[s1 - 1]);
		}
			break;
		case 193: {
			parser.addCatchStatement();
		}
			break;
		case 194: {
			parser.addCatchStatement();
		}
			break;
		case 198: {
			parser.onIfStatement(s[s1 - 2], null);
		}
			break;
		case 199: {
			parser.onIfStatement(s[s1 - 4], s[s1 - 1]);
		}
			break;
		case 200: {
			parser.onWhileStatement(s[s1 - 2]);
		}
			break;
		case 201: {
			parser.onDoWhileStatement(s[s1 - 4], s[s1 - 2]);
		}
			break;
		case 202: {
			parser.onDeclarator();
		}
			break;
		case 204: {
			parser.onForStatement(s[s1 - 7], false);
		}
			break;
		case 205: {
			parser.onForStatement(s[s1 - 8], true);
		}
			break;
		case 209: {
			parser.onJumpStatement(s[s1 - 1], false);
		}
			break;
		case 210: {
			parser.onJumpStatement(s[s1 - 2], true);
		}
			break;
		case 211: {
			parser.onJumpStatement(s[s1 - 1], false);
		}
			break;
		case 212: {
			parser.onJumpStatement(s[s1 - 1], false);
		}
			break;
		case 214: {
			parser.onAssignment(s[s1 - 1]);
		}
			break;
		case 215: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 216: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 217: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 218: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 219: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 220: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 221: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 222: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 223: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 226: {
			parser.onCondition();
		}
			break;
		case 228: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 230: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 232: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 233: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 235: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 236: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 237: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 238: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 239: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 240: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 241: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 242: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 244: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 245: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 247: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 248: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 249: {
			parser.onOperator(s[s1 - 1]);
		}
			break;
		case 252: {
			parser.onUnary(s[s1 - 1]);
		}
			break;
		case 253: {
			parser.onUnary(s[s1 - 1]);
		}
			break;
		case 255: {
			parser.onTypeCast();
		}
			break;
		case 256: {
			parser.onNewExpression(s[s1 - 1], s[s1 - 0]);
		}
			break;
		case 257: {
			parser.onContainer(s[s1 - 0]);
		}
			break;
		case 258: {
			parser.addContainer(s[s1 - 0]);
		}
			break;
		case 259: {
			parser.onBracedExpression(s[s1 - 2], s[s1 - 0]);
		}
			break;
		case 260: {
			parser.onThis(s[s1 - 0]);
		}
			break;
		case 261: {
			parser.onSuper(s[s1 - 0]);
		}
			break;
		case 262: {
			parser.onConstant(s[s1 - 0]);
		}
			break;
		case 265: {
			parser.onTypeToPostfix();
		}
			break;
		case 266: {
			parser.onTypeToPostfix();
		}
			break;
		case 267: {
			parser.onMethodCall(false);
		}
			break;
		case 268: {
			parser.onIndex(s[s1 - 1], s[s1 - 0], false);
		}
			break;
		case 269: {
			parser.onIndex(s[s1 - 2], s[s1 - 0], true);
		}
			break;
		case 270: {
			parser.onIndices(true);
		}
			break;
		case 271: {
			parser.onIndices(false);
		}
			break;
		case 274: {
			parser.onPostfix();
		}
			break;
		case 275: {
			parser.onMethodCall(true);
		}
			break;
		case 276: {
			error(s[s1 - 0]);
		}
			break;
		case 277: {
			parser.onArrayAccess();
		}
			break;
		case 278: {
			parser.onPostfix();
		}
			break;
		case 279: {
			parser.onMethodCall(true);
		}
			break;
		case 280: {
			error(s[s1 - 0]);
		}
			break;
		case 282: {
			parser.startExpressions(s[s1 - 0]);
		}
			break;
		case 283: {
			parser.finishExpressions(s[s1 - 0]);
		}
			break;
		case 284: {
			parser.finishExpressions(s[s1 - 0]);
		}
			break;
		case 285: {
			parser.addExpression();
		}
			break;
		case 286: {
			parser.addExpression();
		}
			break;
		}
		return endStack;
	}
}