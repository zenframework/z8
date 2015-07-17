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

        if(voidType == null || booleanType == null || integerType == null) {
            return new IMethod[0];
        }

        List<IMethod> methods = new ArrayList<IMethod>();

        if(keyType == null) // array methods
        {
            //int size()
            {
                methods.add(new Method(new VariableType(compilationUnit, integerType), "size", null));
            }

            //bool isEmpty()
            {
                methods.add(new Method(new VariableType(compilationUnit, booleanType), "isEmpty", null));
            }

            //bool contains(TYPE element)
            {
                Variable[] parameters = new Variable[] { new Variable(value, "element") };
                methods.add(new Method(new VariableType(compilationUnit, booleanType), "contains", parameters));
            }

            //bool isSubsetOf(TYPE[] element)
            {
                Variable[] parameters = new Variable[] { new Variable(variableType, "elements") };
                methods.add(new Method(new VariableType(compilationUnit, booleanType), "isSubsetOf", parameters));
            }

            //int indexOf(TYPE element)
            {
                Variable[] parameters = new Variable[] { new Variable(value, "element") };
                methods.add(new Method(new VariableType(compilationUnit, integerType), "indexOf", parameters));
            }

            //void clear()
            {
                methods.add(new Method(new VariableType(compilationUnit, voidType), "clear", null));
            }

            //void add(TYPE element)
            {
                Variable[] parameters = new Variable[] { new Variable(value, "element") };
                methods.add(new Method(new VariableType(compilationUnit, voidType), "add", parameters));
            }

            // void add(int index, TYPE element)
            {
                Variable[] parameters = new Variable[] {
                        new Variable(new VariableType(compilationUnit, integerType), "index"),
                        new Variable(value, "element") };
                methods.add(new Method(new VariableType(compilationUnit, voidType), "add", parameters));
            }

            //void addAll(TYPE[] elements)
            {
                Variable[] parameters = new Variable[] { new Variable(variableType, "elements") };
                methods.add(new Method(new VariableType(compilationUnit, voidType), "addAll", parameters));
            }

            // void addAll(int index, TYPE[] elements)
            {
                Variable[] parameters = new Variable[] {
                        new Variable(new VariableType(compilationUnit, integerType), "index"),
                        new Variable(variableType, "elements") };
                methods.add(new Method(new VariableType(compilationUnit, voidType), "addAll", parameters));
            }

            //TYPE removeAt(int index)
            {
                Variable[] parameters = new Variable[] { new Variable(new VariableType(compilationUnit, integerType),
                        "index") };
                methods.add(new Method(value, "removeAt", parameters));
            }

            //boolean remove(TYPE element)
            {
                Variable[] parameters = new Variable[] { new Variable(value, "element") };
                methods.add(new Method(new VariableType(compilationUnit, booleanType), "remove", parameters));
            }

            //void remove(TYPE[] elements)
            {
                Variable[] parameters = new Variable[] { new Variable(variableType, "elements") };
                methods.add(new Method(new VariableType(compilationUnit, voidType), "remove", parameters));
            }

            //void operatorAssign(TYPE[])
            {
                Variable[] parameters = new Variable[] { new Variable(variableType, "elements") };
                methods.add(new Operator(new VariableType(compilationUnit, voidType),
                        new OperatorToken(IToken.ASSIGN, null), parameters));
            }

            //void operatorAddAssign(TYPE[])
            {
                Variable[] parameters = new Variable[] { new Variable(variableType, "elements") };
                methods.add(new Operator(new VariableType(compilationUnit, voidType), new OperatorToken(IToken.ADD_ASSIGN,
                        null), parameters));
            }

            //TYPE[] operatorAdd(TYPE[])
            {
                Variable[] parameters = new Variable[] { new Variable(variableType, "elements") };
                methods.add(new Operator(variableType, new OperatorToken(IToken.PLUS, null), parameters));
            }

        }
        else // map methods
        {
            IVariableType key = new VariableType(compilationUnit, keyType);

            //int size()
            {
                methods.add(new Method(new VariableType(compilationUnit, integerType), "size", null));
            }

            //bool isEmpty()
            {
                methods.add(new Method(new VariableType(compilationUnit, booleanType), "isEmpty", null));
            }

            //void clear()
            {
                methods.add(new Method(new VariableType(compilationUnit, voidType), "clear", null));
            }

            //void add(Key key, Value value)
            {
                Variable[] parameters = new Variable[] { new Variable(key, "key"), new Variable(value, "value") };
                methods.add(new Method(new VariableType(compilationUnit, voidType), "add", parameters));
            }

            //void add(Value[Key] map)
            {
                Variable[] parameters = new Variable[] { new Variable(variableType, "map") };
                methods.add(new Method(new VariableType(compilationUnit, voidType), "add", parameters));
            }

            //Value remove(Key key)
            {
                Variable[] parameters = new Variable[] { new Variable(key, "key") };
                methods.add(new Method(value, "remove", parameters));
            }

            //bool containsKey(Key key)
            {
                Variable[] parameters = new Variable[] { new Variable(key, "key") };
                methods.add(new Method(new VariableType(compilationUnit, booleanType), "containsKey", parameters));
            }

            //bool containsValue(Value value)
            {
                Variable[] parameters = new Variable[] { new Variable(value, "value") };
                methods.add(new Method(new VariableType(compilationUnit, booleanType), "containsValue", parameters));
            }

            //Key[] keys()
            {
                VariableType returnType = new VariableType(key);
                returnType.addRightKey(null);
                methods.add(new Method(returnType, "keys", null));
            }

            //Value[] values()
            {
                VariableType returnType = new VariableType(value);
                returnType.addRightKey(null);
                methods.add(new Method(returnType, "values", null));
            }

            //void operatorAssign(TYPE[])
            {
                Variable[] parameters = new Variable[] { new Variable(variableType, "elements") };
                methods.add(new Operator(new VariableType(compilationUnit, voidType),
                        new OperatorToken(IToken.ASSIGN, null), parameters));
            }

            //void operatorAddAssign(TYPE[])
            {
                Variable[] parameters = new Variable[] { new Variable(variableType, "elements") };
                methods.add(new Operator(new VariableType(compilationUnit, voidType), new OperatorToken(IToken.ADD_ASSIGN,
                        null), parameters));
            }
            //TYPE[] operatorAdd(TYPE[])
            {
                Variable[] parameters = new Variable[] { new Variable(variableType, "elements") };
                methods.add(new Operator(variableType, new OperatorToken(IToken.PLUS, null), parameters));
            }
        }
        return methods.toArray(new IMethod[methods.size()]);
    }
}