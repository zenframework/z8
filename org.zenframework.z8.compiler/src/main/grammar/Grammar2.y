/*

1. javaYacc.exe -CGrammar.java -pParser.y Grammar.y
2. copy Grammar.java to org.zenframework.z8.compiler.parser

*/


%{
package org.zenframework.z8.compiler.Parser;

import org.zenframework.z8.compiler.Core.*;
import org.zenframework.z8.compiler.Lexer.*;
import org.zenframework.z8.compiler.Compiler.*;

public class Grammar
{
	private IToken m_accessModifier;
%}

%start program

%token <token> WHITESPACE
%token <token> LINEBREAK
%token <token> COMMENT
%token <token> KEYWORD
%token <token> OPERATOR

%token <token> NOT
%token <token> MUL
%token <token> DIV
%token <token> MOD
%token <token> PLUS
%token <token> MINUS
%token <token> EQU
%token <token> NOT_EQU
%token <token> LESS
%token <token> MORE
%token <token> LESS_EQU
%token <token> MORE_EQU
%token <token> AND
%token <token> OR
%token <token> GROUP

%token <token> PLUS_ASSIGN

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
	
%token <token> INT
%token <token> BOOL
%token <token> DATE
%token <token> DECIMAL
%token <token> STRING
%token <token> DATETIME
%token <token> DATESPAN
%token <token> BINARY
%token <token> GUID
%token <token> VOID

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

%token <token> OPERATOR


%left COMMA SEMICOLON

%right COLON
%right ASSIGN
%right QUESTION

%left OR
%left AND
%left EQU NOT_EQU
%left LESS MORE LESS_EQU MORE_EQU
%left PLUS MINUS
%left MUL DIV MOD

%left LBRACE RBRACE
%left LCBRACE RCBRACE

%%
/* ========================================================================================= */

program
	: class
	| enum
	| program class
	| program enum
	;

constant
	: CONSTANT								{ m_parser.onConstant($1); }
	;
	
qualified_name
	: IDENTIFIER							{ m_parser.createQualifiedName($1); }
	| qualified_name DOT IDENTIFIER			{ m_parser.extendQualifiedName($3); }
	;

enum
	: simple_enum
	| modifiers simple_enum					{ m_parser.onModifiers(); }
	;
	
simple_enum
	: ENUM IDENTIFIER LCBRACE RCBRACE							
		{ 
			m_parser.onEnum($2, false);
		}
	| ENUM IDENTIFIER LCBRACE enum_body RCBRACE				
		{ 
			m_parser.onEnum($2, true); 
		}
	;

enum_body
	: IDENTIFIER
		{
			m_parser.createEnumBody($1);
		}
	| enum_body COMMA IDENTIFIER
		{
			m_parser.extendEnumBody($3);
		}
	;
	
records
	: RECORDS LCBRACE RCBRACE
		{ 
			m_parser.onRecords($1, false);
		}
	| RECORDS LCBRACE records_body RCBRACE				
		{ 
			m_parser.onRecords($1, true); 
		}
	;

record
	: simple_record
	| modifiers simple_record					{ m_parser.onModifiers(); }
	;
	
simple_record
	: IDENTIFIER ASSIGN CONSTANT SEMICOLON		{ m_parser.onRecord($1, $3); }
	;
	
records_body
	: record									{ m_parser.createRecordsBody(); }
	| records_body record						{ m_parser.extendRecordsBody(); }
	;

class
	: simple_class
	| modifiers simple_class					{ m_parser.onModifiers(); }
	;
	
simple_class
	: CLASS IDENTIFIER LCBRACE RCBRACE
		{
			m_parser.onClass($2, null, false);
		}
	| CLASS IDENTIFIER LCBRACE class_body RCBRACE
		{
			m_parser.onClass($2, null, true);
		}
	| CLASS IDENTIFIER EXTENDS IDENTIFIER LCBRACE RCBRACE
		{ 
			m_parser.onClass($2, $4, false);
		}
	| CLASS IDENTIFIER EXTENDS IDENTIFIER LCBRACE class_body RCBRACE
		{
			m_parser.onClass($2, $4, true);
		}
	;

attributes
	: LBRACKET IDENTIFIER RBRACKET
		{ 
			IPosition brackets = $1.getPosition().union($3.getPosition());
			m_parser.createAttributes(brackets, $2, null);
		}			
	| LBRACKET IDENTIFIER CONSTANT RBRACKET
		{ 
			IPosition brackets = $1.getPosition().union($4.getPosition());
			m_parser.createAttributes(brackets, $2, $3);
		}			
	| attributes LBRACKET IDENTIFIER RBRACKET
		{ 
			IPosition brackets = $2.getPosition().union($4.getPosition());
			m_parser.extendAttributes(brackets, $3, null);
		}
	| attributes LBRACKET IDENTIFIER CONSTANT RBRACKET
		{ 
			IPosition brackets = $2.getPosition().union($5.getPosition());
			m_parser.extendAttributes(brackets, $3, $4);
		}
	;

class_body
	: class_member						{ m_parser.createClassBody(); }
	| class_body class_member			{ m_parser.extendClassBody(); }
	;
	
class_member
	: member SEMICOLON
	| access
	| method
	| records
	;

access
	: PUBLIC COLON						{ m_parser.onAccess($1); }
	| PRIVATE COLON						{ m_parser.onAccess($1); }
	| PROTECTED COLON					{ m_parser.onAccess($1); }
	;
	
simple_member
	: type IDENTIFIER
		{ 
			m_parser.onMember($2, null);
		}
	| type IDENTIFIER ASSIGN initializer
		{ 
			m_parser.onMember($2, $3);
		}
	| qualified_name ASSIGN initializer
		{ 
			m_parser.onMemberInit($2);
		}
	| qualified_name PLUS_ASSIGN initializer
		{ 
			m_parser.onMemberInit($2);
		}
	| qualified_name
		{ 
			m_parser.onMemberInit(null);
		}
	| type IDENTIFIER ASSIGN CLASS LCBRACE RCBRACE
		{ 
			m_parser.onMemberClass($2, false);
		}
	| type IDENTIFIER ASSIGN CLASS LCBRACE class_body RCBRACE
		{
			m_parser.onMemberClass($2, true);
		}
	| qualified_name ASSIGN CLASS LCBRACE RCBRACE
		{
			m_parser.onMemberInitClass(false);
		}
	| qualified_name ASSIGN CLASS LCBRACE class_body RCBRACE
		{
			m_parser.onMemberInitClass(true);
		}
	;

access_modifier
	: PUBLIC										{ m_accessModifier = $1; }
	| PRIVATE										{ m_accessModifier = $1; }
	| PROTECTED										{ m_accessModifier = $1; }
	;

modifiers
	: STATIC										{ m_parser.onModifiers($1, null, false); }
	| attributes									{ m_parser.onModifiers(null, null, true); }
	| access_modifier								{ m_parser.onModifiers(null, m_accessModifier, false); }
	
	| STATIC attributes								{ m_parser.onModifiers($1, null, true); }
	| STATIC access_modifier						{ m_parser.onModifiers($1, m_accessModifier, false); }
	
	| attributes STATIC								{ m_parser.onModifiers($2, null, true); }
	| attributes access_modifier					{ m_parser.onModifiers(null, m_accessModifier, true); }
	| access_modifier STATIC						{ m_parser.onModifiers($2, m_accessModifier, false); }
	| access_modifier attributes					{ m_parser.onModifiers(null, m_accessModifier, true); }

	| STATIC attributes access_modifier				{ m_parser.onModifiers($1, m_accessModifier, true); }
	| STATIC access_modifier attributes				{ m_parser.onModifiers($1, m_accessModifier, true); }
	| attributes STATIC access_modifier				{ m_parser.onModifiers($2, m_accessModifier, true); }
	| attributes access_modifier STATIC				{ m_parser.onModifiers($3, m_accessModifier, true); }
	| access_modifier STATIC attributes				{ m_parser.onModifiers($2, m_accessModifier, true); }
	| access_modifier attributes STATIC				{ m_parser.onModifiers($3, m_accessModifier, true); }
	;
	
member
	: simple_member
	| modifiers simple_member						{ m_parser.onModifiers(); }
	;

method
	: simple_method
	| modifiers simple_method						{ m_parser.onModifiers(); }
	;
	
type
	: type_name
	| AUTO type_name								{ m_parser.onTypeAuto($1); }
	| type BRACKETS									{ m_parser.onTypeArray(); }
	| type LESS INT MORE							{ m_parser.onTypeMap($3); }
	| type LESS DECIMAL MORE						{ m_parser.onTypeMap($3); }
	| type LESS STRING MORE							{ m_parser.onTypeMap($3); }
	| type LESS BOOL MORE							{ m_parser.onTypeMap($3); }
	| type LESS DATE MORE							{ m_parser.onTypeMap($3); }
	| type LESS DATETIME MORE						{ m_parser.onTypeMap($3); }
	| type LESS DATESPAN MORE						{ m_parser.onTypeMap($3); }
	| type LESS BINARY MORE							{ m_parser.onTypeMap($3); }
	| type LESS GUID MORE							{ m_parser.onTypeMap($3); }
	| type LESS IDENTIFIER MORE						{ m_parser.onTypeMap($3); }
	;

type_name
	: INT											{ m_parser.onType($1); }
	| DECIMAL										{ m_parser.onType($1); }
	| STRING										{ m_parser.onType($1); }
	| BOOL											{ m_parser.onType($1); }
	| DATE											{ m_parser.onType($1); }
	| DATESPAN										{ m_parser.onType($1); }
	| DATETIME										{ m_parser.onType($1); }
	| BINARY										{ m_parser.onType($1); }
	| GUID											{ m_parser.onType($1); }
	| VOID											{ m_parser.onType($1); }
	| qualified_name								{ m_parser.onType(); }
	;

initializer
	: expression
	| NEW type_name
		{
			m_parser.onTypeNew($1);
		}
	| LCBRACE initializers RCBRACE
		{
			IPosition braces = $1.getPosition().union($3.getPosition());
			m_parser.createInitializer(braces);
		}
	;

initializers
	: initializer							{ m_parser.createInitializer(null); }
	| map_element							{ m_parser.createInitializer(null); }
	| initializers COMMA initializer		{ m_parser.extendInitializer(null); }
	| initializers COMMA map_element		{ m_parser.extendInitializer(null); }
	;

map_element
	: LBRACE expression COMMA initializer RBRACE		
		{
			IPosition braces = $1.getPosition().union($5.getPosition());
			m_parser.onMapElement(braces);
		}
	;

simple_method
	: type IDENTIFIER LBRACE parameters RBRACE compound_statement				
		{ 
			IPosition braces = $3.getPosition().union($5.getPosition());
			m_parser.onMethod($2, braces, true, true);
		}
	| type IDENTIFIER LBRACE RBRACE compound_statement
		{
			IPosition braces = $3.getPosition().union($4.getPosition());
			m_parser.onMethod($2, braces, false, true);
		}
	| type IDENTIFIER LBRACE parameters RBRACE SEMICOLON
		{ 
			IPosition braces = $3.getPosition().union($5.getPosition());
			m_parser.onMethod($2, braces, true, false);
		}
	| type IDENTIFIER LBRACE RBRACE SEMICOLON
		{
			IPosition braces = $3.getPosition().union($4.getPosition());
			m_parser.onMethod($2, braces, false, false);
		}

	;

parameters
	: type IDENTIFIER						{ m_parser.createParameters($2); }
	| parameters COMMA type IDENTIFIER		{ m_parser.extendParameters($4); }
	;

compound_statement
	: LCBRACE RCBRACE					
		{ 
			IPosition braces = $1.getPosition().union($2.getPosition());
			m_parser.onCompoundStatement(braces, false);
		}
	| LCBRACE statement_list RCBRACE
		{ 
			IPosition braces = $1.getPosition().union($3.getPosition());
			m_parser.onCompoundStatement(braces, true);
		}
						
	;

statement_list
	: statement							{ m_parser.createStatementList(); }
	| statement_list statement			{ m_parser.extendStatementList(); }
	;

statement
	: try_catch_finally_statement
	| throw_statement
	| expression_statement
	| compound_statement
	| selection_statement
	| iteration_statement
	| jump_statement
	;

throw_statement
	: THROW expression SEMICOLON		{ m_parser.onThrowStatement($1); }
	;
	
try_catch_finally_statement
	: try_catch_statement
	| try_statement FINALLY compound_statement
		{
			m_parser.onTryFinallyStatement($2);
		}
	| try_catch_statement FINALLY compound_statement
		{
			m_parser.onTryFinallyStatement($2);
		}
	;

try_catch_statement
	: try_statement catch_statement
		{
			m_parser.onTryCatchStatement();
		}
	| try_catch_statement catch_statement
		{
			m_parser.onTryCatchStatement();
		}
	;

catch_statement
	: CATCH LBRACE type IDENTIFIER RBRACE compound_statement
		{ 
			m_parser.onCatchClause($1, $4);
		}
	;

try_statement
	: TRY compound_statement			{ m_parser.onTryStatement($1); }
	;
	
expression_statement
	: assignment SEMICOLON				{ m_parser.onStatement(); }
	| declaration SEMICOLON				{ m_parser.onStatement(); }
	;

selection_statement
	: IF LBRACE expression RBRACE statement
		{
			m_parser.onIfStatement();
		}
	/* s/r error #1 */
	| IF LBRACE expression RBRACE statement ELSE statement
		{ 
			m_parser.onIfElseStatement();
		}
	;

iteration_statement
	: WHILE LBRACE expression RBRACE statement
		{ 
			m_parser.onWhileStatement();
		}
	| DO statement WHILE LBRACE expression RBRACE SEMICOLON
		{
			m_parser.onDoWhileStatement();
		}
	| FOR LBRACE for_init SEMICOLON expression SEMICOLON assignment RBRACE statement
		{
			m_parser.onForStatement(true);
		}
	| FOR LBRACE SEMICOLON expression SEMICOLON assignment RBRACE statement
		{
			m_parser.onForStatement(false);
		}
	;

for_init
	: assignment
	| declaration
	;

jump_statement
	: BREAK SEMICOLON					{ m_parser.onJumpStatement($1); }
	| CONTINUE SEMICOLON				{ m_parser.onJumpStatement($1); }
	| RETURN expression SEMICOLON		{ m_parser.onJumpStatement($1, true); }
	| RETURN SEMICOLON					{ m_parser.onJumpStatement($1, false); }
	;

assignment
	: expression
	| member_access ASSIGN expression	{ m_parser.onAssignment(); }
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
	| logical_or OR logical_and			{ m_parser.onOperator($2); }
	;
 
logical_and
	: equality
	| logical_and AND equality			{ m_parser.onOperator($2); }
	;
 
equality
	: relational
	| relational EQU relational			{ m_parser.onOperator($2); }
	| relational NOT_EQU relational		{ m_parser.onOperator($2); }
	;
 
relational
	: additive
	| additive LESS additive			{ m_parser.onOperator($2); }
	| additive MORE additive			{ m_parser.onOperator($2); }
	| additive LESS_EQU additive		{ m_parser.onOperator($2); }
	| additive MORE_EQU additive		{ m_parser.onOperator($2); }
 	;

additive
	: multiplicative
	| additive PLUS multiplicative		{ m_parser.onOperator($2); }
	| additive MINUS multiplicative		{ m_parser.onOperator($2); }
 	;

multiplicative
	: unary
	| multiplicative MUL unary			{ m_parser.onOperator($2); }
	| multiplicative DIV unary			{ m_parser.onOperator($2); }
	| multiplicative MOD unary			{ m_parser.onOperator($2); }
 	;

unary
	: PLUS unary
	| MINUS unary						{ m_parser.onUnary($1); }
	| NOT unary							{ m_parser.onUnary($1); }
	| LBRACE expression RBRACE			
		{
			IPosition braces = $1.getPosition().union($3.getPosition());
			m_parser.onGroup(braces);
		}
	| member_access
	;

expressions
	: expression						{ m_parser.createExpressions(); }
	| expressions COMMA expression		{ m_parser.extendExpressions(); }
	;

member_access
	: constant							{ m_parser.onMemberAccess(); }
	| this_access						{ m_parser.onMemberAccess(); }
	;
	
this_access
	: postfix_expression
	| THIS								{ m_parser.onThis($1, false); }
	| THIS DOT postfix_expression		{ m_parser.onThis($1, true); }
	| SUPER DOT postfix_expression		{ m_parser.onSuper($1); }
	| CONTAINER							{ m_parser.onContainer($1, false); }
	| CONTAINER DOT postfix_expression	{ m_parser.onContainer($1, true); }
	;

postfix_expression
	: qualified_name
	| postfix
	| postfix DOT postfix_expression	{ m_parser.onPostfix(); }
	;

postfix
	: array_access
	| method_call
	;

array_access
	: qualified_name LBRACKET expression RBRACKET
		{ 
			IPosition backets = $2.getPosition().union($4.getPosition());
			m_parser.onArrayAccess(backets);
		}
	| method_call LBRACKET expression RBRACKET
		{
			IPosition backets = $2.getPosition().union($4.getPosition());
			m_parser.onArrayAccess(backets);
		}
	| array_access LBRACKET expression RBRACKET
		{
			IPosition backets = $2.getPosition().union($4.getPosition());
			m_parser.onArrayAccess(backets);
		}
	;

method_call
	: qualified_name LBRACE expressions RBRACE
		{
			IPosition braces = $2.getPosition().union($4.getPosition());
			m_parser.onMethodCall(braces, true);
		}
	| qualified_name LBRACE RBRACE
		{
			IPosition braces = $2.getPosition().union($3.getPosition());
			m_parser.onMethodCall(braces, false);
		}
	;

declaration
	: type declarators					{ m_parser.onDeclaration(); }
	;
	
declarators
	: declarator						{ m_parser.createDeclarators(); }
	| declarators COMMA declarator		{ m_parser.extendDeclarators(); }
	;

declarator
	: IDENTIFIER												
		{ 
			m_parser.onDeclarator($1);
		}
	| IDENTIFIER ASSIGN initializer
		{
			m_parser.onDeclaratorInit($1);
		}
	| IDENTIFIER ASSIGN CLASS LCBRACE RCBRACE
		{
			m_parser.onDeclaratorClass($1, false, false);
		}
	| IDENTIFIER ASSIGN CLASS LCBRACE class_body RCBRACE
		{
			m_parser.onDeclaratorClass($1, false, true);
		}
	| IDENTIFIER ASSIGN modifiers CLASS LCBRACE RCBRACE
		{
			m_parser.onDeclaratorClass($2, true, false);
		}
	| IDENTIFIER ASSIGN modifiers CLASS LCBRACE class_body RCBRACE
		{
			m_parser.onDeclaratorClass($2, true, true);
		}
	;
