package org.zenframework.z8.compiler.parser.type.members;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.expressions.Initialization;
import org.zenframework.z8.compiler.parser.expressions.OperatorNew;
import org.zenframework.z8.compiler.parser.expressions.QualifiedName;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.OperatorToken;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class MemberInit extends Initialization implements IInitializer {
    public MemberInit(QualifiedName left, OperatorToken operatorToken, ILanguageElement right) {
        super(left, operatorToken, right);
    }

    public QualifiedName getQualifiedName() {
        return (QualifiedName)getLeftElement();
    }

    public String getName() {
        return getQualifiedName().toString();
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String getLeftName() {
        QualifiedName qualifiedName = (QualifiedName)getLeftElement();
        return qualifiedName.toString();
    }

    @Override
    public String getRightName() {
        ILanguageElement initializer = getRightElement();

        if(initializer instanceof OperatorNew) {
            OperatorNew type = (OperatorNew)initializer;
            return type.getVariableType().getJavaNew(getStaticContext());
        }

        CodeGenerator codeGenerator = new CodeGenerator(getCompilationUnit());
        initializer.getCode(codeGenerator);
        return codeGenerator.toString();
    }

    @Override
    public boolean getStaticContext() {
        ILanguageElement parent = getParent();

        if(parent != null) {
            IMember member = (IMember)parent;
            return member.isStatic();
        }

        return false;
    }

    @Override
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }

        if(object instanceof MemberInit) {
            MemberInit memberInit = (MemberInit)object;

            if(getDeclaringType().equals(memberInit.getDeclaringType())) {
                return getLeftName().equals(memberInit.getLeftName());
            }
        }

        return false;
    }

    @Override
    public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod,
            IVariable leftHandValue, IVariableType context) {
        if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
            return false;

        if(getAttributes().length != 0 && !getVariableType().isReference()) {
            setError(getLeftElement().getPosition(), "Attributes cannot be applied to variables of a primary type");
        }

        IInitializer initializer = declaringType.findInitializer(getName());

        if(initializer != null && initializer instanceof MemberInit) {
            if(declaringType.equals(initializer.getDeclaringType())) {
                setError(
                        initializer.getLeftElement().getPosition(),
                        "Duplicate initialization of " + initializer.getLeftName() + " in type "
                                + declaringType.getUserName());
                setError(getLeftElement().getPosition(), "Duplicate initialization of " + initializer.getLeftName()
                        + " in type " + declaringType.getUserName());
            }
            /*
            			else if(initializer.getVariableType().isArray() && getOperator().getId() == IToken.ASSIGN)
            			{
            				setError(getPosition(), "Array initialization conflicts with one in type " + initializer.getDeclaringType().getUserName() + "; use += instead");
            			}
            */
            else {
                declaringType.addInitializer(this);
            }
        }
        /*
        		else if(getVariableType().isArray() && getOperator() != null && getOperator().getId() == IToken.ASSIGN)
        		{
        			QualifiedName qualifiedName = (QualifiedName)getLeftElement();
        			
        			IToken[] tokens = qualifiedName.getTokens();
        			
        			for(int i = 0; i < tokens.length - 1; i++)
        			{
        				String name = qualifiedName.toString(i + 1);

        				initializer = qualifiedName.getVariableType(i).getType().findInitializer(name);
        				
        				if(initializer != null && initializer.getVariableType().isArray())
        				{
        					setError(getPosition(), "Array initialization conflicts with one in type " + initializer.getDeclaringType().getUserName() + "; use += instead");
        					return true;
        				}
        			}

        			declaringType.addInitializer(this);
        		}
        */
        else {
            declaringType.addInitializer(this);
        }

        return true;
    }

    @Override
    public void getCode(CodeGenerator codeGenerator) {}

    private boolean isConstructor1Assignment() {
        return getVariableType().isReference() && getOperator() != null && 
                getOperator().getId() == IToken.ASSIGN;
    }

    @Override
    public void getConstructor1(CodeGenerator codeGenerator) {
        if(getRightElement() != null && isConstructor1Assignment()) {
            codeGenerator.indent();

            super.getCode(codeGenerator);

            codeGenerator.append(";");
            codeGenerator.breakLine();
        }
    }

    @Override
    public void getConstructor2(CodeGenerator codeGenerator) {
        if(getRightElement() != null && !isConstructor1Assignment()) {
            codeGenerator.indent();

            super.getCode(codeGenerator);

            codeGenerator.append(";");
            codeGenerator.breakLine();
        }

        IAttribute[] attributes = getAttributes();

        for(IAttribute attribute : attributes) {
            codeGenerator.indent();
            getLeftElement().getCode(codeGenerator);
            codeGenerator.append(".setAttribute(" + '"' + attribute.getName() + '"' + ", ");
            attribute.getCode(codeGenerator);
            codeGenerator.append(");");
            codeGenerator.breakLine();
        }
    }
}
