
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
int INSTANCE_OF = 325;
int YYERRCODE = 256;

int yyexca[] = {
  -1, 1,
  0, -1,
  -2, 0,
  -1, 71,
  294, 94,
  295, 94,
  -2, 104,
  -1, 234,
  294, 96,
  -2, 246,
  -1, 235,
  294, 97,
  -2, 247,
  0,
};

int YYNPROD = 270;
int YYLAST = 1170;
int yyact[] = {
     104,     158,      30,     261,     213,      48,     112,     287,
      70,     211,      35,      53,     102,     263,      35,      76,
      35,     321,     322,     107,       9,      35,     399,     385,
     147,     148,      35,     152,      84,     265,      24,      26,
      25,     292,     195,      77,      28,     190,      27,      80,
      78,     150,      30,      29,      78,      79,      78,     116,
     222,     189,      54,     137,      52,      50,      35,      76,
     115,     390,      56,      80,     185,      56,      80,      80,
     137,      79,     220,      80,      79,      79,      24,      26,
      25,      79,     178,     107,      28,     138,      27,      80,
     189,     399,     146,      29,      78,      79,     399,      65,
     174,      35,      30,     345,      56,      55,     155,     137,
     118,      56,     132,     110,     111,     114,      35,      76,
     181,      56,     414,      65,      30,     408,      65,      65,
     115,     199,      66,     128,     179,     407,      24,      26,
      25,      89,     364,      80,      28,     363,      27,      80,
     365,      79,     402,      29,      78,      79,       9,      22,
      24,      26,      25,     383,      21,     235,      28,     211,
      27,     382,     373,     234,     370,      29,      23,     208,
      80,     221,     369,      30,     107,     343,      79,     135,
     264,     123,     295,     337,     160,     267,     137,      35,
     367,     141,     362,     128,     395,     134,     186,     128,
      56,     341,     368,     103,     361,     159,     122,      24,
      26,      25,     141,     336,     194,      28,     139,      27,
     140,     193,     192,     191,      29,      78,     236,     198,
      81,     198,     198,      57,     205,     184,      39,     145,
     235,      43,     301,     157,     156,     235,     234,     137,
     307,     315,     182,     234,     221,     418,     312,     415,
     411,     221,     269,     371,      30,     410,     405,     295,
     152,     235,     102,     338,     104,     161,     240,     234,
      35,     112,     319,     101,     212,     221,      59,      99,
     100,      69,     289,     162,     163,     132,     320,     298,
      24,      26,      25,     168,     169,     238,      28,     107,
      27,     137,     297,     184,     216,      29,      78,      36,
     159,     371,     207,     188,     174,      35,     170,     171,
     172,     187,     105,     180,     106,       6,     325,     110,
     111,     114,      33,     108,      22,      92,     334,     215,
     326,     290,      98,     141,     115,      91,     299,     304,
      93,     117,      90,      23,     136,      14,     226,     223,
     212,     232,     233,     231,     131,      63,       3,       6,
     230,      31,     341,      45,     168,     169,     387,     372,
     164,     165,     166,     167,     225,     235,     230,     224,
     183,     219,     217,     234,     235,     335,     235,     322,
     218,     221,     234,     207,     234,     182,       3,     389,
     221,       5,     221,      14,     368,      32,     366,     303,
     223,     214,      80,     140,      59,     219,     288,      68,
      79,     286,     264,     183,      80,     394,     151,     391,
     262,     405,      79,     121,     398,      69,      72,      83,
     340,      32,       5,      74,     239,     242,     388,     237,
     227,     374,     376,     377,     378,     379,     380,     408,
     235,     291,     287,     332,     368,     263,     234,      70,
     143,     144,     412,     339,     221,     227,     142,      73,
     235,     213,     413,     235,      60,     221,     234,     414,
     416,     234,     211,     221,     221,     113,     392,     221,
     289,     326,     130,     396,      12,     222,      64,      63,
     375,     327,     201,     328,     329,     330,     331,     363,
      94,     235,      46,      58,      37,     401,     270,     232,
      38,     311,     197,     365,      10,     323,     403,     386,
      15,     109,     271,      85,     395,     409,     402,      47,
     300,     324,      51,      34,     220,      41,      82,     290,
     344,     318,     397,      12,     381,      42,     201,      40,
      11,     365,     130,     384,     332,      49,      17,      19,
      20,     101,      68,     342,      38,      99,     100,     209,
      71,       4,      96,     309,     317,     404,     136,     395,
     231,      86,     210,       8,       7,     291,     199,     385,
       2,     184,     393,       1,      71,       0,       0,      71,
      71,       0,     174,      35,      71,     339,       0,       0,
     399,     151,       0,     266,     133,     110,     111,     114,
     154,       0,     327,     328,     329,     330,     331,     406,
      58,     137,     115,       0,     212,     101,     415,       0,
     326,      99,     100,     178,      92,     400,     319,     417,
       0,     418,     419,     208,      36,     308,     316,       0,
      93,     107,       0,     320,     103,     141,       0,     240,
       0,     310,       0,     216,      71,       0,     174,      35,
      71,     223,       0,     232,     233,     231,     228,     227,
     229,     110,     111,     114,     238,     212,       0,       0,
     284,       0,       0,     272,     273,     215,     115,     294,
     230,     154,       0,     219,     109,      78,     101,       0,
     268,       0,      99,     100,     245,     246,     247,     248,
     249,     250,     251,     252,     253,     254,     255,     256,
     257,     258,     107,     177,     283,       0,     141,     302,
       0,     101,      83,     293,     196,      99,     100,     174,
      35,     259,     223,       0,     232,     233,     231,     228,
     227,     229,     110,     111,     114,     107,     212,       0,
       0,     141,     206,     278,     279,       0,     214,     115,
      18,     230,     174,      35,     219,     223,      78,     232,
     233,     231,     228,     227,     229,     110,     111,     114,
       0,       0,       0,     101,       0,       0,       0,      99,
     100,       0,     115,       0,     230,       0,      62,     219,
       0,      78,     185,     305,     313,       0,     133,     107,
     177,     163,      94,     141,     113,       0,     306,     314,
       0,       0,       0,       0,     174,      35,     178,     223,
       0,     232,     233,     231,     228,     227,     229,     110,
     111,     114,     101,       0,      30,     185,      99,     100,
       0,       0,       0,      67,     115,       0,     230,     361,
     243,     219,     177,      78,       0,       0,     107,       0,
     149,      61,      30,       0,     119,     125,     386,       0,
      24,      26,      25,     174,      35,       0,      28,       0,
      27,     169,      96,     202,     203,      29,     110,     111,
     114,       0,       0,       0,       0,      22,      24,      26,
      25,       0,      21,     115,      28,       0,      27,       0,
       0,     212,      78,      29,      23,     200,     101,     200,
     200,     209,      99,     100,     101,     212,     129,       0,
      99,     100,       0,     154,      97,     210,      62,     120,
     124,       0,     107,       0,      62,     154,     184,       0,
     107,     296,     333,       0,     184,       0,       0,     174,
      35,       0,       0,       0,       0,     174,      35,     135,
     196,       0,     110,     111,     114,       0,       0,     101,
     110,     111,     114,      99,     100,     101,       0,     115,
       0,      99,     100,       0,       0,     115,     129,      95,
       0,      18,     129,     291,       0,      67,       0,     184,
     285,     107,      62,       0,       0,     184,      61,       0,
     174,      35,       0,       0,       0,     101,     174,      35,
       0,      99,     100,     110,     111,     114,       0,      16,
       0,     110,     111,     114,     244,       0,     101,     260,
     115,     107,      99,     100,     204,      44,     115,      13,
     173,     175,     176,       0,       0,       0,     174,      35,
       0,       0,     107,     101,       0,      87,       0,      99,
     100,     110,     111,     114,       0,       0,       0,      88,
      35,       0,       0,       0,      16,       0,     115,     107,
       0,       0,     110,     111,     114,       0,      66,       0,
       0,       0,       0,       0,     174,      35,       0,     115,
       0,       0,      44,       0,       0,       0,       0,     110,
     111,     114,      66,       0,       0,     122,     126,       0,
       0,       0,      61,       0,     115,       0,       0,     280,
     281,     282,     100,       0,     101,     170,       0,       0,
       0,     127,       0,       0,       0,     150,     346,     347,
     348,     349,     350,     351,     352,     353,     354,     355,
     356,     357,     358,     359,     360,     245,       0,       0,
       0,       0,      44,       0,       0,       0,      44,       0,
       0,       0,       0,       0,     274,     275,     276,     277,
      75,       0,      62,       0,       0,       0,     144,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,      75,       0,       0,      75,
     241,       0,       0,       0,      75,       0,       0,       0,
       0,       0,       0,       0,       0,     127,       0,       0,
     153,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,     171,     172,      97,
       0,       0,       0,     246,     247,     248,     249,     250,
     251,     252,     253,     254,     255,     256,     257,     258,
     259,     149,       0,       0,      75,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,      44,     165,     166,     167,      95,       0,       0,
       0,       0,       0,     122,       0,     241,       0,       0,
       0,      13,
};

int yypact[] = {
    -174,   -1000,     488,     488,    -288,   -1000,   -1000,   -1000,
   -1000,    -268,     -78,     -75,   -1000,     488,   -1000,    -308,
   -1000,    -241,    -242,   -1000,   -1000,   -1000,   -1000,    -298,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,    -244,     488,
   -1000,   -1000,    -195,   -1000,   -1000,     -82,    -280,   -1000,
   -1000,     -85,    -266,   -1000,   -1000,   -1000,   -1000,    -268,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,     658,   -1000,
    -247,   -1000,    -280,   -1000,   -1000,    -192,    -127,   -1000,
   -1000,   -1000,    -280,   -1000,   -1000,   -1000,    -191,    -116,
     -98,     -77,    -210,    -270,    -253,   -1000,    -268,   -1000,
   -1000,   -1000,    -197,   -1000,   -1000,   -1000,    -234,   -1000,
     -71,     -72,    -324,    -109,     -35,   -1000,     -15,      60,
      17,   -1000,   -1000,     679,     679,     679,    -218,    -205,
     -79,   -1000,    -232,     679,   -1000,    -231,   -1000,   -1000,
   -1000,   -1000,   -1000,    -257,   -1000,   -1000,   -1000,     -93,
     -94,   -1000,    -240,   -1000,     -95,    -100,     -54,    -260,
   -1000,   -1000,   -1000,   -1000,     -17,     -67,     -17,     -17,
   -1000,     641,   -1000,   -1000,     389,   -1000,   -1000,     -87,
     466,   -1000,   -1000,     -48,     368,   -1000,     -48,    -278,
   -1000,    -265,    -187,    -266,   -1000,   -1000,    -268,     679,
     679,     679,     679,     679,     679,     679,     679,     679,
     679,     679,     679,     679,     679,   -1000,   -1000,   -1000,
   -1000,     -67,    -268,    -218,   -1000,   -1000,   -1000,     603,
   -1000,    -273,     -50,   -1000,     560,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
     -78,   -1000,   -1000,   -1000,   -1000,     -73,   -1000,     362,
   -1000,   -1000,   -1000,      15,     297,   -1000,   -1000,   -1000,
   -1000,    -303,    -303,     679,   -1000,     271,   -1000,    -261,
   -1000,   -1000,   -1000,     554,    -101,    -125,    -115,    -261,
     439,    -103,   -1000,     -79,   -1000,     466,   -1000,    -131,
   -1000,     466,    -198,   -1000,   -1000,     -48,     -48,     -48,
     -48,     -48,     -48,     -48,     -48,     -48,     -48,     -48,
     -48,     -48,     -48,     -48,   -1000,   -1000,    -111,   -1000,
   -1000,   -1000,   -1000,   -1000,    -234,    -162,     -35,   -1000,
   -1000,   -1000,      -5,      -5,      -5,      -5,      17,      17,
   -1000,   -1000,   -1000,    -200,   -1000,   -1000,    -163,   -1000,
   -1000,   -1000,   -1000,     679,   -1000,    -200,   -1000,   -1000,
   -1000,    -113,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,    -134,
    -140,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,     -53,    -115,   -1000,   -1000,    -142,     609,     679,
     679,     679,     679,     679,     439,   -1000,    -143,    -149,
   -1000,   -1000,   -1000,     439,    -277,     486,   -1000,   -1000,
   -1000,    -236,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,   -1000,
   -1000,    -284,   -1000,     679,   -1000,     237,    -119,   -1000,
     609,   -1000,   -1000,    -284,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,   -1000,   -1000,    -275,   -1000,   -1000,
   -1000,    -261,     679,    -158,   -1000,   -1000,   -1000,   -1000,
   -1000,   -1000,   -1000,     609,   -1000,   -1000,     -51,     439,
    -171,    -179,     679,     -52,     -57,    -115,   -1000,   -1000,
     679,    -182,   -1000,   -1000,   -1000,     -58,     679,     439,
     -60,   -1000,     439,   -1000,
};

int yypgo[] = {
       0,     523,     520,     318,     353,     516,     515,     505,
     285,     465,     496,      66,     495,     911,     927,     494,
     493,     309,     488,     487,     485,     383,     478,     477,
     680,     474,     471,     467,     464,     436,     460,     271,
     458,     456,     452,     451,     769,      48,     710,     755,
     317,     367,     438,     249,     246,     420,     415,     414,
     409,     408,     391,     389,     238,     388,     261,       6,
       0,     540,    1009,       8,     387,     652,     382,     768,
       4,      13,     376,     374,     113,     371,     369,       7,
     366,     363,     347,     151,     244,     503,     514,       9,
     361,     295,     268,     344,     242,     254,     338,     179,
     335,     332,     326,     310,      12,     306,     301,     293,
     304,     448,     879,     506,     828,     298,     291,     284,
     282,     429,     308,     281,     275,     266,
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
      11,      93,      93,      94,      94,      95,      95,      96,
      96,      96,      97,      97,      97,      97,      97,      98,
      98,      98,      99,      99,      99,      99,     100,     100,
     100,     100,     101,     101,     101,     102,     103,     103,
      87,      87,     104,     104,     104,     104,     104,     104,
     104,     106,     106,      57,      57,      92,      92,      92,
      92,      92,      92,      92,      92,      92,     105,     108,
     107,     107,     109,     109,     109,     109,
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
       3,       1,       5,       1,       3,       1,       3,       1,
       3,       3,       1,       3,       3,       3,       3,       1,
       3,       3,       1,       3,       3,       3,       1,       2,
       2,       2,       1,       2,       2,       2,       1,       3,
       3,       1,       1,       1,       1,       1,       1,       1,
       1,       2,       3,       1,       2,       1,       1,       3,
       3,       2,       2,       3,       3,       2,       2,       1,
       2,       3,       1,       1,       3,       3,
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
     280,     -58,      -9,     291,     283,     283,     325,     290,
     273,     272,     266,     267,     268,     269,     270,     271,
     264,     265,     261,     262,     263,    -100,     293,    -100,
    -100,     -57,     292,     -92,     -87,     -56,     -68,     -69,
     284,     292,     -11,    -107,    -108,     280,     294,     288,
     288,     288,     288,     294,     -61,     -32,     -11,     -68,
     -24,    -106,     -61,     -61,     283,     -11,     285,     -74,
     -75,     -77,     -78,     -79,     -76,     -64,     -80,     -81,
     -82,     -86,     -83,     322,     -11,     -92,     -37,     296,
     -88,     -89,     -91,     302,     301,     303,     319,     300,
     298,     299,     -55,     -56,     285,     -50,     -54,     -53,
     -52,     -14,     -51,     294,     -63,     260,     261,     262,
     263,     264,     265,     266,     267,     268,     269,     270,
     271,     272,     273,     289,     -63,     281,     -66,     -65,
     -59,     294,     -21,     -55,      -9,     -11,     -95,     -96,
     -97,     -97,     -98,     -98,     -98,     -98,     -99,     -99,
    -100,    -100,    -100,      -9,    -105,     285,     -70,     -71,
     -72,     -11,     -68,     280,     306,      -9,    -105,     281,
     281,    -109,     -11,     -68,     -31,     283,     285,     -75,
     288,     -77,     -78,     -64,     -80,     -81,     288,     -76,
     -79,     -77,     -78,     -64,     -80,     -81,     -82,     -84,
     -85,     320,     321,     -84,     -85,     -11,     289,     275,
     276,     277,     278,     279,     -87,     288,     -11,     -68,
     288,     288,     -64,     -87,     -75,     280,     -54,     288,
     -52,     289,     -63,     -63,     -63,     -63,     -63,     -63,
     -63,     -63,     -63,     -63,     -63,     -63,     -63,     -63,
     -63,     291,     281,     287,     285,     291,     -11,     281,
     291,     288,     288,     280,     -64,     288,     -11,     -68,
     -11,     -11,     -11,     -11,     -11,     -75,     288,     288,
     -75,     300,     288,     -90,     -37,     -79,     293,     -65,
     -11,     285,     -71,     291,     -11,     -68,     -59,     297,
     -87,     -11,     288,     -11,     -68,     281,     -75,     288,
     288,     -11,     281,     281,     -64,     -79,     288,     281,
     -79,     -75,     281,     -75,
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
     244,       0,     207,     209,     211,     213,     215,     218,
     223,     226,     230,       0,       0,       0,     234,     245,
     247,     253,     254,       0,     241,      94,     242,     243,
     246,     248,     238,       0,      27,      80,      70,       0,
       0,      76,       0,      71,       0,       0,       0,       0,
      62,      64,      66,      68,       0,      95,       0,       0,
     251,       0,     125,     126,       0,     147,      82,       0,
      84,      83,      99,     100,       0,     108,       0,       0,
     130,       0,      94,      40,      14,      15,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,       0,     231,     244,     232,
     233,     258,     261,     235,     245,     247,     236,       0,
     136,     257,       0,     262,       0,     263,     237,      72,
      73,      74,      75,     100,     103,     133,     134,     135,
       0,     252,     105,     106,     249,       0,     148,       0,
     150,     153,     154,       0,       0,     159,     160,     161,
     162,     180,       0,       0,     199,     234,     152,       0,
     191,     192,     193,       0,       0,       0,       0,       0,
       0,       0,      -2,      -2,      86,      85,      92,       0,
      89,       0,       0,      87,     107,       0,       0,       0,
       0,       0,       0,       0,       0,       0,       0,       0,
       0,       0,       0,       0,     109,     131,       0,     128,
     127,     101,      39,     208,      94,       0,     212,     214,
     216,     217,     219,     220,     221,     222,     224,     225,
     227,     228,     229,     259,     260,     137,       0,     140,
     142,     143,     144,       0,     239,     255,     256,     240,
     264,       0,     266,     267,      56,     250,     149,     151,
     155,     163,     164,     165,     166,     167,     156,       0,
       0,     168,     169,     170,     171,     172,     173,     179,
     182,       0,       0,     178,     181,       0,       0,       0,
       0,       0,       0,       0,       0,     194,       0,       0,
     197,     198,     175,       0,       0,       0,      93,      91,
      90,       0,     110,     111,     112,     113,     114,     115,
     116,     117,     118,     119,     120,     121,     122,     123,
     124,       0,     132,       0,     138,       0,       0,     265,
       0,     157,     158,       0,     177,     174,     200,     201,
     202,     203,     204,     205,     206,     183,     195,     196,
     185,       0,       0,       0,     187,     188,      88,     129,
     210,     139,     141,       0,     268,     269,       0,       0,
       0,       0,       0,       0,       0,       0,     184,     186,
       0,       0,     145,     146,     176,       0,       0,       0,
       0,     189,       0,     190,
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
case 208:
{ parser.onInstanceOf(s[s1 - 1]); } break;
case 210:
{ parser.onTernaryOperator(); } break;
case 212:
{ parser.onOperator(s[s1 - 1]); } break;
case 214:
{ parser.onOperator(s[s1 - 1]); } break;
case 216:
{ parser.onOperator(s[s1 - 1]); } break;
case 217:
{ parser.onOperator(s[s1 - 1]); } break;
case 219:
{ parser.onOperator(s[s1 - 1]); } break;
case 220:
{ parser.onOperator(s[s1 - 1]); } break;
case 221:
{ parser.onOperator(s[s1 - 1]); } break;
case 222:
{ parser.onOperator(s[s1 - 1]); } break;
case 224:
{ parser.onOperator(s[s1 - 1]); } break;
case 225:
{ parser.onOperator(s[s1 - 1]); } break;
case 227:
{ parser.onOperator(s[s1 - 1]); } break;
case 228:
{ parser.onOperator(s[s1 - 1]); } break;
case 229:
{ parser.onOperator(s[s1 - 1]); } break;
case 232:
{ parser.onUnary(s[s1 - 1]); } break;
case 233:
{ parser.onUnary(s[s1 - 1]); } break;
case 235:
{ parser.onTypeCast(); } break;
case 236:
{ parser.onArrayTypeCast(); } break;
case 237:
{ parser.onNewExpression(s[s1 - 1], s[s1 - 0]); } break;
case 238:
{ parser.onContainer(s[s1 - 0]); } break;
case 239:
{ parser.addContainer(s[s1 - 0]); } break;
case 240:
{ parser.onBracedExpression(s[s1 - 2], s[s1 - 0]); } break;
case 242:
{ parser.onThis(s[s1 - 0]); } break;
case 243:
{ parser.onSuper(s[s1 - 0]); } break;
case 244:
{ parser.onConstant(s[s1 - 0]); } break;
case 246:
{ parser.onTypeToPostfix(); } break;
case 247:
{ parser.onTypeToPostfix(); } break;
case 248:
{ parser.onMethodCall(false); } break;
case 249:
{ parser.onIndex(s[s1 - 1], s[s1 - 0], false); } break;
case 250:
{ parser.onIndex(s[s1 - 2], s[s1 - 0], true); } break;
case 251:
{ parser.onIndices(true); } break;
case 252:
{ parser.onIndices(false); } break;
case 255:
{ parser.onPostfix(); } break;
case 256:
{ parser.onMethodCall(true); } break;
case 257:
{ error(s[s1 - 0]); } break;
case 258:
{ parser.onArrayAccess(); } break;
case 259:
{ parser.onPostfix(); } break;
case 260:
{ parser.onMethodCall(true); } break;
case 261:
{ error(s[s1 - 0]); } break;
case 263:
{ parser.startExpressions(s[s1 - 0]); } break;
case 264:
{ parser.finishExpressions(s[s1 - 0]); } break;
case 265:
{ parser.finishExpressions(s[s1 - 0]); } break;
case 266:
{ parser.addExpression(); } break;
case 267:
{ parser.addExpression(); } break;
case 268:
{ parser.addExpression(); } break;
case 269:
{ parser.addExpression(); } break;
		}
		return endStack;
	}
}