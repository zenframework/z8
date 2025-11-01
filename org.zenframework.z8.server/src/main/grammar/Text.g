grammar Text ;

options {
	language = Java;
}

@header {
package org.zenframework.z8.server.expression.generated;
}

text : textPart* EOF ;

textPart
	: TEXT 					#plainTextPart
	| '${' TEXT '}'			#expressionTextPart
	;

TEXT : (~[${}] | EscapeSequence)+ ;

fragment EscapeSequence
	: '\\' 'u005c'? [bstnfr"'\\]
	| '\\' 'u005c'? ([0-3]? [0-7])? [0-7]
	| '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
	;

fragment HexDigit : [0-9a-fA-F] ;
