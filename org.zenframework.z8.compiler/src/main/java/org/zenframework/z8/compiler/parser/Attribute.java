package org.zenframework.z8.compiler.parser;

import org.zenframework.z8.compiler.content.HyperlinkKind;
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
import org.zenframework.z8.compiler.parser.expressions.Constant;
import org.zenframework.z8.compiler.parser.expressions.Postfix;
import org.zenframework.z8.compiler.parser.expressions.QualifiedName;
import org.zenframework.z8.compiler.parser.grammar.lexer.ABC;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.BooleanToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.ConstantToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.DateToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.StringToken;
import org.zenframework.z8.compiler.parser.type.Enum;
import org.zenframework.z8.compiler.parser.type.Type;
import org.zenframework.z8.compiler.parser.type.members.Record;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Attribute extends LanguageElement implements IAttribute {
	static final int MaxNameAttributeValueLength = 128;
	static final int MaxScheduledAttributeValueLength = 128;

	private IToken leftBracket;
	private IToken rightBracket;

	private IToken nameToken;
	private ConstantToken valueToken;

	private ILanguageElement value;

	public Attribute(IToken leftBracket, IToken nameToken, ConstantToken valueToken, IToken rightBracket) {
		this.leftBracket = leftBracket;
		this.nameToken = nameToken;
		this.valueToken = valueToken;
		this.value = this.valueToken != null ? new Constant(this.valueToken) : null;
		this.rightBracket = rightBracket;
	}

	public Attribute(IToken leftBracket, IToken nameToken, ILanguageElement value, IToken rightBracket) {
		this.leftBracket = leftBracket;
		this.nameToken = nameToken;
		this.value = value;
		this.rightBracket = rightBracket;
	}

	@Override
	public IPosition getSourceRange() {
		return leftBracket.getPosition().union(rightBracket.getPosition());
	}

	@Override
	public IToken getFirstToken() {
		return leftBracket;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public String getName() {
		return nameToken.getRawText();
	}

	@Override
	public String getValueString() {
		if(valueToken != null)
			return valueToken.getValueString();
		return "";
	}

	@Override
	public IToken getNameToken() {
		return nameToken;
	}

	@Override
	public ConstantToken getValueToken() {
		return valueToken;
	}

	@Override
	public ILanguageElement getValue() {
		return value;
	}

	@Override
	public IVariableType getVariableType() {
		if(value != null)
			return value.getVariableType();
		return null;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		return value == null || value.resolveTypes(compilationUnit, declaringType);
	}

	private boolean isRecordAttribute() {
		ILanguageElement parent = getParent();

		return parent instanceof Record;
	}

	private boolean isTypeAttribute() {
		ILanguageElement parent = getParent();

		return parent instanceof Type || parent instanceof Enum;
	}

	private boolean validateValue() {
		if(valueToken != null || value == null)
			return true;

		if(isTypeAttribute()) {
			if(value instanceof Postfix) {
				Postfix postfix = (Postfix)value;

				if(postfix.getPostfix() == null && postfix.getPrefix() instanceof QualifiedName)
					return true;
			}

			return false;
		}

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		if(value != null) {
			if(!validateValue()) {
				setError(value.getPosition(), "Only constants or qualified names can be used as an attribute value in this context");
				return false;
			}

			if(!value.checkSemantics(compilationUnit, isTypeAttribute() ? null : declaringType, declaringMethod, null, null))
				return false;

			IVariableType variableType = getVariableType();

			if(!variableType.isReference() && !variableType.extendsPrimary()) {
				setError(value.getPosition(), variableType.getSignature() + " cannot be used as an attribute value");
				return false;
			}
		}

		if(isRecordAttribute())
			return true;

		IPosition position = getNameToken().getPosition();
		compilationUnit.addHyperlink(position, compilationUnit, position, HyperlinkKind.Attribute);

		String name = getName();

		if(name.equals(IAttribute.Primary) || name.equals(IAttribute.Native)) {
			if(value == null) {
				compilationUnit.error(getNameToken().getPosition(), "The attribute " + name + " must have a constant value");
				return false;
			} else if(valueToken == null) {
				compilationUnit.error(getNameToken().getPosition(), "The attribute " + name + " must have a constant value not an expression");
				return false;
			}
		}

		if(name.equals(IAttribute.Job) && valueToken != null) {
			if (!(valueToken instanceof StringToken)) {
				getCompilationUnit().error(valueToken.getPosition(), "The attribute " + name + " must be a string");
				return false;
			}

			String cron = valueToken.getValueString();

			if (!cron.isEmpty() && !DateToken.checkCron(valueToken.getValueString())) {
				getCompilationUnit().error(valueToken.getPosition(), "The attribute " + name + " must be a valid CRON expression:"
						+ "\n    +---------- minute (0 - 59)"
						+ "\n    | +-------- hour (0 - 23)"
						+ "\n    | | +------ day of month (1 - 31)"
						+ "\n    | | | +---- month (1 - 12)"
						+ "\n    | | | | +-- day of week (0 - 6)"
						+ "\n    | | | | |"
						+ "\n    X X X X X - CRON expression contains 5 fields"
						+ "\n"
						+ "\n Value of a field can be:"
						+ "\n    - asterisk '*' means any value"
						+ "\n    - number, i.e. '1', '2', etc."
						+ "\n    - period, i.e. '1-5', '2-23'"
						+ "\n    - asterisk or period with multiplicity, i.e. '*/2' (any even value), '2-9/3' (any value multiple of 3 between 2 and 9)"
						+ "\n    - comma-separated list of other values, i.e. '1,2-4,6-12/3,*/5'"
						+ "\n"
						+ "\n Examples:"
						+ "\n    * * * * * - every minute"
						+ "\n    */5 * * * * - every five minutes"
						+ "\n    5 0 * * * - every day at 00:05"
						+ "\n    15 14 1 * * - every 1st day of month at 14:15"
						+ "\n   0 22 * * 1-5 - Monday to Friday at 22:00"
						+ "\n    23 */2 * * * - at 23rd minute of every even hour"
						+ "\n    15 10,13 * * 1,4 - Monday and Thursday at 10:15 and 13:15");
				return false;
			}
		}

		if(name.equals(IAttribute.Request) && valueToken != null && !(valueToken instanceof BooleanToken)) {
			getCompilationUnit().error(valueToken.getPosition(), "The attribute " + name + " must be a boolean");
			return false;
		}

		if(name.equals(IAttribute.Name) && value != null) {
			if(valueToken == null) {
				getCompilationUnit().error(getNameToken().getPosition(), "The attribute '" + name + "' must have a constant string value not an expression");
				return false;
			} else {
				Constant constant = (Constant)value;

				if(constant.isNLSString()) {
					getCompilationUnit().error(getNameToken().getPosition(), "The attribute '" + name + "' must have a constant string value not a resource string key");
					return false;
				}
			}

			char[] chars = getValueString().toCharArray();

			for(int i = 0; i < chars.length; i++) {
				char ch = chars[i];

				if(ch == '_' || ABC.isAlpha(ch) || i > 0 && (ch == ' ' || ABC.isDigit(ch) || ABC.isPunctuator(ch)))
					continue;

				compilationUnit.error(valueToken.getPosition(), "The attribute '" + name + "' has an illegal value");
				return false;
			}

			if(chars.length > MaxNameAttributeValueLength) {
				getCompilationUnit().error(valueToken.getPosition(), "The attribute " + name + " must be no longer then " + MaxNameAttributeValueLength + " symbols");
				return false;
			}

			if(!checkTypeAttribute(IAttribute.Name))
				return false;

			if(!checkMemberAttribute(declaringType))
				return false;
		}

		return true;

	}

	private boolean checkTypeAttribute(String name) {
		if(!isTypeAttribute())
			return true;

		CompilationUnit[] dependencies = getCompilationUnit().getProject().getDependencies();

		for(CompilationUnit compilationUnit : dependencies) {
			if(compilationUnit != getCompilationUnit()) {
				IType type = compilationUnit.getType();

				if(type != null && !checkAttributesAgainst(name, type.getAttributes()))
					return false;
			}
		}
		return true;
	}

	private boolean checkMemberAttribute(IType type) {
		if(isTypeAttribute())
			return true;

		IMember[] members = type.getMembers();
		IInitializer[] initializers = type.getInitializers();

		for(IMember member : members) {
			if(member != getParent() && !checkAttributesAgainst(IAttribute.Name, member.getAttributes()))
				return false;
		}

		for(IInitializer initializer : initializers) {
			if(initializer != getParent() && !checkAttributesAgainst(IAttribute.Name, initializer.getAttributes()))
				return false;
		}

		IType baseType = type.getBaseType();

		if(baseType != null)
			return checkMemberAttribute(baseType);

		return true;
	}

	private boolean checkAttributesAgainst(String name, IAttribute[] attributes) {
		String value = getValueString();

		for(IAttribute attribute : attributes) {
			if(attribute.getName().equals(name) && value.equals(attribute.getValueString())) {
				getCompilationUnit().error(getPosition(), "The value " + value + " of the attribute " + name + " conflicts with one in the type " + attribute.getDeclaringType().getUserName());
				return false;
			}
		}

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		if(value != null) {
			IVariableType variableType = getVariableType();

			if(variableType != null) {
				if(variableType.isReference()) {
					value.getCode(codeGenerator);
					codeGenerator.append(".getAttribute(");
					codeGenerator.append('"');
					codeGenerator.append(nameToken.getRawText());
					codeGenerator.append('"');
					codeGenerator.append(')');
				} else if(valueToken != null) {
					codeGenerator.append('"');
					codeGenerator.append(StringToken.encode(getValueString()));
					codeGenerator.append('"');
				} else {
					value.getCode(codeGenerator);
					if(variableType.getType() != org.zenframework.z8.compiler.parser.type.Primary.resolveType(getCompilationUnit(), org.zenframework.z8.compiler.parser.type.Primary.String))
						codeGenerator.append(".toString()");
					else
						codeGenerator.append(".get()");
				}
			}
		} else
			codeGenerator.append('"').append('"');
	}
}
