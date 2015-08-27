package org.zenframework.z8.compiler.parser.type.members;

import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Member extends LanguageElement implements IMember {
    private VariableType variableType;

    private IToken staticToken;
    private IToken accessToken;
    private IToken nameToken;

    private ILanguageElement initializer;

    public Member(VariableType variableType, IToken nameToken) {
        this(variableType, nameToken, null);
    }

    public Member(VariableType variableType, IToken nameToken, ILanguageElement initializer) {
        this.variableType = variableType;
        this.nameToken = nameToken;
        this.initializer = initializer;

        if(this.initializer != null) {
            this.initializer.setParent(this);
        }
    }

    @Override
    public IPosition getSourceRange() {
        if(initializer != null) {
            return variableType.getSourceRange().union(initializer.getSourceRange());
        }
        return variableType.getSourceRange().union(nameToken.getPosition());
    }

    @Override
    public IPosition getPosition() {
        return nameToken.getPosition();
    }

    @Override
    public IToken getFirstToken() {
        return getFirstToken(variableType.getFirstToken(),
                getFirstToken(super.getFirstToken(), getFirstToken(staticToken, accessToken)));
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }

        if(object instanceof IMember) {
            IMember member = (IMember)object;

            String userName = getUserName();

            return getDeclaringType().equals(member.getDeclaringType()) && userName != null
                    && userName.equals(member.getUserName());

        }

        return false;
    }

    @Override
    public String getName() {
        return nameToken.getRawText();
    }

    @Override
    public String getJavaName() {
        return nameToken.getRawText();
    }

    @Override
    public String getUserName() {
        return getName() + " " + getVariableType().getSignature();
    }

    @Override
    public String getSignature() {
        assert (false);
        return null;
    }

    @Override
    public IInitializer getInitializer() {
        return (IInitializer)initializer;
    }

    @Override
    public IVariableType getVariableType() {
        return variableType;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isStatic() {
        return staticToken != null;
    }

    @Override
    public boolean isPublic() {
        return accessToken == null || accessToken.getId() == IToken.PUBLIC;
    }

    @Override
    public boolean isProtected() {
        return accessToken != null && accessToken.getId() == IToken.PROTECTED;
    }

    @Override
    public boolean isPrivate() {
        return accessToken != null && accessToken.getId() == IToken.PRIVATE;
    }

    public void setStaticToken(IToken staticToken) {
        this.staticToken = staticToken;
    }

    public void setAccessToken(IToken accessToken) {
        this.accessToken = accessToken;
    }

    public void setAutoToken(IToken token) {
        variableType.setAutoToken(token);
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(declaringType.getContainerType() != null) {
            setFatalError(getPosition(), "Nested Duplicate field " + declaringType.getUserName() + "." + getName());
        }

        return super.resolveTypes(compilationUnit, declaringType)
                && variableType.resolveTypes(compilationUnit, declaringType)
                && (initializer == null || initializer.resolveTypes(compilationUnit, declaringType));
    }

    @Override
    public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveStructure(compilationUnit, declaringType))
            return false;

        if(!variableType.resolveStructure(compilationUnit, declaringType))
            return false;

        String name = getName();

        if(!Lexer.checkIdentifier(name)) {
            setFatalError(nameToken.getPosition(), "Syntax error on token '" + name + "'. " + name
                    + " is a reserved keyword.");
            return false;
        }

        IMember member = declaringType.findMember(name);

        if(member != null) {
            if(member.getDeclaringType() == declaringType) {
                setFatalError(getPosition(), "Duplicate field " + declaringType.getUserName() + "." + getName());
                setFatalError(member.getPosition(), "Duplicate field " + declaringType.getUserName() + "." + getName());
            }
            else {
                setFatalError(getPosition(), getName() + ": redefinition of " + member.getDeclaringType().getUserName()
                        + "." + getName());
            }
            return false;
        }

        if(isStatic()) {
            IVariableType variableType = getVariableType();

            if(variableType.isReference() || variableType.isArrayOfReferences()) {
                setError(staticToken.getPosition(), "The modifier static cannot be used for a reference type member");
            }

            if(declaringType.getContainerType() != null) {
                setError(staticToken.getPosition(), "The modifier static cannot be used inside a nested type");
            }
        }

        if(getAttributes().length != 0 && !getVariableType().isReference()) {
            setError(getPosition(), "Attributes can only be applied to variables of a reference type");
        }

        declaringType.addMember(this);

        compilationUnit.addHyperlink(getPosition(), compilationUnit, getPosition());
        compilationUnit.addContentProposal(nameToken.getPosition(), getVariableType());

        if(initializer != null) {
            initializer.resolveStructure(compilationUnit, declaringType);
        }

        return true;
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        if(!variableType.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        if(initializer != null) {
            initializer.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
        }

        return true;
    }

    @Override
    public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveNestedTypes(compilationUnit, declaringType))
            return false;

        if(initializer != null) {
            initializer.resolveNestedTypes(compilationUnit, declaringType);
        }

        return true;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        codeGenerator.indent();

        codeGenerator.append("public ");

        if(isStatic()) {
            codeGenerator.append("static ");
        }

        variableType.getCode(codeGenerator);

        codeGenerator.append(" " + getJavaName() + ";");
        codeGenerator.breakLine();

        if(initializer != null) {
            initializer.getCode(codeGenerator);
        }
    }

    @Override
    public void getStaticBlock(CodeGenerator codeGenerator) {
        if(isStatic()) {
            getConstructorCode(codeGenerator);
        }
    }

    @Override
    public void getStaticConstructor(CodeGenerator codeGenerator) {
        if(isStatic()) {
            getConstructorCode(codeGenerator);

            if(initializer != null) {
                initializer.getConstructor2(codeGenerator);
            }
        }
    }

    @Override
    public void getConstructor(CodeGenerator codeGenerator) {
        if(!isStatic()) {
            getConstructorCode(codeGenerator);
        }

        if(initializer != null) {
            initializer.getConstructor(codeGenerator);
        }
    }

    private void getConstructorCode(CodeGenerator codeGenerator) {
        if(!getVariableType().isEnum() && !(initializer instanceof IType)) {
            codeGenerator.indent();
            codeGenerator.getCompilationUnit().importType(variableType.getType());

            codeGenerator.append(getJavaName() + " = ");
            codeGenerator.append(variableType.getJavaNew(getStaticContext()));

            codeGenerator.append(';');
            codeGenerator.breakLine();
        }
    }

    @Override
    public void getConstructor1(CodeGenerator codeGenerator) {
        if(initializer != null) {
            initializer.getConstructor1(codeGenerator);
        }
    }

    @Override
    public void getConstructor2(CodeGenerator codeGenerator) {
        IAttribute[] attributes = getAttributes();

        for(IAttribute attribute : attributes) {
            codeGenerator.indent();
            codeGenerator.append(getJavaName() + "." + "setAttribute(" + '"' + attribute.getName() + '"' + ", ");
            attribute.getCode(codeGenerator);
            codeGenerator.append(");");
            codeGenerator.breakLine();
        }

        if(getVariableType().isReference())
        {
            codeGenerator.indent();
            codeGenerator.append(getJavaName() + "." + "setAttribute(" + '"' + "index" + '"' + ", " + '"' + getName() + '"' + ");");
            codeGenerator.breakLine();
        }
        
        if(!isStatic() && initializer != null) {
            initializer.getConstructor2(codeGenerator);
        }
    }

    public IPosition getStaticTokenPosition() {
        if(staticToken != null) {
            return staticToken.getPosition();
        }
        return null;
    }

    public IPosition getAccessTokenPosition() {
        if(accessToken != null) {
            return accessToken.getPosition();
        }
        return null;
    }

    @Override
    public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
        variableType.replaceTypeName(parent, type, newTypeName);

        if(initializer != null) {
            initializer.replaceTypeName(parent, type, newTypeName);
        }
    }

}
