grammar Expression;

options {
	language = Java;
}

@header {
package org.zenframework.z8.server.expression.generated;
}

rootExpression : expression EOF ;

expression
	// Level 11, Primary, array and member access
	: primary																			#PrimaryExpression
	| array = expression '[' index = expression ']'										#SquareBracketExpression
	| object = expression op = '.' (
		property = identifier
		| method = methodCall
	)																					#MemberReferenceExpression

	// Level 10, Method calls
	| methodCall																		#MethodCallExpression

	// Level 9, Unary operators
	| prefix = ('+' | '-' | '!') expression												#UnaryOperatorExpression

	// Level 8, Multiplicative operators
	| left = expression op = ('*' | '/' | '%') right = expression						#BinaryOperatorExpression
	// Level 7, Additive operators
	| left = expression op = ('+' | '-') right = expression								#BinaryOperatorExpression
	// Level 6, Relational operators
	| left = expression op = ('<=' | '>=' | '>' | '<') right = expression				#BinaryOperatorExpression
	// Level 5, Equality Operators
	| left = expression op = ('==' | '!=') right = expression							#BinaryOperatorExpression
	// Level 4, Logic AND
	| left = expression op = '&&' right = expression									#BinaryOperatorExpression
	// Level 3, Logic OR
	| left = expression op = '||' right = expression									#BinaryOperatorExpression

	// Level 2, Ternary
	| condition = expression op = '?' trueExp = expression ':' falseExp = expression	#TernaryExpression
	// Level 1, Elvis
	| value = expression op = '?:' alternative = expression								#ElvisExpression
	;

expressionList : expression (',' expression)* ;

methodCall : function = identifier '(' arguments = expressionList? ')' ;

primary
	: priority
	| literal
	| identifier
	;

priority : '(' expression ')' ;

literal
	: integer = INTEGER_LITERAL
	| decimal = FLOAT_LITERAL
	| string = STRING_LITERAL
	| bool = BOOL_LITERAL
	;

identifier : IDENTIFIER ;

INTEGER_LITERAL : ('0' | [1-9] (Digits? | '_'+ Digits)) [lL]? ;

FLOAT_LITERAL
	: (Digits '.' Digits? | '.' Digits) ExponentPart? [fFdD]?
	| Digits (ExponentPart [fFdD]? | [fFdD])
	;

BOOL_LITERAL: 'true' | 'false' ;

STRING_LITERAL
	: '\'' (~['\\\r\n] | EscapeSequence)* '\''
	| '"' (~["\\\r\n] | EscapeSequence)* '"'
	| '«' (~[«»\\\r\n] | EscapeSequence)* '»'
	| '„'  (~[„“\\\r\n] | EscapeSequence)* '“'
	;

IDENTIFIER : Letter LetterOrDigit* ;

fragment ExponentPart : [eE] [+-]? Digits ;

fragment EscapeSequence
	: '\\' 'u005c'? [bstnfr"'\\]
	| '\\' 'u005c'? ([0-3]? [0-7])? [0-7]
	| '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
	;

fragment HexDigits : HexDigit ((HexDigit | '_')* HexDigit)? ;

fragment HexDigit : [0-9a-fA-F] ;

fragment Digits : [0-9] ([0-9_]* [0-9])? ;

fragment Letter : [a-zA-Zа-яА-Я_] ;
fragment LetterOrDigit : Letter | [0-9] ;

WS : [ \r\n\t]+ -> skip ;
