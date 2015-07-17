/*

1. javaYacc.exe -CGrammar.java -pParser.y Grammar.y
2. copy Grammar.java to org.zenframework.z8.compiler.Parser

*/


%{
package org.zenframework.z8.compiler.Parser;

import org.zenframework.z8.compiler.Core.*;
import org.zenframework.z8.compiler.Lexer.*;
import org.zenframework.z8.compiler.Compiler.*;

public class Grammar
{
	private IToken accessModifier;
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
	: CONSTANT								{ parser.onConstant($1); }
	;
	
qualified_name
	: IDENTIFIER							{ parser.createQualifiedName($1); }
	| qualified_name DOT IDENTIFIER			{ parser.extendQualifiedName($3); }
	;

enum
	: simple_enum
	| modifiers simple_enum					{ parser.onModifiers(); }
	;
	
simple_enum
	: ENUM IDENTIFIER LCBRACE RCBRACE							
		{ 
			parser.onEnum($2, false);
		}
	| ENUM IDENTIFIER LCBRACE enum_body RCBRACE				
		{ 
			parser.onEnum($2, true); 
		}
	;

enum_body
	: IDENTIFIER
		{
			parser.createEnumBody($1);
		}
	| enum_body COMMA IDENTIFIER
		{
			parser.extendEnumBody($3);
		}
	;
	
records
	: RECORDS LCBRACE RCBRACE
		{ 
			parser.onRecords($1, false);
		}
	| RECORDS LCBRACE records_body RCBRACE				
		{ 
			parser.onRecords($1, true); 
		}
	;

record
	: simple_record
	| modifiers simple_record					{ parser.onModifiers(); }
	;
	
simple_record
	: IDENTIFIER ASSIGN CONSTANT SEMICOLON		{ parser.onRecord($1, $3); }
	;
	
records_body
	: record									{ parser.createRecordsBody(); }
	| records_body record						{ parser.extendRecordsBody(); }
	;

class
	: simple_class
	| modifiers simple_class					{ parser.onModifiers(); }
	;
	
simple_class
	: CLASS IDENTIFIER LCBRACE RCBRACE
		{
			parser.onClass($2, null, false);
		}
	| CLASS IDENTIFIER LCBRACE class_body RCBRACE
		{
			parser.onClass($2, null, true);
		}
	| CLASS IDENTIFIER EXTENDS IDENTIFIER LCBRACE RCBRACE
		{ 
			parser.onClass($2, $4, false);
		}
	| CLASS IDENTIFIER EXTENDS IDENTIFIER LCBRACE class_body RCBRACE
		{
			parser.onClass($2, $4, true);
		}
	;

attributes
	: LBRACKET IDENTIFIER RBRACKET
		{ 
			IPosition brackets = $1.getPosition().union($3.getPosition());
			parser.createAttributes(brackets, $2, null);
		}			
	| LBRACKET IDENTIFIER CONSTANT RBRACKET
		{ 
			IPosition brackets = $1.getPosition().union($4.getPosition());
			parser.createAttributes(brackets, $2, $3);
		}			
	| attributes LBRACKET IDENTIFIER RBRACKET
		{ 
			IPosition brackets = $2.getPosition().union($4.getPosition());
			parser.extendAttributes(brackets, $3, null);
		}
	| attributes LBRACKET IDENTIFIER CONSTANT RBRACKET
		{ 
			IPosition brackets = $2.getPosition().union($5.getPosition());
			parser.extendAttributes(brackets, $3, $4);
		}
	;

class_body
	: class_member						{ parser.createClassBody(); }
	| class_body class_member			{ parser.extendClassBody(); }
	;
	
class_member
	: member SEMICOLON
	| access
	| method
	| records
	;

access
	: PUBLIC COLON						{ parser.onAccess($1); }
	| PRIVATE COLON						{ parser.onAccess($1); }
	| PROTECTED COLON					{ parser.onAccess($1); }
	;
	
simple_member
	: type IDENTIFIER
		{ 
			parser.onMember($2, null);
		}
	| type IDENTIFIER ASSIGN initializer
		{ 
			parser.onMember($2, $3);
		}
	| qualified_name ASSIGN initializer
		{ 
			parser.onMemberInit($2);
		}
	| qualified_name PLUS_ASSIGN initializer
		{ 
			parser.onMemberInit($2);
		}
	| qualified_name
		{ 
			parser.onMemberInit(null);
		}
	| type IDENTIFIER ASSIGN CLASS LCBRACE RCBRACE
		{ 
			parser.onMemberClass($2, false);
		}
	| type IDENTIFIER ASSIGN CLASS LCBRACE class_body RCBRACE
		{
			parser.onMemberClass($2, true);
		}
	| qualified_name ASSIGN CLASS LCBRACE RCBRACE
		{
			parser.onMemberInitClass(false);
		}
	| qualified_name ASSIGN CLASS LCBRACE class_body RCBRACE
		{
			parser.onMemberInitClass(true);
		}
	;

access_modifier
	: PUBLIC										{ accessModifier = $1; }
	| PRIVATE										{ accessModifier = $1; }
	| PROTECTED										{ accessModifier = $1; }
	;

modifiers
	: STATIC										{ parser.onModifiers($1, null, false); }
	| attributes									{ parser.onModifiers(null, null, true); }
	| access_modifier								{ parser.onModifiers(null, accessModifier, false); }
	
	| STATIC attributes								{ parser.onModifiers($1, null, true); }
	| STATIC access_modifier						{ parser.onModifiers($1, accessModifier, false); }
	
	| attributes STATIC								{ parser.onModifiers($2, null, true); }
	| attributes access_modifier					{ parser.onModifiers(null, accessModifier, true); }
	| access_modifier STATIC						{ parser.onModifiers($2, accessModifier, false); }
	| access_modifier attributes					{ parser.onModifiers(null, accessModifier, true); }

	| STATIC attributes access_modifier				{ parser.onModifiers($1, accessModifier, true); }
	| STATIC access_modifier attributes				{ parser.onModifiers($1, accessModifier, true); }
	| attributes STATIC access_modifier				{ parser.onModifiers($2, accessModifier, true); }
	| attributes access_modifier STATIC				{ parser.onModifiers($3, accessModifier, true); }
	| access_modifier STATIC attributes				{ parser.onModifiers($2, accessModifier, true); }
	| access_modifier attributes STATIC				{ parser.onModifiers($3, accessModifier, true); }
	;
	
member
	: simple_member
	| modifiers simple_member						{ parser.onModifiers(); }
	;

method
	: simple_method
	| modifiers simple_method						{ parser.onModifiers(); }
	;
	
type
	: type_name
	| AUTO type_name								{ parser.onTypeAuto($1); }
	| type BRACKETS									{ parser.onTypeArray(); }
	| type LESS INT MORE							{ parser.onTypeMap($3); }
	| type LESS DECIMAL MORE						{ parser.onTypeMap($3); }
	| type LESS STRING MORE							{ parser.onTypeMap($3); }
	| type LESS BOOL MORE							{ parser.onTypeMap($3); }
	| type LESS DATE MORE							{ parser.onTypeMap($3); }
	| type LESS DATETIME MORE						{ parser.onTypeMap($3); }
	| type LESS DATESPAN MORE						{ parser.onTypeMap($3); }
	| type LESS BINARY MORE							{ parser.onTypeMap($3); }
	| type LESS GUID MORE							{ parser.onTypeMap($3); }
	| type LESS IDENTIFIER MORE						{ parser.onTypeMap($3); }
	;

type_name
	: INT											{ parser.onType($1); }
	| DECIMAL										{ parser.onType($1); }
	| STRING										{ parser.onType($1); }
	| BOOL											{ parser.onType($1); }
	| DATE											{ parser.onType($1); }
	| DATESPAN										{ parser.onType($1); }
	| DATETIME										{ parser.onType($1); }
	| BINARY										{ parser.onType($1); }
	| GUID											{ parser.onType($1); }
	| VOID											{ parser.onType($1); }
	| qualified_name								{ parser.onType(); }
	;

initializer
	: expression
	| NEW type_name
		{
			parser.onTypeNew($1);
		}
	| LCBRACE initializers RCBRACE
		{
			IPosition braces = $1.getPosition().union($3.getPosition());
			parser.createInitializer(braces);
		}
	;

initializers
	: initializer							{ parser.createInitializer(null); }
	| map_element							{ parser.createInitializer(null); }
	| initializers COMMA initializer		{ parser.extendInitializer(null); }
	| initializers COMMA map_element		{ parser.extendInitializer(null); }
	;

map_element
	: LBRACE expression COMMA initializer RBRACE		
		{
			IPosition braces = $1.getPosition().union($5.getPosition());
			parser.onMapElement(braces);
		}
	;

simple_method
	: type IDENTIFIER LBRACE parameters RBRACE compound_statement				
		{ 
			IPosition braces = $3.getPosition().union($5.getPosition());
			parser.onMethod($2, braces, true, true);
		}
	| type IDENTIFIER LBRACE RBRACE compound_statement
		{
			IPosition braces = $3.getPosition().union($4.getPosition());
			parser.onMethod($2, braces, false, true);
		}
	| type IDENTIFIER LBRACE parameters RBRACE SEMICOLON
		{ 
			IPosition braces = $3.getPosition().union($5.getPosition());
			parser.onMethod($2, braces, true, false);
		}
	| type IDENTIFIER LBRACE RBRACE SEMICOLON
		{
			IPosition braces = $3.getPosition().union($4.getPosition());
			parser.onMethod($2, braces, false, false);
		}

	;

parameters
	: type IDENTIFIER						{ parser.createParameters($2); }
	| parameters COMMA type IDENTIFIER		{ parser.extendParameters($4); }
	;

compound_statement
	: LCBRACE RCBRACE					
		{ 
			IPosition braces = $1.getPosition().union($2.getPosition());
			parser.onCompoundStatement(braces, false);
		}
	| LCBRACE statement_list RCBRACE
		{ 
			IPosition braces = $1.getPosition().union($3.getPosition());
			parser.onCompoundStatement(braces, true);
		}
						
	;

statement_list
	: statement							{ parser.createStatementList(); }
	| statement_list statement			{ parser.extendStatementList(); }
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
	: THROW expression SEMICOLON		{ parser.onThrowStatement($1); }
	;
	
try_catch_finally_statement
	: try_catch_statement
	| try_statement FINALLY compound_statement
		{
			parser.onTryFinallyStatement($2);
		}
	| try_catch_statement FINALLY compound_statement
		{
			parser.onTryFinallyStatement($2);
		}
	;

try_catch_statement
	: try_statement catch_statement
		{
			parser.onTryCatchStatement();
		}
	| try_catch_statement catch_statement
		{
			parser.onTryCatchStatement();
		}
	;

catch_statement
	: CATCH LBRACE type IDENTIFIER RBRACE compound_statement
		{ 
			parser.onCatchClause($1, $4);
		}
	;

try_statement
	: TRY compound_statement			{ parser.onTryStatement($1); }
	;
	
expression_statement
	: assignment SEMICOLON				{ parser.onStatement(); }
	| declaration SEMICOLON				{ parser.onStatement(); }
	;

selection_statement
	: IF LBRACE expression RBRACE statement
		{
			parser.onIfStatement();
		}
	/* s/r error #1 */
	| IF LBRACE expression RBRACE statement ELSE statement
		{ 
			parser.onIfElseStatement();
		}
	;

iteration_statement
	: WHILE LBRACE expression RBRACE statement
		{ 
			parser.onWhileStatement();
		}
	| DO statement WHILE LBRACE expression RBRACE SEMICOLON
		{
			parser.onDoWhileStatement();
		}
	| FOR LBRACE for_init SEMICOLON expression SEMICOLON assignment RBRACE statement
		{
			parser.onForStatement(true);
		}
	| FOR LBRACE SEMICOLON expression SEMICOLON assignment RBRACE statement
		{
			parser.onForStatement(false);
		}
	;

for_init
	: assignment
	| declaration
	;

jump_statement
	: BREAK SEMICOLON					{ parser.onJumpStatement($1); }
	| CONTINUE SEMICOLON				{ parser.onJumpStatement($1); }
	| RETURN expression SEMICOLON		{ parser.onJumpStatement($1, true); }
	| RETURN SEMICOLON					{ parser.onJumpStatement($1, false); }
	;

assignment
	: expression
	| member_access ASSIGN expression	{ parser.onAssignment(); }
	;

expression
	: logical_or
	| logical_or QUESTION expression COLON expression
		{ 
			parser.onCondition();
		}
	;

logical_or
	: logical_and
	| logical_or OR logical_and			{ parser.onOperator($2); }
	;
 
logical_and
	: equality
	| logical_and AND equality			{ parser.onOperator($2); }
	;
 
equality
	: relational
	| relational EQU relational			{ parser.onOperator($2); }
	| relational NOT_EQU relational		{ parser.onOperator($2); }
	;
 
relational
	: additive
	| additive LESS additive			{ parser.onOperator($2); }
	| additive MORE additive			{ parser.onOperator($2); }
	| additive LESS_EQU additive		{ parser.onOperator($2); }
	| additive MORE_EQU additive		{ parser.onOperator($2); }
 	;

additive
	: multiplicative
	| additive PLUS multiplicative		{ parser.onOperator($2); }
	| additive MINUS multiplicative		{ parser.onOperator($2); }
 	;

multiplicative
	: unary
	| multiplicative MUL unary			{ parser.onOperator($2); }
	| multiplicative DIV unary			{ parser.onOperator($2); }
	| multiplicative MOD unary			{ parser.onOperator($2); }
 	;

unary
	: PLUS unary
	| MINUS unary						{ parser.onUnary($1); }
	| NOT unary							{ parser.onUnary($1); }
	| LBRACE expression RBRACE			
		{
			IPosition braces = $1.getPosition().union($3.getPosition());
			parser.onGroup(braces);
		}
	| member_access
	;

expressions
	: expression						{ parser.createExpressions(); }
	| expressions COMMA expression		{ parser.extendExpressions(); }
	;

member_access
	: constant							{ parser.onMemberAccess(); }
	| this_access						{ parser.onMemberAccess(); }
	;
	
this_access
	: postfix_expression
	| THIS								{ parser.onThis($1, false); }
	| THIS DOT postfix_expression		{ parser.onThis($1, true); }
	| SUPER DOT postfix_expression		{ parser.onSuper($1); }
	| CONTAINER							{ parser.onContainer($1, false); }
	| CONTAINER DOT postfix_expression	{ parser.onContainer($1, true); }
	;

postfix_expression
	: qualified_name
	| postfix
	| postfix DOT postfix_expression	{ parser.onPostfix(); }
	;

postfix
	: array_access
	| method_call
	;

array_access
	: qualified_name LBRACKET expression RBRACKET
		{ 
			IPosition backets = $2.getPosition().union($4.getPosition());
			parser.onArrayAccess(backets);
		}
	| method_call LBRACKET expression RBRACKET
		{
			IPosition backets = $2.getPosition().union($4.getPosition());
			parser.onArrayAccess(backets);
		}
	| array_access LBRACKET expression RBRACKET
		{
			IPosition backets = $2.getPosition().union($4.getPosition());
			parser.onArrayAccess(backets);
		}
	;

method_call
	: qualified_name LBRACE expressions RBRACE
		{
			IPosition braces = $2.getPosition().union($4.getPosition());
			parser.onMethodCall(braces, true);
		}
	| qualified_name LBRACE RBRACE
		{
			IPosition braces = $2.getPosition().union($3.getPosition());
			parser.onMethodCall(braces, false);
		}
	;

declaration
	: type declarators					{ parser.onDeclaration(); }
	;
	
declarators
	: declarator						{ parser.createDeclarators(); }
	| declarators COMMA declarator		{ parser.extendDeclarators(); }
	;

declarator
	: IDENTIFIER												
		{ 
			parser.onDeclarator($1);
		}
	| IDENTIFIER ASSIGN initializer
		{
			parser.onDeclaratorInit($1);
		}
	| IDENTIFIER ASSIGN CLASS LCBRACE RCBRACE
		{
			parser.onDeclaratorClass($1, false, false);
		}
	| IDENTIFIER ASSIGN CLASS LCBRACE class_body RCBRACE
		{
			parser.onDeclaratorClass($1, false, true);
		}
	| IDENTIFIER ASSIGN modifiers CLASS LCBRACE RCBRACE
		{
			parser.onDeclaratorClass($2, true, false);
		}
	| IDENTIFIER ASSIGN modifiers CLASS LCBRACE class_body RCBRACE
		{
			parser.onDeclaratorClass($2, true, true);
		}
	;
