
/*
1. javaYacc.exe -CGrammar.java -pParser.y Grammar.y
2. copy Grammar.java to org.zenframework.z8.compiler.parser
*/


%{
package org.zenframework.z8.compiler.parser.grammar;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.Token;

public class Grammar
{
%}

%start program

%token <token> WHITESPACE
%token <token> LINEBREAK
%token <token> COMMENT

%token <token> NOT
%token <token> MUL
%token <token> DIV
%token <token> MOD
%token <token> ADD
%token <token> SUB
%token <token> EQU
%token <token> NOT_EQU
%token <token> LESS
%token <token> MORE
%token <token> LESS_EQU
%token <token> MORE_EQU
%token <token> AND
%token <token> OR
%token <token> GROUP

%token <token> ADD_ASSIGN
%token <token> SUB_ASSIGN
%token <token> MUL_ASSIGN
%token <token> DIV_ASSIGN
%token <token> MOD_ASSIGN

%token <token> LBRACE
%token <token> RBRACE
%token <token> LBRACKET
%token <token> RBRACKET
%token <token> LCBRACE
%token <token> RCBRACE
%token <token> BRACKETS
%token <token> COLON
%token <token> SEMICOLON
%token <token> ASSIGN
%token <token> QUESTION
%token <token> COMMA
%token <token> DOT 

%token <token> CONSTANT
%token <token> IDENTIFIER
%token <token> OPERATOR

%token <token> IF
%token <token> ELSE

%token <token> DO
%token <token> FOR
%token <token> WHILE

%token <token> BREAK
%token <token> RETURN
%token <token> CONTINUE

%token <token> THIS
%token <token> SUPER
%token <token> CONTAINER

%token <token> NULL

%token <token> IMPORT

%token <token> CLASS
%token <token> PUBLIC
%token <token> PROTECTED
%token <token> PRIVATE
%token <token> EXTENDS

%token <token> ENUM
%token <token> RECORDS

%token <token> AUTO

%token <token> NEW
%token <token> STATIC

%token <token> TRY
%token <token> CATCH
%token <token> FINALLY
%token <token> THROW

%token <token> VIRTUAL

%left COMMA SEMICOLON
%left COLON
%left ASSIGN
%left OR
%left AND
%left EQU NOT_EQU
%left LESS MORE LESS_EQU MORE_EQU
%left ADD SUB
%left MUL DIV MOD
%left QUESTION
%left LBRACE RBRACE
%left LBRACKET RBRACKET

%%
/* ========================================================================================= */

program
	: import
	| classes
	| import classes
	;

classes
	: class_enum
	| classes class_enum
	;
	
class_enum
	: class
	| enum
	;
	
import
	: import_list							{ m_parser.onImport(); }
	;
	
import_list
	: import_element						{ m_parser.onImportList(true); }
	| import_list import_element			{ m_parser.onImportList(false); }
	;
	
import_element
	: IMPORT qualified_name SEMICOLON		{ m_parser.createImportElement($1); }
	;
	
attribute_lbracket
	: LBRACKET
		{
			m_parser.startAttribute($1);
		}
/*	| attribute_lbracket NOT		{ error($2); }	| attribute_lbracket MUL		{ error($2); }
	| attribute_lbracket DIV		{ error($2); }	| attribute_lbracket MOD		{ error($2); }
	| attribute_lbracket ADD		{ error($2); }	| attribute_lbracket SUB		{ error($2); }
	| attribute_lbracket EQU		{ error($2); }	| attribute_lbracket NOT_EQU	{ error($2); }
	| attribute_lbracket LESS		{ error($2); }	| attribute_lbracket MORE		{ error($2); }
	| attribute_lbracket LESS_EQU	{ error($2); }	| attribute_lbracket MORE_EQU	{ error($2); }
	| attribute_lbracket AND		{ error($2); }	| attribute_lbracket OR			{ error($2); }
	| attribute_lbracket ADD_ASSIGN{ error($2); }
	| attribute_lbracket RBRACE		{ error($2); }	| attribute_lbracket COLON		{ error($2); }
	| attribute_lbracket ASSIGN		{ error($2); }	| attribute_lbracket QUESTION	{ error($2); }
	| attribute_lbracket COMMA		{ error($2); }	| attribute_lbracket DOT 		{ error($2); }
	| attribute_lbracket CONSTANT	{ error($2); }	| attribute_lbracket IF			{ error($2); }
	| attribute_lbracket ELSE		{ error($2); }	| attribute_lbracket DO			{ error($2); }
	| attribute_lbracket FOR		{ error($2); }	| attribute_lbracket WHILE		{ error($2); }
	| attribute_lbracket BREAK		{ error($2); }	| attribute_lbracket RETURN		{ error($2); }
	| attribute_lbracket CONTINUE	{ error($2); }	| attribute_lbracket THIS		{ error($2); }
	| attribute_lbracket SUPER		{ error($2); }	| attribute_lbracket CONTAINER	{ error($2); }
	| attribute_lbracket NULL		{ error($2); }	| attribute_lbracket EXTENDS	{ error($2); }
	| attribute_lbracket NEW		{ error($2); }	| attribute_lbracket TRY		{ error($2); }
	| attribute_lbracket CATCH		{ error($2); }	| attribute_lbracket FINALLY	{ error($2); }
	| attribute_lbracket THROW		{ error($2); }
*/	;
	
attribute_identifier
	: IDENTIFIER
		{
			m_parser.setAttributeName($1);
		}						
/*	| attribute_identifier NOT			{ error($2); }	| attribute_identifier MUL			{ error($2); }
	| attribute_identifier DIV			{ error($2); }	| attribute_identifier MOD			{ error($2); }
	| attribute_identifier ADD			{ error($2); }	| attribute_identifier SUB		{ error($2); }
	| attribute_identifier EQU			{ error($2); }	| attribute_identifier NOT_EQU		{ error($2); }
	| attribute_identifier LESS			{ error($2); }	| attribute_identifier MORE			{ error($2); }
	| attribute_identifier LESS_EQU		{ error($2); }	| attribute_identifier MORE_EQU		{ error($2); }
	| attribute_identifier AND			{ error($2); }	| attribute_identifier OR			{ error($2); }
	| attribute_identifier ADD_ASSIGN	{ error($2); }
	| attribute_identifier RBRACE		{ error($2); }	| attribute_identifier COLON		{ error($2); }
	| attribute_identifier ASSIGN		{ error($2); }	| attribute_identifier QUESTION		{ error($2); }
	| attribute_identifier COMMA		{ error($2); }	| attribute_identifier DOT 			{ error($2); }
	| attribute_identifier IF			{ error($2); }
	| attribute_identifier ELSE			{ error($2); }	| attribute_identifier DO			{ error($2); }
	| attribute_identifier FOR			{ error($2); }	| attribute_identifier WHILE		{ error($2); }
	| attribute_identifier BREAK		{ error($2); }	| attribute_identifier RETURN		{ error($2); }
	| attribute_identifier CONTINUE		{ error($2); }	| attribute_identifier THIS			{ error($2); }
	| attribute_identifier SUPER		{ error($2); }	| attribute_identifier CONTAINER	{ error($2); }
	| attribute_identifier NULL			{ error($2); }	| attribute_identifier EXTENDS		{ error($2); }
	| attribute_identifier NEW			{ error($2); }	| attribute_identifier TRY			{ error($2); }
	| attribute_identifier CATCH		{ error($2); }	| attribute_identifier FINALLY		{ error($2); }
	| attribute_identifier THROW		{ error($2); }
*/	;

attribute_constant
	: CONSTANT
		{
			m_parser.setAttributeValue($1);
		}
/*	| attribute_constant CONSTANT
	| attribute_constant NOT			{ error($2); }	| attribute_constant MUL		{ error($2); }
	| attribute_constant DIV			{ error($2); }	| attribute_constant MOD		{ error($2); }
	| attribute_constant ADD			{ error($2); }	| attribute_constant SUB		{ error($2); }
	| attribute_constant EQU			{ error($2); }	| attribute_constant NOT_EQU	{ error($2); }
	| attribute_constant LESS			{ error($2); }	| attribute_constant MORE		{ error($2); }
	| attribute_constant LESS_EQU		{ error($2); }	| attribute_constant MORE_EQU	{ error($2); }
	| attribute_constant AND			{ error($2); }	| attribute_constant OR			{ error($2); }
	| attribute_constant ADD_ASSIGN	{ error($2); }
	| attribute_constant RBRACE			{ error($2); }	| attribute_constant COLON		{ error($2); }
	| attribute_constant ASSIGN			{ error($2); }	| attribute_constant QUESTION	{ error($2); }
	| attribute_constant COMMA			{ error($2); }	| attribute_constant DOT 		{ error($2); }
	| attribute_constant CONSTANT		{ error($2); }
	| attribute_constant IF				{ error($2); }
	| attribute_constant ELSE			{ error($2); }	| attribute_constant DO			{ error($2); }
	| attribute_constant FOR			{ error($2); }	| attribute_constant WHILE		{ error($2); }
	| attribute_constant BREAK			{ error($2); }	| attribute_constant RETURN		{ error($2); }
	| attribute_constant CONTINUE		{ error($2); }	| attribute_constant THIS		{ error($2); }
	| attribute_constant SUPER			{ error($2); }	| attribute_constant CONTAINER	{ error($2); }
	| attribute_constant NULL			{ error($2); }	| attribute_constant EXTENDS	{ error($2); }
	| attribute_constant NEW			{ error($2); }	| attribute_constant TRY		{ error($2); }
	| attribute_constant CATCH			{ error($2); }	| attribute_constant FINALLY	{ error($2); }
	| attribute_constant THROW			{ error($2); }
*/	;
	
attribute_rbracket
	: RBRACKET
		{
			m_parser.finishAttribute($1);
		}
/*	| attribute_rbracket NOT		{ error($2); }	| attribute_rbracket MUL		{ error($2); }
	| attribute_rbracket DIV		{ error($2); }	| attribute_rbracket MOD		{ error($2); }
	| attribute_rbracket ADD		{ error($2); }	| attribute_rbracket SUB		{ error($2); }
	| attribute_rbracket EQU		{ error($2); }	| attribute_rbracket NOT_EQU	{ error($2); }
	| attribute_rbracket LESS		{ error($2); }	| attribute_rbracket MORE		{ error($2); }
	| attribute_rbracket LESS_EQU	{ error($2); }	| attribute_rbracket MORE_EQU	{ error($2); }
	| attribute_rbracket AND		{ error($2); }	| attribute_rbracket OR			{ error($2); }
	| attribute_rbracket ADD_ASSIGN{ error($2); }	| attribute_rbracket RBRACE		{ error($2); }
	| attribute_rbracket COLON		{ error($2); }
	| attribute_rbracket ASSIGN		{ error($2); }	| attribute_rbracket QUESTION	{ error($2); }
	| attribute_rbracket COMMA		{ error($2); }	| attribute_rbracket DOT 		{ error($2); }
	| attribute_rbracket CONSTANT	{ error($2); }	| attribute_rbracket IF			{ error($2); }
	| attribute_rbracket ELSE		{ error($2); }	| attribute_rbracket DO			{ error($2); }
	| attribute_rbracket FOR		{ error($2); }	| attribute_rbracket WHILE		{ error($2); }
	| attribute_rbracket BREAK		{ error($2); }	| attribute_rbracket RETURN		{ error($2); }
	| attribute_rbracket CONTINUE	{ error($2); }	| attribute_rbracket THIS		{ error($2); }
	| attribute_rbracket SUPER		{ error($2); }	| attribute_rbracket CONTAINER	{ error($2); }
	| attribute_rbracket NULL		{ error($2); }	| attribute_rbracket EXTENDS	{ error($2); }
	| attribute_rbracket NEW		{ error($2); }	| attribute_rbracket TRY		{ error($2); }
	| attribute_rbracket CATCH		{ error($2); }	| attribute_rbracket FINALLY	{ error($2); }
	| attribute_rbracket THROW		{ error($2); }
*/	;

attribute_name
	: attribute_lbracket
		{
			error("Identifier expected after this token");
		}
	| attribute_lbracket attribute_identifier
	;

attribute_value
	: attribute_name
	| attribute_name attribute_constant
	;
	
attribute
	: attribute_value
		{
			error("']' expected");
		}
	| attribute_value attribute_rbracket
	;

access
	: PUBLIC			{ m_parser.onToken($1); }
	| PRIVATE			{ m_parser.onToken($1); }
	| PROTECTED			{ m_parser.onToken($1); }
	| STATIC			{ m_parser.onToken($1); }
	| AUTO 				{ m_parser.onToken($1); }
	| VIRTUAL			{ m_parser.onToken($1); }
/*	| access NOT		{ error($2); }	| access MUL		{ error($2); }
	| access DIV		{ error($2); }	| access MOD		{ error($2); }
	| access ADD		{ error($2); }	| access SUB		{ error($2); }
	| access EQU		{ error($2); }	| access NOT_EQU	{ error($2); }
	| access LESS		{ error($2); }	| access MORE		{ error($2); }
	| access LESS_EQU	{ error($2); }	| access MORE_EQU	{ error($2); }
	| access AND		{ error($2); }	| access OR			{ error($2); }
	| access ADD_ASSIGN{ error($2); }	| access RBRACE		{ error($2); }
	| access COLON		{ error($2); }
	| access ASSIGN		{ error($2); }	| access QUESTION	{ error($2); }
	| access COMMA		{ error($2); }	| access DOT 		{ error($2); }
	| access CONSTANT	{ error($2); }	| access IF			{ error($2); }
	| access ELSE		{ error($2); }	| access DO			{ error($2); }
	| access FOR		{ error($2); }	| access WHILE		{ error($2); }
	| access BREAK		{ error($2); }	| access RETURN		{ error($2); }
	| access CONTINUE	{ error($2); }	| access THIS		{ error($2); }
	| access SUPER		{ error($2); }	| access CONTAINER	{ error($2); }
	| access NULL		{ error($2); }	| access EXTENDS	{ error($2); }
	| access NEW		{ error($2); }	| access TRY		{ error($2); }
	| access CATCH		{ error($2); }	| access FINALLY	{ error($2); }
	| access THROW		{ error($2); }
*/	;

modifier
	: access
	| attribute
	;

modifiers
	: modifier
		{
			m_parser.onModifiers(true);
		}
	| modifiers modifier
		{
			m_parser.onModifiers(false);
		}
	;

qualified_name
	: IDENTIFIER
		{
			m_parser.onQualifiedName(true, $1);
		}
	| qualified_name DOT IDENTIFIER
		{
			m_parser.onQualifiedName(false, $3);
		}
	| qualified_name DOT
		{
			error($2);
		}
	;

enum_keyword
	: ENUM
		{
			m_parser.startEnum($1);
		}
/*	| enum_keyword NOT			{ error($2); }	| enum_keyword MUL			{ error($2); }
	| enum_keyword DIV			{ error($2); }	| enum_keyword MOD			{ error($2); }
	| enum_keyword ADD			{ error($2); }	| enum_keyword SUB		{ error($2); }
	| enum_keyword EQU			{ error($2); }	| enum_keyword NOT_EQU		{ error($2); }
	| enum_keyword LESS			{ error($2); }	| enum_keyword MORE			{ error($2); }
	| enum_keyword LESS_EQU		{ error($2); }	| enum_keyword MORE_EQU		{ error($2); }
	| enum_keyword AND			{ error($2); }	| enum_keyword OR			{ error($2); }
	| enum_keyword GROUP		{ error($2); }	| enum_keyword ADD_ASSIGN	{ error($2); }
	| enum_keyword LBRACE		{ error($2); }	| enum_keyword RBRACE		{ error($2); }
	| enum_keyword BRACKETS		{ error($2); }	| enum_keyword COLON		{ error($2); }
	| enum_keyword SEMICOLON	{ error($2); }	| enum_keyword ASSIGN		{ error($2); }
	| enum_keyword QUESTION		{ error($2); }	| enum_keyword COMMA		{ error($2); }
	| enum_keyword DOT 			{ error($2); }	| enum_keyword CONSTANT		{ error($2); }
	| enum_keyword IF			{ error($2); }	| enum_keyword ELSE			{ error($2); }
	| enum_keyword DO			{ error($2); }	| enum_keyword FOR			{ error($2); }
	| enum_keyword WHILE		{ error($2); }	| enum_keyword BREAK		{ error($2); }
	| enum_keyword RETURN		{ error($2); }	| enum_keyword CONTINUE		{ error($2); }
	| enum_keyword THIS			{ error($2); }	| enum_keyword SUPER		{ error($2); }
	| enum_keyword CONTAINER	{ error($2); }	| enum_keyword NULL			{ error($2); }
	| enum_keyword EXTENDS		{ error($2); }	| enum_keyword RECORDS		{ error($2); }
	| enum_keyword NEW			{ error($2); }	| enum_keyword TRY			{ error($2); }
	| enum_keyword CATCH		{ error($2); }	| enum_keyword FINALLY		{ error($2); }
	| enum_keyword THROW		{ error($2); }
*/	;
	
enum_name
	: IDENTIFIER									
		{
			m_parser.setEnumName($1);
		}
/*	| enum_name NOT			{ error($2); }	| enum_name MUL			{ error($2); }
	| enum_name DIV			{ error($2); }	| enum_name MOD			{ error($2); }
	| enum_name ADD		{ error($2); }	| enum_name SUB		{ error($2); }
	| enum_name EQU			{ error($2); }	| enum_name NOT_EQU		{ error($2); }
	| enum_name LESS		{ error($2); }	| enum_name MORE		{ error($2); }
	| enum_name LESS_EQU	{ error($2); }	| enum_name MORE_EQU	{ error($2); }
	| enum_name AND			{ error($2); }	| enum_name OR			{ error($2); }
	| enum_name GROUP		{ error($2); }	| enum_name ADD_ASSIGN	{ error($2); }
	| enum_name LBRACE		{ error($2); }	| enum_name RBRACE		{ error($2); }
	| enum_name COLON		{ error($2); }
	| enum_name SEMICOLON	{ error($2); }	| enum_name ASSIGN		{ error($2); }
	| enum_name QUESTION	{ error($2); }	| enum_name DOT 		{ error($2); }
	| enum_name CONSTANT	{ error($2); }	| enum_name IF			{ error($2); }
	| enum_name ELSE		{ error($2); }	| enum_name DO			{ error($2); }
	| enum_name FOR			{ error($2); }	| enum_name WHILE		{ error($2); }
	| enum_name BREAK		{ error($2); }	| enum_name RETURN		{ error($2); }
	| enum_name CONTINUE	{ error($2); }	| enum_name THIS		{ error($2); }
	| enum_name SUPER		{ error($2); }	| enum_name CONTAINER	{ error($2); }
	| enum_name NULL		{ error($2); }	| enum_name EXTENDS		{ error($2); }
	| enum_name RECORDS		{ error($2); }	| enum_name NEW			{ error($2); }
	| enum_name TRY			{ error($2); }	| enum_name CATCH		{ error($2); }
	| enum_name FINALLY		{ error($2); }	| enum_name THROW		{ error($2); }
*/	;

simple_enum_header
	: enum_keyword
		{
			error("Identifier expected after this token");
		}
	| enum_keyword enum_name
	;

enum_header
	: simple_enum_header
	| modifiers simple_enum_header
		{
			m_parser.applyModifiers();
		}
	;

enum
	: enum_header
		{
			error("EnumBody is missing");
		}
	
	| enum_header enum_body
	;

enum_body_lcbrace
	: LCBRACE
		{
			m_parser.startEnumBody($1);
		}
/*	| enum_body_lcbrace NOT			{ error($2); }	| enum_body_lcbrace MUL			{ error($2); }
	| enum_body_lcbrace DIV			{ error($2); }	| enum_body_lcbrace MOD			{ error($2); }
	| enum_body_lcbrace ADD		{ error($2); }	| enum_body_lcbrace SUB		{ error($2); }
	| enum_body_lcbrace EQU			{ error($2); }	| enum_body_lcbrace NOT_EQU		{ error($2); }
	| enum_body_lcbrace LESS		{ error($2); }	| enum_body_lcbrace MORE		{ error($2); }
	| enum_body_lcbrace LESS_EQU	{ error($2); }	| enum_body_lcbrace MORE_EQU	{ error($2); }
	| enum_body_lcbrace AND			{ error($2); }	| enum_body_lcbrace OR			{ error($2); }
	| enum_body_lcbrace ADD_ASSIGN	{ error($2); }	| enum_body_lcbrace LBRACE		{ error($2); }
	| enum_body_lcbrace RBRACE		{ error($2); }	| enum_body_lcbrace COLON		{ error($2); }
	| enum_body_lcbrace ASSIGN		{ error($2); }	| enum_body_lcbrace QUESTION	{ error($2); }
	| enum_body_lcbrace COMMA		{ error($2); }	| enum_body_lcbrace DOT 		{ error($2); }
	| enum_body_lcbrace CONSTANT	{ error($2); }	| enum_body_lcbrace IF			{ error($2); }
	| enum_body_lcbrace ELSE		{ error($2); }	| enum_body_lcbrace DO			{ error($2); }
	| enum_body_lcbrace FOR			{ error($2); }	| enum_body_lcbrace WHILE		{ error($2); }
	| enum_body_lcbrace BREAK		{ error($2); }	| enum_body_lcbrace RETURN		{ error($2); }
	| enum_body_lcbrace CONTINUE	{ error($2); }	| enum_body_lcbrace THIS		{ error($2); }
	| enum_body_lcbrace SUPER		{ error($2); }	| enum_body_lcbrace CONTAINER	{ error($2); }
	| enum_body_lcbrace NULL		{ error($2); }	| enum_body_lcbrace EXTENDS		{ error($2); }
	| enum_body_lcbrace NEW			{ error($2); }	| enum_body_lcbrace TRY			{ error($2); }
	| enum_body_lcbrace CATCH		{ error($2); }	| enum_body_lcbrace FINALLY		{ error($2); }
	| enum_body_lcbrace THROW		{ error($2); }
*/	;
	
enum_identifier
	: IDENTIFIER										
		{
			m_parser.addEnumMember($1);
		}
/*	| enum_identifier NOT			{ error($2); }	| enum_identifier MUL			{ error($2); }
	| enum_identifier DIV			{ error($2); }	| enum_identifier MOD			{ error($2); }
	| enum_identifier ADD			{ error($2); }	| enum_identifier SUB			{ error($2); }
	| enum_identifier EQU			{ error($2); }	| enum_identifier NOT_EQU		{ error($2); }
	| enum_identifier LESS			{ error($2); }	| enum_identifier MORE			{ error($2); }
	| enum_identifier LESS_EQU		{ error($2); }	| enum_identifier MORE_EQU		{ error($2); }
	| enum_identifier AND			{ error($2); }	| enum_identifier OR			{ error($2); }
	| enum_identifier GROUP			{ error($2); }	| enum_identifier ADD_ASSIGN	{ error($2); }
	| enum_identifier LBRACE		{ error($2); }	| enum_identifier RBRACE		{ error($2); }
	| enum_identifier LCBRACE		{ error($2); }	| enum_identifier COLON			{ error($2); }
	| enum_identifier SEMICOLON		{ error($2); }	| enum_identifier ASSIGN		{ error($2); }
	| enum_identifier QUESTION		{ error($2); }	| enum_identifier DOT 			{ error($2); }
	| enum_identifier CONSTANT		{ error($2); }	| enum_identifier IF			{ error($2); }
	| enum_identifier ELSE			{ error($2); }	| enum_identifier DO			{ error($2); }
	| enum_identifier FOR			{ error($2); }	| enum_identifier WHILE			{ error($2); }
	| enum_identifier BREAK			{ error($2); }	| enum_identifier RETURN		{ error($2); }
	| enum_identifier CONTINUE		{ error($2); }	| enum_identifier THIS			{ error($2); }
	| enum_identifier SUPER			{ error($2); }	| enum_identifier CONTAINER		{ error($2); }
	| enum_identifier NULL			{ error($2); }	| enum_identifier EXTENDS		{ error($2); }
	| enum_identifier RECORDS		{ error($2); }	| enum_identifier NEW			{ error($2); }
	| enum_identifier TRY			{ error($2); }	| enum_identifier CATCH			{ error($2); }
	| enum_identifier FINALLY		{ error($2); }	| enum_identifier THROW			{ error($2); }
*/	;
	
enum_comma
	: COMMA										
/*	| enum_comma NOT			{ error($2); }	| enum_comma MUL			{ error($2); }
	| enum_comma DIV			{ error($2); }	| enum_comma MOD			{ error($2); }
	| enum_comma ADD			{ error($2); }	| enum_comma SUB			{ error($2); }
	| enum_comma EQU			{ error($2); }	| enum_comma NOT_EQU		{ error($2); }
	| enum_comma LESS			{ error($2); }	| enum_comma MORE			{ error($2); }
	| enum_comma LESS_EQU		{ error($2); }	| enum_comma MORE_EQU		{ error($2); }
	| enum_comma AND			{ error($2); }	| enum_comma OR				{ error($2); }
	| enum_comma GROUP			{ error($2); }	| enum_comma ADD_ASSIGN	{ error($2); }
	| enum_comma LBRACE			{ error($2); }	| enum_comma RBRACE			{ error($2); }
	| enum_comma LBRACKET		{ error($2); }	| enum_comma RBRACKET		{ error($2); }
	| enum_comma LCBRACE		{ error($2); }	| enum_comma COLON			{ error($2); }
	| enum_comma SEMICOLON		{ error($2); }	| enum_comma ASSIGN			{ error($2); }
	| enum_comma QUESTION		{ error($2); }	| enum_comma DOT 			{ error($2); }
	| enum_comma COMMA			{ error($2); }
	| enum_comma CONSTANT		{ error($2); }	| enum_comma IF				{ error($2); }
	| enum_comma ELSE			{ error($2); }	| enum_comma DO				{ error($2); }
	| enum_comma FOR			{ error($2); }	| enum_comma WHILE			{ error($2); }
	| enum_comma BREAK			{ error($2); }	| enum_comma RETURN			{ error($2); }
	| enum_comma CONTINUE		{ error($2); }	| enum_comma THIS			{ error($2); }
	| enum_comma SUPER			{ error($2); }	| enum_comma CONTAINER		{ error($2); }
	| enum_comma NULL			{ error($2); }	| enum_comma EXTENDS		{ error($2); }
	| enum_comma RECORDS		{ error($2); }	| enum_comma NEW			{ error($2); }
	| enum_comma TRY			{ error($2); }	| enum_comma CATCH			{ error($2); }
	| enum_comma FINALLY		{ error($2); }	| enum_comma THROW			{ error($2); }
*/	;

enum_members
	: enum_identifier										
	| enum_members enum_comma enum_identifier
	| enum_members enum_comma
		{
			error((String)null);
		}
	| enum_members enum_identifier
		{
			error("',' expected before this token");
		}
	;

enum_body_left
	: enum_body_lcbrace
	| enum_body_lcbrace enum_members
	;

enum_body
	: enum_body_left
		{
			error("'}' expected after this token");
		}
	| enum_body_left RCBRACE
		{
			m_parser.finishEnumBody($2);
		}
	;

class_keyword
	: CLASS
		{
			m_parser.startClass($1);
		}
/*	| class_keyword NOT			{ error($2); }	| class_keyword MUL			{ error($2); }
	| class_keyword DIV			{ error($2); }	| class_keyword MOD			{ error($2); }
	| class_keyword ADD		{ error($2); }	| class_keyword SUB		{ error($2); }
	| class_keyword EQU			{ error($2); }	| class_keyword NOT_EQU		{ error($2); }
	| class_keyword LESS		{ error($2); }	| class_keyword MORE		{ error($2); }
	| class_keyword LESS_EQU	{ error($2); }	| class_keyword MORE_EQU	{ error($2); }
	| class_keyword AND			{ error($2); }	| class_keyword OR			{ error($2); }
	| class_keyword GROUP		{ error($2); }	| class_keyword ADD_ASSIGN	{ error($2); }
	| class_keyword LBRACE		{ error($2); }	| class_keyword RBRACE		{ error($2); }
	| class_keyword BRACKETS	{ error($2); }	| class_keyword COLON		{ error($2); }
	| class_keyword ASSIGN		{ error($2); }
	| class_keyword QUESTION	{ error($2); }	| class_keyword COMMA		{ error($2); }
	| class_keyword DOT 		{ error($2); }	| class_keyword CONSTANT	{ error($2); }
	| class_keyword IF			{ error($2); }	| class_keyword ELSE		{ error($2); }
	| class_keyword DO			{ error($2); }	| class_keyword FOR			{ error($2); }
	| class_keyword WHILE		{ error($2); }	| class_keyword BREAK		{ error($2); }
	| class_keyword RETURN		{ error($2); }	| class_keyword CONTINUE	{ error($2); }
	| class_keyword THIS		{ error($2); }	| class_keyword SUPER		{ error($2); }
	| class_keyword CONTAINER	{ error($2); }	| class_keyword NULL		{ error($2); }
	| class_keyword NEW			{ error($2); }	| class_keyword TRY			{ error($2); }
	| class_keyword CATCH		{ error($2); }	| class_keyword FINALLY		{ error($2); }
	| class_keyword THROW		{ error($2); }
*/	;

class_name
	: IDENTIFIER									
		{
			m_parser.setClassName($1);
		}
/*	| class_name NOT			{ error($2); }	| class_name MUL			{ error($2); }
	| class_name DIV			{ error($2); }	| class_name MOD			{ error($2); }
	| class_name ADD			{ error($2); }	| class_name SUB			{ error($2); }
	| class_name EQU			{ error($2); }	| class_name NOT_EQU		{ error($2); }
	| class_name LESS			{ error($2); }	| class_name MORE			{ error($2); }
	| class_name LESS_EQU		{ error($2); }	| class_name MORE_EQU		{ error($2); }
	| class_name AND			{ error($2); }	| class_name OR				{ error($2); }
	| class_name GROUP			{ error($2); }	| class_name ADD_ASSIGN	{ error($2); }
	| class_name LBRACE			{ error($2); }	| class_name RBRACE			{ error($2); }
	| class_name COLON			{ error($2); }	| class_name IDENTIFIER		{ error($2); }
	| class_name SEMICOLON		{ error($2); }	| class_name ASSIGN			{ error($2); }
	| class_name QUESTION		{ error($2); }	| class_name DOT 			{ error($2); }
	| class_name CONSTANT		{ error($2); }	| class_name IF				{ error($2); }
	| class_name ELSE			{ error($2); }	| class_name DO				{ error($2); }
	| class_name FOR			{ error($2); }	| class_name WHILE			{ error($2); }
	| class_name BREAK			{ error($2); }	| class_name RETURN			{ error($2); }
	| class_name CONTINUE		{ error($2); }	| class_name THIS			{ error($2); }
	| class_name SUPER			{ error($2); }	| class_name CONTAINER		{ error($2); }
	| class_name NULL			{ error($2); } 	| class_name COMMA			{ error($2); }
	| class_name RECORDS		{ error($2); }	| class_name NEW			{ error($2); }
	| class_name TRY			{ error($2); }	| class_name CATCH			{ error($2); }
	| class_name FINALLY		{ error($2); }	| class_name THROW			{ error($2); }
*/	;

extends_keyword
	: EXTENDS
/*	| extends_keyword NOT		{ error($2); }	| extends_keyword MUL		{ error($2); }
	| extends_keyword DIV		{ error($2); }	| extends_keyword MOD		{ error($2); }
	| extends_keyword ADD		{ error($2); }	| extends_keyword SUB		{ error($2); }
	| extends_keyword EQU		{ error($2); }	| extends_keyword NOT_EQU	{ error($2); }
	| extends_keyword LESS		{ error($2); }	| extends_keyword MORE		{ error($2); }
	| extends_keyword LESS_EQU	{ error($2); }	| extends_keyword MORE_EQU	{ error($2); }
	| extends_keyword AND		{ error($2); }	| extends_keyword OR		{ error($2); }
	| extends_keyword GROUP		{ error($2); }	| extends_keyword ADD_ASSIGN{ error($2); }
	| extends_keyword LBRACE	{ error($2); }	| extends_keyword RBRACE	{ error($2); }
	| extends_keyword BRACKETS	{ error($2); }	| extends_keyword COLON		{ error($2); }
	| extends_keyword SEMICOLON	{ error($2); }	| extends_keyword ASSIGN	{ error($2); }
	| extends_keyword QUESTION	{ error($2); }	| extends_keyword COMMA		{ error($2); }
	| extends_keyword DOT 		{ error($2); }	| extends_keyword CONSTANT	{ error($2); }
	| extends_keyword IF		{ error($2); }	| extends_keyword ELSE		{ error($2); }
	| extends_keyword DO		{ error($2); }	| extends_keyword FOR		{ error($2); }
	| extends_keyword WHILE		{ error($2); }	| extends_keyword BREAK		{ error($2); }
	| extends_keyword RETURN	{ error($2); }	| extends_keyword CONTINUE	{ error($2); }
	| extends_keyword THIS		{ error($2); }	| extends_keyword SUPER		{ error($2); }
	| extends_keyword CONTAINER	{ error($2); }	| extends_keyword NULL		{ error($2); }
	| extends_keyword NEW		{ error($2); }	| extends_keyword TRY		{ error($2); }
	| extends_keyword CATCH		{ error($2); }	| extends_keyword FINALLY	{ error($2); }
	| extends_keyword THROW		{ error($2); }
*/	;

base_name
	: IDENTIFIER									
		{
			m_parser.setClassBase($1);
		}
/*	| base_name NOT				{ error($2); }	| base_name MUL			{ error($2); }
	| base_name DIV				{ error($2); }	| base_name MOD			{ error($2); }
	| base_name ADD			{ error($2); }	| base_name SUB		{ error($2); }
	| base_name EQU				{ error($2); }	| base_name NOT_EQU		{ error($2); }
	| base_name LESS			{ error($2); }	| base_name MORE		{ error($2); }
	| base_name LESS_EQU		{ error($2); }	| base_name MORE_EQU	{ error($2); }
	| base_name AND				{ error($2); }	| base_name OR			{ error($2); }
	| base_name GROUP			{ error($2); }	| base_name ADD_ASSIGN	{ error($2); }
	| base_name LBRACE			{ error($2); }	| base_name RBRACE		{ error($2); }
	| base_name COLON			{ error($2); }	| base_name COMMA		{ error($2); }
	| base_name SEMICOLON		{ error($2); }	| base_name ASSIGN		{ error($2); }
	| base_name QUESTION		{ error($2); }	| base_name DOT 		{ error($2); }
	| base_name CONSTANT		{ error($2); }	| base_name IF			{ error($2); }
	| base_name ELSE			{ error($2); }	| base_name DO			{ error($2); }
	| base_name FOR				{ error($2); }	| base_name WHILE		{ error($2); }
	| base_name BREAK			{ error($2); }	| base_name RETURN		{ error($2); }
	| base_name CONTINUE		{ error($2); }	| base_name THIS		{ error($2); }
	| base_name SUPER			{ error($2); }	| base_name CONTAINER	{ error($2); }
	| base_name NULL			{ error($2); }	| base_name EXTENDS		{ error($2); }
	| base_name RECORDS			{ error($2); }	| base_name NEW			{ error($2); }
	| base_name TRY				{ error($2); }	| base_name CATCH		{ error($2); }
	| base_name FINALLY			{ error($2); }	| base_name THROW		{ error($2); }
	| base_name IDENTIFIER		{ error($2); }
*/	;

simple_class_header
	: class_keyword
		{
			error("Identifier expected after this token");
		}
	| class_keyword class_name
	;
	
extended_class_header
	: simple_class_header
	| simple_class_header extends_keyword
		{
			error("Identifier expected after this token");
		}
	| simple_class_header extends_keyword base_name
	;
	
class_header
	: extended_class_header
	| modifiers extended_class_header
		{
			m_parser.applyModifiers();
		}
	;

class
	: class_header
		{
			error("ClassBody is missing");
		}
	| class_header class_body
		{
			m_parser.finishClass();
		}
	;

noname_class
	: class_keyword
		{
			error("ClassBody is missing");
		}
	| class_keyword class_body
		{
			m_parser.finishClass();
		}
	;

class_body_lcbrace
	: LCBRACE
		{
			m_parser.startClassBody($1);
		}
/*	| class_body_lcbrace NOT		{ error($2); }	| class_body_lcbrace MUL		{ error($2); }
	| class_body_lcbrace DIV		{ error($2); }	| class_body_lcbrace MOD		{ error($2); }
	| class_body_lcbrace ADD		{ error($2); }	| class_body_lcbrace SUB		{ error($2); }
	| class_body_lcbrace EQU		{ error($2); }	| class_body_lcbrace NOT_EQU	{ error($2); }
	| class_body_lcbrace LESS		{ error($2); }	| class_body_lcbrace MORE		{ error($2); }
	| class_body_lcbrace LESS_EQU	{ error($2); }	| class_body_lcbrace MORE_EQU	{ error($2); }
	| class_body_lcbrace AND		{ error($2); }	| class_body_lcbrace OR			{ error($2); }
	| class_body_lcbrace ADD_ASSIGN{ error($2); }	
	| class_body_lcbrace RBRACE		{ error($2); }	| class_body_lcbrace COLON		{ error($2); }
	| class_body_lcbrace ASSIGN		{ error($2); }	| class_body_lcbrace QUESTION	{ error($2); }
	| class_body_lcbrace COMMA		{ error($2); }	| class_body_lcbrace DOT 		{ error($2); }
	| class_body_lcbrace CONSTANT	{ error($2); }	| class_body_lcbrace IF			{ error($2); }
	| class_body_lcbrace ELSE		{ error($2); }	| class_body_lcbrace DO			{ error($2); }
	| class_body_lcbrace FOR		{ error($2); }	| class_body_lcbrace WHILE		{ error($2); }
	| class_body_lcbrace BREAK		{ error($2); }	| class_body_lcbrace RETURN		{ error($2); }
	| class_body_lcbrace CONTINUE	{ error($2); }	| class_body_lcbrace THIS		{ error($2); }
	| class_body_lcbrace SUPER		{ error($2); }	| class_body_lcbrace CONTAINER	{ error($2); }
	| class_body_lcbrace NULL		{ error($2); }	| class_body_lcbrace EXTENDS	{ error($2); }
	| class_body_lcbrace NEW		{ error($2); }	| class_body_lcbrace TRY		{ error($2); }
	| class_body_lcbrace CATCH		{ error($2); }	| class_body_lcbrace FINALLY	{ error($2); }
	| class_body_lcbrace THROW		{ error($2); }
*/	;

class_body_left
	: class_body_lcbrace
	| class_body_lcbrace class_members
	;

class_body
	: class_body_left
		{
			error("'}' expected after this token");
		}
	| class_body_left RCBRACE
		{
			m_parser.finishClassBody($2);
		}
	;

declarator_semicolon
	: SEMICOLON
/*	| declarator_semicolon NOT			{ error($2); }	| declarator_semicolon MUL			{ error($2); }
	| declarator_semicolon DIV			{ error($2); }	| declarator_semicolon MOD			{ error($2); }
	| declarator_semicolon ADD			{ error($2); }	| declarator_semicolon SUB		{ error($2); }
	| declarator_semicolon EQU			{ error($2); }	| declarator_semicolon NOT_EQU		{ error($2); }
	| declarator_semicolon LESS			{ error($2); }	| declarator_semicolon MORE			{ error($2); }
	| declarator_semicolon LESS_EQU		{ error($2); }	| declarator_semicolon MORE_EQU		{ error($2); }
	| declarator_semicolon AND			{ error($2); }	| declarator_semicolon OR			{ error($2); }
	| declarator_semicolon ADD_ASSIGN	{ error($2); }	| declarator_semicolon RBRACE		{ error($2); }
	| declarator_semicolon RBRACKET		{ error($2); }	| declarator_semicolon COLON		{ error($2); }
	| declarator_semicolon ASSIGN		{ error($2); }	| declarator_semicolon CONSTANT		{ error($2); }
	| declarator_semicolon QUESTION		{ error($2); }	| declarator_semicolon COMMA		{ error($2); }
	| declarator_semicolon DOT 			{ error($2); }	| declarator_semicolon IF			{ error($2); }
	| declarator_semicolon ELSE			{ error($2); }	| declarator_semicolon DO			{ error($2); }
	| declarator_semicolon FOR			{ error($2); }	| declarator_semicolon WHILE		{ error($2); }
	| declarator_semicolon BREAK		{ error($2); }	| declarator_semicolon RETURN		{ error($2); }
	| declarator_semicolon CONTINUE		{ error($2); }	| declarator_semicolon THIS			{ error($2); }
	| declarator_semicolon SUPER		{ error($2); }	| declarator_semicolon CONTAINER	{ error($2); }
	| declarator_semicolon NULL			{ error($2); }	| declarator_semicolon EXTENDS		{ error($2); }
	| declarator_semicolon NEW			{ error($2); }
	| declarator_semicolon TRY			{ error($2); }	| declarator_semicolon CATCH		{ error($2); }
	| declarator_semicolon FINALLY		{ error($2); }	| declarator_semicolon THROW		{ error($2); }
*/	;

class_member1
	: declarator										
		{
			error("insert ';' to complete MemberDeclaration");
			m_parser.createMemberDeclarator();
		}
	| declarator declarator_semicolon
		{
			m_parser.createMemberDeclarator();
		}
	| method
	| records
;
	
class_member2
	: class_member1
	| modifiers class_member1
		{
			m_parser.applyModifiers();
		}
	;
	
class_member
	: class_member2						{ m_parser.addClassMember(); }	
	;
	
class_members
	: class_member
	| class_members class_member
	;

records_keyword
	: RECORDS
		{
			m_parser.startRecords($1);
		}
/*	| records_keyword NOT		{ error($2); }	| records_keyword MUL		{ error($2); }
	| records_keyword DIV		{ error($2); }	| records_keyword MOD		{ error($2); }
	| records_keyword ADD		{ error($2); }	| records_keyword SUB		{ error($2); }
	| records_keyword EQU		{ error($2); }	| records_keyword NOT_EQU	{ error($2); }
	| records_keyword LESS		{ error($2); }	| records_keyword MORE		{ error($2); }
	| records_keyword LESS_EQU	{ error($2); }	| records_keyword MORE_EQU	{ error($2); }
	| records_keyword AND		{ error($2); }	| records_keyword OR		{ error($2); }
	| records_keyword ADD_ASSIGN{ error($2); }	| records_keyword RBRACE	{ error($2); }
	| records_keyword COLON		{ error($2); }
	| records_keyword ASSIGN	{ error($2); }	| records_keyword QUESTION	{ error($2); }
	| records_keyword COMMA		{ error($2); }	| records_keyword DOT 		{ error($2); }
	| records_keyword CONSTANT	{ error($2); }	| records_keyword IF		{ error($2); }
	| records_keyword ELSE		{ error($2); }	| records_keyword DO		{ error($2); }
	| records_keyword FOR		{ error($2); }	| records_keyword WHILE		{ error($2); }
	| records_keyword BREAK		{ error($2); }	| records_keyword RETURN	{ error($2); }
	| records_keyword CONTINUE	{ error($2); }	| records_keyword THIS		{ error($2); }
	| records_keyword SUPER		{ error($2); }	| records_keyword CONTAINER	{ error($2); }
	| records_keyword NULL		{ error($2); }	| records_keyword EXTENDS	{ error($2); }
	| records_keyword NEW		{ error($2); }	| records_keyword TRY		{ error($2); }
	| records_keyword CATCH		{ error($2); }	| records_keyword FINALLY	{ error($2); }
	| records_keyword THROW		{ error($2); }
*/	;

records
	: records_keyword
		{
			error("RecordsBody is missing");
		}
	
	| records_keyword records_body
		{
			m_parser.finishRecords();
		}
	;
	
records_lcbrace
	: LCBRACE
		{
			m_parser.startRecordsBody($1);
		}
/*	| records_lcbrace NOT		{ error($2); }	| records_lcbrace MUL		{ error($2); }
	| records_lcbrace DIV		{ error($2); }	| records_lcbrace MOD		{ error($2); }
	| records_lcbrace ADD		{ error($2); }	| records_lcbrace SUB		{ error($2); }
	| records_lcbrace EQU		{ error($2); }	| records_lcbrace NOT_EQU	{ error($2); }
	| records_lcbrace LESS		{ error($2); }	| records_lcbrace MORE		{ error($2); }
	| records_lcbrace LESS_EQU	{ error($2); }	| records_lcbrace MORE_EQU	{ error($2); }
	| records_lcbrace AND		{ error($2); }	| records_lcbrace OR		{ error($2); }
	| records_lcbrace ADD_ASSIGN{ error($2); }	
	| records_lcbrace RBRACE	{ error($2); }	| records_lcbrace COLON		{ error($2); }
	| records_lcbrace ASSIGN	{ error($2); }	| records_lcbrace QUESTION	{ error($2); }
	| records_lcbrace COMMA		{ error($2); }	| records_lcbrace DOT 		{ error($2); }
	| records_lcbrace CONSTANT	{ error($2); }	| records_lcbrace IF		{ error($2); }
	| records_lcbrace ELSE		{ error($2); }	| records_lcbrace DO		{ error($2); }
	| records_lcbrace FOR		{ error($2); }	| records_lcbrace WHILE		{ error($2); }
	| records_lcbrace BREAK		{ error($2); }	| records_lcbrace RETURN	{ error($2); }
	| records_lcbrace CONTINUE	{ error($2); }	| records_lcbrace THIS		{ error($2); }
	| records_lcbrace SUPER		{ error($2); }	| records_lcbrace CONTAINER	{ error($2); }
	| records_lcbrace NULL		{ error($2); }	| records_lcbrace EXTENDS	{ error($2); }
	| records_lcbrace NEW		{ error($2); }	| records_lcbrace TRY		{ error($2); }
	| records_lcbrace CATCH		{ error($2); }	| records_lcbrace FINALLY	{ error($2); }
	| records_lcbrace THROW		{ error($2); }
*/	;	

records_body_left
	: records_lcbrace
	| records_lcbrace records_list
	;
	
records_body
	: records_body_left
		{
			error("'}' expected after this token");
		}
	| records_body_left RCBRACE
		{
			m_parser.finishRecordsBody($2);
		}
	;

record_name
	: IDENTIFIER
		{
			m_parser.startRecord($1);
		}
/*	| record_name NOT			{ error($2); }	| record_name MUL			{ error($2); }
	| record_name DIV			{ error($2); }	| record_name MOD			{ error($2); }
	| record_name ADD			{ error($2); }	| record_name SUB			{ error($2); }
	| record_name EQU			{ error($2); }	| record_name NOT_EQU		{ error($2); }
	| record_name LESS			{ error($2); }	| record_name MORE			{ error($2); }
	| record_name LESS_EQU		{ error($2); }	| record_name MORE_EQU		{ error($2); }
	| record_name AND			{ error($2); }	| record_name OR			{ error($2); }
	| record_name ADD_ASSIGN	{ error($2); }	| record_name RBRACE		{ error($2); }
	| record_name RBRACKET		{ error($2); }	| record_name COLON			{ error($2); }
	| record_name QUESTION		{ error($2); }	| record_name COMMA			{ error($2); }
	| record_name DOT 			{ error($2); }	| record_name IDENTIFIER	{ error($2); }
	| record_name IF			{ error($2); }	| record_name ELSE			{ error($2); }
	| record_name DO			{ error($2); }	| record_name FOR			{ error($2); }
	| record_name WHILE			{ error($2); }	| record_name BREAK			{ error($2); }
	| record_name RETURN		{ error($2); }	| record_name CONTINUE		{ error($2); }
	| record_name THIS			{ error($2); }	| record_name SUPER			{ error($2); }
	| record_name CONTAINER		{ error($2); }	| record_name NULL			{ error($2); }
	| record_name EXTENDS		{ error($2); }	| record_name ENUM			{ error($2); }
	| record_name NEW			{ error($2); }	| record_name TRY			{ error($2); }
	| record_name CATCH			{ error($2); }	| record_name FINALLY		{ error($2); }
	| record_name THROW			{ error($2); }
*/	;
		
record_assign		
	: ASSIGN
/*	| record_assign NOT			{ error($2); }	| record_assign MUL			{ error($2); }
	| record_assign DIV			{ error($2); }	| record_assign MOD			{ error($2); }
	| record_assign ADD		{ error($2); }	| record_assign SUB		{ error($2); }
	| record_assign EQU			{ error($2); }	| record_assign NOT_EQU		{ error($2); }
	| record_assign LESS		{ error($2); }	| record_assign MORE		{ error($2); }
	| record_assign LESS_EQU	{ error($2); }	| record_assign MORE_EQU	{ error($2); }
	| record_assign AND			{ error($2); }	| record_assign OR			{ error($2); }
	| record_assign ADD_ASSIGN	{ error($2); }	| record_assign ASSIGN		{ error($2); }
	| record_assign RBRACE		{ error($2); }
	| record_assign RBRACKET	{ error($2); }	| record_assign COLON		{ error($2); }
	| record_assign QUESTION	{ error($2); }	| record_assign COMMA		{ error($2); }
	| record_assign DOT 		{ error($2); }	| record_assign IDENTIFIER	{ error($2); }
	| record_assign IF			{ error($2); }	| record_assign ELSE		{ error($2); }
	| record_assign DO			{ error($2); }	| record_assign FOR			{ error($2); }
	| record_assign WHILE		{ error($2); }	| record_assign BREAK		{ error($2); }
	| record_assign RETURN		{ error($2); }	| record_assign CONTINUE	{ error($2); }
	| record_assign THIS		{ error($2); }	| record_assign SUPER		{ error($2); }
	| record_assign CONTAINER	{ error($2); }	| record_assign NULL		{ error($2); }
	| record_assign EXTENDS		{ error($2); }	| record_assign ENUM		{ error($2); }
	| record_assign NEW			{ error($2); }	| record_assign TRY			{ error($2); }
	| record_assign CATCH		{ error($2); }	| record_assign FINALLY		{ error($2); }
	| record_assign THROW		{ error($2); }
*/	;

simple_record
	: record_name
		{
			error("'=' expected after this token");
		}
	| record_name record_assign
		{
			error("Constant expected after this token");
		}
	| record_name CONSTANT
		{
			error("'=' expected after this token");
			m_parser.setRecordValue($2);
		}
	| record_name record_assign CONSTANT
		{
			m_parser.setRecordValue($3);
		}
	;
	
record1
	: simple_record
	| modifiers simple_record
		{
			m_parser.applyModifiers();
		}
	;

record_semicolon
	: SEMICOLON
/*	| record_semicolon NOT			{ error($2); }	| record_semicolon MUL			{ error($2); }
	| record_semicolon DIV			{ error($2); }	| record_semicolon MOD			{ error($2); }
	| record_semicolon ADD			{ error($2); }	| record_semicolon SUB		{ error($2); }
	| record_semicolon EQU			{ error($2); }	| record_semicolon NOT_EQU		{ error($2); }
	| record_semicolon LESS			{ error($2); }	| record_semicolon MORE			{ error($2); }
	| record_semicolon LESS_EQU		{ error($2); }	| record_semicolon MORE_EQU		{ error($2); }
	| record_semicolon AND			{ error($2); }	| record_semicolon OR			{ error($2); }
	| record_semicolon ADD_ASSIGN	{ error($2); }	| record_semicolon RBRACE		{ error($2); }
	| record_semicolon RBRACKET		{ error($2); }	| record_semicolon COLON		{ error($2); }
	| record_semicolon ASSIGN		{ error($2); }	| record_semicolon CONSTANT		{ error($2); }
	| record_semicolon QUESTION		{ error($2); }	| record_semicolon COMMA		{ error($2); }
	| record_semicolon DOT 			{ error($2); }	| record_semicolon IF			{ error($2); }
	| record_semicolon ELSE			{ error($2); }	| record_semicolon DO			{ error($2); }
	| record_semicolon FOR			{ error($2); }	| record_semicolon WHILE		{ error($2); }
	| record_semicolon BREAK		{ error($2); }	| record_semicolon RETURN		{ error($2); }
	| record_semicolon CONTINUE		{ error($2); }	| record_semicolon THIS			{ error($2); }
	| record_semicolon SUPER		{ error($2); }	| record_semicolon CONTAINER	{ error($2); }
	| record_semicolon NULL			{ error($2); }	| record_semicolon EXTENDS		{ error($2); }
	| record_semicolon NEW			{ error($2); }
	| record_semicolon TRY			{ error($2); }	| record_semicolon CATCH		{ error($2); }
	| record_semicolon FINALLY		{ error($2); }	| record_semicolon THROW		{ error($2); }
*/	;
			
record
	: record1
		{
			error("';' expected after this token");
		}
	| record1 record_semicolon
	;

records_list
	: record						{ m_parser.addRecord(); }
	| records_list record			{ m_parser.addRecord(); }
	;
			
type	
	: qualified_name
		{
			m_parser.onTypeHelper(false);
		}
	| simple_array_access
/*	| type NOT			{ error($2); }	| type MUL			{ error($2); }
	| type DIV			{ error($2); }	| type MOD			{ error($2); }
	| type ADD			{ error($2); }	| type SUB		{ error($2); }
	| type EQU			{ error($2); }	| type NOT_EQU		{ error($2); }
	| type LESS			{ error($2); }	| type MORE			{ error($2); }		
	| type LESS_EQU		{ error($2); }	| type MORE_EQU		{ error($2); }
	| type AND			{ error($2); }	| type OR			{ error($2); }
	| type COLON		{ error($2); }	| type QUESTION		{ error($2); }
	| type CONSTANT		{ error($2); }	| type RBRACKET		{ error($2); }
	| type IF			{ error($2); }	| type ELSE			{ error($2); }
	| type DO			{ error($2); }	| type FOR			{ error($2); }
	| type WHILE		{ error($2); }	| type BREAK		{ error($2); }
	| type RETURN		{ error($2); }	| type CONTINUE		{ error($2); }
	| type THIS			{ error($2); }	| type SUPER		{ error($2); }
	| type CONTAINER	{ error($2); }	| type NULL			{ error($2); }
	| type EXTENDS		{ error($2); }	| type TRY			{ error($2); }
	| type CATCH		{ error($2); }	| type FINALLY		{ error($2); }
	| type THROW		{ error($2); }
*/	;

simple_declarator
	: type							{ m_parser.onSimpleDeclarator(false); }
	| type type						{ m_parser.onSimpleDeclarator(true); }
	;
	
initialization_left
	: simple_declarator ASSIGN		{ m_parser.setInitOperator($2); }
	| simple_declarator ADD_ASSIGN	{ m_parser.setInitOperator($2); }
/*	| initialization_left MUL		{ error($2); }
	| initialization_left DIV		{ error($2); }	| initialization_left MOD		{ error($2); }
	| initialization_left EQU		{ error($2); }	| initialization_left NOT_EQU	{ error($2); }
	| initialization_left LESS		{ error($2); }	| initialization_left MORE		{ error($2); }		
	| initialization_left LESS_EQU	{ error($2); }	| initialization_left MORE_EQU	{ error($2); }
	| initialization_left AND		{ error($2); }	| initialization_left OR		{ error($2); }
	| initialization_left COLON		{ error($2); }	| initialization_left QUESTION	{ error($2); }
	| initialization_left DOT 		{ error($2); }	| initialization_left IF		{ error($2); }
	| initialization_left ELSE		{ error($2); }	| initialization_left DO		{ error($2); }
	| initialization_left FOR		{ error($2); }
	| initialization_left WHILE		{ error($2); }	| initialization_left BREAK		{ error($2); }
	| initialization_left RETURN	{ error($2); }	| initialization_left CONTINUE	{ error($2); }
	| initialization_left EXTENDS	{ error($2); }	| initialization_left ENUM		{ error($2); }
	| initialization_left TRY		{ error($2); }	| initialization_left CATCH		{ error($2); }
	| initialization_left FINALLY	{ error($2); }	| initialization_left THROW		{ error($2); }
*/	;

initialization
	: initialization_left initializer
		{
			m_parser.setInitializer();
		}
	;

declarator
	: simple_declarator
	| initialization_left
		{
			error("VariableInitializer is missing");
		}
	| initialization
 	;

method_header
	: simple_declarator parameters
		{
			m_parser.createMethod();
		}
	| OPERATOR simple_declarator parameters					{ m_parser.createOperator($1); }
	| simple_declarator OPERATOR NOT parameters				{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR MUL parameters				{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR DIV parameters				{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR MOD parameters				{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR ADD parameters			{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR SUB parameters			{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR EQU parameters				{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR NOT_EQU parameters			{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR LESS parameters			{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR MORE parameters			{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR LESS_EQU parameters		{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR MORE_EQU parameters		{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR AND parameters				{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR OR parameters				{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR ASSIGN parameters			{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR ADD_ASSIGN parameters		{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR SUB_ASSIGN parameters		{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR MUL_ASSIGN parameters		{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR DIV_ASSIGN parameters		{ m_parser.createOperator($2, $3); }
	| simple_declarator OPERATOR MOD_ASSIGN parameters		{ m_parser.createOperator($2, $3); }
	| parameters
		{
			m_parser.removeMethod();
		}
	;

method
	: method_header compound
		{
			m_parser.setMethodBody();
		}
	| method_header declarator_semicolon
	| method_header
		{
			if(m_parser.peek() instanceof ILanguageElement )
			{
				ILanguageElement element = (ILanguageElement)m_parser.peek();
				error(element.getPosition(), "MethodBody is missing");
			}
		}
	| compound
		{
			m_parser.removeMethodBody();
			error("MethodHeader is missing");
		}
	;

parameter
	: simple_declarator			{ m_parser.createParameter(); }
	| parameter ASSIGN			{ error($2); }
	| parameter ADD_ASSIGN		{ error($2); }
	;
	
parameters_comma
	: COMMA
	| parameters_comma NOT			{ error($2); }	| parameters_comma MUL			{ error($2); }
	| parameters_comma DIV			{ error($2); }	| parameters_comma MOD			{ error($2); }
	| parameters_comma ADD			{ error($2); }	| parameters_comma SUB		{ error($2); }
	| parameters_comma EQU			{ error($2); }	| parameters_comma NOT_EQU		{ error($2); }
	| parameters_comma LESS			{ error($2); }	| parameters_comma MORE			{ error($2); }
	| parameters_comma LESS_EQU		{ error($2); }	| parameters_comma MORE_EQU		{ error($2); }
	| parameters_comma AND			{ error($2); }	| parameters_comma OR			{ error($2); }
	| parameters_comma ADD_ASSIGN	{ error($2); }	| parameters_comma COLON		{ error($2); }
	| parameters_comma ASSIGN		{ error($2); }	| parameters_comma QUESTION		{ error($2); }
	| parameters_comma COMMA		{ error($2); }	| parameters_comma DOT 			{ error($2); }
	| parameters_comma CONSTANT		{ error($2); }	| parameters_comma IF			{ error($2); }
	| parameters_comma ELSE			{ error($2); }	| parameters_comma DO			{ error($2); }
	| parameters_comma FOR			{ error($2); }	| parameters_comma WHILE		{ error($2); }
	| parameters_comma BREAK		{ error($2); }	| parameters_comma RETURN		{ error($2); }
	| parameters_comma CONTINUE		{ error($2); }	| parameters_comma THIS			{ error($2); }
	| parameters_comma SUPER		{ error($2); }	| parameters_comma CONTAINER	{ error($2); }
	| parameters_comma NULL			{ error($2); }	| parameters_comma EXTENDS		{ error($2); }
	| parameters_comma NEW			{ error($2); }	| parameters_comma TRY			{ error($2); }
	| parameters_comma CATCH		{ error($2); }	| parameters_comma FINALLY		{ error($2); }
	| parameters_comma THROW		{ error($2); }
	;
			
parameters_list
	: parameter
		{
			m_parser.addParameter();
		}
	| parameters_list parameters_comma parameter
		{
			m_parser.addParameter();
		}
	| parameters_list parameters_comma
		{
			error((String)null);
		}
	;

parameters_lbrace
	: LBRACE
		{
			m_parser.startParameters($1);
		}
	| parameters_lbrace NOT			{ error($2); }	| parameters_lbrace MUL			{ error($2); }
	| parameters_lbrace DIV			{ error($2); }	| parameters_lbrace MOD			{ error($2); }
	| parameters_lbrace ADD		{ error($2); }	| parameters_lbrace SUB		{ error($2); }
	| parameters_lbrace EQU			{ error($2); }	| parameters_lbrace NOT_EQU		{ error($2); }
	| parameters_lbrace LESS		{ error($2); }	| parameters_lbrace MORE		{ error($2); }
	| parameters_lbrace LESS_EQU	{ error($2); }	| parameters_lbrace MORE_EQU	{ error($2); }
	| parameters_lbrace AND			{ error($2); }	| parameters_lbrace OR			{ error($2); }
	| parameters_lbrace ADD_ASSIGN	{ error($2); }	| parameters_lbrace COLON		{ error($2); }
	| parameters_lbrace ASSIGN		{ error($2); }	| parameters_lbrace QUESTION	{ error($2); }
	| parameters_lbrace COMMA		{ error($2); }	| parameters_lbrace DOT 		{ error($2); }
	| parameters_lbrace CONSTANT	{ error($2); }	| parameters_lbrace IF			{ error($2); }
	| parameters_lbrace ELSE		{ error($2); }	| parameters_lbrace DO			{ error($2); }
	| parameters_lbrace FOR			{ error($2); }	| parameters_lbrace WHILE		{ error($2); }
	| parameters_lbrace BREAK		{ error($2); }	| parameters_lbrace RETURN		{ error($2); }
	| parameters_lbrace CONTINUE	{ error($2); }	| parameters_lbrace THIS		{ error($2); }
	| parameters_lbrace SUPER		{ error($2); }	| parameters_lbrace CONTAINER	{ error($2); }
	| parameters_lbrace NULL		{ error($2); }	| parameters_lbrace EXTENDS		{ error($2); }
	| parameters_lbrace NEW			{ error($2); }	| parameters_lbrace TRY			{ error($2); }
	| parameters_lbrace CATCH		{ error($2); }	| parameters_lbrace FINALLY		{ error($2); }
	| parameters_lbrace THROW		{ error($2); }
	;

parameters_left
	: parameters_lbrace
	| parameters_lbrace parameters_list
	;
	
parameters
	: parameters_left
		{ 
			error("')' expected after this token");
		}
	| parameters_left RBRACE
		{ 
			m_parser.finishParameters($2);
		}
	;

initializer
	: noname_class
	| array_initializer
	;
		
array_initializer_lcbrace
	: LCBRACE
		{ 
			m_parser.startArrayInitializer($1);
		}
	;
	
array_initializer_left
	: array_initializer_lcbrace
	| array_initializer_lcbrace array_initializers
	;

array_initializer
	: expression
	| new
	| array_initializer_left
		{
			error("insert '}' to complete ArrayInitializer");
		}
	| array_initializer_left RCBRACE
		{ 
			m_parser.finishArrayInitializer($2);
		}
	;

array_initializers_element
	: map_element					{ m_parser.addArrayInitializer(); }
	| array_initializer				{ m_parser.addArrayInitializer(); }
	;
	
array_initializers
	: array_initializers_element
	| array_initializers COMMA array_initializers_element
	| array_initializers array_initializers_element
		{
			ILanguageElement element = (ILanguageElement)m_parser.peek();
			error(element.getPosition(), "',' is missing");
		}
	| array_initializers COMMA
		{
			error($2);
		}
	;

map_element_comma
	: COMMA
	| map_element_comma MUL			{ error($2); }	| map_element_comma DIV			{ error($2); }
	| map_element_comma MOD			{ error($2); }	| map_element_comma EQU			{ error($2); }
	| map_element_comma NOT_EQU		{ error($2); }	| map_element_comma LESS		{ error($2); }
	| map_element_comma MORE		{ error($2); }	| map_element_comma LESS_EQU	{ error($2); }
	| map_element_comma MORE_EQU	{ error($2); }	| map_element_comma AND			{ error($2); }
	| map_element_comma OR			{ error($2); }	| map_element_comma ADD_ASSIGN	{ error($2); }
	| map_element_comma COLON		{ error($2); }	| map_element_comma ASSIGN		{ error($2); }
	| map_element_comma QUESTION	{ error($2); }	| map_element_comma DOT			{ error($2); }
	| map_element_comma IF			{ error($2); }	| map_element_comma ELSE		{ error($2); }
	| map_element_comma DO			{ error($2); }	| map_element_comma FOR			{ error($2); }
	| map_element_comma WHILE		{ error($2); }	| map_element_comma BREAK		{ error($2); }
	| map_element_comma RETURN		{ error($2); }	| map_element_comma CONTINUE	{ error($2); }
	| map_element_comma NULL		{ error($2); }	| map_element_comma EXTENDS		{ error($2); }
	| map_element_comma TRY			{ error($2); }	| map_element_comma CATCH		{ error($2); }
	| map_element_comma FINALLY		{ error($2); }	| map_element_comma THROW		{ error($2); }
	;
	
map_element_left1
	: braced_expression_left
		{
			error("',' is missing");
			m_parser.startMapElement();
		}
	| braced_expression_left map_element_comma
		{
			m_parser.startMapElement();
		}
;
		
map_element_left		
 	: map_element_left1
		{
			error("MapValue is missing");
		}
  	| map_element_left1 array_initializer
		{
			m_parser.setMapElementValue();
		}
 	;
	
map_element
	: map_element_left
	| map_element_left RBRACE
		{
			m_parser.finishMapElement($2);
		}
	;
	
compound_lcbrace
	: LCBRACE
		{
			m_parser.startCompound($1);
		}
	| compound_lcbrace MUL		{ error($2); }	| compound_lcbrace DIV			{ error($2); }
	| compound_lcbrace MOD		{ error($2); }	| compound_lcbrace EQU			{ error($2); }
	| compound_lcbrace NOT_EQU	{ error($2); }	| compound_lcbrace LESS			{ error($2); }
	| compound_lcbrace MORE		{ error($2); }	| compound_lcbrace LESS_EQU		{ error($2); }
	| compound_lcbrace MORE_EQU	{ error($2); }	| compound_lcbrace AND			{ error($2); }
	| compound_lcbrace OR		{ error($2); }	| compound_lcbrace ADD_ASSIGN	{ error($2); }
	| compound_lcbrace COLON	{ error($2); }	| compound_lcbrace ASSIGN		{ error($2); }
	| compound_lcbrace QUESTION	{ error($2); }	| compound_lcbrace DOT			{ error($2); }
	| compound_lcbrace NULL		{ error($2); }	| compound_lcbrace EXTENDS		{ error($2); }
	;
	
compound_left
	: compound_lcbrace
	| compound_lcbrace statements
	;
	
compound
	: compound_left
		{
			error("'}' expected after this token");
		}
	| compound_left RCBRACE
		{
			m_parser.finishCompound($2);
		}
	;

statements
	: statement					{ m_parser.addStatement(); }
	| statements statement		{ m_parser.addStatement(); }
	;

statement
	: try_catch_finally
	| throw
	| expression_statement		{ m_parser.onStatement(); }
	| compound
	| selection
	| iteration
	| jump
	;

throw_keyword
	: THROW
		{
			m_parser.startThrowStatement($1);
		}
	;
	
try_keyword
	: TRY
		{
			m_parser.startTryStatement($1);
		}
	;

catch_keyword
	: CATCH
		{
			m_parser.startCatchStatement($1);
		}
	;

finally_keyword
	: FINALLY
		{
			m_parser.startFinallyStatement($1);
		}
	;
	
throw
	: throw_keyword
		{
			error("Expression expected after this token");
		}
	| throw_keyword expression
		{
			error("';' expected after this token");
			m_parser.setThrowExpression();
		}
	| throw_keyword expression SEMICOLON
		{
			m_parser.setThrowExpression();
		}
	;
	
try
	: try_keyword 
		{
			error("BlockStatement expected after this token");
		}
	| try_keyword compound
		{
			m_parser.finishTryStatement();
		}
	;

catch_left
	: catch_keyword
		{
			error("'(' expected after this token");
		}
	| catch_keyword if_lbrace
		{
			error("Declaration expected after this token");
		}
	| catch_keyword if_lbrace simple_declarator
		{
			m_parser.setCatchExpression();
		}
	;
	
catch_left1
	: catch_left
		{
			error("')' expected after this token");
		}
	| catch_left RBRACE
	;
	
catch
	: catch_left1
		{
			error("BlockStatement expected after this token");
		}
	| catch_left1 compound
		{
			m_parser.finishCatchStatement();
		}
	;

finally
	: finally_keyword
		{
			error("BlockStatement expected after this token");
		}
	| finally_keyword compound
		{
			m_parser.finishFinallyStatement();
		}
	;

try_catch
	: try catch					{ m_parser.addCatchStatement(); }
	| try_catch catch			{ m_parser.addCatchStatement(); }
	;

try_catch_finally
	: try_catch
	| try finally
	| try_catch finally
	| try	
		{
			error("Catch or FinallyStatement expected after this token");
		}
	| catch
		{
			error("TryStatement is missing");
		}
	;

expression_statement
	: assignment
		{
			error("';' expected after this token");
		}
	| declarator
		{
			error("';' expected after this token");
			m_parser.createDeclarator();
		}
	| simple_declarator LBRACE
		{
			error("'}' expected before this token");
			m_parser.createDeclarator();
		}
	| assignment SEMICOLON
	| declarator SEMICOLON
		{
			m_parser.createDeclarator();
		}
	;

if_keyword
	: IF
		{
			m_parser.startIfStatement($1);
		}
	| if_keyword MUL		{ error($2); }	| if_keyword DIV			{ error($2); }
	| if_keyword MOD		{ error($2); }	| if_keyword EQU			{ error($2); }
	| if_keyword NOT_EQU	{ error($2); }	| if_keyword LESS			{ error($2); }
	| if_keyword MORE		{ error($2); }	| if_keyword LESS_EQU		{ error($2); }
	| if_keyword MORE_EQU	{ error($2); }	| if_keyword AND			{ error($2); }
	| if_keyword OR			{ error($2); }	| if_keyword ADD_ASSIGN	{ error($2); }
	| if_keyword COLON		{ error($2); }	| if_keyword ASSIGN			{ error($2); }
	| if_keyword QUESTION	{ error($2); }	| if_keyword DOT			{ error($2); }
	| if_keyword NULL		{ error($2); }	| if_keyword EXTENDS		{ error($2); }
	;
	
else_keyword
	: ELSE
	| else_keyword MUL		{ error($2); }	| else_keyword DIV			{ error($2); }
	| else_keyword MOD		{ error($2); }	| else_keyword EQU			{ error($2); }
	| else_keyword NOT_EQU	{ error($2); }	| else_keyword LESS			{ error($2); }
	| else_keyword MORE		{ error($2); }	| else_keyword LESS_EQU		{ error($2); }
	| else_keyword MORE_EQU	{ error($2); }	| else_keyword AND			{ error($2); }
	| else_keyword OR		{ error($2); }	| else_keyword ADD_ASSIGN	{ error($2); }
	| else_keyword COLON	{ error($2); }	| else_keyword ASSIGN		{ error($2); }
	| else_keyword QUESTION	{ error($2); }	| else_keyword DOT			{ error($2); }
	| else_keyword NULL		{ error($2); }	| else_keyword EXTENDS		{ error($2); }
	;

if_lbrace
	: LBRACE
	| if_lbrace MUL			{ error($2); }	| if_lbrace DIV				{ error($2); }
	| if_lbrace MOD			{ error($2); }	| if_lbrace EQU				{ error($2); }
	| if_lbrace NOT_EQU		{ error($2); }	| if_lbrace LESS			{ error($2); }
	| if_lbrace MORE		{ error($2); }	| if_lbrace LESS_EQU		{ error($2); }
	| if_lbrace MORE_EQU	{ error($2); }	| if_lbrace AND				{ error($2); }
	| if_lbrace OR			{ error($2); }	| if_lbrace ADD_ASSIGN		{ error($2); }
	| if_lbrace COLON		{ error($2); }	| if_lbrace ASSIGN			{ error($2); }
	| if_lbrace QUESTION	{ error($2); }	| if_lbrace DOT				{ error($2); }
	| if_lbrace NULL		{ error($2); }	| if_lbrace EXTENDS			{ error($2); }
	;
		
if_left1
	: if_keyword
		{ 
			error("expected '(' after this token");
		}
	| if_keyword if_lbrace
	;

if_left
	: if_left1
		{ 
			error("Expression expected after this token");
		}
	| if_left1 expression
		{ 
			m_parser.setIfCondition();
		}
	;
	
if
	: if_left
		{
			error("insert ')' to complete IfStatement");
		}
	| if_left RBRACE
	;

if_statement
	: if
		{
			error("IfStatement is missing");
		}
	| if statement
		{ 
			m_parser.finishIfStatement();
		}
	;
	
else_statement
	: else_keyword
		{
			error("ElseStatement is missing");
		}
	| else_keyword statement
		{ 
			m_parser.finishElseStatement();
		}
	;
	
selection
	: if_statement
	| if_statement else_statement
	;

while_keyword
	: WHILE
		{ 
			m_parser.startWhileStatement($1);
		}
	| while_keyword MUL			{ error($2); }	| while_keyword DIV			{ error($2); }
	| while_keyword MOD			{ error($2); }	| while_keyword EQU			{ error($2); }
	| while_keyword NOT_EQU		{ error($2); }	| while_keyword LESS		{ error($2); }
	| while_keyword MORE		{ error($2); }	| while_keyword LESS_EQU	{ error($2); }
	| while_keyword MORE_EQU	{ error($2); }	| while_keyword AND			{ error($2); }
	| while_keyword OR			{ error($2); }	| while_keyword ADD_ASSIGN	{ error($2); }
	| while_keyword COLON		{ error($2); }	| while_keyword ASSIGN		{ error($2); }
	| while_keyword QUESTION	{ error($2); }	| while_keyword DOT			{ error($2); }
	| while_keyword NULL		{ error($2); }	| while_keyword EXTENDS		{ error($2); }
	;

while_left1
	: while_keyword
		{ 
			error("expected '(' after this token");
		}
	| while_keyword if_lbrace							
	;

while_left
	: while_left1
		{ 
			error("Expression expected after this token");
		}
	| while_left1 expression							
		{ 
			m_parser.setWhileCondition();
		}
	;

while
	: while_left							
		{
			error("insert ')' to complete WhileStatement");
		}
	| while_left RBRACE
	;

while_loop
	: while
		{
			error("WhileStatement is missing");
		}
	| while statement
		{ 
			m_parser.finishWhileStatement();
		}
	;

do_keyword
	: DO
		{ 
			m_parser.startDoWhileStatement($1);
		}
	| do_keyword MUL		{ error($2); }	| do_keyword DIV		{ error($2); }
	| do_keyword MOD		{ error($2); }	| do_keyword EQU		{ error($2); }
	| do_keyword NOT_EQU	{ error($2); }	| do_keyword LESS		{ error($2); }
	| do_keyword MORE		{ error($2); }	| do_keyword LESS_EQU	{ error($2); }
	| do_keyword MORE_EQU	{ error($2); }	| do_keyword AND		{ error($2); }
	| do_keyword OR			{ error($2); }	| do_keyword ADD_ASSIGN{ error($2); }
	| do_keyword COLON		{ error($2); }	| do_keyword ASSIGN		{ error($2); }
	| do_keyword QUESTION	{ error($2); }	| do_keyword DOT		{ error($2); }
	| do_keyword NULL		{ error($2); }	| do_keyword EXTENDS	{ error($2); }
	;

for_keyword
	: FOR
		{ 
			m_parser.startForStatement($1);
		}
	| for_keyword MUL		{ error($2); }	| for_keyword DIV		{ error($2); }
	| for_keyword MOD		{ error($2); }	| for_keyword EQU		{ error($2); }
	| for_keyword NOT_EQU	{ error($2); }	| for_keyword LESS		{ error($2); }
	| for_keyword MORE		{ error($2); }	| for_keyword LESS_EQU	{ error($2); }
	| for_keyword MORE_EQU	{ error($2); }	| for_keyword AND		{ error($2); }
	| for_keyword OR		{ error($2); }	| for_keyword ADD_ASSIGN{ error($2); }
	| for_keyword COLON		{ error($2); }	| for_keyword ASSIGN	{ error($2); }
	| for_keyword QUESTION	{ error($2); }	| for_keyword DOT		{ error($2); }
	| for_keyword NULL		{ error($2); }	| for_keyword EXTENDS	{ error($2); }
	;

do
	: do_keyword
		{
			error("DoWhileStatement is missing");
		}
	| do_keyword statement
		{ 
			m_parser.finishDoWhileStatement();
		}
	;
	
do_while_loop
	: do
		{
			error("'while' expected after this token");
		}
	| do while
		{ 
			m_parser.setDoWhileCondition();
		}
	;

for_semicolon
	: SEMICOLON
	| for_semicolon MUL			{ error($2); }	| for_semicolon DIV				{ error($2); }
	| for_semicolon MOD			{ error($2); }	| for_semicolon EQU				{ error($2); }
	| for_semicolon NOT_EQU		{ error($2); }	| for_semicolon LESS			{ error($2); }
	| for_semicolon MORE		{ error($2); }	| for_semicolon LESS_EQU		{ error($2); }
	| for_semicolon MORE_EQU	{ error($2); }	| for_semicolon AND				{ error($2); }
	| for_semicolon OR			{ error($2); }	| for_semicolon ADD_ASSIGN		{ error($2); }
	| for_semicolon COLON		{ error($2); }	| for_semicolon ASSIGN			{ error($2); }
	| for_semicolon QUESTION	{ error($2); }	| for_semicolon DOT				{ error($2); }
	| for_semicolon NULL		{ error($2); }	| for_semicolon EXTENDS			{ error($2); }
	;

for_lbrace
	: LBRACE
	| for_lbrace MUL		{ error($2); }	| for_lbrace DIV			{ error($2); }
	| for_lbrace MOD		{ error($2); }	| for_lbrace EQU			{ error($2); }
	| for_lbrace NOT_EQU	{ error($2); }	| for_lbrace LESS			{ error($2); }
	| for_lbrace MORE		{ error($2); }	| for_lbrace LESS_EQU		{ error($2); }
	| for_lbrace MORE_EQU	{ error($2); }	| for_lbrace AND			{ error($2); }
	| for_lbrace OR			{ error($2); }	| for_lbrace ADD_ASSIGN	{ error($2); }
	| for_lbrace COLON		{ error($2); }	| for_lbrace ASSIGN			{ error($2); }
	| for_lbrace QUESTION	{ error($2); }	| for_lbrace DOT			{ error($2); }
	| for_lbrace NULL		{ error($2); }	| for_lbrace EXTENDS		{ error($2); }
	| for_lbrace CONSTANT	{ error($2); }
	;

for_left1
	: for_keyword
		{ 
			error("expected '(' after this token");
		}
	| for_keyword for_lbrace	
	;
	
for_init
	: /*assignment
	| */declarator
		{
			m_parser.createDeclarator();
		}
	;

for_left2
	: for_left1
	| for_left1 for_init
		{ 
			m_parser.setForInitialization();
		}
	;

for_left3
	: for_left2
		{
			error("expected ';' after this token");
		}
	| for_left2 for_semicolon
	;

for_left4
	: for_left3
		{ 
			error("Expression expected after this token");
		}
	| for_left3 expression
		{ 
			m_parser.setForCondition();
		}
	;

for_left5
	: for_left4
		{ 
			error("expected ';' after this token");
		}
	| for_left4 for_semicolon
	;

for_left6
	: for_left5
		{ 
			error("Expression expected after this token");
		}
	| for_left5 assignment
		{ 
			m_parser.setForExpression();
		}
	| for_left6 SEMICOLON			{ error($2); }
	;

for_left
	: for_left6
		{ 
			error("expected ')' after this token");
		}
	| for_left6 RBRACE
	;
	
for_statement
	: for_left
		{ 
			error("ForStatement is missing");
		}
	| for_left statement
		{
			m_parser.finishForStatement();
		}
	;
	
iteration
	: while_loop
	| do_while_loop SEMICOLON
	| do_while_loop
		{
			error("';' expected after this token");
		}
	| for_statement
	;

return_keyword
	: RETURN			{ m_parser.startJumpStatement($1); }
	;
	
break
	: BREAK				{ m_parser.startJumpStatement($1); }
	;

continue
	: CONTINUE			{ m_parser.startJumpStatement($1); }
	;

return
	: return_keyword
	| return_keyword expression
		{
			m_parser.setJumpExpression();
		}
	;
	
jump
	: return
		{
			error("';' expected after this token");
		}
	| break
		{
			error("';' expected after this token");
		}
	| continue
		{
			error("';' expected after this token");
		}
	| return SEMICOLON										
	| break SEMICOLON
	| continue SEMICOLON
	;

assignment
	: member_access
	| member_access ASSIGN expression
		{
			m_parser.onAssignment($2);
		}
	| member_access ASSIGN
		{
			error("Expression expected after this token");
		}
	;

expression
	: logical_or
	| logical_or QUESTION expression COLON expression
		{
			m_parser.onCondition();
		}
	;

logical_or
	: logical_and
	| logical_or OR logical_and						{ m_parser.onOperator($2); }
	| logical_or OR									{ error($2); }
	;
 
logical_and
	: equality
	| logical_and AND equality						{ m_parser.onOperator($2); }
	| logical_and AND								{ error($2); }
	;
 
equality
	: relational
	| relational EQU relational						{ m_parser.onOperator($2); }
	| relational NOT_EQU relational					{ m_parser.onOperator($2); }
	| relational EQU								{ error($2); }
	| relational NOT_EQU							{ error($2); }
	; 

relational
	: additive
	| additive LESS additive						{ m_parser.onOperator($2); }
	| additive MORE additive						{ m_parser.onOperator($2); }
	| additive LESS_EQU additive					{ m_parser.onOperator($2); }
	| additive MORE_EQU additive					{ m_parser.onOperator($2); }
	| additive LESS									{ error($2); }
	| additive MORE									{ error($2); }
	| additive LESS_EQU								{ error($2); }
	| additive MORE_EQU								{ error($2); }
	;

additive
	: multiplicative
	| additive ADD multiplicative					{ m_parser.onOperator($2); }
	| additive ADD_ASSIGN multiplicative			{ m_parser.onOperator($2); }
	| additive SUB multiplicative					{ m_parser.onOperator($2); }
	| additive SUB_ASSIGN multiplicative			{ m_parser.onOperator($2); }
	| additive ADD									{ error($2); }
	| additive SUB									{ error($2); }
	| additive ADD_ASSIGN							{ error($2); }
	| additive SUB_ASSIGN							{ error($2); }
	;

multiplicative
	: unary
	| multiplicative MUL unary						{ m_parser.onOperator($2); }
	| multiplicative MUL_ASSIGN unary				{ m_parser.onOperator($2); }
	| multiplicative DIV unary						{ m_parser.onOperator($2); }
	| multiplicative DIV_ASSIGN unary				{ m_parser.onOperator($2); }
	| multiplicative MOD unary						{ m_parser.onOperator($2); }
	| multiplicative MOD_ASSIGN unary				{ m_parser.onOperator($2); }
	| multiplicative MUL							{ error($2); }
	| multiplicative DIV							{ error($2); }
	| multiplicative MOD							{ error($2); }
	| multiplicative MUL_ASSIGN						{ error($2); }
	| multiplicative DIV_ASSIGN						{ error($2); }
	| multiplicative MOD_ASSIGN						{ error($2); }
	;

braced_expression_left
	: LBRACE expression
		{
			m_parser.startBracedExpression($1);
		}
	;
	
braced_expression
	: braced_expression_left
		{
			error("')' expected after this token");
		}
	| braced_expression_left RBRACE
		{
			m_parser.finishBracedExpression($2);
		}
	;
			
unary
	: ADD unary
	| SUB unary					{ m_parser.onUnary($1); }
	| NOT unary					{ m_parser.onUnary($1); }
	| member_access
	| ADD						{ error($1); m_parser.onEmptyUnary($1); }
	| SUB						{ error($1); m_parser.onEmptyUnary($1); }
	| NOT						{ error($1); m_parser.onEmptyUnary($1); }
	;

new_keyword
	: NEW
		{
			m_parser.startNewExpression($1);
		}
	| new_keyword NOT			{ error($2); }	| new_keyword ASSIGN		{ error($2); }
	| new_keyword DOT 			{ error($2); }	| new_keyword NEW			{ error($2); }
	| new_keyword CONSTANT		{ error($2); }	| new_keyword IF			{ error($2); }
	| new_keyword ELSE			{ error($2); }	| new_keyword DO			{ error($2); }
	| new_keyword FOR			{ error($2); }	| new_keyword WHILE			{ error($2); }
	| new_keyword BREAK			{ error($2); }	| new_keyword RETURN		{ error($2); }
	| new_keyword CONTINUE		{ error($2); }	| new_keyword THIS			{ error($2); }
	| new_keyword SUPER			{ error($2); }	| new_keyword CONTAINER		{ error($2); }
	| new_keyword NULL			{ error($2); }	| new_keyword EXTENDS		{ error($2); }
	| new_keyword TRY			{ error($2); }	| new_keyword CATCH			{ error($2); }
	| new_keyword FINALLY		{ error($2); }	| new_keyword THROW			{ error($2); }
	;
	
new_identifier
	: IDENTIFIER
		{
			m_parser.finishNewExpression($1);
		}
	| new_identifier NOT		{ error($2); }	
	| new_identifier ADD_ASSIGN{ error($2); }
	| new_identifier ASSIGN		{ error($2); }
	| new_identifier DOT 		{ error($2); }	| new_identifier CONSTANT	{ error($2); }
	| new_identifier IF			{ error($2); }	| new_identifier ELSE		{ error($2); }
	| new_identifier DO			{ error($2); }	| new_identifier FOR		{ error($2); }
	| new_identifier WHILE		{ error($2); }	| new_identifier BREAK		{ error($2); }
	| new_identifier RETURN		{ error($2); }	| new_identifier CONTINUE	{ error($2); }
	| new_identifier THIS		{ error($2); }	| new_identifier SUPER		{ error($2); }
	| new_identifier CONTAINER	{ error($2); }	| new_identifier NULL		{ error($2); }
	| new_identifier EXTENDS	{ error($2); }	| new_identifier NEW		{ error($2); }
	| new_identifier TRY		{ error($2); }	| new_identifier CATCH		{ error($2); }
	| new_identifier FINALLY	{ error($2); }	| new_identifier THROW		{ error($2); }
	;

new
	: new_keyword
		{
			error("Identifier expected after this token");
		}
	| new_keyword new_identifier
	;
	
member_access
	: CONSTANT
		{
			m_parser.onConstant($1);
			m_parser.onMemberAccess();
		}
	| this_access
		{
			m_parser.onMemberAccess();
		}
	| postfix_expression
		{
			m_parser.onMemberAccess();
		}
	;
	
container
	: CONTAINER								{ m_parser.onContainer($1); }
	| container DOT CONTAINER				{ m_parser.addContainer($3); }
	;
		
this_access
	: THIS									{ m_parser.onThis($1, false); }
	| THIS DOT								{ m_parser.onThis($1, false); error($2); }
	| THIS DOT postfix_expression			{ m_parser.onThis($1, true); }
	| SUPER									{ m_parser.onSuper($1, false); error($1); }
	| SUPER DOT								{ m_parser.onSuper($1, false); error($2); }
	| SUPER DOT postfix_expression			{ m_parser.onSuper($1, true); }
	| container
	| container DOT postfix_expression		{ m_parser.addContainer(); }
	| container DOT							{ error($2); }
	| this_access DOT						{ error($2); }
	;

postfix_expression
	: postfix_expression1
	| braced_expression
	| braced_expression DOT postfix_expression1 { m_parser.onPostfix(); }
	;

postfix_expression1
	: qualified_name
	| postfix
	| postfix DOT postfix_expression1		{ m_parser.onPostfix(); }
	| postfix_expression1 DOT				{ error($2); }
	| postfix DOT							{ error($2); }
	;

postfix
	: array_access
	| method_call
	;

lbracket
	: LBRACKET
		{
			m_parser.startIndex($1);
		}
	;
	
index_expression_left
	: lbracket
	| lbracket expression
		{
			m_parser.setIndexExpression();
		}
	;

index_expression
	: index_expression_left RBRACKET
		{
			m_parser.finishIndex($2);
		}
	| index_expression_left
		{
			error("expected ']' after this token"); 
		}
	;

indices
	: index_expression
		{
			m_parser.onIndices(true);
		}
	| indices index_expression
		{
			m_parser.onIndices(false);
		}
	;

simple_array_access
	: qualified_name indices
		{
			m_parser.onTypeHelper(true);
		}
	;
	
array_access
	: simple_array_access
		{
			m_parser.onArrayAccess(true);
		}
	| method_call indices
		{
			m_parser.onArrayAccess(false);
		}
	;

expressions_comma
	: COMMA
	;
	
expressions
	: expression
		{
			m_parser.addExpression();
		}
	| expressions expressions_comma expression
		{
			m_parser.addExpression();
		}
	| expressions expressions_comma
		{
			error((String)null); 
		}
	| expressions expression
		{
			error("',' expected after this token"); 
			m_parser.addExpression();
		}
	;
	
method_call_lbrace
	: LBRACE
		{
			m_parser.startExpressions($1);
		}
	;
	
method_call_params_left
	: method_call_lbrace
	| method_call_lbrace expressions
	;
	
method_call_params
	: method_call_params_left
		{
			error("')' expected after this token"); 
		}
	| method_call_params_left RBRACE
		{
			m_parser.finishExpressions($2);
		}
	;
	
method_call
	: qualified_name method_call_params
		{
			m_parser.onMethodCall();
		}
	;