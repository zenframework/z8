package org.zenframework.z8.compiler.parser.variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.BuiltinNative;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.type.TypeCast;
import org.zenframework.z8.compiler.parser.type.members.ArrayMethods;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class VariableType extends LanguageElement implements IVariableType {
    private IType type;
    private IToken typeNameToken;

    private IToken autoToken;

    private boolean isAuto;
    private boolean isStatic;

    private List<IType> keys = new ArrayList<IType>();
    private List<IToken> keyTokens = new ArrayList<IToken>();
    private List<IPosition> keyBrackets = new ArrayList<IPosition>();

    public VariableType(IToken token) {
        this.typeNameToken = token;
    }

    public VariableType(CompilationUnit compilationUnit, IType type) {
        setCompilationUnit(compilationUnit);
        this.type = type;
    }

    public VariableType(IType type, boolean isStatic) {
        this.type = type;
        this.isStatic = isStatic;
    }

    public VariableType(IVariableType type) {
        setCompilationUnit(type.getCompilationUnit());
        this.type = type.getType();

        if(type.isArray()) {
            keys.addAll(Arrays.asList(type.getKeys()));
        }
    }

    @Override
    public IPosition getSourceRange() {
        if(keyBrackets.size() > 0) {
            return typeNameToken.getPosition().union(keyBrackets.get(keyBrackets.size() - 1));
        }
        return typeNameToken != null ? typeNameToken.getPosition() : null;
    }

    @Override
    public IPosition getPosition() {
        return typeNameToken.getPosition();
    }

    @Override
    public IToken getFirstToken() {
        return typeNameToken;
    }

    public void setAutoToken(IToken autoToken) {
        this.autoToken = autoToken;
    }

    @Override
    public int getDimensions() {
        return keys.size();
    }

    @Override
    public IType[] getKeys() {
        return keys.toArray(new IType[keys.size()]);
    }

    public IToken[] getKeyTokens() {
        return keyTokens.toArray(new IToken[keyTokens.size()]);
    }

    @Override
    public IType getRightKey() {
        return keys.get(keys.size() - 1);
    }

    @Override
    public IType getLeftKey() {
        return keys.get(0);
    }

    @Override
    public void addLeftKey(IType type) {
        keys.add(0, type);
    }

    @Override
    public void addRightKey(IType type) {
        keys.add(type);
    }

    @Override
    public IType removeRightKey() {
        return keys.remove(keys.size() - 1);
    }

    @Override
    public IType removeLeftKey() {
        return keys.remove(0);
    }

    public void addKey(IToken key, IPosition sourceRange) {
        keyTokens.add(key);
        keyBrackets.add(sourceRange);
    }

    @Override
    public IType getType() {
        return type;
    }

    @Override
    public IVariableType getVariableType() {
        return this;
    }

    @Override
    public String getSignature() {
        String signature = "";

        if(typeNameToken != null) {
            signature = typeNameToken.getRawText();
        }
        else if(type != null) {
            signature = type.getUserName();
        }
        else {
            signature = "unknown";
        }

        for(IToken e : keyTokens) {
            signature += "[" + (e == null ? "" : e.getRawText()) + "]";
        }

        return signature;
    }

    private String getJavaName(IType type, boolean declaring) {
        assert (type != null);

        if(type.extendsPrimary() || type.isEnum()) {
            return type.getJavaName();
        }

        if(declaring) {
            return type.getJavaName() + ".CLASS<? extends " + type.getJavaName() + ">";
        }

        return type.getJavaName() + ".CLASS<" + type.getJavaName() + ">";
    }

    public String getJavaName(boolean declaring) {
        if(!isArray()) {
            return getJavaName(type, declaring);
        }

        String javaName = "";

        IType[] keys = getKeys();

        for(int i = 0; i < keys.length; i++) {
            String type = keys[i] == null ? BuiltinNative.Array : BuiltinNative.Map;

            if(i != 0) {
                javaName = type + "<" + (keys[i] == null ? "" : "Object, ") + javaName + ">";
            }
            else {
                javaName = type;
            }
        }

        return javaName;
    }

    @Override
    public String getJavaName() {
        return getJavaName(false);
    }

    @Override
    public String getDeclaringJavaName() {
        return getJavaName(true);
    }

    @Override
    public String getJavaNew() {
        return getJavaNew(false);
    }

    @Override
    public String getJavaNew(boolean staticContext) {
        String newExpression = "new " + getJavaName();

        if(isArray() || type.extendsPrimary()) {
            return newExpression + "()";
        }

        return newExpression + "(" + (staticContext ? "null" : "this") + ")";
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public boolean isAuto() {
        return isAuto;
    }

    @Override
    public boolean isEnum() {
        return !isArray() && type != null && type.isEnum();
    }

    @Override
    public boolean isArray() {
        return getDimensions() != 0;
    }

    @Override
    public boolean isReference() {
        return !isArray() && !isEnum() && !extendsPrimary();
    }

    @Override
    public boolean isArrayOfReferences() {
        if(!isArray()) {
            return false;
        }

        if(!type.isEnum() && !type.extendsPrimary()) {
            return true;
        }

        for(IType key : getKeys()) {
            if(key != null && !key.isEnum() && !key.extendsPrimary()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean extendsPrimary() {
        return !isArray() && type != null && type.extendsPrimary();
    }

    @Override
    public boolean compare(IVariableType type) {
        return getType() != null && getType().equals(type.getType()) && Arrays.deepEquals(getKeys(), type.getKeys());
    }

    @Override
    public ITypeCast getCastTo(IType candidate) {
        return getCastTo(new VariableType(getCompilationUnit(), candidate));
    }

    @Override
    public ITypeCast getCastTo(IVariableType candidate) {
        return getCastTo(null, candidate);
    }

    @Override
    public ITypeCast getCastTo(IType context, IVariableType candidate) {
        if(compare(candidate)) {
            return new TypeCast(this, candidate, 0);
        }

        if(isArray()) {
            if(getDimensions() != candidate.getDimensions()
                    || TypeCast.getCastToBaseType(getCompilationUnit(), getType(), candidate.getType()) == null) {
                return null;
            }

            IType[] keys = getKeys();
            IType[] candidateKeys = candidate.getKeys();

            for(int i = 0; i < keys.length; i++) {
                IType key = keys[i];
                IType candidateKey = candidateKeys[i];

                if(key != null && candidateKey != null
                        && TypeCast.getCastToBaseType(getCompilationUnit(), key, candidateKey) != null) {
                    continue;
                }

                if(key == null && candidateKey == null) {
                    continue;
                }

                return null;
            }

            return new TypeCast(this, candidate, 0);
        }

        if(context == null) {
            if(type != null) {
                return type.getCastTo(candidate);
            }
            return null;
        }

        return candidate.isArray() ? null : TypeCast.getCastToBaseType(getCompilationUnit(), getType(), candidate.getType());
    }

    @Override
    public IMember findMember(String name) {
        if(isArray() || getType() == null) {
            return null;
        }

        return getType().findMember(name);
    }

    @Override
    public IMethod[] getMatchingMethods(String name) {
        if(!isArray()) {
            if(getType() != null) {
                return getType().getMatchingMethods(name);
            }

            return new IMethod[0];
        }

        List<IMethod> result = new ArrayList<IMethod>();

        IMethod[] arrayMethods = ArrayMethods.get(getCompilationUnit(), this);

        for(IMethod method : arrayMethods) {
            if(method.getName().equals(name)) {
                result.add(method);
            }
        }

        return result.toArray(new IMethod[result.size()]);
    }

    @Override
    public IMethod[] getTypeCastOperators() {
        if(isArray() || isEnum()) {
            return new IMethod[0];
        }

        if(getType() == null) {
            return new IMethod[0];
        }

        return getType().getTypeCastOperators();
    }

    @Override
    public IMember[] getAllMembers() {
        if(isArray()) {
            return ArrayMethods.get(getCompilationUnit(), this);
        }

        if(getType() == null) {
            return new IMember[0];
        }

        return getType().getAllMembers();
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveTypes(compilationUnit, declaringType))
            return false;

        if(typeNameToken == null && type != null) {
            return true;
        }

        String name = typeNameToken.getRawText();

        if(name.equals(declaringType.getUserName())) {
            type = declaringType;
        }
        else {
            type = compilationUnit.resolveType(name);
        }

        if(type == null) {
            setFatalError(typeNameToken.getPosition(), name + " cannot be resolved to a type");
            compilationUnit.addUnresolvedType(name);
            return false;
        }

        compilationUnit.addContributor(type.getCompilationUnit());
        compilationUnit.addContentProposal(typeNameToken.getPosition(), new VariableType(getCompilationUnit(), type));

        if(type.getPosition() != null) {
            compilationUnit.addHyperlink(typeNameToken.getPosition(), type);
        }

        for(int i = 0; i < keyTokens.size(); i++) {
            IToken key = keyTokens.get(i);
            IType type = null;

            if(key != null) {
                String keyTypeName = key.getRawText();

                type = compilationUnit.resolveType(keyTypeName);

                if(type == null) {
                    setFatalError(key.getPosition(), keyTypeName + " cannot be resolved to a type");
                    compilationUnit.addUnresolvedType(keyTypeName);
                    return false;
                }

                compilationUnit.addHyperlink(key.getPosition(), type);
                compilationUnit.addContentProposal(key.getPosition(), new VariableType(getCompilationUnit(), type));
            }

            addRightKey(type);
        }

        return true;
    }

    private IVariableType resolveName(String name) {
        if(name != null) {
            IVariable variable = null;

            IType declaringType = getDeclaringType();
            IMethod declaringMethod = getDeclaringMethod();

            if(declaringMethod != null) {
                variable = declaringMethod.findLocalVariable(name);
            }

            if(variable == null && declaringType != null) {
                variable = declaringType.findMember(name);
            }

            if(variable != null) {
                return variable.getVariableType();
            }
        }

        return null;
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null)) {
            IVariableType variableType = resolveName(typeNameToken.getRawText());

            if(variableType != null) {
                compilationUnit.addContentProposal(typeNameToken.getPosition(), variableType);
            }

            for(int i = 0; i < keyTokens.size(); i++) {
                IToken key = keyTokens.get(i);

                if(key != null) {
                    variableType = resolveName(key.getRawText());

                    if(variableType != null) {
                        compilationUnit.addContentProposal(key.getPosition(), variableType);
                    }
                }
            }
            return false;
        }

        if(autoToken != null) {
            if(declaringMethod != null || getDimensions() != 1 || getRightKey() != null) {
                setError(autoToken.getPosition(),
                        "The modifier auto can only be applied to class members of one-dimensional array type");
            }
            else if(type.extendsPrimary()) {
                setError(autoToken.getPosition(), "The modifier auto cannot be applied to an array of a primary type "
                        + type.getUserName());
            }
            else {
                isAuto = true;
            }
        }
        return true;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        codeGenerator.getCompilationUnit().importType(type);
        codeGenerator.append(getDeclaringJavaName());
    }

    @Override
    public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
        if(this.type != null && this.type.equals(type)) {
            IPosition position = typeNameToken.getPosition();
            parent.addChild(new ReplaceEdit(position.getOffset(), position.getLength(), newTypeName));
        }

        IType[] keys = getKeys();
        IToken[] keyTokens = getKeyTokens();

        for(int i = 0; i < keys.length; i++) {
            if(keys[i] != null && keys[i].equals(type)) {
                IPosition position = keyTokens[i].getPosition();
                parent.addChild(new ReplaceEdit(position.getOffset(), position.getLength(), newTypeName));
            }
        }
    }

}
