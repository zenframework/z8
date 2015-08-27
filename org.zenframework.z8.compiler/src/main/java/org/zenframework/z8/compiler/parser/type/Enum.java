package org.zenframework.z8.compiler.parser.type;

import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.OperatorToken;
import org.zenframework.z8.compiler.parser.type.members.AbstractMethod;
import org.zenframework.z8.compiler.parser.type.members.EnumElement;
import org.zenframework.z8.compiler.parser.type.members.TypeBody;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Enum extends AbstractType {
    private IToken enumToken;
    private IToken nameToken;

    @SuppressWarnings("unused")
    private IToken leftBrace;
    private List<EnumElement> values;
    private IToken rightBrace;

    public Enum(IToken enumToken) {
        this.enumToken = enumToken;
    }

    @Override
    public IPosition getSourceRange() {
        IPosition start = super.getSourceRange();

        if(start == null) {
            start = enumToken.getPosition();
        }

        if(rightBrace != null) {
            return start.union(rightBrace.getPosition());
        }
        else if(values != null) {
            return start.union(values.get(values.size() - 1).getSourceRange());
        }
        else if(nameToken != null) {
            return start.union(nameToken.getPosition());
        }
        else {
            return start;
        }
    }

    @Override
    public IPosition getPosition() {
        if(nameToken != null) {
            return enumToken.getPosition().union(nameToken.getPosition());
        }
        return enumToken.getPosition();
    }

    @Override
    public IToken getFirstToken() {
        return getFirstToken(super.getFirstToken(), enumToken);
    }

    @Override
    public IToken getNameToken() {
        return nameToken;
    }

    public void setNameToken(IToken nameToken) {

        this.nameToken = nameToken;
        super.setUserName(nameToken.getRawText());
    }

    public void setLeftBrace(IToken leftBrace) {
        this.leftBrace = leftBrace;
    }

    public void setRightBrace(IToken rightBrace) {
        this.rightBrace = rightBrace;
    }

    public void addElement(IToken name) {
        if(values == null) {
            values = new ArrayList<EnumElement>();
        }
        values.add(new EnumElement(name));
    }

    @Override
    public boolean isEnum() {
        return true;
    }

    @Override
    public boolean isFinal() {
        return true;
    }

    @Override
    public boolean resolveType(CompilationUnit compilationUnit) {
        if(!super.resolveType(compilationUnit))
            return false;

        if(nameToken != null) {
            String typeName = getUserName();

            if(!compilationUnit.getSimpleName().equals(typeName)) {
                setFatalError(nameToken.getPosition(), "The type " + typeName + " must be defined in its own file");
                return false;
            }

            if(!Lexer.checkIdentifier(typeName)) {
                setFatalError(nameToken.getPosition(), "Syntax error on token '" + typeName + "'. " + typeName
                        + " is a reserved keyword.");
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveTypes(compilationUnit, declaringType))
            return false;

        IType booleanType = Primary.resolveType(compilationUnit, Primary.Boolean);

        if(booleanType != null) {
            Variable[] parameters = new Variable[] { new Variable(getVariableType(), "value") };

            IMethod enumEquOperator = new AbstractMethod(new VariableType(getCompilationUnit(), booleanType), parameters,
                    null, null) {
                @Override
                public String getJavaName() {
                    assert (false);
                    return null;
                }

                @Override
                public String getName() {
                    return new OperatorToken(IToken.EQU, null).getName();
                }

                @Override
                public IPosition getNamePosition() {
                    return null;
                }
            };

            IMethod enumNotEquOperator = new AbstractMethod(new VariableType(getCompilationUnit(), booleanType), parameters,
                    null, null) {
                @Override
                public String getJavaName() {
                    assert (false);
                    return null;
                }

                @Override
                public String getName() {
                    return new OperatorToken(IToken.NOT_EQU, null).getName();
                }

                @Override
                public IPosition getNamePosition() {
                    return null;
                }
            };

            addMethod(enumEquOperator);
            addMethod(enumNotEquOperator);
        }

        setupNativeAttribute();
        return true;
    }

    @Override
    public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveStructure(compilationUnit, declaringType))
            return false;

        if(values != null) {
            for(EnumElement value : values) {
                value.resolveStructure(compilationUnit, this);
            }
        }

        return true;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        super.getCode(codeGenerator);

        codeGenerator.append("public enum " + getJavaName());
        codeGenerator.breakLine();
        codeGenerator.append("{");
        codeGenerator.breakLine();

        codeGenerator.incrementIndent();

        if(values != null) {
            for(EnumElement value : values) {
                value.getCode(codeGenerator);
            }
        }

        codeGenerator.decrementIndent();

        codeGenerator.append("}");
        codeGenerator.breakLine();
    }

    @Override
    public TypeBody getTypeBody() {
        return null;
    }
}