package org.zenframework.z8.compiler.parser.expressions;

import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.BuiltinNative;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class ArrayInitializer extends LanguageElement {
	private IToken leftBrace;
	private IToken rightBrace;

	private List<ILanguageElement> elements = new ArrayList<ILanguageElement>();

	private IVariable variable;
	private List<ITypeCast> typeCast = new ArrayList<ITypeCast>();

	public ArrayInitializer(IToken leftBrace) {
		this.leftBrace = leftBrace;
	}

	public void setLeftBrace(IToken leftBrace) {
		this.leftBrace = leftBrace;
	}

	public void setRightBrace(IToken rightBrace) {
		this.rightBrace = rightBrace;
	}

	@Override
	public IPosition getSourceRange() {
		if(rightBrace != null)
			return leftBrace.getPosition().union(rightBrace.getPosition());
		else if(elements.size() > 0)
			return leftBrace.getPosition().union(elements.get(elements.size() - 1).getSourceRange());
		else
			return leftBrace.getPosition();
	}

	@Override
	public IToken getFirstToken() {
		return leftBrace;
	}

	public void add(ILanguageElement element) {
		elements.add(element);
		element.setParent(this);
	}

	public int getIndexOf(ILanguageElement element) {
		return elements.indexOf(element);
	}

	@Override
	public IVariableType getVariableType() {
		return variable.getVariableType();
	}

	@Override
	public void setStaticContext(boolean staticContext) {
		super.setStaticContext(staticContext);

		for(ILanguageElement element : elements)
			element.setStaticContext(staticContext);
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		boolean result = true;

		for(ILanguageElement element : elements)
			result &= element.resolveTypes(compilationUnit, declaringType);

		return result;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		IVariableType initType = new VariableType(leftHandValue.getVariableType());

		if(!initType.isArray()) {
			setError(getPosition(), "The operator ={} cannot be applied to type " + initType.getSignature());
			return false;
		}

		if(initType.getRightKey() == null)
			initType.removeRightKey();

		boolean result = true;

		for(ILanguageElement element : elements) {
			if(!element.checkSemantics(compilationUnit, declaringType, declaringMethod, new Variable(initType), null)) {
				result = false;
				continue;
			}

			ITypeCast typeCast = element.getVariableType().getCastTo(initType);

			if(typeCast == null) {
				setError(element.getPosition(), "Type mismatch: cannot convert from " + element.getVariableType().getSignature() + " to " + initType.getSignature());
				result = false;
			}

			this.typeCast.add(typeCast);
		}

		variable = leftHandValue;

		return result;
	}

	private boolean isMap() {
		return getVariableType().getRightKey() != null;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		boolean isMap = isMap();

		int index = 0;

		StringBuffer keys = new StringBuffer();
		StringBuffer values = new StringBuffer();

		for(ILanguageElement element : elements) {
			boolean isLast = (index == elements.size() - 1);

			if(isMap) {
				MapElement pair = (MapElement)element;

				CodeGenerator keyCodeGenerator = new CodeGenerator(getCompilationUnit());
				pair.getKeyCode(keyCodeGenerator);

				CodeGenerator valueCodeGenerator = new CodeGenerator(getCompilationUnit());
				pair.getValueCode(valueCodeGenerator);

				keys.append(keyCodeGenerator.toString() + (isLast ? "" : ", "));
				values.append(valueCodeGenerator.toString() + (isLast ? "" : ", "));
			} else {
				CodeGenerator keyCodeGenerator = new CodeGenerator(getCompilationUnit());
				typeCast.get(index).getCode(keyCodeGenerator, element);
				keys.append(keyCodeGenerator.toString() + (isLast ? "" : ", "));
			}

			index++;
		}

		codeGenerator.append("new " + BuiltinNative.Object + "[]");
		codeGenerator.append('{');
		codeGenerator.append(keys.toString());
		codeGenerator.append('}');

		if(isMap) {
			codeGenerator.append(", ");

			codeGenerator.append("new " + BuiltinNative.Object + "[]");
			codeGenerator.append('{');
			codeGenerator.append(values.toString());
			codeGenerator.append('}');
		}
	}

	public ILanguageElement[] getElements() {
		return elements.toArray(new ILanguageElement[0]);
	}
}
