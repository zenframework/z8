
package org.zenframework.z8.compiler.parser.grammar;

//import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.Token;

public class Grammar
{
int WHITESPACE = 257;
int LINEBREAK = 258;
int COMMENT = 259;
int NOT = 260;
int MUL = 261;
int DIV = 262;
int MOD = 263;
int ADD = 264;
int SUB = 265;
int EQU = 266;
int NOT_EQU = 267;
int LESS = 268;
int MORE = 269;
int LESS_EQU = 270;
int MORE_EQU = 271;
int AND = 272;
int OR = 273;
int GROUP = 274;
int ADD_ASSIGN = 275;
int SUB_ASSIGN = 276;
int MUL_ASSIGN = 277;
int DIV_ASSIGN = 278;
int MOD_ASSIGN = 279;
int LBRACE = 280;
int RBRACE = 281;
int LBRACKET = 282;
int RBRACKET = 283;
int LCBRACE = 284;
int RCBRACE = 285;
int BRACKETS = 286;
int COLON = 287;
int SEMICOLON = 288;
int ASSIGN = 289;
int QUESTION = 290;
int COMMA = 291;
int DOT = 292;
int CONSTANT = 293;
int IDENTIFIER = 294;
int OPERATOR = 295;
int IF = 296;
int ELSE = 297;
int DO = 298;
int FOR = 299;
int WHILE = 300;
int BREAK = 301;
int RETURN = 302;
int CONTINUE = 303;
int THIS = 304;
int SUPER = 305;
int CONTAINER = 306;
int NULL = 307;
int IMPORT = 308;
int CLASS = 309;
int PUBLIC = 310;
int PROTECTED = 311;
int PRIVATE = 312;
int EXTENDS = 313;
int ENUM = 314;
int RECORDS = 315;
int AUTO = 316;
int NEW = 317;
int STATIC = 318;
int TRY = 319;
int CATCH = 320;
int FINALLY = 321;
int THROW = 322;
int VIRTUAL = 323;
int FINAL = 324;
int YYERRCODE = 256;

int yyexca[] = {
  -1, 1,
  0, -1,
  -2, 0,
  -1, 71,
  294, 94,
  295, 94,
  -2, 104,
  -1, 233,
  294, 96,
  -2, 244,
  -1, 234,
  294, 97,
  -2, 245,
  0,
};

int YYNPROD = 268;
int YYLAST = 1187;
int yyact[] = {
     104,     262,      35,      48,     260,     107,     112,      53,
     284,     107,       9,      70,     318,     319,     382,     141,
     102,      35,     396,     301,      35,     103,     173,      35,
      35,     387,     207,     222,      84,     231,     232,     230,
      78,     110,     111,     114,     289,     152,     210,      80,
     147,     148,     115,     264,     101,      79,     115,      78,
      99,     100,     229,     150,     194,     218,     189,     116,
      54,      52,     188,      80,     137,      50,      80,      80,
     107,      79,     184,      80,      79,      79,      56,     188,
     383,      79,     137,      56,     212,     173,      35,      80,
     146,     396,     342,      56,     177,      79,     396,     137,
     110,     111,     114,     155,     324,     325,     326,     327,
     328,      56,     135,     137,     132,     115,     409,     404,
     180,     137,     323,     109,      78,     177,      55,     292,
     134,     361,      56,      56,     364,      34,     359,     362,
     178,     392,     403,      80,     399,     179,     365,      80,
     358,      79,     380,      30,     141,      79,     159,     219,
     139,     141,     379,     370,     367,     234,      38,      35,
      76,     366,      71,     233,     340,     138,     334,     158,
      80,     333,     193,      86,     192,     220,      79,      24,
      26,      25,     191,     263,      77,      28,      71,      27,
     190,      71,      71,     360,      29,      78,      71,      30,
     235,      81,     183,     151,      57,     122,      39,     145,
      43,     298,     154,      35,      76,     157,      89,     156,
     137,     413,      58,     140,     410,     406,     401,     292,
     368,     222,     152,      24,      26,      25,     338,     234,
     160,      28,     239,      27,     234,     233,     133,     210,
      29,      78,     233,     161,     162,     167,     168,     220,
     215,     107,     317,      83,     220,      69,      71,      93,
     234,     300,      71,     104,     237,     102,     233,     294,
     112,     187,     186,     185,     329,     221,     105,     106,
     220,      36,     309,     136,     336,     169,     170,     171,
     108,     316,     337,     154,     167,     168,     109,      98,
     163,     164,     165,     166,     197,      30,     197,     197,
      12,     204,      91,     368,      14,      90,     225,     384,
     214,     242,     224,     223,      65,     280,      46,     304,
     312,     198,      45,     213,     290,     216,     266,      92,
     131,      24,      26,      25,     217,     206,     211,      28,
      65,      27,     335,      65,      65,       5,      29,      66,
     128,      32,      59,      67,     140,     176,     286,      12,
     285,     132,      14,     295,     283,     182,     151,     231,
     261,      72,     234,      74,     238,     137,     195,       3,
     233,     234,      31,     234,     158,      32,       5,     233,
     241,     233,     220,     236,     338,      96,     143,     230,
       6,     220,     322,     220,     378,      33,     382,      80,
     388,      69,     331,     381,     329,      79,     144,       3,
     128,      80,     263,     391,     128,     117,     338,      79,
     142,     386,      73,      60,     395,     211,     129,     265,
      64,     200,       6,      58,     184,     229,     262,     133,
     268,     176,     181,      37,     369,     234,     358,      38,
     392,     400,     182,     233,     397,     336,     196,      70,
     396,     284,      10,     234,     319,     220,     234,      15,
      85,     233,      47,     103,     233,     220,     399,     402,
     363,     404,     220,     220,     200,     410,     220,     218,
      59,      51,     209,     176,     315,     412,     129,     413,
     414,     207,     129,     408,     321,      67,     401,     409,
     411,     297,     210,     341,     136,      30,      41,     267,
      82,      42,     226,     371,     373,     374,     375,     376,
     377,      40,     154,     288,      11,     201,     202,     323,
     287,     339,      49,     320,     154,     296,     407,       9,
      22,      24,      26,      25,     212,      21,      94,      28,
      17,      27,      19,     306,     314,      83,      29,      23,
     389,      20,     286,     323,      93,     393,     305,     313,
     208,       4,       8,     324,       7,     325,     326,     327,
     328,     360,     308,      30,     332,     275,     276,     398,
      63,     123,       2,       1,     181,     362,     101,      35,
     286,     383,      99,     100,       0,       0,     392,     405,
     135,     195,       0,       0,     215,     219,       0,      24,
      26,      25,     107,      36,     317,      28,     141,      27,
     239,       0,     307,       0,      29,      78,       0,     173,
      35,       0,     222,      92,     231,     232,     230,     227,
     226,     228,     110,     111,     114,       0,      30,     113,
     237,     198,     316,       0,       0,       0,     121,     115,
     385,     229,     226,     211,     218,       0,      78,       0,
       0,       0,     101,     214,       0,     211,      99,     100,
       0,      22,      24,      26,      25,     213,      21,       0,
      28,       0,      27,     211,       0,     372,     107,      29,
      23,       0,     141,     299,      68,       0,     234,       0,
       0,     168,      96,     173,      35,       0,     222,     221,
     231,     232,     230,     227,     226,     228,     110,     111,
     114,       0,      63,       0,       0,     303,     311,       0,
     269,     270,       0,     115,     287,     229,       0,     394,
     218,       0,      78,       0,       0,     101,     362,       0,
       0,      99,     100,     244,     245,     246,     247,     248,
     249,     250,     251,     252,     253,     254,     255,     256,
     257,     107,     287,       0,     392,     141,     205,     130,
     101,       0,       0,       0,      99,     100,     173,      35,
     258,     222,       0,     231,     232,     230,     227,     226,
     228,     110,     111,     114,     107,       0,       0,       0,
     141,      62,       0,     302,     310,       0,     115,       0,
     229,     173,      35,     218,     222,      78,     231,     232,
     230,     227,     226,     228,     110,     111,     114,     101,
      30,       0,     211,      99,     100,       0,     118,     130,
       0,     115,     209,     229,      35,      76,     218,      68,
      78,     281,       0,     107,     162,      94,       0,     183,
     291,      30,       0,       0,      24,      26,      25,       0,
     173,      35,      28,     149,      27,      35,       0,     119,
     125,      29,      78,     110,     111,     114,       0,       0,
      22,       0,       0,       0,       0,      24,      26,      25,
     115,       0,       0,      28,     101,      27,       0,      23,
      99,     100,      29,      78,     101,       0,       0,     211,
      99,     100,       0,       0,      95,       0,       0,     208,
     288,       0,       0,       0,     183,     390,       0,       0,
     107,       0,       0,       0,     183,     173,      35,     101,
     330,      62,       0,      99,     100,     173,      35,      62,
     110,     111,     114,       0,       0,       0,      61,       0,
     110,     111,     114,     107,     293,     115,       0,     183,
       0,     184,     101,       0,       0,     115,      99,     100,
     173,      35,     101,     113,      18,       0,      99,     100,
       0,       0,       0,     110,     111,     114,     288,       0,
       0,       0,     183,     282,       0,       0,     288,       0,
     115,      97,     183,     173,      35,     101,       0,       0,
       0,      99,     100,     173,      35,     101,     110,     111,
     114,      99,     100,       0,     120,     124,     110,     111,
     114,     107,       0,     115,       0,     183,       0,     243,
       0,     107,     259,     115,     203,      13,     173,      35,
     101,       0,      16,       0,      99,     100,     173,      35,
     101,     110,     111,     114,      99,     100,       0,       0,
      44,     110,     111,     114,     107,       0,     115,      87,
       0,       0,       0,       0,     107,       0,     115,       0,
       0,      88,      35,       0,      66,       0,       0,      62,
       0,     173,      35,      61,     110,     111,     114,      16,
     271,     272,     273,     274,     110,     111,     114,       0,
      66,     115,       0,     122,     126,     172,     174,     175,
      61,     115,       0,       0,       0,      44,       0,       0,
       0,     199,       0,     199,     199,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,     150,
     343,     344,     345,     346,     347,     348,     349,     350,
     351,     352,     353,     354,     355,     356,     357,     244,
     127,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
      62,       0,       0,       0,     144,      44,       0,       0,
       0,      44,       0,     277,     278,     279,     100,       0,
     101,     169,       0,       0,      18,       0,     240,      75,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,     164,     165,     166,      95,
       0,       0,       0,      75,       0,       0,      75,       0,
       0,       0,       0,      75,       0,       0,       0,       0,
       0,       0,       0,       0,     127,       0,       0,     153,
       0,       0,       0,     245,     246,     247,     248,     249,
     250,     251,     252,     253,     254,     255,     256,     257,
     258,     149,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,      75,       0,       0,       0,       0,
       0,       0,     240,       0,       0,       0,      13,     170,
     171,      97,       0,      44,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,     122,
};

int yypact[] = {
     171,   -1000,     292,     292,    -298,   -1000,   -1000,   -1000,
   -1000,    -274,    -102,    -100,   -1000,     292,   -1000,    -310,
   -1000,    -233,    -237,   -1000,   -1000,   -1000,   -1000,    -302,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,    -238,     292,
   -1000,   -1000,    -178,   -1000,   -1000,    -105,    -151,   -1000,
   -1000,    -108,    -266,   -1000,   -1000,   -1000,   -1000,    -274,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,     668,   -1000,
    -239,   -1000,    -151,   -1000,   -1000,     454,     233,   -1000,
   -1000,   -1000,    -151,   -1000,   -1000,   -1000,    -189,    -177,
    -152,    -101,    -212,    -254,    -243,   -1000,    -274,   -1000,
   -1000,   -1000,    -200,   -1000,   -1000,   -1000,    -217,   -1000,
     -92,     -94,   -1000,    -139,     -64,   -1000,     -47,      -4,
      -8,   -1000,   -1000,     676,     676,     676,    -208,    -271,
    -106,   -1000,    -226,     676,   -1000,    -222,   -1000,   -1000,
   -1000,   -1000,   -1000,    -240,   -1000,   -1000,   -1000,    -120,
    -126,   -1000,    -107,   -1000,    -132,    -134,     479,    -242,
   -1000,   -1000,   -1000,   -1000,     475,     -90,     475,     475,
   -1000,     641,   -1000,   -1000,     401,   -1000,   -1000,    -109,
     -13,   -1000,   -1000,     -78,     407,   -1000,     -78,    -277,
   -1000,    -251,    -195,    -266,   -1000,   -1000,     676,     676,
     676,     676,     676,     676,     676,     676,     676,     676,
     676,     676,     676,     676,   -1000,   -1000,   -1000,   -1000,
     -90,    -274,    -208,   -1000,   -1000,   -1000,     598,   -1000,
    -270,     -82,   -1000,     571,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,    -102,
   -1000,   -1000,   -1000,   -1000,     -98,   -1000,     334,   -1000,
   -1000,   -1000,    -269,     266,   -1000,   -1000,   -1000,   -1000,
    -308,    -308,     676,   -1000,    -183,   -1000,    -275,   -1000,
   -1000,   -1000,     544,    -135,    -138,    -147,    -275,     428,
     -74,   -1000,    -106,   -1000,     -13,   -1000,    -140,   -1000,
     -13,    -207,   -1000,   -1000,     -78,     -78,     -78,     -78,
     -78,     -78,     -78,     -78,     -78,     -78,     -78,     -78,
     -78,     -78,     -78,   -1000,   -1000,    -163,   -1000,   -1000,
   -1000,   -1000,    -116,     -64,   -1000,   -1000,   -1000,     -43,
     -43,     -43,     -43,      -8,      -8,   -1000,   -1000,   -1000,
    -209,   -1000,   -1000,    -172,   -1000,   -1000,   -1000,   -1000,
     676,   -1000,    -209,   -1000,   -1000,   -1000,    -165,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,    -143,    -148,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,     -80,    -147,
   -1000,   -1000,    -149,     633,     676,     676,     676,     676,
     676,     428,   -1000,    -150,    -158,   -1000,   -1000,   -1000,
     428,    -286,    -216,   -1000,   -1000,   -1000,    -268,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,    -292,   -1000,
     676,   -1000,     536,    -170,   -1000,     633,   -1000,   -1000,
    -292,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,    -279,   -1000,   -1000,   -1000,    -275,     676,
    -164,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
     606,   -1000,   -1000,     -83,     428,    -166,    -185,     676,
     -84,    -147,   -1000,   -1000,     676,    -186,   -1000,   -1000,
     -85,     676,     428,     -88,   -1000,     428,   -1000,
};

int yypgo[] = {
       0,     523,     522,     335,     309,     508,     506,     505,
     352,     107,     497,     135,     490,     930,     925,     488,
     474,     276,     468,     465,     457,     227,     456,     454,
     868,     433,     418,     416,     415,     272,     410,     249,
     406,     399,     395,     387,     846,     245,     713,     315,
     520,     620,     384,     229,     314,     379,     378,     376,
     366,     350,     347,     344,     210,     332,     236,       6,
       0,     214,    1032,      11,     331,     334,     329,     771,
      76,       1,     328,     326,     289,     325,     324,       8,
     320,     316,     301,      26,     302,     504,     434,      38,
     291,     280,     224,     300,     257,     226,     293,      21,
     283,     282,     279,     278,      16,     277,     274,     295,
     231,     486,     812,     349,     889,     263,     256,     247,
     246,     575,     251,     242,     241,     239,
};

int yyr1[] = {
       0,       1,       1,       1,       1,       3,       3,       4,
       4,       2,       7,       7,       8,      10,      10,      10,
      12,      12,      12,      12,      12,      12,      13,      13,
      14,      14,       9,       9,       9,      15,      16,      17,
      18,      18,       6,       6,      20,      21,      22,      22,
      22,      23,      23,      19,      19,      24,      24,      25,
      26,      27,      28,      29,      29,      30,      30,       5,
      32,      33,      34,      34,      31,      36,      36,      38,
      38,      40,      40,      42,      42,      44,      45,      45,
      45,      45,      45,      45,      45,      45,      45,      35,
      35,      46,      43,      48,      49,      49,      47,      51,
      52,      53,      53,      54,      50,      50,      55,      56,
      58,      58,      59,      59,      60,      60,      37,      37,
      39,      39,      39,      62,      62,      62,      62,      62,
      62,      62,      62,      62,      62,      62,      62,      62,
      62,      62,      62,      62,      62,      41,      41,      65,
      66,      66,      67,      63,      63,      61,      61,      61,
      69,      68,      68,      68,      70,      70,      71,      71,
      71,      72,      73,      64,      64,      74,      74,      76,
      75,      75,      75,      75,      75,      75,      75,      75,
      75,      75,      75,      75,      75,      75,      75,      75,
      75,      75,      75,      75,      75,      78,      83,      84,
      85,      86,      86,      77,      77,      77,      80,      80,
      88,      89,      90,      90,      91,      91,      81,      81,
      81,      82,      82,      82,      82,      82,      79,      79,
      79,      79,      79,      79,      79,      79,      11,      93,
      93,      94,      94,      95,      95,      96,      96,      96,
      97,      97,      97,      97,      97,      98,      98,      98,
      99,      99,      99,      99,     100,     100,     100,     100,
     101,     101,     101,     102,     103,     103,      87,      87,
     104,     104,     104,     104,     104,     104,     104,     106,
     106,      57,      57,      92,      92,      92,      92,      92,
      92,      92,      92,      92,     105,     108,     107,     107,
     109,     109,     109,     109,
};

int yyr2[] = {
       2,       0,       1,       1,       2,       1,       2,       1,
       1,       1,       1,       2,       3,       3,       4,       4,
       1,       1,       1,       1,       1,       1,       1,       1,
       1,       2,       1,       3,       2,       1,       1,       2,
       1,       2,       1,       2,       1,       1,       1,       3,
       2,       1,       2,       1,       2,       1,       2,       1,
       1,       1,       2,       1,       3,       1,       2,       2,
       2,       1,       1,       2,       2,       1,       2,       1,
       2,       1,       2,       1,       2,       1,       2,       2,
       3,       3,       3,       3,       2,       1,       1,       1,
       2,       1,       2,       1,       1,       2,       2,       1,
       3,       1,       2,       2,       1,       2,       1,       2,
       1,       1,       1,       2,       2,       3,       1,       3,
       1,       3,       3,       3,       2,       3,       4,       4,
       4,       4,       4,       4,       4,       4,       4,       4,
       4,       4,       4,       4,       4,       2,       2,       1,
       1,       3,       1,       2,       3,       1,       1,       1,
       1,       2,       3,       4,       1,       3,       1,       1,
       1,       5,       1,       2,       3,       1,       2,       1,
       1,       1,       2,       2,       3,       3,       1,       1,
       1,       1,       2,       2,       2,       2,       2,       2,
       2,       2,       2,       2,       2,       3,       2,       5,
       2,       2,       2,       1,       2,       2,       3,       5,
       3,       5,       1,       1,       8,       9,       1,       1,
       1,       2,       3,       3,       2,       2,       1,       3,
       3,       3,       3,       3,       3,       3,       1,       1,
       5,       1,       3,       1,       3,       1,       3,       3,
       1,       3,       3,       3,       3,       1,       3,       3,
       1,       3,       3,       3,       1,       2,       2,       2,
       1,       2,       2,       2,       1,       3,       3,       1,
       1,       1,       1,       1,       1,       1,       1,       2,
       3,       1,       2,       1,       1,       3,       3,       2,
       2,       3,       3,       2,       2,       1,       2,       3,
       1,       1,       3,       3,
};

int yychk[] = {
   -1000,      -1,      -2,      -3,      -7,      -4,      -8,      -5,
      -6,     308,     -30,     -18,     -29,     -14,     -17,     -28,
     -13,     -15,     -24,     -12,     -10,     314,     309,     324,
     310,     312,     311,     318,     316,     323,     282,      -3,
      -4,      -8,      -9,     294,     -31,     -34,     -33,     284,
     -19,     -23,     -20,     284,     -13,     -17,     -29,     -26,
     313,     -16,     294,     -25,     294,     309,     294,     288,
     292,     285,     -35,     -44,     -45,     -36,     -38,     -40,
     -42,     -37,     -14,     -39,     -41,     -43,     -59,      -9,
     -62,     -46,     -60,     -58,     295,     315,     324,     -55,
     -56,     285,     -22,     -21,     294,     -27,      -9,     283,
     293,     -11,     -93,     -94,     -95,     -96,     -97,     -98,
     -99,    -100,    -101,     264,     265,     260,     -92,     -87,
     -56,    -104,    -103,     280,    -102,      -9,     304,     305,
     -55,    -105,     306,     317,     294,     -44,     288,     -38,
     -36,     -40,     -14,     288,     -36,     -38,     -14,     -58,
     -37,     -39,     -41,     -43,     289,     -57,     289,     275,
    -106,     282,     -64,     288,     -73,     284,     -47,     -49,
     -48,     284,     292,     294,     295,     -63,     294,     -67,
     280,     -58,      -9,     291,     283,     283,     290,     273,
     272,     266,     267,     268,     269,     270,     271,     264,
     265,     261,     262,     263,    -100,     293,    -100,    -100,
     -57,     292,     -92,     -87,     -56,     -68,     -69,     284,
     292,     -11,    -107,    -108,     280,     294,     288,     288,
     288,     288,     294,     -61,     -32,     -11,     -68,     -24,
    -106,     -61,     -61,     283,     -11,     285,     -74,     -75,
     -77,     -78,     -79,     -76,     -64,     -80,     -81,     -82,
     -86,     -83,     322,     -11,     -92,     -37,     296,     -88,
     -89,     -91,     302,     301,     303,     319,     300,     298,
     299,     -55,     -56,     285,     -50,     -54,     -53,     -52,
     -14,     -51,     294,     -63,     260,     261,     262,     263,
     264,     265,     266,     267,     268,     269,     270,     271,
     272,     273,     289,     -63,     281,     -66,     -65,     -59,
     294,     -21,     -11,     -95,     -96,     -97,     -97,     -98,
     -98,     -98,     -98,     -99,     -99,    -100,    -100,    -100,
      -9,    -105,     285,     -70,     -71,     -72,     -11,     -68,
     280,     306,      -9,    -105,     281,     281,    -109,     -11,
     -68,     -31,     283,     285,     -75,     288,     -77,     -78,
     -64,     -80,     -81,     288,     -76,     -79,     -77,     -78,
     -64,     -80,     -81,     -82,     -84,     -85,     320,     321,
     -84,     -85,     -11,     289,     275,     276,     277,     278,
     279,     -87,     288,     -11,     -68,     288,     288,     -64,
     -87,     -75,     280,     -54,     288,     -52,     289,     -63,
     -63,     -63,     -63,     -63,     -63,     -63,     -63,     -63,
     -63,     -63,     -63,     -63,     -63,     -63,     291,     281,
     287,     285,     291,     -11,     281,     291,     288,     288,
     280,     -64,     288,     -11,     -68,     -11,     -11,     -11,
     -11,     -11,     -75,     288,     288,     -75,     300,     288,
     -90,     -37,     -79,     293,     -65,     -11,     285,     -71,
     291,     -11,     -68,     -59,     297,     -87,     -11,     288,
     -71,     281,     -75,     288,     288,     -11,     281,     -64,
     -79,     288,     281,     -79,     -75,     281,     -75,
};

int yydef[] = {
       1,      -2,       2,       3,       9,       5,      10,       7,
       8,       0,       0,      34,      53,       0,      32,      51,
      24,       0,       0,      22,      23,      29,      45,       0,
      16,      17,      18,      19,      20,      21,       0,       4,
       6,      11,       0,      26,      55,       0,      58,      57,
      35,      43,      41,      36,      25,      33,      54,       0,
      48,      31,      30,      50,      47,      46,       0,      12,
      28,      60,      59,      79,      69,       0,       0,      77,
      78,      61,       0,      63,      65,      67,     102,      -2,
       0,       0,      98,       0,       0,      81,       0,      96,
      97,      44,      42,      38,      37,      52,      49,      13,
     242,       0,     206,     207,     209,     211,     213,     216,
     221,     224,     228,       0,       0,       0,     232,     243,
     245,     251,     252,       0,     239,      94,     240,     241,
     244,     246,     236,       0,      27,      80,      70,       0,
       0,      76,       0,      71,       0,       0,       0,       0,
      62,      64,      66,      68,       0,      95,       0,       0,
     249,       0,     125,     126,       0,     146,      82,       0,
      84,      83,      99,     100,       0,     108,       0,       0,
     130,       0,      94,      40,      14,      15,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,     229,     242,     230,     231,
     256,     259,     233,     243,     245,     234,       0,     136,
     255,       0,     260,       0,     261,     235,      72,      73,
      74,      75,     100,     103,     133,     134,     135,       0,
     250,     105,     106,     247,       0,     147,       0,     149,
     152,     153,       0,       0,     158,     159,     160,     161,
     179,       0,       0,     198,     232,     151,       0,     190,
     191,     192,       0,       0,       0,       0,       0,       0,
       0,      -2,      -2,      86,      85,      92,       0,      89,
       0,       0,      87,     107,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,     109,     131,       0,     128,     127,
     101,      39,       0,     210,     212,     214,     215,     217,
     218,     219,     220,     222,     223,     225,     226,     227,
     257,     258,     137,       0,     140,     142,     143,     144,
       0,     237,     253,     254,     238,     262,       0,     264,
     265,      56,     248,     148,     150,     154,     162,     163,
     164,     165,     166,     155,       0,       0,     167,     168,
     169,     170,     171,     172,     178,     181,       0,       0,
     177,     180,       0,       0,       0,       0,       0,       0,
       0,       0,     193,       0,       0,     196,     197,     174,
       0,       0,       0,      93,      91,      90,       0,     110,
     111,     112,     113,     114,     115,     116,     117,     118,
     119,     120,     121,     122,     123,     124,       0,     132,
       0,     138,       0,       0,     263,       0,     156,     157,
       0,     176,     173,     199,     200,     201,     202,     203,
     204,     205,     182,     194,     195,     184,       0,       0,
       0,     186,     187,      88,     129,     208,     139,     141,
       0,     266,     267,       0,       0,       0,       0,       0,
       0,       0,     183,     185,       0,       0,     145,     175,
       0,       0,       0,       0,     188,       0,     189,
};

	int yyxi;

	final int endStack = 100;
	final int newState = 101;

	final int YYMAXDEPTH = 200;
	final int YYREDMAX  = 1000;
	final int PCYYFLAG = -1000;
	final int WAS0ERR = 0;
	final int WAS1ERR = 1;
	final int WAS2ERR = 2;
	final int WAS3ERR = 3;

	Token yylval = new Token();
	Token yyval = new Token();

	Token[] s = new Token[YYMAXDEPTH];     /* value stack */
	int pcyyerrfl = 0;           /* error flag */
	int pcyyerrct = 0;           /* error count */
	
	int[] redseq = new int[YYREDMAX];
	int redcnt = 0;
	int pcyytoken = -1;          /* input token */

	int[] statestack = new int[YYMAXDEPTH]; /* state stack */
	int j, m;              /* working index */
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
		if(token != null && token.getPosition() != null) { // empty or fully commented file
			message = message != null ? message : "delete this token";
			parser.getCompilationUnit().error(token.getPosition(), "Syntax error on token '" + getLexer().getString(token.getPosition()) + "', " + message);
		} else if(token != null && token.getId() == 0) {
			token = getLexer().previousToken();
			if(token.getPosition() != null)
				parser.getCompilationUnit().error(token.getPosition(), "Syntax error on token '" + getLexer().getString(token.getPosition()) + "', unexpected end of file");
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
					error(); //"pcyacc internal stack overflow"
					return(1);
				}
				statestack[ssPos] = tmpstate;
				++sp;
				s[sp] = yyval;
			}

			// newState:
			n = yypact[tmpstate];
			if (n <= PCYYFLAG) {
				nResult = doDefault();
				if(nResult < endStack)
					return nResult;
				continue; // defaultact; /*  a simple state */
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

			if(yychk[n=yyact[n]] == pcyytoken) { 
			/* a shift */
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
		if ((n=yydef[tmpstate]) == -2) {
			if (pcyytoken < 0) {
				if((pcyytoken = lex()) < 0)
					pcyytoken = 0;
			}

			for(yyxi = 0; (yyexca[yyxi] != -1) || (yyexca[yyxi + 1] != tmpstate); yyxi += 2) {
			}

			while(yyexca[yyxi += 2] >= 0) {
				if(yyexca[yyxi] == pcyytoken)
					break;
			}

			if((n = yyexca[yyxi + 1]) < 0) /* an accept action */
				return (0);
		}

		if(n == 0) {
			/* error situation */
			switch (pcyyerrfl) {
			case WAS0ERR:          /* an error just occurred */
				error();
				++pcyyerrct;
			case WAS1ERR:
			case WAS2ERR:           /* try again */
				pcyyerrfl = WAS3ERR;
				/* find a state for a legal shift action */
				while (ssPos >= 0) {
					n = yypact[statestack[ssPos]] + YYERRCODE;
					if(n >= 0 && n < YYLAST && yychk[yyact[n]] == YYERRCODE) {
						tmpstate = yyact[n];  /* simulate a shift of "error" */
						return endStack;//break enstack;
					}
					n = yypact[statestack[ssPos]];

					/* the current yyps has no shift on "error", pop stack */
					--ssPos;
					--sp;
				}
				return(1);
			case WAS3ERR:  /* clobber input char */
				if (pcyytoken == 0)
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
		if(j >= YYLAST || yychk[ tmpstate = yyact[j] ] != -n) 
			tmpstate = yyact[yypgo[n]];

		switch (m) {
			
case 9:
{ parser.onImport(); } break;
case 10:
{ parser.onImportList(true); } break;
case 11:
{ parser.onImportList(false); } break;
case 12:
{ parser.onImportElement(s[s1 - 2], s[s1 - 0]); } break;
case 13:
{ parser.onAttribute(s[s1 - 2], s[s1 - 1], null, s[s1 - 0]); } break;
case 14:
{ parser.onAttribute(s[s1 - 3], s[s1 - 2], s[s1 - 1], s[s1 - 0]); } break;
case 15:
{ parser.onAttribute(s[s1 - 3], s[s1 - 2], s[s1 - 0]); } break;
case 16:
{ parser.onToken(s[s1 - 0]); } break;
case 17:
{ parser.onToken(s[s1 - 0]); } break;
case 18:
{ parser.onToken(s[s1 - 0]); } break;
case 19:
{ parser.onToken(s[s1 - 0]); } break;
case 20:
{ parser.onToken(s[s1 - 0]); } break;
case 21:
{ parser.onToken(s[s1 - 0]); } break;
case 24:
{ parser.onModifiers(true); } break;
case 25:
{ parser.onModifiers(false); } break;
case 26:
{ parser.onQualifiedName(true, s[s1 - 0]); } break;
case 27:
{ parser.onQualifiedName(false, s[s1 - 0]); } break;
case 28:
{ error(s[s1 - 0]); } break;
case 29:
{ parser.startEnum(s[s1 - 0]); } break;
case 30:
{ parser.setEnumName(s[s1 - 0]); } break;
case 33:
{ parser.applyModifiers(); } break;
case 34:
{ error("EnumBody is missing"); } break;
case 36:
{ parser.startEnumBody(s[s1 - 0]); } break;
case 37:
{ parser.addEnumMember(s[s1 - 0]); } break;
case 43:
{ error("'}' expected after this token"); } break;
case 44:
{ parser.finishEnumBody(s[s1 - 0]); } break;
case 45:
{ parser.startClass(null, s[s1 - 0]); } break;
case 46:
{ parser.startClass(s[s1 - 1], s[s1 - 0]); } break;
case 47:
{ parser.setClassName(s[s1 - 0]); } break;
case 49:
{ parser.setClassBase(); } break;
case 54:
{ parser.applyModifiers(); } break;
case 55:
{ parser.finishClass(); } break;
case 56:
{ parser.finishClass(); } break;
case 57:
{ parser.startClassBody(s[s1 - 0]); } break;
case 60:
{ parser.finishClassBody(s[s1 - 0]); } break;
case 62:
{ parser.applyModifiers(); } break;
case 64:
{ parser.applyModifiers(); } break;
case 66:
{ parser.applyModifiers(); } break;
case 68:
{ parser.applyModifiers(); } break;
case 69:
{ parser.addClassMember(); } break;
case 72:
{ parser.onMissingSemicolon();} break;
case 73:
{ parser.onMissingSemicolon();} break;
case 74:
{ parser.onMissingSemicolon();} break;
case 75:
{ parser.onMissingSemicolon();} break;
case 76:
{ parser.onMissingSemicolon();} break;
case 81:
{ parser.startRecords(s[s1 - 0]); } break;
case 82:
{ parser.finishRecords(); } break;
case 83:
{ parser.startRecordsBody(s[s1 - 0]); } break;
case 86:
{ parser.finishRecordsBody(s[s1 - 0]); } break;
case 87:
{ parser.startRecord(s[s1 - 0]); } break;
case 88:
{ parser.setRecordValue(s[s1 - 0]); } break;
case 90:
{ parser.applyModifiers(); } break;
case 91:
{ parser.addRecord(); } break;
case 94:
{ parser.onVariableType(false); } break;
case 95:
{ parser.onVariableType(true); } break;
case 99:
{ error(s[s1 - 0]); } break;
case 100:
{ parser.onVariableDeclarator(s[s1 - 0]); } break;
case 101:
{ parser.onVariableDeclarator(s[s1 - 2], s[s1 - 0]); } break;
case 103:
{ parser.onVariableDeclaratorInit(s[s1 - 1]); } break;
case 104:
{ parser.onVariableInit(null); } break;
case 105:
{ parser.onVariableInit(s[s1 - 1]); } break;
case 106:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onVariableInit(s[s1 - 1]); } break;
case 107:
{ parser.createMethod(s[s1 - 1]); } break;
case 108:
{ parser.createPriorityOperator(s[s1 - 1]); } break;
case 109:
{ parser.createCastOperator(s[s1 - 2], s[s1 - 1]); } break;
case 110:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 111:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 112:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 113:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 114:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 115:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 116:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 117:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 118:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 119:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 120:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 121:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 122:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 123:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 124:
{ parser.createOperator(s[s1 - 2], s[s1 - 1]); } break;
case 125:
{ parser.setMethodBody(); } break;
case 127:
{ parser.createParameter(); } break;
case 130:
{ parser.startParameters(s[s1 - 0]); } break;
case 131:
{ parser.finishParameters(s[s1 - 0]); } break;
case 132:
{ parser.finishParameters(s[s1 - 0]); } break;
case 136:
{ parser.startArrayInitializer(s[s1 - 0]); } break;
case 138:
{ parser.finishArrayInitializer(s[s1 - 0]); } break;
case 139:
{ parser.finishArrayInitializer(s[s1 - 0]); } break;
case 142:
{ parser.addArrayInitializer(); } break;
case 143:
{ parser.addArrayInitializer(); } break;
case 144:
{ parser.addArrayInitializer(); } break;
case 145:
{ parser.onMapElement(s[s1 - 4], s[s1 - 0]); } break;
case 146:
{ parser.startCompound(s[s1 - 0]); } break;
case 147:
{ parser.finishCompound(s[s1 - 0]); } break;
case 148:
{ parser.finishCompound(s[s1 - 0]); } break;
case 149:
{ parser.addStatement(); } break;
case 150:
{ parser.addStatement(); } break;
case 151:
{ parser.onDeclarator(); } break;
case 154:
{ parser.onStatement(); } break;
case 155:
{ parser.onStatement(); } break;
case 156:
{ parser.onStatement2(); } break;
case 157:
{ parser.onStatement2(); } break;
case 162:
{ parser.onStatement1(); } break;
case 163:
{ parser.onStatement1(); } break;
case 164:
{ parser.onStatement1(); } break;
case 165:
{ parser.onStatement1(); } break;
case 166:
{ parser.onStatement1(); } break;
case 167:
{ parser.onStatement1(); } break;
case 168:
{ parser.onStatement1(); } break;
case 169:
{ parser.onStatement1(); } break;
case 170:
{ parser.onStatement1(); } break;
case 171:
{ parser.onStatement1(); } break;
case 172:
{ parser.onStatement1(); } break;
case 173:
{ parser.onThrowStatement(s[s1 - 2]); } break;
case 174:
{ parser.onTryStatement(s[s1 - 1]); } break;
case 175:
{ parser.onCatchClause(s[s1 - 4]); } break;
case 176:
{ parser.onFinallyStatement(s[s1 - 1]); } break;
case 177:
{ parser.addCatchStatement(); } break;
case 178:
{ parser.addCatchStatement(); } break;
case 182:
{ parser.onIfStatement(s[s1 - 2], null); } break;
case 183:
{ parser.onIfStatement(s[s1 - 4], s[s1 - 1]); } break;
case 184:
{ parser.onWhileStatement(s[s1 - 2]); } break;
case 185:
{ parser.onDoWhileStatement(s[s1 - 4], s[s1 - 2]); } break;
case 186:
{ parser.onDeclarator(); } break;
case 188:
{ parser.onForStatement(s[s1 - 7], false); } break;
case 189:
{ parser.onForStatement(s[s1 - 8], true); } break;
case 193:
{ parser.onJumpStatement(s[s1 - 1], false); } break;
case 194:
{ parser.onJumpStatement(s[s1 - 2], true); } break;
case 195:
{ parser.onJumpStatement(s[s1 - 2], true); } break;
case 196:
{ parser.onJumpStatement(s[s1 - 1], false); } break;
case 197:
{ parser.onJumpStatement(s[s1 - 1], false); } break;
case 199:
{ parser.onAssignment(s[s1 - 1]); } break;
case 200:
{ parser.onAssignment(s[s1 - 1]); } break;
case 201:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 202:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 203:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 204:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 205:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 208:
{ parser.onCondition(); } break;
case 210:
{ parser.onOperator(s[s1 - 1]); } break;
case 212:
{ parser.onOperator(s[s1 - 1]); } break;
case 214:
{ parser.onOperator(s[s1 - 1]); } break;
case 215:
{ parser.onOperator(s[s1 - 1]); } break;
case 217:
{ parser.onOperator(s[s1 - 1]); } break;
case 218:
{ parser.onOperator(s[s1 - 1]); } break;
case 219:
{ parser.onOperator(s[s1 - 1]); } break;
case 220:
{ parser.onOperator(s[s1 - 1]); } break;
case 222:
{ parser.onOperator(s[s1 - 1]); } break;
case 223:
{ parser.onOperator(s[s1 - 1]); } break;
case 225:
{ parser.onOperator(s[s1 - 1]); } break;
case 226:
{ parser.onOperator(s[s1 - 1]); } break;
case 227:
{ parser.onOperator(s[s1 - 1]); } break;
case 230:
{ parser.onUnary(s[s1 - 1]); } break;
case 231:
{ parser.onUnary(s[s1 - 1]); } break;
case 233:
{ parser.onTypeCast(); } break;
case 234:
{ parser.onArrayTypeCast(); } break;
case 235:
{ parser.onNewExpression(s[s1 - 1], s[s1 - 0]); } break;
case 236:
{ parser.onContainer(s[s1 - 0]); } break;
case 237:
{ parser.addContainer(s[s1 - 0]); } break;
case 238:
{ parser.onBracedExpression(s[s1 - 2], s[s1 - 0]); } break;
case 240:
{ parser.onThis(s[s1 - 0]); } break;
case 241:
{ parser.onSuper(s[s1 - 0]); } break;
case 242:
{ parser.onConstant(s[s1 - 0]); } break;
case 244:
{ parser.onTypeToPostfix(); } break;
case 245:
{ parser.onTypeToPostfix(); } break;
case 246:
{ parser.onMethodCall(false); } break;
case 247:
{ parser.onIndex(s[s1 - 1], s[s1 - 0], false); } break;
case 248:
{ parser.onIndex(s[s1 - 2], s[s1 - 0], true); } break;
case 249:
{ parser.onIndices(true); } break;
case 250:
{ parser.onIndices(false); } break;
case 253:
{ parser.onPostfix(); } break;
case 254:
{ parser.onMethodCall(true); } break;
case 255:
{ error(s[s1 - 0]); } break;
case 256:
{ parser.onArrayAccess(); } break;
case 257:
{ parser.onPostfix(); } break;
case 258:
{ parser.onMethodCall(true); } break;
case 259:
{ error(s[s1 - 0]); } break;
case 261:
{ parser.startExpressions(s[s1 - 0]); } break;
case 262:
{ parser.finishExpressions(s[s1 - 0]); } break;
case 263:
{ parser.finishExpressions(s[s1 - 0]); } break;
case 264:
{ parser.addExpression(); } break;
case 265:
{ parser.addExpression(); } break;
case 266:
{ parser.addExpression(); } break;
case 267:
{ parser.addExpression(); } break;
		}
		return endStack;
	}
}