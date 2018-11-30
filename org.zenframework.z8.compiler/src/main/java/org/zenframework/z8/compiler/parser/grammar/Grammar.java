
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
  -2, 245,
  -1, 234,
  294, 97,
  -2, 246,
  0,
};

int YYNPROD = 269;
int YYLAST = 1154;
int yyact[] = {
     104,      30,      35,     107,     212,      48,     112,     284,
      70,     210,      53,     141,     102,      35,      76,     301,
     382,     262,     318,     319,       9,     396,      35,     222,
      35,     231,     232,     230,     260,      24,      26,      25,
      78,     152,      77,      28,     289,      27,      84,      80,
     115,      35,      29,      78,     264,      79,     229,     150,
     221,     218,     147,     148,     194,     189,     116,      54,
     101,      52,      50,      80,      99,     100,      80,      80,
     387,      79,     219,      80,      79,      79,     188,      78,
     137,      79,     137,     184,     107,     138,      56,      80,
     183,     396,      56,     188,     177,      79,     396,      65,
     137,     173,      35,     146,     155,      55,     360,      56,
      30,      56,      56,     342,     110,     111,     114,     411,
     180,      22,     132,      65,      35,     207,      65,      65,
     292,     115,      66,     128,     178,     405,     404,     235,
      23,      89,     392,      80,      24,      26,      25,      80,
     135,      79,      28,     159,      27,      79,     361,     137,
     364,      29,      78,     399,     362,     234,     134,     210,
     380,      56,     365,     233,     158,     103,     379,     359,
      80,     220,      30,     141,     107,     370,      79,     139,
     263,     358,     367,     366,      30,     340,      35,      76,
     334,     333,     123,     128,     193,     192,     185,     128,
      35,     338,     191,     190,      81,     122,      24,      26,
      25,      57,     141,     183,      28,      39,      27,     140,
      24,      26,      25,      29,      78,     145,      28,     197,
      27,     197,     197,      43,     204,      29,      78,     234,
     298,     157,     156,     137,     234,     233,     415,     304,
     312,     412,     233,     220,     408,     309,     407,     402,
     220,     266,     292,     368,     152,     160,     161,     162,
     234,     102,     335,     104,     167,     168,     233,     215,
     112,     169,     170,     171,     220,      30,      83,      69,
     294,     286,     195,     118,     132,     179,     295,     239,
     101,      35,      76,     208,      99,     100,     316,     317,
     137,     211,     198,     187,      96,     186,     105,     158,
     368,      24,      26,      25,     107,     106,     133,      28,
     141,      27,     237,     108,     307,     322,      29,      78,
      98,     173,      35,     214,     222,     331,     231,     232,
     230,     227,     226,     228,     110,     111,     114,       3,
      63,      91,      31,      90,     225,     211,     384,      36,
     224,     115,     131,     229,     300,     229,     218,     338,
      78,     167,     168,     223,     369,     163,     164,     165,
     166,     222,     234,     216,      14,     182,     217,       3,
     233,     234,     113,     234,     319,     337,     220,     233,
      93,     233,      45,     206,     386,     220,       5,     220,
     140,     365,      32,     363,     285,     213,      92,      80,
     283,     182,     218,     151,      68,      79,     121,     263,
     261,      80,     391,     181,     329,      72,     402,      79,
     388,     395,      14,      69,     336,     176,      32,       5,
      74,     201,     202,     385,     238,     226,     371,     373,
     374,     375,     376,     377,     405,     234,     288,     284,
     241,     236,     265,     233,      70,     143,     262,     409,
     144,     220,     231,     142,      73,     234,     212,     410,
     234,      60,     220,     233,     411,     413,     233,     210,
     220,     220,      63,     389,     220,     286,     323,     130,
     393,     209,     221,      64,     275,     276,     324,     378,
     325,     326,     327,     328,     360,      58,     381,     329,
     323,     287,     398,     315,     135,     195,     296,     133,
     362,     176,      37,     400,     383,      38,     196,      10,
     101,     392,     406,     399,      99,     100,     302,     310,
      15,     219,      85,      47,      51,     308,      41,     230,
     320,     321,      82,      42,     107,      94,     382,     130,
     336,      40,      11,     396,     383,     332,     109,      68,
     341,     173,      35,     176,      49,     181,     306,     314,
      34,     268,     403,      30,     110,     111,     114,     297,
      83,     412,      17,      19,     281,      20,     267,     339,
       4,     115,     414,     291,     415,     416,     207,       8,
      78,      38,       7,     136,     397,      71,      22,      24,
      26,      25,       2,      21,       1,      28,      86,      27,
     168,      96,       0,     103,      29,      23,      30,      59,
       0,      71,     365,     215,      71,      71,       0,      30,
       6,      71,     211,     226,       0,      33,     151,       0,
     305,     313,     208,     242,       0,     154,     211,       0,
       9,      22,      24,      26,      25,      58,      21,     316,
      28,     317,      27,      24,      26,      25,     372,      29,
      23,      28,       6,      27,       0,     239,     211,     234,
      29,     324,     325,     326,     327,     328,     214,     101,
     137,      36,     117,      99,     100,      93,      18,     323,
       0,      71,     177,      12,     184,      71,     237,       0,
       0,       0,      92,     107,       0,     287,     113,     183,
     394,      46,       0,     330,       0,       0,       0,     362,
     173,      35,       0,       0,     303,     311,     154,     269,
     270,     109,     101,     110,     111,     114,      99,     100,
       0,       0,       0,     401,       0,     392,       0,     211,
     115,     200,      12,       0,     198,      59,     107,     213,
     280,       0,     141,     299,       0,     101,       0,     290,
       0,      99,     100,     173,      35,      16,     222,       0,
     231,     232,     230,     227,     226,     228,     110,     111,
     114,     107,       0,      44,       0,     141,     205,       0,
       0,       0,       0,     115,     200,     229,     173,      35,
     218,     222,      78,     231,     232,     230,     227,     226,
     228,     110,     111,     114,     101,       0,       0,       0,
      99,     100,      16,       0,     136,       0,     115,       0,
     229,     211,      62,     218,       0,      78,       0,     149,
     107,     209,       0,     199,     141,     199,     199,       0,
      44,       0,       0,     162,      94,     173,      35,       0,
     222,       0,     231,     232,     230,     227,     226,     228,
     110,     111,     114,       0,     101,       0,       0,     184,
      99,     100,       0,       0,      95,     115,       0,     229,
       0,     358,     218,       0,      78,     101,       0,       0,
     288,      99,     100,       0,     183,     390,       0,       0,
     119,     125,       0,       0,       0,     173,      35,       0,
      44,     107,     293,       0,      44,     183,      18,       0,
     110,     111,     114,       0,       0,     101,     173,      35,
       0,      99,     100,     101,       0,     115,       0,      99,
     100,     110,     111,     114,       0,       0,      97,       0,
       0,     107,       0,       0,       0,     183,     115,     288,
       0,       0,       0,     183,     282,     154,     173,      35,
      61,       0,      62,       0,     173,      35,       0,     154,
      62,     110,     111,     114,       0,       0,     101,     110,
     111,     114,      99,     100,     101,       0,     115,       0,
      99,     100,       0,       0,     115,       0,       0,       0,
       0,       0,     107,     243,       0,     203,     259,       0,
     107,     101,       0,      87,       0,      99,     100,     173,
      35,       0,      67,       0,       0,      88,      35,     107,
      13,       0,     110,     111,     114,     107,     120,     124,
     110,     111,     114,       0,     173,      35,      44,     115,
       0,       0,     173,      35,       0,     115,       0,     110,
     111,     114,     172,     174,     175,     110,     111,     114,
     271,     272,     273,     274,     115,       0,       0,      66,
       0,       0,     115,     244,     245,     246,     247,     248,
     249,     250,     251,     252,     253,     254,     255,     256,
     257,       0,       0,      66,       0,     129,     122,     126,
       0,      62,       0,      61,       0,      61,     127,       0,
     258,       0,       0,     150,     343,     344,     345,     346,
     347,     348,     349,     350,     351,     352,     353,     354,
     355,     356,     357,     244,       0,       0,       0,       0,
     277,     278,     279,     100,       0,     101,     169,       0,
       0,       0,       0,       0,       0,      75,       0,       0,
       0,       0,       0,       0,       0,     129,       0,       0,
       0,     129,       0,      62,      67,       0,       0,     144,
       0,      75,       0,       0,      75,       0,       0,       0,
       0,      75,       0,       0,     164,     165,     166,      95,
       0,     240,     127,       0,       0,     153,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,     245,
     246,     247,     248,     249,     250,     251,     252,     253,
     254,     255,     256,     257,     258,     149,       0,       0,
       0,      75,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,     170,     171,      97,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
     122,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,     240,       0,       0,
       0,      13,
};

int yypact[] = {
     268,   -1000,     225,     225,    -288,   -1000,   -1000,   -1000,
   -1000,    -272,     -95,     -81,   -1000,     225,   -1000,    -308,
   -1000,    -236,    -237,   -1000,   -1000,   -1000,   -1000,    -299,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,    -239,     225,
   -1000,   -1000,    -195,   -1000,   -1000,    -100,    -281,   -1000,
   -1000,    -105,    -256,   -1000,   -1000,   -1000,   -1000,    -272,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,     616,   -1000,
    -240,   -1000,    -281,   -1000,   -1000,     -37,    -118,   -1000,
   -1000,   -1000,    -281,   -1000,   -1000,   -1000,    -183,    -147,
    -129,     -87,    -201,    -244,    -247,   -1000,    -272,   -1000,
   -1000,   -1000,    -199,   -1000,   -1000,   -1000,    -214,   -1000,
     -73,     -74,   -1000,    -142,     -43,   -1000,     -36,      57,
     -20,   -1000,   -1000,     637,     637,     637,    -208,     631,
     -97,   -1000,    -217,     637,   -1000,    -210,   -1000,   -1000,
   -1000,   -1000,   -1000,    -241,   -1000,   -1000,   -1000,    -109,
    -110,   -1000,    -128,   -1000,    -115,    -116,    -186,    -242,
   -1000,   -1000,   -1000,   -1000,    -204,     -71,    -204,    -204,
   -1000,     610,   -1000,   -1000,     417,   -1000,   -1000,    -166,
     277,   -1000,   -1000,     -52,     695,   -1000,     -52,    -253,
   -1000,    -250,    -194,    -256,   -1000,   -1000,     637,     637,
     637,     637,     637,     637,     637,     637,     637,     637,
     637,     637,     637,     637,   -1000,   -1000,   -1000,   -1000,
     -71,    -272,    -208,   -1000,   -1000,   -1000,     567,   -1000,
    -270,     -55,   -1000,     529,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,     -95,
   -1000,   -1000,   -1000,   -1000,     -75,   -1000,     390,   -1000,
   -1000,   -1000,    -273,      -4,   -1000,   -1000,   -1000,   -1000,
    -302,    -302,     637,   -1000,     326,   -1000,    -277,   -1000,
   -1000,   -1000,     347,    -119,    -120,     -98,    -277,     464,
    -103,   -1000,     -97,   -1000,     277,   -1000,    -123,   -1000,
     277,    -190,   -1000,   -1000,     -52,     -52,     -52,     -52,
     -52,     -52,     -52,     -52,     -52,     -52,     -52,     -52,
     -52,     -52,     -52,   -1000,   -1000,    -130,   -1000,   -1000,
   -1000,   -1000,    -193,     -43,   -1000,   -1000,   -1000,     -28,
     -28,     -28,     -28,     -20,     -20,   -1000,   -1000,   -1000,
    -197,   -1000,   -1000,    -151,   -1000,   -1000,   -1000,   -1000,
     637,   -1000,    -197,   -1000,   -1000,   -1000,    -145,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,    -125,    -126,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,     -53,     -98,
   -1000,   -1000,    -131,     561,     637,     637,     637,     637,
     637,     464,   -1000,    -138,    -144,   -1000,   -1000,   -1000,
     464,    -284,     204,   -1000,   -1000,   -1000,    -229,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,    -292,   -1000,
     637,   -1000,     512,    -169,   -1000,     561,   -1000,   -1000,
    -292,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,    -276,   -1000,   -1000,   -1000,    -277,     637,
    -149,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
     561,   -1000,   -1000,     -58,     464,    -170,    -171,     637,
     -59,     -61,     -98,   -1000,   -1000,     637,    -185,   -1000,
   -1000,   -1000,     -64,     637,     464,     -67,   -1000,     464,
   -1000,
};

int yypgo[] = {
       0,     540,     538,     303,     350,     530,     527,     520,
     560,     494,     517,      66,     515,     685,     912,     514,
     500,     332,     490,     489,     483,     246,     482,     478,
     614,     476,     475,     474,     472,     619,     463,     311,
     462,     461,     458,     445,     856,      48,     738,     906,
     304,     364,     435,     247,     551,     417,     412,     411,
     408,     405,     401,     400,     255,     388,     282,       6,
       0,     278,     982,       8,     384,     250,     373,     743,
       4,      17,     368,     363,     266,     361,     360,       7,
     356,     352,     347,     109,     265,     259,     433,       9,
     357,     291,     239,     334,     262,     263,     331,     149,
     323,     312,     310,     308,      12,     307,     305,     358,
     344,     485,     780,     268,     838,     288,     283,     277,
     270,     338,     531,     269,     267,     248,
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
      71,      72,      72,      73,      64,      64,      74,      74,
      76,      75,      75,      75,      75,      75,      75,      75,
      75,      75,      75,      75,      75,      75,      75,      75,
      75,      75,      75,      75,      75,      75,      78,      83,
      84,      85,      86,      86,      77,      77,      77,      80,
      80,      88,      89,      90,      90,      91,      91,      81,
      81,      81,      82,      82,      82,      82,      82,      79,
      79,      79,      79,      79,      79,      79,      79,      11,
      93,      93,      94,      94,      95,      95,      96,      96,
      96,      97,      97,      97,      97,      97,      98,      98,
      98,      99,      99,      99,      99,     100,     100,     100,
     100,     101,     101,     101,     102,     103,     103,      87,
      87,     104,     104,     104,     104,     104,     104,     104,
     106,     106,      57,      57,      92,      92,      92,      92,
      92,      92,      92,      92,      92,     105,     108,     107,
     107,     109,     109,     109,     109,
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
       1,       5,       5,       1,       2,       3,       1,       2,
       1,       1,       1,       2,       2,       3,       3,       1,
       1,       1,       1,       2,       2,       2,       2,       2,
       2,       2,       2,       2,       2,       2,       3,       2,
       5,       2,       2,       2,       1,       2,       2,       3,
       5,       3,       5,       1,       1,       8,       9,       1,
       1,       1,       2,       3,       3,       2,       2,       1,
       3,       3,       3,       3,       3,       3,       3,       1,
       1,       5,       1,       3,       1,       3,       1,       3,
       3,       1,       3,       3,       3,       3,       1,       3,
       3,       1,       3,       3,       3,       1,       2,       2,
       2,       1,       2,       2,       2,       1,       3,       3,
       1,       1,       1,       1,       1,       1,       1,       1,
       2,       3,       1,       2,       1,       1,       3,       3,
       2,       2,       3,       3,       2,       2,       1,       2,
       3,       1,       1,       3,       3,
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
     -11,     -68,     281,     -75,     288,     288,     -11,     281,
     281,     -64,     -79,     288,     281,     -79,     -75,     281,
     -75,
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
     243,       0,     207,     208,     210,     212,     214,     217,
     222,     225,     229,       0,       0,       0,     233,     244,
     246,     252,     253,       0,     240,      94,     241,     242,
     245,     247,     237,       0,      27,      80,      70,       0,
       0,      76,       0,      71,       0,       0,       0,       0,
      62,      64,      66,      68,       0,      95,       0,       0,
     250,       0,     125,     126,       0,     147,      82,       0,
      84,      83,      99,     100,       0,     108,       0,       0,
     130,       0,      94,      40,      14,      15,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,     230,     243,     231,     232,
     257,     260,     234,     244,     246,     235,       0,     136,
     256,       0,     261,       0,     262,     236,      72,      73,
      74,      75,     100,     103,     133,     134,     135,       0,
     251,     105,     106,     248,       0,     148,       0,     150,
     153,     154,       0,       0,     159,     160,     161,     162,
     180,       0,       0,     199,     233,     152,       0,     191,
     192,     193,       0,       0,       0,       0,       0,       0,
       0,      -2,      -2,      86,      85,      92,       0,      89,
       0,       0,      87,     107,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,     109,     131,       0,     128,     127,
     101,      39,       0,     211,     213,     215,     216,     218,
     219,     220,     221,     223,     224,     226,     227,     228,
     258,     259,     137,       0,     140,     142,     143,     144,
       0,     238,     254,     255,     239,     263,       0,     265,
     266,      56,     249,     149,     151,     155,     163,     164,
     165,     166,     167,     156,       0,       0,     168,     169,
     170,     171,     172,     173,     179,     182,       0,       0,
     178,     181,       0,       0,       0,       0,       0,       0,
       0,       0,     194,       0,       0,     197,     198,     175,
       0,       0,       0,      93,      91,      90,       0,     110,
     111,     112,     113,     114,     115,     116,     117,     118,
     119,     120,     121,     122,     123,     124,       0,     132,
       0,     138,       0,       0,     264,       0,     157,     158,
       0,     177,     174,     200,     201,     202,     203,     204,
     205,     206,     183,     195,     196,     185,       0,       0,
       0,     187,     188,      88,     129,     209,     139,     141,
       0,     267,     268,       0,       0,       0,       0,       0,
       0,       0,       0,     184,     186,       0,       0,     145,
     146,     176,       0,       0,       0,       0,     189,       0,
     190,
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
{ parser.onMapElement(s[s1 - 4], s[s1 - 0]); } break;
case 147:
{ parser.startCompound(s[s1 - 0]); } break;
case 148:
{ parser.finishCompound(s[s1 - 0]); } break;
case 149:
{ parser.finishCompound(s[s1 - 0]); } break;
case 150:
{ parser.addStatement(); } break;
case 151:
{ parser.addStatement(); } break;
case 152:
{ parser.onDeclarator(); } break;
case 155:
{ parser.onStatement(); } break;
case 156:
{ parser.onStatement(); } break;
case 157:
{ parser.onStatement2(); } break;
case 158:
{ parser.onStatement2(); } break;
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
{ parser.onStatement1(); } break;
case 174:
{ parser.onThrowStatement(s[s1 - 2]); } break;
case 175:
{ parser.onTryStatement(s[s1 - 1]); } break;
case 176:
{ parser.onCatchClause(s[s1 - 4]); } break;
case 177:
{ parser.onFinallyStatement(s[s1 - 1]); } break;
case 178:
{ parser.addCatchStatement(); } break;
case 179:
{ parser.addCatchStatement(); } break;
case 183:
{ parser.onIfStatement(s[s1 - 2], null); } break;
case 184:
{ parser.onIfStatement(s[s1 - 4], s[s1 - 1]); } break;
case 185:
{ parser.onWhileStatement(s[s1 - 2]); } break;
case 186:
{ parser.onDoWhileStatement(s[s1 - 4], s[s1 - 2]); } break;
case 187:
{ parser.onDeclarator(); } break;
case 189:
{ parser.onForStatement(s[s1 - 7], false); } break;
case 190:
{ parser.onForStatement(s[s1 - 8], true); } break;
case 194:
{ parser.onJumpStatement(s[s1 - 1], false); } break;
case 195:
{ parser.onJumpStatement(s[s1 - 2], true); } break;
case 196:
{ parser.onJumpStatement(s[s1 - 2], true); } break;
case 197:
{ parser.onJumpStatement(s[s1 - 1], false); } break;
case 198:
{ parser.onJumpStatement(s[s1 - 1], false); } break;
case 200:
{ parser.onAssignment(s[s1 - 1]); } break;
case 201:
{ parser.onAssignment(s[s1 - 1]); } break;
case 202:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 203:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 204:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 205:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 206:
{ parser.onOperatorAssign(s[s1 - 1]); parser.onAssignment(s[s1 - 1]); } break;
case 209:
{ parser.onCondition(); } break;
case 211:
{ parser.onOperator(s[s1 - 1]); } break;
case 213:
{ parser.onOperator(s[s1 - 1]); } break;
case 215:
{ parser.onOperator(s[s1 - 1]); } break;
case 216:
{ parser.onOperator(s[s1 - 1]); } break;
case 218:
{ parser.onOperator(s[s1 - 1]); } break;
case 219:
{ parser.onOperator(s[s1 - 1]); } break;
case 220:
{ parser.onOperator(s[s1 - 1]); } break;
case 221:
{ parser.onOperator(s[s1 - 1]); } break;
case 223:
{ parser.onOperator(s[s1 - 1]); } break;
case 224:
{ parser.onOperator(s[s1 - 1]); } break;
case 226:
{ parser.onOperator(s[s1 - 1]); } break;
case 227:
{ parser.onOperator(s[s1 - 1]); } break;
case 228:
{ parser.onOperator(s[s1 - 1]); } break;
case 231:
{ parser.onUnary(s[s1 - 1]); } break;
case 232:
{ parser.onUnary(s[s1 - 1]); } break;
case 234:
{ parser.onTypeCast(); } break;
case 235:
{ parser.onArrayTypeCast(); } break;
case 236:
{ parser.onNewExpression(s[s1 - 1], s[s1 - 0]); } break;
case 237:
{ parser.onContainer(s[s1 - 0]); } break;
case 238:
{ parser.addContainer(s[s1 - 0]); } break;
case 239:
{ parser.onBracedExpression(s[s1 - 2], s[s1 - 0]); } break;
case 241:
{ parser.onThis(s[s1 - 0]); } break;
case 242:
{ parser.onSuper(s[s1 - 0]); } break;
case 243:
{ parser.onConstant(s[s1 - 0]); } break;
case 245:
{ parser.onTypeToPostfix(); } break;
case 246:
{ parser.onTypeToPostfix(); } break;
case 247:
{ parser.onMethodCall(false); } break;
case 248:
{ parser.onIndex(s[s1 - 1], s[s1 - 0], false); } break;
case 249:
{ parser.onIndex(s[s1 - 2], s[s1 - 0], true); } break;
case 250:
{ parser.onIndices(true); } break;
case 251:
{ parser.onIndices(false); } break;
case 254:
{ parser.onPostfix(); } break;
case 255:
{ parser.onMethodCall(true); } break;
case 256:
{ error(s[s1 - 0]); } break;
case 257:
{ parser.onArrayAccess(); } break;
case 258:
{ parser.onPostfix(); } break;
case 259:
{ parser.onMethodCall(true); } break;
case 260:
{ error(s[s1 - 0]); } break;
case 262:
{ parser.startExpressions(s[s1 - 0]); } break;
case 263:
{ parser.finishExpressions(s[s1 - 0]); } break;
case 264:
{ parser.finishExpressions(s[s1 - 0]); } break;
case 265:
{ parser.addExpression(); } break;
case 266:
{ parser.addExpression(); } break;
case 267:
{ parser.addExpression(); } break;
case 268:
{ parser.addExpression(); } break;
		}
		return endStack;
	}
}