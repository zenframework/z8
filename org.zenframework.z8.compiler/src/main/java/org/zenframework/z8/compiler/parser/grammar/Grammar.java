
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
  -2, 243,
  -1, 234,
  294, 97,
  -2, 244,
  0,
};

int YYNPROD = 267;
int YYLAST = 1180;
int yyact[] = {
     104,     262,      35,      70,     212,     260,     112,     101,
      30,      48,     284,      99,     100,     317,     318,      53,
     102,     107,      35,       9,     242,      35,     381,     147,
     148,     395,      35,     107,      84,     152,     264,     210,
      78,     288,     194,     382,      24,      26,      25,      80,
     173,      35,      28,     150,      27,      79,      30,     189,
      78,      29,     103,     110,     111,     114,     115,     116,
      54,      52,      35,      80,      50,     386,      80,      80,
     115,      79,     188,      80,      79,      79,     207,      78,
     184,      79,      24,      26,      25,     138,      56,      80,
      28,     395,      27,      30,     137,      79,     395,      29,
      78,     137,     188,      56,     137,     141,     177,      35,
      76,     300,     146,      56,     341,     159,      56,     155,
     180,     222,      55,     231,     232,     230,      56,      24,
      26,      25,     132,     360,      77,      28,     158,      27,
     178,     361,     198,      80,      29,      78,     408,      80,
     229,      79,     291,     218,     363,      79,     141,     358,
     219,     107,     139,     135,     391,     234,     364,     210,
     101,     357,     137,     233,      99,     100,     173,      35,
      80,     134,     179,     263,      56,     220,      79,     403,
     402,     110,     111,     114,     107,     398,     379,     378,
     141,     369,     366,     365,     306,     339,     115,     333,
     332,     173,      35,     193,     222,     122,     231,     232,
     230,     227,     226,     228,     110,     111,     114,      89,
     192,     191,     190,     140,     359,     235,      81,      57,
     141,     115,     183,     229,      39,     145,     218,     234,
      78,      43,     297,     157,     234,     233,     156,     303,
     311,     137,     233,     412,     409,     367,      30,     220,
     405,     400,     107,     181,     220,     291,     222,     152,
     234,     337,     334,     104,     160,     102,     233,     316,
     112,     237,     101,     308,     185,      63,      99,     100,
     220,      22,      24,      26,      25,     315,      21,      92,
      28,     293,      27,     161,     162,      69,     107,      29,
      23,     239,     183,     367,     187,     197,      61,     197,
     197,     328,     204,     173,      35,     299,     167,     168,
     215,     335,      93,     186,      30,     211,     110,     111,
     114,     105,     118,      22,     169,     170,     171,     266,
      35,      76,       3,     115,     106,      31,     336,     208,
     322,     108,      23,     121,     211,      83,     295,     133,
      24,      26,      25,     213,     229,      18,      28,     286,
      27,      36,     132,     368,     294,      29,      78,      98,
     131,     234,       3,      59,     120,     124,     137,     233,
     234,      91,     234,     318,     337,     158,     233,       5,
     233,     220,      14,      32,      90,     331,     225,       6,
     220,     383,     220,     321,      33,     181,      80,     387,
      45,     263,      96,     330,      79,     214,     224,      63,
      80,     385,     231,     394,     390,     400,      79,      32,
       5,     223,     216,     230,     323,     324,     325,     326,
     327,       6,     381,     137,     217,     262,     117,      62,
      14,      69,     322,      61,     234,     177,      70,     377,
     206,     284,     233,     182,     140,     406,     380,     328,
      12,     285,     234,     212,     220,     234,     176,     267,
     233,     403,     226,     233,     220,     136,      46,     364,
     362,     220,     220,     283,     182,     220,     151,     218,
     396,     261,      72,     407,      74,      94,     238,     408,
     410,     241,     210,     268,     236,     371,     113,     103,
     335,      59,     199,     395,     199,     199,     234,      12,
     143,     320,     226,     370,     372,     373,     374,     375,
     376,     265,     401,     287,     144,     167,     168,     319,
     409,     163,     164,     165,     166,     142,     338,      73,
     411,     195,     412,     413,     207,     109,      60,     393,
     133,      64,     176,      58,     314,      37,     364,      34,
     388,     307,     286,     322,     221,     392,      38,     196,
      10,      15,     340,     323,      85,     324,     325,     326,
     327,     359,     301,     309,     198,      18,      47,     397,
      38,     296,      51,      92,      71,     361,     304,     312,
     399,     382,     275,     276,     176,      86,     391,     404,
     398,      41,      82,      65,      42,     219,      40,      11,
      71,     101,      49,      71,      71,      99,     100,      93,
      71,      17,      19,     200,      20,     151,     209,      65,
     316,       4,      65,      65,     154,     107,      66,     128,
       8,     141,     298,       7,      58,     315,       2,      83,
     305,     313,     173,      35,     237,     222,       1,     231,
     232,     230,     227,     226,     228,     110,     111,     114,
       0,     211,       0,     215,       0,      67,     200,     269,
     270,       0,     115,       0,     229,     211,     239,     218,
      71,      78,     101,       0,      71,     208,      99,     100,
     201,     202,      36,       0,     211,       0,     136,     128,
     281,       0,       0,     128,     213,     337,     107,     290,
       0,       0,     141,     205,       0,     154,     168,      96,
     109,       0,       0,     173,      35,       0,     222,       0,
     231,     232,     230,     227,     226,     228,     110,     111,
     114,     101,       0,       0,       0,      99,     100,     280,
     129,       0,       0,     115,       0,     229,     289,     211,
     218,       0,      78,       0,       0,     107,       0,     214,
       0,     141,       0,     135,     195,       0,       0,       0,
       0,       0,     173,      35,       0,     222,       0,     231,
     232,     230,     227,     226,     228,     110,     111,     114,
      30,       0,       0,     162,      94,       0,       0,       0,
       0,      30,     115,       0,     229,       0,       0,     218,
     129,      78,       0,      30,     129,      35,      76,      67,
     184,     123,       9,      22,      24,      26,      25,      35,
      21,     113,      28,       0,      27,      24,      26,      25,
     149,      29,      23,      28,      95,      27,       0,      24,
      26,      25,      29,      78,     101,      28,      62,      27,
      99,     100,       0,       0,      29,      78,     184,       0,
       0,     302,     310,       0,       0,     101,       0,     357,
     107,      99,     100,       0,     183,       0,       0,       0,
     329,       0,       0,       0,       0,     173,      35,     101,
       0,     107,     292,      99,     100,     183,       0,       0,
     110,     111,     114,       0,      68,       0,     173,      35,
       0,       0,       0,     107,       0,     115,       0,     183,
       0,     110,     111,     114,     119,     125,     101,       0,
     173,      35,      99,     100,       0,     101,     115,       0,
       0,      99,     100,     110,     111,     114,     384,       0,
       0,       0,     287,     154,       0,       0,       0,     389,
     115,     287,       0,       0,       0,     154,     282,     173,
      35,     127,       0,       0,     101,     211,     173,      35,
      99,     100,     110,     111,     114,     209,      97,     130,
       0,     110,     111,     114,       0,     101,      62,     115,
     107,      99,     100,     203,      62,     221,     115,      13,
       0,       0,       0,       0,     243,     173,      35,     259,
      75,     107,     101,       0,      87,       0,      99,     100,
     110,     111,     114,       0,       0,       0,      88,      35,
     271,     272,     273,     274,      75,     115,     107,      75,
       0,     110,     111,     114,      75,       0,      66,     130,
       0,       0,       0,     173,      35,     127,     115,      68,
     153,       0,      16,       0,       0,       0,     110,     111,
     114,       0,      66,       0,       0,     122,     126,       0,
      44,       0,      61,     115,     244,     245,     246,     247,
     248,     249,     250,     251,     252,     253,     254,     255,
     256,     257,     172,     174,     175,       0,       0,       0,
       0,       0,       0,       0,      75,       0,       0,      16,
       0,     258,       0,       0,     150,     342,     343,     344,
     345,     346,     347,     348,     349,     350,     351,     352,
     353,     354,     355,     356,     244,      44,       0,       0,
       0,       0,      62,     122,       0,       0,     144,       0,
       0,       0,       0,       0,     164,     165,     166,      95,
       0,       0,       0,       0,       0,       0,       0,       0,
     240,       0,       0,       0,       0,       0,       0,       0,
     277,     278,     279,     100,       0,     101,     169,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,      44,       0,       0,
       0,      44,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,     245,
     246,     247,     248,     249,     250,     251,     252,     253,
     254,     255,     256,     257,     258,     149,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,     240,       0,       0,       0,
      13,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,     170,     171,      97,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,      44,
};

int yypact[] = {
     422,   -1000,     -60,     -60,    -289,   -1000,   -1000,   -1000,
   -1000,    -268,     -80,     -75,   -1000,     -60,   -1000,    -304,
   -1000,    -234,    -237,   -1000,   -1000,   -1000,   -1000,    -294,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,    -238,     -60,
   -1000,   -1000,    -182,   -1000,   -1000,     -86,    -199,   -1000,
   -1000,     -87,    -266,   -1000,   -1000,   -1000,   -1000,    -268,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,     617,   -1000,
    -239,   -1000,    -199,   -1000,   -1000,       2,     441,   -1000,
   -1000,   -1000,    -199,   -1000,   -1000,   -1000,    -175,    -136,
    -150,     -79,    -194,    -271,    -251,   -1000,    -268,   -1000,
   -1000,   -1000,    -188,   -1000,   -1000,   -1000,    -201,   -1000,
     -69,     -72,   -1000,    -172,     -36,   -1000,      -7,     205,
      31,   -1000,   -1000,     638,     638,     638,    -198,    -143,
     -82,   -1000,    -220,     638,   -1000,    -190,   -1000,   -1000,
   -1000,   -1000,   -1000,    -247,   -1000,   -1000,   -1000,     -94,
     -95,   -1000,     431,   -1000,     -96,    -109,    -236,    -260,
   -1000,   -1000,   -1000,   -1000,     -18,     -65,     -18,     -18,
   -1000,     600,   -1000,   -1000,     350,   -1000,   -1000,     -88,
    -274,   -1000,   -1000,     -49,     696,   -1000,     -49,    -276,
   -1000,    -264,    -193,    -266,   -1000,   -1000,     638,     638,
     638,     638,     638,     638,     638,     638,     638,     638,
     638,     638,     638,     638,   -1000,   -1000,   -1000,   -1000,
     -65,    -268,    -198,   -1000,   -1000,   -1000,     569,   -1000,
    -273,     -52,   -1000,     513,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,     -80,
   -1000,   -1000,   -1000,   -1000,     -73,   -1000,     285,   -1000,
   -1000,   -1000,    -191,    -116,   -1000,   -1000,   -1000,   -1000,
    -307,    -307,     638,   -1000,     105,   -1000,    -263,   -1000,
   -1000,   -1000,     496,    -112,    -113,     -84,    -263,     397,
     -47,   -1000,     -82,   -1000,    -274,   -1000,    -115,   -1000,
    -274,    -189,   -1000,   -1000,     -49,     -49,     -49,     -49,
     -49,     -49,     -49,     -49,     -49,     -49,     -49,     -49,
     -49,     -49,     -49,   -1000,   -1000,    -146,   -1000,   -1000,
   -1000,   -1000,     -91,     -36,   -1000,   -1000,   -1000,      14,
      14,      14,      14,      31,      31,   -1000,   -1000,   -1000,
    -214,   -1000,   -1000,    -170,   -1000,   -1000,   -1000,     638,
   -1000,    -214,   -1000,   -1000,   -1000,    -149,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,    -117,    -118,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,     -59,     -84,   -1000,
   -1000,    -119,     531,     638,     638,     638,     638,     638,
     397,   -1000,    -121,    -122,   -1000,   -1000,   -1000,     397,
    -278,    -253,   -1000,   -1000,   -1000,    -232,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,    -292,   -1000,     638,
   -1000,     562,    -151,   -1000,     531,   -1000,   -1000,    -292,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,    -272,   -1000,   -1000,   -1000,    -263,     638,    -123,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,     638,
   -1000,   -1000,     -56,     397,    -128,    -129,     638,     -57,
     -84,   -1000,   -1000,     638,    -162,   -1000,   -1000,     -61,
     638,     397,     -62,   -1000,     397,   -1000,
};

int yypgo[] = {
       0,     582,     574,     298,     343,     571,     568,     561,
     351,     485,     556,     136,     554,     938,     887,     553,
     546,     346,     543,     542,     540,     309,     538,     537,
     317,     522,     518,     508,     505,     408,     504,     321,
     503,     502,     493,     491,     270,     500,     758,     597,
     245,     804,     489,     261,     331,     486,     479,     477,
     468,     456,     444,     441,     265,     438,     241,       6,
       0,     311,     857,       3,     436,     481,     434,     744,
       4,       1,     433,     430,     122,     428,     427,      10,
     409,     404,     400,      70,     285,     303,     558,      31,
     315,     365,     280,     388,     253,     239,     378,      50,
     377,     366,     353,     350,      16,     348,     337,     255,
     282,     437,     748,     362,     870,     327,     305,     300,
     289,     446,     421,     283,     268,     257,
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
      72,      73,      64,      64,      74,      74,      76,      75,
      75,      75,      75,      75,      75,      75,      75,      75,
      75,      75,      75,      75,      75,      75,      75,      75,
      75,      75,      75,      75,      78,      83,      84,      85,
      86,      86,      77,      77,      77,      80,      80,      88,
      89,      90,      90,      91,      91,      81,      81,      81,
      82,      82,      82,      82,      82,      79,      79,      79,
      79,      79,      79,      79,      79,      11,      93,      93,
      94,      94,      95,      95,      96,      96,      96,      97,
      97,      97,      97,      97,      98,      98,      98,      99,
      99,      99,      99,     100,     100,     100,     100,     101,
     101,     101,     102,     103,     103,      87,      87,     104,
     104,     104,     104,     104,     104,     104,     106,     106,
      57,      57,      92,      92,      92,      92,      92,      92,
      92,      92,      92,     105,     108,     107,     107,     109,
     109,     109,     109,
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
       5,       1,       2,       3,       1,       2,       1,       1,
       1,       2,       2,       3,       3,       1,       1,       1,
       1,       2,       2,       2,       2,       2,       2,       2,
       2,       2,       2,       2,       3,       2,       5,       2,
       2,       2,       1,       2,       2,       3,       5,       3,
       5,       1,       1,       8,       9,       1,       1,       1,
       2,       3,       3,       2,       2,       1,       3,       3,
       3,       3,       3,       3,       3,       1,       1,       5,
       1,       3,       1,       3,       1,       3,       3,       1,
       3,       3,       3,       3,       1,       3,       3,       1,
       3,       3,       3,       1,       2,       2,       2,       1,
       2,       2,       2,       1,       3,       3,       1,       1,
       1,       1,       1,       1,       1,       1,       2,       3,
       1,       2,       1,       1,       3,       3,       2,       2,
       3,       3,       2,       2,       1,       2,       3,       1,
       1,       3,       3,
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
      -9,    -105,     285,     -70,     -71,     -72,     -11,     280,
     306,      -9,    -105,     281,     281,    -109,     -11,     -68,
     -31,     283,     285,     -75,     288,     -77,     -78,     -64,
     -80,     -81,     288,     -76,     -79,     -77,     -78,     -64,
     -80,     -81,     -82,     -84,     -85,     320,     321,     -84,
     -85,     -11,     289,     275,     276,     277,     278,     279,
     -87,     288,     -11,     -68,     288,     288,     -64,     -87,
     -75,     280,     -54,     288,     -52,     289,     -63,     -63,
     -63,     -63,     -63,     -63,     -63,     -63,     -63,     -63,
     -63,     -63,     -63,     -63,     -63,     291,     281,     287,
     285,     291,     -11,     281,     291,     288,     288,     280,
     -64,     288,     -11,     -68,     -11,     -11,     -11,     -11,
     -11,     -75,     288,     288,     -75,     300,     288,     -90,
     -37,     -79,     293,     -65,     -11,     285,     -71,     291,
     -11,     -68,     -59,     297,     -87,     -11,     288,     -11,
     281,     -75,     288,     288,     -11,     281,     -64,     -79,
     288,     281,     -79,     -75,     281,     -75,
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
     241,       0,     205,     206,     208,     210,     212,     215,
     220,     223,     227,       0,       0,       0,     231,     242,
     244,     250,     251,       0,     238,      94,     239,     240,
     243,     245,     235,       0,      27,      80,      70,       0,
       0,      76,       0,      71,       0,       0,       0,       0,
      62,      64,      66,      68,       0,      95,       0,       0,
     248,       0,     125,     126,       0,     145,      82,       0,
      84,      83,      99,     100,       0,     108,       0,       0,
     130,       0,      94,      40,      14,      15,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,     228,     241,     229,     230,
     255,     258,     232,     242,     244,     233,       0,     136,
     254,       0,     259,       0,     260,     234,      72,      73,
      74,      75,     100,     103,     133,     134,     135,       0,
     249,     105,     106,     246,       0,     146,       0,     148,
     151,     152,       0,       0,     157,     158,     159,     160,
     178,       0,       0,     197,     231,     150,       0,     189,
     190,     191,       0,       0,       0,       0,       0,       0,
       0,      -2,      -2,      86,      85,      92,       0,      89,
       0,       0,      87,     107,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,     109,     131,       0,     128,     127,
     101,      39,       0,     209,     211,     213,     214,     216,
     217,     218,     219,     221,     222,     224,     225,     226,
     256,     257,     137,       0,     140,     142,     143,       0,
     236,     252,     253,     237,     261,       0,     263,     264,
      56,     247,     147,     149,     153,     161,     162,     163,
     164,     165,     154,       0,       0,     166,     167,     168,
     169,     170,     171,     177,     180,       0,       0,     176,
     179,       0,       0,       0,       0,       0,       0,       0,
       0,     192,       0,       0,     195,     196,     173,       0,
       0,       0,      93,      91,      90,       0,     110,     111,
     112,     113,     114,     115,     116,     117,     118,     119,
     120,     121,     122,     123,     124,       0,     132,       0,
     138,       0,       0,     262,       0,     155,     156,       0,
     175,     172,     198,     199,     200,     201,     202,     203,
     204,     181,     193,     194,     183,       0,       0,       0,
     185,     186,      88,     129,     207,     139,     141,       0,
     265,     266,       0,       0,       0,       0,       0,       0,
       0,     182,     184,       0,       0,     144,     174,       0,
       0,       0,       0,     187,       0,     188,
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
{ parser.onMapElement(s[s1 - 4], s[s1 - 0]); } break;
case 145:
{ parser.startCompound(s[s1 - 0]); } break;
case 146:
{ parser.finishCompound(s[s1 - 0]); } break;
case 147:
{ parser.finishCompound(s[s1 - 0]); } break;
case 148:
{ parser.addStatement(); } break;
case 149:
{ parser.addStatement(); } break;
case 150:
{ parser.onDeclarator(); } break;
case 153:
{ parser.onStatement(); } break;
case 154:
{ parser.onStatement(); } break;
case 155:
{ parser.onStatement2(); } break;
case 156:
{ parser.onStatement2(); } break;
case 161:
{ parser.onStatement1(); } break;
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
{ parser.onThrowStatement(s[s1 - 2]); } break;
case 173:
{ parser.onTryStatement(s[s1 - 1]); } break;
case 174:
{ parser.onCatchClause(s[s1 - 4]); } break;
case 175:
{ parser.onFinallyStatement(s[s1 - 1]); } break;
case 176:
{ parser.addCatchStatement(); } break;
case 177:
{ parser.addCatchStatement(); } break;
case 181:
{ parser.onIfStatement(s[s1 - 2], null); } break;
case 182:
{ parser.onIfStatement(s[s1 - 4], s[s1 - 1]); } break;
case 183:
{ parser.onWhileStatement(s[s1 - 2]); } break;
case 184:
{ parser.onDoWhileStatement(s[s1 - 4], s[s1 - 2]); } break;
case 185:
{ parser.onDeclarator(); } break;
case 187:
{ parser.onForStatement(s[s1 - 7], false); } break;
case 188:
{ parser.onForStatement(s[s1 - 8], true); } break;
case 192:
{ parser.onJumpStatement(s[s1 - 1], false); } break;
case 193:
{ parser.onJumpStatement(s[s1 - 2], true); } break;
case 194:
{ parser.onJumpStatement(s[s1 - 2], true); } break;
case 195:
{ parser.onJumpStatement(s[s1 - 1], false); } break;
case 196:
{ parser.onJumpStatement(s[s1 - 1], false); } break;
case 198:
{ parser.onAssignment(s[s1 - 1]); } break;
case 199:
{ parser.onAssignment(s[s1 - 1]); } break;
case 200:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 201:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 202:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 203:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 204:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 207:
{ parser.onCondition(); } break;
case 209:
{ parser.onOperator(s[s1 - 1]); } break;
case 211:
{ parser.onOperator(s[s1 - 1]); } break;
case 213:
{ parser.onOperator(s[s1 - 1]); } break;
case 214:
{ parser.onOperator(s[s1 - 1]); } break;
case 216:
{ parser.onOperator(s[s1 - 1]); } break;
case 217:
{ parser.onOperator(s[s1 - 1]); } break;
case 218:
{ parser.onOperator(s[s1 - 1]); } break;
case 219:
{ parser.onOperator(s[s1 - 1]); } break;
case 221:
{ parser.onOperator(s[s1 - 1]); } break;
case 222:
{ parser.onOperator(s[s1 - 1]); } break;
case 224:
{ parser.onOperator(s[s1 - 1]); } break;
case 225:
{ parser.onOperator(s[s1 - 1]); } break;
case 226:
{ parser.onOperator(s[s1 - 1]); } break;
case 229:
{ parser.onUnary(s[s1 - 1]); } break;
case 230:
{ parser.onUnary(s[s1 - 1]); } break;
case 232:
{ parser.onTypeCast(); } break;
case 233:
{ parser.onArrayTypeCast(); } break;
case 234:
{ parser.onNewExpression(s[s1 - 1], s[s1 - 0]); } break;
case 235:
{ parser.onContainer(s[s1 - 0]); } break;
case 236:
{ parser.addContainer(s[s1 - 0]); } break;
case 237:
{ parser.onBracedExpression(s[s1 - 2], s[s1 - 0]); } break;
case 239:
{ parser.onThis(s[s1 - 0]); } break;
case 240:
{ parser.onSuper(s[s1 - 0]); } break;
case 241:
{ parser.onConstant(s[s1 - 0]); } break;
case 243:
{ parser.onTypeToPostfix(); } break;
case 244:
{ parser.onTypeToPostfix(); } break;
case 245:
{ parser.onMethodCall(false); } break;
case 246:
{ parser.onIndex(s[s1 - 1], s[s1 - 0], false); } break;
case 247:
{ parser.onIndex(s[s1 - 2], s[s1 - 0], true); } break;
case 248:
{ parser.onIndices(true); } break;
case 249:
{ parser.onIndices(false); } break;
case 252:
{ parser.onPostfix(); } break;
case 253:
{ parser.onMethodCall(true); } break;
case 254:
{ error(s[s1 - 0]); } break;
case 255:
{ parser.onArrayAccess(); } break;
case 256:
{ parser.onPostfix(); } break;
case 257:
{ parser.onMethodCall(true); } break;
case 258:
{ error(s[s1 - 0]); } break;
case 260:
{ parser.startExpressions(s[s1 - 0]); } break;
case 261:
{ parser.finishExpressions(s[s1 - 0]); } break;
case 262:
{ parser.finishExpressions(s[s1 - 0]); } break;
case 263:
{ parser.addExpression(); } break;
case 264:
{ parser.addExpression(); } break;
case 265:
{ parser.addExpression(); } break;
case 266:
{ parser.addExpression(); } break;
		}
		return endStack;
	}
}