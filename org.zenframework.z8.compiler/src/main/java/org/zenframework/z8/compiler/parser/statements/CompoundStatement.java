package org.zenframework.z8.compiler.parser.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IStatement;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class CompoundStatement extends LanguageElement implements IStatement {
    private IToken leftBrace;
    private IToken rightBrace;

    private List<ILanguageElement> elements;
    private boolean autoOpenScope;

    public CompoundStatement(IToken leftBrace) {
        this.leftBrace = leftBrace;
        setAutoOpenScope(true);
    }

    public void setLeftBrace(IToken leftBrace) {
        this.leftBrace = leftBrace;
    }

    public void setRightBrace(IToken rightBrace) {
        this.rightBrace = rightBrace;
    }

    @Override
    public IPosition getPosition() {
        if(elements == null) {
            return getSourceRange();
        }

        return elements.get(0).getPosition();
    }

    @Override
    public IPosition getSourceRange() {
        if(rightBrace != null) {
            return leftBrace.getPosition().union(rightBrace.getPosition());
        }
        else if(elements != null) {
            return leftBrace.getPosition().union(elements.get(elements.size() - 1).getSourceRange());
        }
        else {
            return leftBrace.getPosition();
        }
    }

    @Override
    public IToken getFirstToken() {
        return leftBrace;
    }

    public void addStatement(ILanguageElement element) {
        if(elements == null) {
            elements = new ArrayList<ILanguageElement>();
        }

        element.setParent(this);
        elements.add(element);
    }

    public void setAutoOpenScope(boolean autoOpenScope) {
        this.autoOpenScope = autoOpenScope;
    }

    @Override
    public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveTypes(compilationUnit, declaringType))
            return false;

        if(elements == null)
            return true;

        for(ILanguageElement element : elements) {
            element.resolveTypes(compilationUnit, declaringType);
        }

        return true;
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        if(elements == null)
            return true;

        if(autoOpenScope) {
            declaringMethod.openLocalScope();
        }

        for(ILanguageElement element : elements) {
            element.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null);
        }

        if(autoOpenScope) {
            declaringMethod.closeLocalScope();
        }

        return true;
    }

    @Override
    public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
        if(!super.resolveNestedTypes(compilationUnit, declaringType))
            return false;

        if(elements != null) {
            for(ILanguageElement element : elements) {
                element.resolveNestedTypes(compilationUnit, declaringType);
            }
        }
        return true;
    }

    @Override
    public boolean returnsOnAllControlPaths() {
        if(elements == null) {
            return false;
        }

        for(int i = 0; i < elements.size(); i++) {
            ILanguageElement element = elements.get(i);

            assert (element instanceof IStatement);
            IStatement statement = (IStatement)element;

            boolean returnsOnAllControlPaths = statement.returnsOnAllControlPaths();
            boolean breaksControlFlow = statement.breaksControlFlow();

            if(returnsOnAllControlPaths || breaksControlFlow) {
                if(i != elements.size() - 1) {
                    setError(elements.get(i + 1).getPosition(), "Unreachable code");
                }
                return returnsOnAllControlPaths;
            }
        }

        return false;
    }

    @Override
    public boolean breaksControlFlow() {
        return false;
    }

    @Override
    public void getClassCode(CodeGenerator codeGenerator) {
        if(elements != null) {
            for(ILanguageElement element : elements) {
                element.getClassCode(codeGenerator);
            }
        }
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {
        codeGenerator.append("{");
        codeGenerator.breakLine();

        if(elements != null) {
            codeGenerator.incrementIndent();

            for(ILanguageElement element : elements) {
                codeGenerator.indent();
                element.getCode(codeGenerator);
            }

            codeGenerator.decrementIndent();
        }

        codeGenerator.indent();
        codeGenerator.append("}");
        codeGenerator.breakLine();
    }

    public List<ILanguageElement> getElements() {
        return elements;
    }

    @Override
    public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
        if(elements != null) {
            for(ILanguageElement element : elements) {
                element.replaceTypeName(parent, type, newTypeName);
            }
        }
    }
}
