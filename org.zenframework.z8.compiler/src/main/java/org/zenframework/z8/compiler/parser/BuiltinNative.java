package org.zenframework.z8.compiler.parser;

public class BuiltinNative {
	static public String[] StandardImports = { "import org.zenframework.z8.server.runtime.*;" };

	static public String OBJECT = "OBJECT";
	static public String IObject = "IObject";
	static public String Object = "Object";

	static public String Constructor = "IClass.Constructor";
	static public String Constructor1 = "IClass.Constructor1";
	static public String Constructor2 = "";

	static public String Class = "CLASS";
	static public String ClassQualifiedName = "org.zenframework.z8.server.runtime.CLASS";

	static public String Map = "RLinkedHashMap";
	static public String MapEntry = "java.util.LinkedHashMap$Entry";
	static public String MapQualifiedName = "org.zenframework.z8.server.runtime.RLinkedHashMap";

	static public String Array = "RCollection";
	static public String ArrayQualifiedName = "org.zenframework.z8.server.runtime.RCollection";
}
