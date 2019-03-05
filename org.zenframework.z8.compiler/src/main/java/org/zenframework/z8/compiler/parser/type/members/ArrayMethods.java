package org.zenframework.z8.compiler.parser.type.members;

import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.OperatorToken;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class ArrayMethods {
	static public IMethod[] get(CompilationUnit compilationUnit, IVariableType variableType) {
		VariableType value = new VariableType(variableType);
		IType keyType = value.removeRightKey();

		IType voidType = Primary.resolveType(compilationUnit, Primary.Void);
		IType booleanType = Primary.resolveType(compilationUnit, Primary.Boolean);
		IType integerType = Primary.resolveType(compilationUnit, Primary.Integer);
		IType stringType = Primary.resolveType(compilationUnit, Primary.String);

		if(voidType == null || booleanType == null || integerType == null || stringType == null)
			return new IMethod[0];

		List<IMethod> methods = new ArrayList<IMethod>();

		if(keyType == null) { // array methods
			// int size()
			methods.add(new Method(new VariableType(compilationUnit, integerType), "size", null));

			// bool isEmpty()
			methods.add(new Method(new VariableType(compilationUnit, booleanType), "isEmpty", null));

			// bool contains(TYPE element)
			methods.add(new Method(new VariableType(compilationUnit, booleanType), "contains",
					new Variable[] { new Variable(value, "element") }));

			// bool isSubsetOf(TYPE[] element)
			methods.add(new Method(new VariableType(compilationUnit, booleanType), "isSubsetOf",
					new Variable[] { new Variable(variableType, "elements") }));

			// int indexOf(TYPE element)
			methods.add(new Method(new VariableType(compilationUnit, integerType), "indexOf",
					new Variable[] { new Variable(value, "element") }));

			// void clear()
			methods.add(new Method(new VariableType(compilationUnit, voidType), "clear", null));

			// void add(TYPE element)
			methods.add(new Method(new VariableType(compilationUnit, voidType), "add",
					new Variable[] { new Variable(value, "element") }));

			// void add(int index, TYPE element)
			methods.add(new Method(new VariableType(compilationUnit, voidType), "add",
					new Variable[] { new Variable(new VariableType(compilationUnit, integerType), "index"), new Variable(value, "element") }));

			// void set(int index, TYPE element)
			methods.add(new Method(new VariableType(compilationUnit, voidType), "set",
					new Variable[] { new Variable(new VariableType(compilationUnit, integerType), "index"), new Variable(value, "element") }));

			// void addAll(TYPE[] elements)
			methods.add(new Method(new VariableType(compilationUnit, voidType), "addAll",
					new Variable[] { new Variable(variableType, "elements") }));

			// void addAll(int index, TYPE[] elements)
			methods.add(new Method(new VariableType(compilationUnit, voidType), "addAll",
					new Variable[] { new Variable(new VariableType(compilationUnit, integerType), "index"), new Variable(variableType, "elements") }));

			// TYPE removeAt(int index)
			methods.add(new Method(value, "removeAt",
					new Variable[] { new Variable(new VariableType(compilationUnit, integerType), "index") }));

			// boolean remove(TYPE element)
			methods.add(new Method(new VariableType(compilationUnit, booleanType), "remove",
					new Variable[] { new Variable(value, "element") }));

			// TYPE[] removeAll(TYPE element)
			methods.add(new Method(variableType, "removeAll",
					new Variable[] { new Variable(value, "element") }));

			// TYPE[] removeAll(TYPE[] elements)
			methods.add(new Method(variableType, "removeAll",
					new Variable[] { new Variable(variableType, "elements") }));

/*
			// void operatorAssign(TYPE[])
			methods.add(new Operator(new VariableType(compilationUnit, voidType), new OperatorToken(IToken.ASSIGN, null),
					new Variable[] { new Variable(variableType, "elements") }));

			// void operatorAddAssign(TYPE[])
			methods.add(new Operator(new VariableType(compilationUnit, voidType), new OperatorToken(IToken.ADD_ASSIGN, null),
					new Variable[] { new Variable(variableType, "elements") }));
*/
			// TYPE[] operatorAdd(TYPE[])
			methods.add(new Operator(variableType, new OperatorToken(IToken.PLUS, null),
					new Variable[] { new Variable(variableType, "elements") }));

			// TYPE[] sort()
			methods.add(new Method(new VariableType(variableType), "sort", null));

			// TYPE[] unique()
			methods.add(new Method(new VariableType(variableType), "unique", null));

			// string join()
			methods.add(new Method(new VariableType(compilationUnit, stringType), "join", null));

			// string join(string separator)
			methods.add(new Method(new VariableType(compilationUnit, stringType), "join", 
					new Variable[] { new Variable(new VariableType(compilationUnit, stringType), "separator") }));
		} else { // map methods
			IVariableType key = new VariableType(compilationUnit, keyType);

			// int size()
			methods.add(new Method(new VariableType(compilationUnit, integerType), "size", null));

			// bool isEmpty()
			methods.add(new Method(new VariableType(compilationUnit, booleanType), "isEmpty", null));

			// void clear()
			methods.add(new Method(new VariableType(compilationUnit, voidType), "clear", null));

			// void add(Key key, Value value)
			methods.add(new Method(new VariableType(compilationUnit, voidType), "add",
					new Variable[] { new Variable(key, "key"), new Variable(value, "value") }));

			// void add(Value[Key] map)
			methods.add(new Method(new VariableType(compilationUnit, voidType), "add",
					new Variable[] { new Variable(variableType, "map") }));

			// Value remove(Key key)
			methods.add(new Method(value, "remove",
					new Variable[] { new Variable(key, "key") }));

			// bool containsKey(Key key)
			methods.add(new Method(new VariableType(compilationUnit, booleanType), "containsKey",
					new Variable[] { new Variable(key, "key") }));

			// bool containsValue(Value value)
			methods.add(new Method(new VariableType(compilationUnit, booleanType), "containsValue",
					new Variable[] { new Variable(value, "value") }));

			// Key[] keys()
			VariableType returnType = new VariableType(key);
			returnType.addRightKey(null);
			methods.add(new Method(returnType, "keys", null));

			// Value[] values()
			returnType = new VariableType(value);
			returnType.addRightKey(null);
			methods.add(new Method(returnType, "values", null));

/*
			// void operatorAssign(TYPE[])
			{
				Variable[] parameters = new Variable[] { new Variable(variableType, "elements") };
				methods.add(new Operator(new VariableType(compilationUnit, voidType), new OperatorToken(IToken.ASSIGN, null), parameters));
			}

			// void operatorAddAssign(TYPE[])
			methods.add(new Operator(new VariableType(compilationUnit, voidType), new OperatorToken(IToken.ADD_ASSIGN, null),
					new Variable[] { new Variable(variableType, "elements") }));
*/

			// TYPE[] operatorAdd(TYPE[])
			methods.add(new Operator(variableType, new OperatorToken(IToken.PLUS, null),
					new Variable[] { new Variable(variableType, "elements") }));
		}

		return methods.toArray(new IMethod[methods.size()]);
	}
}