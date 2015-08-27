/*
1. javaYacc.exe -CGrammar.java -pParser.y Grammar.y
2. copy Grammar.java to org.zenframework.z8.compiler.parser.grammar
*/

%{
package org.zenframework.z8.compiler.parser.grammar;

//import org.zenframework.z8.compiler.core.ILanguageElement;
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
%token <token> CARET
%token <token> MUL_CARET
%token <token> CARET_MUL
%token <token> MUL_CARET_MUL

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

%token <token> CARET_ASSIGN
%token <token> MUL_CARET_ASSIGN
%token <token> CARET_MUL_ASSIGN
%token <token> MUL_CARET_MUL_ASSIGN

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
%token <token> FINAL


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
	: 
	| import
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
	: IMPORT qualified_name SEMICOLON		{ m_parser.onImportElement($1, $3); }
	;
	
attribute
	: LBRACKET IDENTIFIER RBRACKET					{ m_parser.onAttribute($1, $2, null, $3); }
	| LBRACKET IDENTIFIER CONSTANT RBRACKET			{ m_parser.onAttribute($1, $2, $3, $4); }
	| LBRACKET IDENTIFIER expression RBRACKET	{ m_parser.onAttribute($1, $2, $4); }
	;

access
	: PUBLIC								{ m_parser.onToken($1); }
	| PRIVATE								{ m_parser.onToken($1); }
	| PROTECTED								{ m_parser.onToken($1); }
	| STATIC								{ m_parser.onToken($1); }
	| AUTO 									{ m_parser.onToken($1); }
	| VIRTUAL								{ m_parser.onToken($1); }
	;

modifier
	: access
	| attribute
	;

modifiers
	: modifier								{ m_parser.onModifiers(true); }
	| modifiers modifier					{ m_parser.onModifiers(false); }
	;

qualified_name
	: IDENTIFIER							{ m_parser.onQualifiedName(true, $1); }
	| qualified_name DOT IDENTIFIER			{ m_parser.onQualifiedName(false, $3); }
	| qualified_name DOT					{ error($2); }
	;

enum_keyword
	: ENUM									{ m_parser.startEnum($1); }
	;
	
enum_name
	: IDENTIFIER							{ m_parser.setEnumName($1); }
	;

simple_enum_header							
	: enum_keyword enum_name
	;

enum_header
	: simple_enum_header					
	| modifiers simple_enum_header			{ m_parser.applyModifiers(); }
	;

enum
	: enum_header							{ error("EnumBody is missing"); }
	| enum_header enum_body
	;

enum_body_lcbrace
	: LCBRACE								{ m_parser.startEnumBody($1); }
	;
	
enum_identifier
	: IDENTIFIER							{ m_parser.addEnumMember($1); }
	;
	
enum_members
	: enum_identifier										
	| enum_members COMMA enum_identifier
	| enum_members COMMA
	;

enum_body_left
	: enum_body_lcbrace
	| enum_body_lcbrace enum_members
	;

enum_body
	: enum_body_left						{ error("'}' expected after this token"); }
	| enum_body_left RCBRACE				{ m_parser.finishEnumBody($2); }
	;

class_keyword
	: CLASS									{ m_parser.startClass(null, $1); }
	| FINAL CLASS							{ m_parser.startClass($1, $2); }
	;

class_name
	: IDENTIFIER							{ m_parser.setClassName($1); }
	;

extends_keyword
	: EXTENDS
	;

base_name
	: IDENTIFIER							{ m_parser.setClassBase($1); }
	;

simple_class_header
	: class_keyword class_name
	;
	
extended_class_header
	: simple_class_header
	| simple_class_header extends_keyword base_name
	;
	
class_header
	: extended_class_header
	| modifiers extended_class_header		{ m_parser.applyModifiers(); }
	;

class
	: class_header class_body				{ m_parser.finishClass(); }
	;

noname_class
	: class_keyword class_body				{ m_parser.finishClass(); }
	;

class_body_lcbrace
	: LCBRACE								{ m_parser.startClassBody($1); }
	;

class_body_left
	: class_body_lcbrace
	| class_body_lcbrace class_members
	;

class_body
	: class_body_left RCBRACE				{ m_parser.finishClassBody($2); }
	;

variable_declarator_init1
	: variable_declarator_init
	| modifiers variable_declarator_init	{ m_parser.applyModifiers(); }
	;
	
qualified_name_init1
	: qualified_name_init
	| modifiers qualified_name_init			{ m_parser.applyModifiers(); }
	;
	
method1
	: method
	| modifiers method						{ m_parser.applyModifiers(); }
	;

records1
	: records
	| modifiers records						{ m_parser.applyModifiers(); }
	;

class_member
	: class_member1							{ m_parser.addClassMember(); }	
	;
	
class_member1
	: variable_declarator_init1 SEMICOLON
	| qualified_name_init1 SEMICOLON
	| variable_declarator_init1 qualified_name_init1 SEMICOLON		{ m_parser.onMissingSemicolon();}
	| variable_declarator_init1 variable_declarator_init1 SEMICOLON	{ m_parser.onMissingSemicolon();}
	| qualified_name_init1 variable_declarator_init1 SEMICOLON 		{ m_parser.onMissingSemicolon();}
	| qualified_name_init1 qualified_name_init1 SEMICOLON			{ m_parser.onMissingSemicolon();}
/*	| qualified_name_init1 method1									{ m_parser.onMissingSemicolon();}*/
	| variable_declarator_init1 method1								{ m_parser.onMissingSemicolon();}
	| method1
	| records1
	;
	
class_members
	: class_member
	| class_members class_member
	;

records_keyword
	: RECORDS								{ m_parser.startRecords($1); }
	;

records
	: records_keyword records_body			{ m_parser.finishRecords(); }
	;
	
records_lcbrace
	: LCBRACE								{ m_parser.startRecordsBody($1); }
	;	

records_body_left
	: records_lcbrace
	| records_lcbrace records_list
	;
	
records_body
	: records_body_left RCBRACE				{ m_parser.finishRecordsBody($2); }
	;

record_name
	: IDENTIFIER							{ m_parser.startRecord($1); }
	;
		
simple_record
	: record_name ASSIGN CONSTANT			{ m_parser.setRecordValue($3); }
	;
	
record1
	: simple_record
	| modifiers simple_record				{ m_parser.applyModifiers(); }
	;

record
	: record1 SEMICOLON						{ m_parser.addRecord(); }
	;

records_list
	: record
	| records_list record
	;

simple_type
	: qualified_name							{ m_parser.onVariableType(false); }
	;
	
array_type
	: qualified_name indices					{ m_parser.onVariableType(true); }
	;

type
	: simple_type
	| array_type
	;
	
variable_declarator
	: variable_declarator1
	| variable_declarator1 DOT					{ error($2); }
	;
	
variable_declarator1
	: type IDENTIFIER							{ m_parser.onVariableDeclarator($2); }
	| FINAL type IDENTIFIER						{ m_parser.onVariableDeclarator($1, $3); }
	;
	
variable_declarator_init
	: variable_declarator
	| variable_declarator ASSIGN initializer	{ m_parser.onVariableDeclaratorInit($2); }
	;
	
qualified_name_init
	: qualified_name									{ m_parser.onVariableInit(null); }
	| qualified_name ASSIGN initializer					{ m_parser.onVariableInit($2); }
	| qualified_name ADD_ASSIGN initializer				{ m_parser.onVariableInit($2); }
	| qualified_name CARET_ASSIGN initializer			{ m_parser.onVariableInit($2); }
	| qualified_name MUL_CARET_ASSIGN initializer		{ m_parser.onVariableInit($2); }
	| qualified_name CARET_MUL_ASSIGN initializer		{ m_parser.onVariableInit($2); }
	| qualified_name MUL_CARET_MUL_ASSIGN initializer	{ m_parser.onVariableInit($2); }
	;

method_header
	: type IDENTIFIER parameters				{ m_parser.createMethod($2); }
	| OPERATOR parameters						{ m_parser.createPriorityOperator($1); }
	| OPERATOR IDENTIFIER parameters			{ m_parser.createCastOperator($1, $2); }
	| type OPERATOR NOT parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR MUL parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR DIV parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR MOD parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR ADD parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR SUB parameters				{ m_parser.createOperator($2, $3); }

	| type OPERATOR CARET parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR MUL_CARET parameters			{ m_parser.createOperator($2, $3); }
	| type OPERATOR CARET_MUL parameters			{ m_parser.createOperator($2, $3); }
	| type OPERATOR MUL_CARET_MUL parameters		{ m_parser.createOperator($2, $3); }

	| type OPERATOR EQU parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR NOT_EQU parameters			{ m_parser.createOperator($2, $3); }
	| type OPERATOR LESS parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR MORE parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR LESS_EQU parameters			{ m_parser.createOperator($2, $3); }
	| type OPERATOR MORE_EQU parameters			{ m_parser.createOperator($2, $3); }
	| type OPERATOR AND parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR OR parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR ASSIGN parameters			{ m_parser.createOperator($2, $3); }

	| type OPERATOR ADD_ASSIGN parameters		{ m_parser.createOperator($2, $3); }
	| type OPERATOR SUB_ASSIGN parameters		{ m_parser.createOperator($2, $3); }
	| type OPERATOR MUL_ASSIGN parameters		{ m_parser.createOperator($2, $3); }
	| type OPERATOR DIV_ASSIGN parameters		{ m_parser.createOperator($2, $3); }
	| type OPERATOR MOD_ASSIGN parameters		{ m_parser.createOperator($2, $3); }

	| type OPERATOR CARET_ASSIGN parameters				{ m_parser.createOperator($2, $3); }
	| type OPERATOR MUL_CARET_ASSIGN parameters			{ m_parser.createOperator($2, $3); }
	| type OPERATOR CARET_MUL_ASSIGN parameters			{ m_parser.createOperator($2, $3); }
	| type OPERATOR MUL_CARET_MUL_ASSIGN parameters		{ m_parser.createOperator($2, $3); }
	;

method
	: method_header compound				{ m_parser.setMethodBody(); }
	| method_header SEMICOLON
	;

parameter
	: variable_declarator					{ m_parser.createParameter(); }
	;
	
parameters_list
	: parameter
	| parameters_list COMMA parameter
	;

parameters_left
	: LBRACE									{ m_parser.startParameters($1); }
	;
	
parameters
	: parameters_left RBRACE					{ m_parser.finishParameters($2); }
	| parameters_left parameters_list RBRACE	{ m_parser.finishParameters($3); }
	;
	
initializer
	: noname_class
	| array_initializer
	;
		
array_initializer_left
	: LCBRACE								{ m_parser.startArrayInitializer($1); }
	;
	
array_initializer
	: expression
	| array_initializer_left RCBRACE
	| array_initializer_left array_initializers RCBRACE			{ m_parser.finishArrayInitializer($3); }
	| array_initializer_left array_initializers COMMA RCBRACE	{ m_parser.finishArrayInitializer($4); }
	;

array_initializers
	: array_initializers_element
	| array_initializers COMMA array_initializers_element
	;

array_initializers_element
	: map_element									{ m_parser.addArrayInitializer(); }
	| array_initializer								{ m_parser.addArrayInitializer(); }
	;
	
map_element
 	: LBRACE expression COMMA array_initializer	RBRACE		
 		{ m_parser.onMapElement($1, $5); }
 	;

compound_left
	: LCBRACE							{ m_parser.startCompound($1); }
	;
	
compound
	: compound_left RCBRACE				{ m_parser.finishCompound($2); }
	| compound_left statements RCBRACE	{ m_parser.finishCompound($3); }
	;

statements
	: statement							{ m_parser.addStatement(); }
	| statements statement				{ m_parser.addStatement(); }
	;

declarator_statement
	: variable_declarator_init			{ m_parser.onDeclarator(); }
	;
	
statement
	: try_catch_finally
	| throw
	| assignment SEMICOLON					{ m_parser.onStatement(); }
	| declarator_statement SEMICOLON		{ m_parser.onStatement(); }
	
/*	| assignment assignment SEMICOLON*/
	| declarator_statement declarator_statement SEMICOLON
												{ m_parser.onStatement2(); }
	| declarator_statement assignment SEMICOLON
												{ m_parser.onStatement2(); }

	| compound
	| selection
	| iteration
	| jump
	
	| assignment try_catch_finally				{ m_parser.onStatement1(); }
	| assignment throw							{ m_parser.onStatement1(); }
	| assignment compound						{ m_parser.onStatement1(); }
	| assignment selection						{ m_parser.onStatement1(); }
	| assignment iteration						{ m_parser.onStatement1(); }

	| declarator_statement try_catch_finally	{ m_parser.onStatement1(); }
	| declarator_statement throw				{ m_parser.onStatement1(); }
	| declarator_statement compound				{ m_parser.onStatement1(); }
	| declarator_statement selection			{ m_parser.onStatement1(); }
	| declarator_statement iteration			{ m_parser.onStatement1(); }
	| declarator_statement jump					{ m_parser.onStatement1(); }
	;

throw
	: THROW expression SEMICOLON			{ m_parser.onThrowStatement($1); }
	;
	
try
	: TRY compound							{ m_parser.onTryStatement($1); }
	;

catch
	: CATCH LBRACE variable_declarator RBRACE compound		
			{ m_parser.onCatchClause($1); }
	;
	
finally
	: FINALLY compound						{ m_parser.onFinallyStatement($1); }
	;

try_catch
	: try catch								{ m_parser.addCatchStatement(); }
	| try_catch catch						{ m_parser.addCatchStatement(); }
	;

try_catch_finally
	: try_catch
	| try finally
	| try_catch finally
	;

selection
	: IF braced_expression statement
		{ m_parser.onIfStatement($1, null); }
	| IF braced_expression statement ELSE statement
		{ m_parser.onIfStatement($1, $4); }
	;

while_loop
	: WHILE braced_expression statement
		{ m_parser.onWhileStatement($1); }
	;

do_while_loop
	: DO statement WHILE braced_expression SEMICOLON
		{ m_parser.onDoWhileStatement($1, $3); }
	;

for_init
	: variable_declarator_init
		{ m_parser.onDeclarator(); }
	| assignment
	;

for_statement
	: FOR LBRACE SEMICOLON expression SEMICOLON assignment RBRACE statement
		{ m_parser.onForStatement($1, false); }
	| FOR LBRACE for_init SEMICOLON expression SEMICOLON assignment RBRACE statement
		{ m_parser.onForStatement($1, true); }
	;

iteration
	: while_loop
	| do_while_loop
	| for_statement
	;

jump
	: RETURN SEMICOLON						{ m_parser.onJumpStatement($1, false); }
	| RETURN expression SEMICOLON			{ m_parser.onJumpStatement($1, true); }	
	| BREAK SEMICOLON						{ m_parser.onJumpStatement($1, false); }
	| CONTINUE SEMICOLON					{ m_parser.onJumpStatement($1, false); }
	;

assignment
	: expression
	| postfix ASSIGN array_initializer		{ m_parser.onAssignment($2); }
	| postfix ADD_ASSIGN expression			{ m_parser.onOperator($2); }
	| postfix SUB_ASSIGN expression			{ m_parser.onOperator($2); }
	| postfix MUL_ASSIGN expression			{ m_parser.onOperator($2); }
	| postfix DIV_ASSIGN expression			{ m_parser.onOperator($2); }
	| postfix MOD_ASSIGN expression			{ m_parser.onOperator($2); }
	| postfix CARET_ASSIGN expression			{ m_parser.onOperator($2); }
	| postfix MUL_CARET_ASSIGN expression		{ m_parser.onOperator($2); }
	| postfix CARET_MUL_ASSIGN expression		{ m_parser.onOperator($2); }
	| postfix MUL_CARET_MUL_ASSIGN expression	{ m_parser.onOperator($2); }
	;

expression
	: ternary_expression
	;
	
ternary_expression
	: logical_or
	| logical_or QUESTION expression COLON expression
		{ m_parser.onCondition(); }
	;

logical_or
	: logical_and
	| logical_or OR logical_and				{ m_parser.onOperator($2); }
	;
 
logical_and
	: equality
	| logical_and AND equality				{ m_parser.onOperator($2); }
	;
 
equality
	: relational
	| relational EQU relational				{ m_parser.onOperator($2); }
	| relational NOT_EQU relational			{ m_parser.onOperator($2); }
	; 

relational
	: additive
	| additive LESS additive				{ m_parser.onOperator($2); }
	| additive MORE additive				{ m_parser.onOperator($2); }
	| additive LESS_EQU additive			{ m_parser.onOperator($2); }
	| additive MORE_EQU additive			{ m_parser.onOperator($2); }
	| relational CARET additive				{ m_parser.onOperator($2); }
	| relational MUL_CARET additive			{ m_parser.onOperator($2); }
	| relational CARET_MUL additive			{ m_parser.onOperator($2); }
	| relational MUL_CARET_MUL additive		{ m_parser.onOperator($2); }
	;

additive
	: multiplicative
	| additive ADD multiplicative			{ m_parser.onOperator($2); }
	| additive SUB multiplicative			{ m_parser.onOperator($2); }
	;

multiplicative
	: unary
	| multiplicative MUL unary				{ m_parser.onOperator($2); }
	| multiplicative DIV unary				{ m_parser.onOperator($2); }
	| multiplicative MOD unary				{ m_parser.onOperator($2); }
	;

unary
	: type_cast
	| ADD unary
	| SUB unary								{ m_parser.onUnary($1); }
	| NOT unary								{ m_parser.onUnary($1); }
	;

type_cast
	: postfix
	| braced_expression postfix				{ m_parser.onTypeCast(); }
	;
	
new
	: NEW IDENTIFIER						{ m_parser.onNewExpression($1, $2); }
	;
	
container
	: CONTAINER								{ m_parser.onContainer($1); }
	| container DOT CONTAINER				{ m_parser.addContainer($3); }
	;
	
braced_expression
	: LBRACE expression RBRACE
		{ m_parser.onBracedExpression($1, $3); }
	;

prefix
	: THIS									{ m_parser.onThis($1); }
	| SUPER									{ m_parser.onSuper($1); }
	| CONSTANT								{ m_parser.onConstant($1); }
	| new
	| braced_expression
	| simple_type							{ m_parser.onTypeToPostfix(); }
	| array_type							{ m_parser.onTypeToPostfix(); }
	| method_call							{ m_parser.onMethodCall(false); }
	;
	
index_expression
	: LBRACKET RBRACKET						{ m_parser.onIndex($1, $2, false); }
	| LBRACKET expression RBRACKET			{ m_parser.onIndex($1, $3, true); }
	;

indices
	: index_expression						{ m_parser.onIndices(true); }
	| indices index_expression				{ m_parser.onIndices(false); }
	;

postfix
	: prefix
	| container
	| container DOT qualified_name			{ m_parser.onPostfix(); }
	| container DOT method_call				{ m_parser.onMethodCall(true); }
	| container DOT							{ error($2); }
	| postfix indices						{ m_parser.onArrayAccess(); }
	| postfix DOT qualified_name			{ m_parser.onPostfix(); }
	| postfix DOT method_call				{ m_parser.onMethodCall(true); }
	| postfix DOT							{ error($2); }
	;

method_call
	: qualified_name expressions
	;
	
expressions_left
	: LBRACE							{ m_parser.startExpressions($1); }
	;
	
expressions
	: expressions_left RBRACE
		{ m_parser.finishExpressions($2); }
	| expressions_left expressions_list RBRACE
		{ m_parser.finishExpressions($3); }
	;
	
expressions_list
	: expression						{ m_parser.addExpression(); }
	| expressions_list COMMA expression	{ m_parser.addExpression(); }
	;
	