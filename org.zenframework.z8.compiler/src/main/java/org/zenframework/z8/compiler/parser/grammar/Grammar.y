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
%token <token> ELVIS
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
%token <token> INSTANCE_OF


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
	: import_list                               { parser.onImport(); }
	;

import_list
	: import_element                            { parser.onImportList(true); }
	| import_list import_element                { parser.onImportList(false); }
	;

import_element
	: IMPORT qualified_name SEMICOLON           { parser.onImportElement($1, $3); }
	;
	
attribute
	: LBRACKET IDENTIFIER RBRACKET              { parser.onAttribute($1, $2, null, $3); }
	| LBRACKET IDENTIFIER CONSTANT RBRACKET     { parser.onAttribute($1, $2, $3, $4); }
	| LBRACKET IDENTIFIER expression RBRACKET   { parser.onAttribute($1, $2, $4); }
	;

access
	: PUBLIC                                    { parser.onToken($1); }
	| PRIVATE                                   { parser.onToken($1); }
	| PROTECTED                                 { parser.onToken($1); }
	| STATIC                                    { parser.onToken($1); }
	| AUTO                                      { parser.onToken($1); }
	| VIRTUAL                                   { parser.onToken($1); }
	;

modifier
	: access
	| attribute
	;

modifiers
	: modifier                                  { parser.onModifiers(true); }
	| modifiers modifier                        { parser.onModifiers(false); }
	;

qualified_name
	: IDENTIFIER                                { parser.onQualifiedName(true, $1); }
	| qualified_name DOT IDENTIFIER             { parser.onQualifiedName(false, $3); }
	| qualified_name DOT                        { error($2); }
	;

enum_keyword
	: ENUM                                      { parser.startEnum($1); }
	;
	
enum_name
	: IDENTIFIER                                { parser.setEnumName($1); }
	;

simple_enum_header
	: enum_keyword enum_name
	;

enum_header
	: simple_enum_header
	| modifiers simple_enum_header              { parser.applyModifiers(); }
	;

enum
	: enum_header                               { error("EnumBody is missing"); }
	| enum_header enum_body
	;

enum_body_lcbrace
	: LCBRACE                                   { parser.startEnumBody($1); }
	;
	
enum_identifier
	: IDENTIFIER                                { parser.addEnumMember($1); }
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
	: enum_body_left                            { error("'}' expected after this token"); }
	| enum_body_left RCBRACE                    { parser.finishEnumBody($2); }
	;

class_keyword
	: CLASS                                     { parser.startClass(null, $1); }
	| FINAL CLASS                               { parser.startClass($1, $2); }
	;

class_name
	: IDENTIFIER                                { parser.setClassName($1); }
	;

extends_keyword
	: EXTENDS
	;

base_name
	: qualified_name                            { parser.setClassBase(); }
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
	| modifiers extended_class_header           { parser.applyModifiers(); }
	;

class
	: class_header class_body                   { parser.finishClass(); }
	;

noname_class
	: class_keyword class_body                  { parser.finishClass(); }
	;

class_body_lcbrace
	: LCBRACE                                   { parser.startClassBody($1); }
	;

class_body_left
	: class_body_lcbrace
	| class_body_lcbrace class_members
	;

class_body
	: class_body_left RCBRACE                   { parser.finishClassBody($2); }
	;

variable_declarator_init1
	: variable_declarator_init
	| modifiers variable_declarator_init        { parser.applyModifiers(); }
	;
	
qualified_name_init1
	: qualified_name_init
	| modifiers qualified_name_init             { parser.applyModifiers(); }
	;
	
method1
	: method
	| modifiers method                          { parser.applyModifiers(); }
	;

records1
	: records
	| modifiers records                         { parser.applyModifiers(); }
	;

class_member
	: class_member1                         { parser.addClassMember(); }
	;

class_member1
	: variable_declarator_init1 SEMICOLON
	| qualified_name_init1 SEMICOLON
	| variable_declarator_init1 qualified_name_init1 SEMICOLON      { parser.onMissingSemicolon();}
	| variable_declarator_init1 variable_declarator_init1 SEMICOLON { parser.onMissingSemicolon();}
	| qualified_name_init1 variable_declarator_init1 SEMICOLON      { parser.onMissingSemicolon();}
	| qualified_name_init1 qualified_name_init1 SEMICOLON           { parser.onMissingSemicolon();}
	| variable_declarator_init1 method1                             { parser.onMissingSemicolon();}
	| method1
	| records1
	;

class_members
	: class_member
	| class_members class_member
	;

records_keyword
	: RECORDS                                   { parser.startRecords($1); }
	;

records
	: records_keyword records_body              { parser.finishRecords(); }
	;

records_lcbrace
	: LCBRACE                                   { parser.startRecordsBody($1); }
	;

records_body_left
	: records_lcbrace
	| records_lcbrace records_list
	;
	
records_body
	: records_body_left RCBRACE                 { parser.finishRecordsBody($2); }
	;

record_name
	: IDENTIFIER                                { parser.startRecord($1); }
	;
		
simple_record
	: record_name ASSIGN CONSTANT               { parser.setRecordValue($3); }
	;
	
record1
	: simple_record
	| modifiers simple_record                   { parser.applyModifiers(); }
	;

record
	: record1 SEMICOLON                         { parser.addRecord(); }
	;

records_list
	: record
	| records_list record
	;

simple_type
	: qualified_name                            { parser.onVariableType(false); }
	;

array_type
	: qualified_name indices                    { parser.onVariableType(true); }
	;

type
	: simple_type
	| array_type
	;

variable_declarator
	: variable_declarator1
	| variable_declarator1 DOT                  { error($2); }
	;

variable_declarator1
	: type IDENTIFIER                           { parser.onVariableDeclarator($2); }
	| FINAL type IDENTIFIER                     { parser.onVariableDeclarator($1, $3); }
	;

variable_declarator_init
	: variable_declarator
	| variable_declarator ASSIGN initializer    { parser.onVariableDeclaratorInit($2); }
	;

qualified_name_init
	: qualified_name                            { parser.onVariableInit(null); }
	| qualified_name ASSIGN initializer         { parser.onVariableInit($2); }
	| qualified_name ADD_ASSIGN initializer     { parser.onOperatorAssign($2); parser.onVariableInit($2); }
	;

method_header
	: type IDENTIFIER parameters                { parser.createMethod($2); }
	| OPERATOR parameters                       { parser.createPriorityOperator($1); }
	| OPERATOR IDENTIFIER parameters            { parser.createCastOperator($1, $2); }
	| type OPERATOR NOT parameters              { parser.createOperator($2, $3); }
	| type OPERATOR MUL parameters              { parser.createOperator($2, $3); }
	| type OPERATOR DIV parameters              { parser.createOperator($2, $3); }
	| type OPERATOR MOD parameters              { parser.createOperator($2, $3); }
	| type OPERATOR ADD parameters              { parser.createOperator($2, $3); }
	| type OPERATOR SUB parameters              { parser.createOperator($2, $3); }

	| type OPERATOR EQU parameters              { parser.createOperator($2, $3); }
	| type OPERATOR NOT_EQU parameters          { parser.createOperator($2, $3); }
	| type OPERATOR LESS parameters             { parser.createOperator($2, $3); }
	| type OPERATOR MORE parameters             { parser.createOperator($2, $3); }
	| type OPERATOR LESS_EQU parameters         { parser.createOperator($2, $3); }
	| type OPERATOR MORE_EQU parameters         { parser.createOperator($2, $3); }
	| type OPERATOR AND parameters              { parser.createOperator($2, $3); }
	| type OPERATOR OR parameters               { parser.createOperator($2, $3); }
	| type OPERATOR ASSIGN parameters           { parser.createOperator($2, $3); }
	;

method
	: method_header compound                    { parser.setMethodBody(); }
	| method_header SEMICOLON
	;

parameter
	: variable_declarator                       { parser.createParameter(); }
	;

parameters_list
	: parameter
	| parameters_list COMMA parameter
	;

parameters_left
	: LBRACE                                    { parser.startParameters($1); }
	;

parameters
	: parameters_left RBRACE                    { parser.finishParameters($2); }
	| parameters_left parameters_list RBRACE    { parser.finishParameters($3); }
	;

initializer
	: noname_class
	| expression
	| array_initializer
	;

array_initializer_left
	: LCBRACE                                   { parser.startArrayInitializer($1); }
	;

array_initializer
	: array_initializer_left RCBRACE
	| array_initializer_left array_initializers RCBRACE         { parser.finishArrayInitializer($3); }
	| array_initializer_left array_initializers COMMA RCBRACE   { parser.finishArrayInitializer($4); }
	;

array_initializers
	: array_initializers_element
	| array_initializers COMMA array_initializers_element
	;

array_initializers_element
	: map_element                               { parser.addArrayInitializer(); }
	| expression                                { parser.addArrayInitializer(); }
	| array_initializer                         { parser.addArrayInitializer(); }
	;

map_element
	: LBRACE expression COMMA expression RBRACE         { parser.onMapElement($1, $5); }
	| LBRACE expression COMMA array_initializer RBRACE  { parser.onMapElement($1, $5); }
	;

compound_left
	: LCBRACE                                   { parser.startCompound($1); }
	;

compound
	: compound_left RCBRACE                     { parser.finishCompound($2); }
	| compound_left statements RCBRACE          { parser.finishCompound($3); }
	;

statements
	: statement                                 { parser.addStatement(); }
	| statements statement                      { parser.addStatement(); }
	;

declarator_statement
	: variable_declarator_init                  { parser.onDeclarator(); }
	;

statement
	: try_catch_finally
	| throw
	| assignment SEMICOLON                      { parser.onStatement(); }
	| declarator_statement SEMICOLON            { parser.onStatement(); }

/*  | assignment assignment SEMICOLON */
	| declarator_statement declarator_statement SEMICOLON
												{ parser.onStatement2(); }
	| declarator_statement assignment SEMICOLON
												{ parser.onStatement2(); }

	| compound
	| selection
	| iteration
	| jump

	| assignment try_catch_finally              { parser.onStatement1(); }
	| assignment throw                          { parser.onStatement1(); }
	| assignment compound                       { parser.onStatement1(); }
	| assignment selection                      { parser.onStatement1(); }
	| assignment iteration                      { parser.onStatement1(); }

	| declarator_statement try_catch_finally    { parser.onStatement1(); }
	| declarator_statement throw                { parser.onStatement1(); }
	| declarator_statement compound             { parser.onStatement1(); }
	| declarator_statement selection            { parser.onStatement1(); }
	| declarator_statement iteration            { parser.onStatement1(); }
	| declarator_statement jump                 { parser.onStatement1(); }
	;

throw
	: THROW expression SEMICOLON                { parser.onThrowStatement($1); }
	;

try
	: TRY compound                              { parser.onTryStatement($1); }
	;

catch
	: CATCH LBRACE variable_declarator RBRACE compound
			{ parser.onCatchClause($1); }
	;

finally
	: FINALLY compound                          { parser.onFinallyStatement($1); }
	;

try_catch
	: try catch                                 { parser.addCatchStatement(); }
	| try_catch catch                           { parser.addCatchStatement(); }
	;

try_catch_finally
	: try_catch
	| try finally
	| try_catch finally
	;

selection
	: IF braced_expression statement                     { parser.onIfStatement($1, null); }
	| IF braced_expression statement ELSE statement      { parser.onIfStatement($1, $4); }
	;

while_loop
	: WHILE braced_expression statement         { parser.onWhileStatement($1); }
	;

do_while_loop
	: DO statement WHILE braced_expression SEMICOLON     { parser.onDoWhileStatement($1, $3); }
	;

for_init
	: variable_declarator_init                  { parser.onDeclarator(); }
	| assignment
	;

for_statement
	: FOR LBRACE SEMICOLON expression SEMICOLON assignment RBRACE statement
		{ parser.onForStatement($1, false); }
	| FOR LBRACE for_init SEMICOLON expression SEMICOLON assignment RBRACE statement
		{ parser.onForStatement($1, true); }
	;

iteration
	: while_loop
	| do_while_loop
	| for_statement
	;

jump
	: RETURN SEMICOLON                          { parser.onJumpStatement($1, false); }
	| RETURN expression SEMICOLON               { parser.onJumpStatement($1, true); }	
	| RETURN array_initializer SEMICOLON        { parser.onJumpStatement($1, true); }	
	| BREAK SEMICOLON                           { parser.onJumpStatement($1, false); }
	| CONTINUE SEMICOLON                        { parser.onJumpStatement($1, false); }
	;

assignment
	: expression
	| postfix ASSIGN expression                 { parser.onAssignment($2); }
	| postfix ASSIGN array_initializer          { parser.onAssignment($2); }
	| postfix ADD_ASSIGN expression             { parser.onOperatorAssign($2); parser.onAssignment($2); }
	| postfix SUB_ASSIGN expression             { parser.onOperatorAssign($2); parser.onAssignment($2); }
	| postfix MUL_ASSIGN expression             { parser.onOperatorAssign($2); parser.onAssignment($2); }
	| postfix DIV_ASSIGN expression             { parser.onOperatorAssign($2); parser.onAssignment($2); }
	| postfix MOD_ASSIGN expression             { parser.onOperatorAssign($2); parser.onAssignment($2); }
	;

expression
	: ternary_expression
	| ternary_expression INSTANCE_OF simple_type         { parser.onInstanceOf($2); }
	;

ternary_expression
	: logical_or
	| logical_or QUESTION expression COLON expression    { parser.onTernaryOperator($2, $4); }
	| logical_or ELVIS expression					     { parser.onElvisOperator($2); }
	| logical_or ELVIS array_initializer				 { parser.onElvisOperator($2); }
	;

logical_or
	: logical_and
	| logical_or OR logical_and                 { parser.onOperator($2); }
	;

logical_and
	: equality
	| logical_and AND equality                  { parser.onOperator($2); }
	;

equality
	: relational
	| relational EQU relational                 { parser.onOperator($2); }
	| relational NOT_EQU relational             { parser.onOperator($2); }
	;

relational
	: additive
	| NULL									    { parser.onNull($1); }
	| additive LESS additive                    { parser.onOperator($2); }
	| additive MORE additive                    { parser.onOperator($2); }
	| additive LESS_EQU additive                { parser.onOperator($2); }
	| additive MORE_EQU additive                { parser.onOperator($2); }
	;

additive
	: multiplicative
	| additive ADD multiplicative               { parser.onOperator($2); }
	| additive SUB multiplicative               { parser.onOperator($2); }
	;

multiplicative
	: unary
	| multiplicative MUL unary                  { parser.onOperator($2); }
	| multiplicative DIV unary                  { parser.onOperator($2); }
	| multiplicative MOD unary                  { parser.onOperator($2); }
	;

unary
	: type_cast
	| ADD unary
	| SUB unary                                 { parser.onUnary($1); }
	| NOT unary                                 { parser.onUnary($1); }
	;

type_cast
	: postfix
	| braced_expression postfix                 { parser.onTypeCast(); }
	| array_type array_initializer              { parser.onArrayTypeCast(); }
	;

new
	: NEW IDENTIFIER                            { parser.onNewExpression($1, $2); }
	;

container
	: CONTAINER                                 { parser.onContainer($1); }
	| container DOT CONTAINER                   { parser.addContainer($3); }
	;

braced_expression
	: LBRACE expression RBRACE                  { parser.onBracedExpression($1, $3); }
	| new
	;

prefix
	: THIS                                      { parser.onThis($1); }
	| SUPER                                     { parser.onSuper($1); }
	| CONSTANT                                  { parser.onConstant($1); }
	| braced_expression
	| simple_type                               { parser.onTypeToPostfix(); }
	| array_type                                { parser.onTypeToPostfix(); }
	| method_call                               { parser.onMethodCall(false); }
	;

index_expression
	: LBRACKET RBRACKET                         { parser.onIndex($1, $2, false); }
	| LBRACKET expression RBRACKET              { parser.onIndex($1, $3, true); }
	;

indices
	: index_expression                          { parser.onIndices(true); }
	| indices index_expression                  { parser.onIndices(false); }
	;

postfix
	: prefix
	| container
	| container DOT qualified_name              { parser.onPostfix(); }
	| container DOT method_call                 { parser.onMethodCall(true); }
	| container DOT                             { error($2); }
	| postfix indices                           { parser.onArrayAccess(); }
	| postfix DOT qualified_name                { parser.onPostfix(); }
	| postfix DOT method_call                   { parser.onMethodCall(true); }
	| postfix DOT                               { error($2); }
	;

method_call
	: qualified_name expressions
	;

expressions_left
	: LBRACE                                    { parser.startExpressions($1); }
	;

expressions
	: expressions_left RBRACE                   { parser.finishExpressions($2); }
	| expressions_left expressions_list RBRACE  { parser.finishExpressions($3); }
	;

expressions_list
	: expression                                { parser.addExpression(); }
	| array_initializer                         { parser.addExpression(); }
	| expressions_list COMMA expression         { parser.addExpression(); }
	| expressions_list COMMA array_initializer  { parser.addExpression(); }
	;
