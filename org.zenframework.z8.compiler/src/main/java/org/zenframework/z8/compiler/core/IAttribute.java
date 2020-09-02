package org.zenframework.z8.compiler.core;

import org.zenframework.z8.compiler.parser.grammar.lexer.token.ConstantToken;

public interface IAttribute extends ILanguageElement {
	final static String Name = "name";
	final static String DisplayName = "displayName";

	final static String Abstract = "abstract";
	final static String Primary = "primary";
	final static String Native = "native";
	final static String Generatable = "generatable";
	final static String Request = "request";
	final static String Activator = "activator";
	final static String Entry = "entry";
	final static String Job = "job";
	final static String ApiDescription = "apiDescription";
	final static String СontentParams = "contentParams";

	String getName();

	String getValueString();

	IToken getNameToken();

	ConstantToken getValueToken();

	ILanguageElement getValue();
}
