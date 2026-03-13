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

%token <token> Whitespace
%token <token> Linebreak
%token <token> Comment

%token <token> Not
%token <token> BitwiseNot

%token <token> Mul
%token <token> Div
%token <token> Mod
%token <token> Plus
%token <token> Minus

%token <token> Equ
%token <token> NotEqu
%token <token> Less
%token <token> More
%token <token> LessEqu
%token <token> MoreEqu

%token <token> BitwiseAnd
%token <token> BitwiseOr
%token <token> BitwiseXor

%token <token> And
%token <token> Or

%token <token> AddAssign
%token <token> SubAssign
%token <token> MulAssign
%token <token> DivAssign
%token <token> ModAssign
%token <token> BitwiseAndAssign
%token <token> BitwiseOrAssign
%token <token> BitwiseXorAssign

%token <token> LBrace
%token <token> RBrace
%token <token> LBracket
%token <token> RBracket
%token <token> LCBrace
%token <token> RCBrace
%token <token> Brackets
%token <token> Colon
%token <token> Semicolon
%token <token> Assign
%token <token> Question
%token <token> Comma
%token <token> Elvis
%token <token> Dot

%token <token> Constant
%token <token> Identifier

%token <token> Operator

%token <token> If
%token <token> Else

%token <token> Do
%token <token> For
%token <token> While

%token <token> Break
%token <token> Return
%token <token> Continue

%token <token> This
%token <token> Super
%token <token> Container

%token <token> Null

%token <token> Import
%token <token> Class
%token <token> Public
%token <token> Protected
%token <token> Private
%token <token> Extends

%token <token> Enum
%token <token> Records

%token <token> Auto

%token <token> New
%token <token> Static

%token <token> Try
%token <token> Catch
%token <token> Finally
%token <token> Throw

%token <token> Virtual
%token <token> Final
%token <token> InstanceOf


%left Comma Semicolon
%left Colon
%left Assign
%left Or
%left And
%left BitwiseOr
%left BitwiseXor
%left BitwiseAnd
%left Equ NotEqu
%left Less More LessEqu MoreEqu
%left Plus Minus
%left Mul Div Mod
%left Question
%left LBrace RBrace
%left LBracket RBracket

%%
/* ========================================================================================= */

program
	: 
	| import
	| classes
	| import classes
	;

classes
	: classEnum
	| classes classEnum
	;

classEnum
	: class
	| enum
	;

import
	: importList                               { parser.onImport(); }
	;

importList
	: importElement                            { parser.onImportList(true); }
	| importList importElement                 { parser.onImportList(false); }
	;

importElement
	: Import qualifiedName Semicolon           { parser.onImportElement($1, $3); }
	;
	
attribute
	: LBracket Identifier RBracket             { parser.onAttribute($1, $2, null, $3); }
	| LBracket Identifier Constant RBracket    { parser.onAttribute($1, $2, $3, $4); }
	| LBracket Identifier expression RBracket  { parser.onAttribute($1, $2, $4); }
	;

access
	: Public                                   { parser.onToken($1); }
	| Private                                  { parser.onToken($1); }
	| Protected                                { parser.onToken($1); }
	| Static                                   { parser.onToken($1); }
	| Auto                                     { parser.onToken($1); }
	| Virtual                                  { parser.onToken($1); }
	;

modifier
	: access
	| attribute
	;

modifiers
	: modifier                                 { parser.onModifiers(true); }
	| modifiers modifier                       { parser.onModifiers(false); }
	;

qualifiedName
	: Identifier                               { parser.onQualifiedName(true, $1); }
	| qualifiedName Dot Identifier             { parser.onQualifiedName(false, $3); }
	| qualifiedName Dot                        { error($2); }
	;

enumKeyword
	: Enum                                     { parser.startEnum($1); }
	;
	
enumName
	: Identifier                               { parser.setEnumName($1); }
	;

simpleEnumHeader
	: enumKeyword enumName
	;

enumHeader
	: simpleEnumHeader
	| modifiers simpleEnumHeader               { parser.applyModifiers(); }
	;

enum
	: enumHeader                               { error("EnumBody is missing"); }
	| enumHeader enumBody
	;

enumBodyLCBrace
	: LCBrace                                  { parser.startEnumBody($1); }
	;

enumIdentifier
	: Identifier                               { parser.addEnumMember($1); }
	;

enumMembers
	: enumIdentifier
	| enumMembers Comma enumIdentifier
	| enumMembers Comma
	;

enumBodyLeft
	: enumBodyLCBrace
	| enumBodyLCBrace enumMembers
	;

enumBody
	: enumBodyLeft                             { error("'}' expected after this token"); }
	| enumBodyLeft RCBrace                     { parser.finishEnumBody($2); }
	;

classKeyword
	: Class                                    { parser.startClass(null, $1); }
	| Final Class                              { parser.startClass($1, $2); }
	;

className
	: Identifier                               { parser.setClassName($1); }
	;

extendsKeyword
	: Extends
	;

baseName
	: qualifiedName                            { parser.setClassBase(); }
	;

simpleClassHeader
	: classKeyword className
	;
	
extendedClassHeader
	: simpleClassHeader
	| simpleClassHeader extendsKeyword baseName
	;
	
classHeader
	: extendedClassHeader
	| modifiers extendedClassHeader            { parser.applyModifiers(); }
	;

class
	: classHeader classBody                    { parser.finishClass(); }
	;

nonameClass
	: classKeyword classBody                   { parser.finishClass(); }
	;

classBodyLCBrace
	: LCBrace                                  { parser.startClassBody($1); }
	;

classBodyLeft
	: classBodyLCBrace
	| classBodyLCBrace classMembers
	;

classBody
	: classBodyLeft RCBrace                    { parser.finishClassBody($2); }
	;

variableDeclaratorInit1
	: variableDeclaratorInit
	| modifiers variableDeclaratorInit         { parser.applyModifiers(); }
	;
	
qualifiedNameInit1
	: qualifiedNameInit
	| modifiers qualifiedNameInit              { parser.applyModifiers(); }
	;
	
method1
	: method
	| modifiers method                         { parser.applyModifiers(); }
	;

records1
	: records
	| modifiers records                        { parser.applyModifiers(); }
	;

classMember
	: classMember1                             { parser.addClassMember(); }
	;

classMember1
	: variableDeclaratorInit1 Semicolon
	| qualifiedNameInit1 Semicolon
	| variableDeclaratorInit1 qualifiedNameInit1 Semicolon         { parser.onMissingSemicolon(); }
	| variableDeclaratorInit1 variableDeclaratorInit1 Semicolon    { parser.onMissingSemicolon(); }
	| qualifiedNameInit1 variableDeclaratorInit1 Semicolon         { parser.onMissingSemicolon(); }
	| qualifiedNameInit1 qualifiedNameInit1 Semicolon              { parser.onMissingSemicolon(); }
	| variableDeclaratorInit1 method1                              { parser.onMissingSemicolon(); }
	| method1
	| records1
	;

classMembers
	: classMember
	| classMembers classMember
	;

recordsKeyword
	: Records                                  { parser.startRecords($1); }
	;

records
	: recordsKeyword recordsBody               { parser.finishRecords(); }
	;

recordsLCBrace
	: LCBrace                                  { parser.startRecordsBody($1); }
	;

recordsBodyLeft
	: recordsLCBrace
	| recordsLCBrace recordsList
	;
	
recordsBody
	: recordsBodyLeft RCBrace                  { parser.finishRecordsBody($2); }
	;

recordName
	: Identifier                               { parser.startRecord($1); }
	;
		
simpleRecord
	: recordName Assign Constant               { parser.setRecordValue($3); }
	;
	
record1
	: simpleRecord
	| modifiers simpleRecord                   { parser.applyModifiers(); }
	;

record
	: record1 Semicolon                        { parser.addRecord(); }
	;

recordsList
	: record
	| recordsList record
	;

simpleType
	: qualifiedName                            { parser.onVariableType(false); }
	;

arrayType
	: qualifiedName indices                    { parser.onVariableType(true); }
	;

type
	: simpleType
	| arrayType
	;

variableDeclarator
	: variableDeclarator1
	| variableDeclarator1 Dot                  { error($2); }
	;

variableDeclarator1
	: type Identifier                          { parser.onVariableDeclarator($2); }
	| Final type Identifier                    { parser.onVariableDeclarator($1, $3); }
	;

variableDeclaratorInit
	: variableDeclarator
	| variableDeclarator Assign initializer    { parser.onVariableDeclaratorInit($2); }
	;

qualifiedNameInit
	: qualifiedName                            { parser.onVariableInit(null); }
	| qualifiedName Assign initializer         { parser.onVariableInit($2); }
	| qualifiedName AddAssign initializer      { parser.onOperatorAssign($2); parser.onVariableInit($2); }
	;

methodHeader
	: type Identifier parameters               { parser.createMethod($2); }
	| Operator parameters                      { parser.createPriorityOperator($1); }
	| Operator Identifier parameters           { parser.createCastOperator($1, $2); }
	| type Operator Not parameters             { parser.createOperator($2, $3); }
	| type Operator Mul parameters             { parser.createOperator($2, $3); }
	| type Operator Div parameters             { parser.createOperator($2, $3); }
	| type Operator Mod parameters             { parser.createOperator($2, $3); }
	| type Operator Plus parameters            { parser.createOperator($2, $3); }
	| type Operator Minus parameters           { parser.createOperator($2, $3); }

	| type Operator Equ parameters             { parser.createOperator($2, $3); }
	| type Operator NotEqu parameters          { parser.createOperator($2, $3); }
	| type Operator Less parameters            { parser.createOperator($2, $3); }
	| type Operator More parameters            { parser.createOperator($2, $3); }
	| type Operator LessEqu parameters         { parser.createOperator($2, $3); }
	| type Operator MoreEqu parameters         { parser.createOperator($2, $3); }

	| type Operator BitwiseNot parameters      { parser.createOperator($2, $3); }
	| type Operator BitwiseAnd parameters      { parser.createOperator($2, $3); }
	| type Operator BitwiseOr parameters       { parser.createOperator($2, $3); }
	| type Operator BitwiseXor parameters      { parser.createOperator($2, $3); }

	| type Operator And parameters             { parser.createOperator($2, $3); }
	| type Operator Or parameters              { parser.createOperator($2, $3); }
	| type Operator Assign parameters          { parser.createOperator($2, $3); }
	;

method
	: methodHeader compound                    { parser.setMethodBody(); }
	| methodHeader Semicolon
	;

parameter
	: variableDeclarator                       { parser.createParameter(); }
	;

parametersList
	: parameter
	| parametersList Comma parameter
	;

parametersLeft
	: LBrace                                   { parser.startParameters($1); }
	;

parameters
	: parametersLeft RBrace                    { parser.finishParameters($2); }
	| parametersLeft parametersList RBrace     { parser.finishParameters($3); }
	;

initializer
	: nonameClass
	| expression
	| arrayInitializer
	;

arrayInitializerLeft
	: LCBrace                                  { parser.startArrayInitializer($1); }
	;

arrayInitializer
	: arrayInitializerLeft RCBrace
	| arrayInitializerLeft arrayInitializers RCBrace           { parser.finishArrayInitializer($3); }
	| arrayInitializerLeft arrayInitializers Comma RCBrace     { parser.finishArrayInitializer($4); }
	;

arrayInitializers
	: arrayInitializersElement
	| arrayInitializers Comma arrayInitializersElement
	;

arrayInitializersElement
	: mapElement                               { parser.addArrayInitializer(); }
	| expression                               { parser.addArrayInitializer(); }
	| arrayInitializer                         { parser.addArrayInitializer(); }
	;

mapElement
	: LBrace expression Comma expression RBrace            { parser.onMapElement($1, $5); }
	| LBrace expression Comma arrayInitializer RBrace      { parser.onMapElement($1, $5); }
	;

compoundLeft
	: LCBrace                                  { parser.startCompound($1); }
	;

compound
	: compoundLeft RCBrace                     { parser.finishCompound($2); }
	| compoundLeft statements RCBrace          { parser.finishCompound($3); }
	;

statements
	: statement                                { parser.addStatement(); }
	| statements statement                     { parser.addStatement(); }
	;

declaratorStatement
	: variableDeclaratorInit                   { parser.onDeclarator(); }
	;

statement
	: tryCatchFinally
	| throw
	| assignment Semicolon                                 { parser.onStatement(); }
	| declaratorStatement Semicolon                        { parser.onStatement(); }
	| declaratorStatement declaratorStatement Semicolon    { parser.onStatement2(); }
	| declaratorStatement assignment Semicolon             { parser.onStatement2(); }

	| compound
	| selection
	| iteration
	| jump

	| assignment tryCatchFinally               { parser.onStatement1(); }
	| assignment throw                         { parser.onStatement1(); }
	| assignment compound                      { parser.onStatement1(); }
	| assignment selection                     { parser.onStatement1(); }
	| assignment iteration                     { parser.onStatement1(); }

	| declaratorStatement tryCatchFinally      { parser.onStatement1(); }
	| declaratorStatement throw                { parser.onStatement1(); }
	| declaratorStatement compound             { parser.onStatement1(); }
	| declaratorStatement selection            { parser.onStatement1(); }
	| declaratorStatement iteration            { parser.onStatement1(); }
	| declaratorStatement jump                 { parser.onStatement1(); }
	;

throw
	: Throw expression Semicolon               { parser.onThrowStatement($1); }
	;

try
	: Try compound                             { parser.onTryStatement($1); }
	;

catch
	: Catch LBrace variableDeclarator RBrace compound      { parser.onCatchClause($1); }
	;

finally
	: Finally compound                         { parser.onFinallyStatement($1); }
	;

tryCatch
	: try catch                                { parser.addCatchStatement(); }
	| tryCatch catch                           { parser.addCatchStatement(); }
	;

tryCatchFinally
	: tryCatch
	| try finally
	| tryCatch finally
	;

selection
	: If bracedExpression statement                    { parser.onIfStatement($1, null); }
	| If bracedExpression statement Else statement     { parser.onIfStatement($1, $4); }
	;

whileLoop
	: While bracedExpression statement         { parser.onWhileStatement($1); }
	;

doWhileLoop
	: Do statement While bracedExpression Semicolon    { parser.onDoWhileStatement($1, $3); }
	;

forInit
	: variableDeclaratorInit                   { parser.onDeclarator(); }
	| assignment
	;

forEachInit
	: variableDeclarator                       { parser.onDeclarator(); }
	;

forStatement
	: For LBrace Semicolon expression Semicolon assignment RBrace statement            { parser.onForStatement($1, false); }
	| For LBrace forInit Semicolon expression Semicolon assignment RBrace statement    { parser.onForStatement($1, true); }
	| For LBrace forEachInit Colon expression RBrace statement                         { parser.onForEachStatement($1); }
	;

iteration
	: whileLoop
	| doWhileLoop
	| forStatement
	;

jump
	: Return Semicolon                         { parser.onJumpStatement($1, false); }
	| Return expression Semicolon              { parser.onJumpStatement($1, true); }
	| Return arrayInitializer Semicolon        { parser.onJumpStatement($1, true); }
	| Break Semicolon                          { parser.onJumpStatement($1, false); }
	| Continue Semicolon                       { parser.onJumpStatement($1, false); }
	;

assignment
	: expression
	| postfix Assign expression                { parser.onAssignment($2); }
	| postfix Assign arrayInitializer          { parser.onAssignment($2); }
	| postfix AddAssign expression             { parser.onOperatorAssign($2); parser.onAssignment($2); }
	| postfix SubAssign expression             { parser.onOperatorAssign($2); parser.onAssignment($2); }
	| postfix MulAssign expression             { parser.onOperatorAssign($2); parser.onAssignment($2); }
	| postfix DivAssign expression             { parser.onOperatorAssign($2); parser.onAssignment($2); }
	| postfix ModAssign expression             { parser.onOperatorAssign($2); parser.onAssignment($2); }
	| postfix BitwiseAndAssign expression      { parser.onOperatorAssign($2); parser.onAssignment($2); }
	| postfix BitwiseOrAssign expression       { parser.onOperatorAssign($2); parser.onAssignment($2); }
	| postfix BitwiseXorAssign expression      { parser.onOperatorAssign($2); parser.onAssignment($2); }
	;

expression
	: ternaryExpression
	| ternaryExpression InstanceOf simpleType  { parser.onInstanceOf($2); }
	;

ternaryExpression
	: logicalOr
	| logicalOr Question expression Colon ternaryExpression   { parser.onTernaryOperator($2, $4); }
	| logicalOr Elvis expression                       { parser.onElvisOperator($2); }
	| logicalOr Elvis arrayInitializer                 { parser.onElvisOperator($2); }
	;

logicalOr
	: logicalAnd
	| logicalOr Or logicalAnd                  { parser.onOperator($2); }
	;

logicalAnd
	: bitwiseOr
	| logicalAnd And bitwiseOr                 { parser.onOperator($2); }
	;

bitwiseOr
	: bitwiseXor
	| bitwiseOr BitwiseOr bitwiseXor           { parser.onOperator($2); }
	;

bitwiseXor
	: bitwiseAnd
	| bitwiseXor BitwiseXor bitwiseAnd         { parser.onOperator($2); }
	;

bitwiseAnd
	: equality
	| bitwiseAnd BitwiseAnd equality           { parser.onOperator($2); }
	;

equality
	: relational
	| relational Equ relational                { parser.onOperator($2); }
	| relational NotEqu relational             { parser.onOperator($2); }
	;

relational
	: additive
	| Null                                     { parser.onNull($1); }
	| additive Less additive                   { parser.onOperator($2); }
	| additive More additive                   { parser.onOperator($2); }
	| additive LessEqu additive                { parser.onOperator($2); }
	| additive MoreEqu additive                { parser.onOperator($2); }
	;

additive
	: multiplicative
	| additive Plus multiplicative             { parser.onOperator($2); }
	| additive Minus multiplicative            { parser.onOperator($2); }
	;

multiplicative
	: unary
	| multiplicative Mul unary                 { parser.onOperator($2); }
	| multiplicative Div unary                 { parser.onOperator($2); }
	| multiplicative Mod unary                 { parser.onOperator($2); }
	;

unary
	: postfix
	| Plus unary
	| Minus unary                              { parser.onUnary($1); }
	| Not unary                                { parser.onUnary($1); }
	| BitwiseNot unary                         { parser.onUnary($1); }
	;

typeCast
	: bracedExpression postfix                 { parser.onTypeCast(); }
	| arrayType arrayInitializer               { parser.onArrayTypeCast(); }
	;

new
	: New Identifier                           { parser.onNewExpression($1, $2); }
	;

container
	: Container                                { parser.onContainer($1); }
	| container Dot Container                  { parser.addContainer($3); }
	;

bracedExpression
	: LBrace expression RBrace                 { parser.onBracedExpression($1, $3); }
	| new
	;

prefix
	: This                                     { parser.onThis($1); }
	| Super                                    { parser.onSuper($1); }
	| Constant                                 { parser.onConstant($1); }
	| bracedExpression
	| simpleType                               { parser.onTypeToPostfix(); }
	| arrayType                                { parser.onTypeToPostfix(); }
	| methodCall                               { parser.onMethodCall(false); }
	| typeCast
	;

indexExpression
	: LBracket RBracket                        { parser.onIndex($1, $2, false); }
	| LBracket expression RBracket             { parser.onIndex($1, $3, true); }
	;

indices
	: indexExpression                          { parser.onIndices(true); }
	| indices indexExpression                  { parser.onIndices(false); }
	;

postfix
	: prefix
	| container
	| container Dot qualifiedName              { parser.onPostfix(); }
	| container Dot methodCall                 { parser.onMethodCall(true); }
	| container Dot                            { error($2); }
	| postfix indices                          { parser.onArrayAccess(); }
	| postfix Dot qualifiedName                { parser.onPostfix(); }
	| postfix Dot methodCall                   { parser.onMethodCall(true); }
	| postfix Dot                              { error($2); }
	;

methodCall
	: qualifiedName expressions
	;

expressionsLeft
	: LBrace                                   { parser.startExpressions($1); }
	;

expressions
	: expressionsLeft RBrace                   { parser.finishExpressions($2); }
	| expressionsLeft expressionsList RBrace   { parser.finishExpressions($3); }
	;

expressionsList
	: expression                               { parser.addExpression(); }
	| arrayInitializer                         { parser.addExpression(); }
	| expressionsList Comma expression         { parser.addExpression(); }
	| expressionsList Comma arrayInitializer   { parser.addExpression(); }
	;