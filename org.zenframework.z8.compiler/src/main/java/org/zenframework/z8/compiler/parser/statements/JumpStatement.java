package org.zenframework.z8.compiler.parser.statements;

import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IStatement;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.parser.type.members.PriorityOperator;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class JumpStatement extends LanguageElement implements IStatement {
    private IToken jump;
    private ILanguageElement expression;

    private ITypeCast typeCast;

    public JumpStatement(IToken jump, ILanguageElement expression) {
        this.jump = jump;
        this.expression = expression;
    }

    @Override
    public IPosition getSourceRange() {
        if(expression != null) {
            return jump.getPosition().union(expression.getSourceRange());
        }
        return jump.getPosition();
    }

    @Override
    public IToken getFirstToken() {
        return jump;
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveTypes(compilationUnit, declaringType))
            return false;

        if(expression != null && !expression.resolveTypes(compilationUnit, declaringType))
            return false;

        return true;
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        if(jump.getId() == IToken.RETURN) {
            IVariableType returnType = declaringMethod.getVariableType();

            assert (returnType != null);

            IType voidType = Primary.resolveType(compilationUnit, Primary.Void);

            boolean isVoidMethod = false;

            if(voidType != null) {
                isVoidMethod = returnType.getCastTo(voidType) != null;
            }

            if(expression != null) {
                if(!expression.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null)) {
                    return false;
                }

                if(!isVoidMethod) {
                    typeCast = expression.getVariableType().getCastTo(returnType);

                    if(typeCast == null) {
                        setError(expression.getPosition(), "Type mismatch: cannot convert from "
                                + expression.getVariableType().getSignature() + " to " + returnType.getSignature());
                        return false;
                    }
                }
                else {
                    setError(expression.getPosition(), "Void methods cannot return a value");
                    return false;
                }
            }
            else {
                if(!isVoidMethod) {
                    setError(getPosition(), "This method must return result of type " + returnType.getSignature());
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean returnsOnAllControlPaths() {
        return jump.getId() == IToken.RETURN;
    }

    @Override
    public boolean breaksControlFlow() {
        if(jump.getId() == IToken.RETURN) {
            return true;
        }

        ILanguageElement parent = getParent();

        while(parent != null) {
            if(parent instanceof ForStatement || parent instanceof WhileStatement || parent instanceof DoWhileStatement) {
                return true;
            }

            parent = parent.getParent();
        }

        setError(getPosition(), (jump.getId() == IToken.CONTINUE ? "continue" : "break")
                + " cannot be used outside a loop");
        return false;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        codeGenerator.getCompilationUnit().setLineNumbers(getSourceRange().getLine(), codeGenerator.getCurrentLine());

        if(jump.getId() == IToken.BREAK) {
            codeGenerator.append("break;");
            codeGenerator.breakLine();
        }
        else if(jump.getId() == IToken.CONTINUE) {
            codeGenerator.append("continue;");
            codeGenerator.breakLine();
        }
        else if(jump.getId() == IToken.RETURN) {
            codeGenerator.append("return");

            if(expression != null) {
                codeGenerator.append(" ");

                IMethod[] methods = getDeclaringMethod().getVariableType().getMatchingMethods(PriorityOperator.Name);

                assert (methods.length <= 1);

                typeCast.getCode(codeGenerator, expression);

                if(methods.length == 1) {
                    codeGenerator.append("." + methods[0].getJavaName() + "()");
                }
            }
            codeGenerator.append(";");
            codeGenerator.breakLine();
        }
    }

    public ILanguageElement getExpression() {
        return expression;
    }

    @Override
    public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
        if(expression != null) {
            expression.replaceTypeName(parent, type, newTypeName);
        }
    }
}
