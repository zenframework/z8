package org.zenframework.z8.compiler.parser.type.members;

import org.zenframework.z8.compiler.content.HyperlinkKind;
import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.expressions.Constant;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.ConstantToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.GuidToken;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Record extends LanguageElement implements IMember {
	private IToken nameToken;
	private Constant constant;

	private IToken accessToken;

	private ITypeCast typeCast;

	public Record(IToken nameToken) {
		this.nameToken = nameToken;
	}

	@Override
	public IPosition getSourceRange() {
		if(constant != null) {
			return nameToken.getPosition().union(constant.getPosition());
		}
		return nameToken.getPosition();
	}

	@Override
	public IToken getFirstToken() {
		return getFirstToken(super.getFirstToken(), nameToken);
	}

	@Override
	public IPosition getPosition() {
		return nameToken.getPosition();
	}

	@Override
	public int getClosure() {
		return -1;
	}

	@Override
	public void setClosure(int closure) {
	}

	@Override
	public IInitializer getInitializer() {
		return null;
	}

	public void setValue(ConstantToken valueToken) {
		constant = new Constant(valueToken);
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
	public String getJavaName() {
		return nameToken.getRawText();
	}

	@Override
	public String getUserName() {
		return getName();
	}

	@Override
	public String getSignature() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IVariableType getVariableType() {
		return constant.getVariableType();
	}

	@Override
	public boolean isFinal() {
		return true;
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public boolean isPublic() {
		return accessToken != null && accessToken.getId() == IToken.PUBLIC;
	}

	@Override
	public boolean isProtected() {
		return accessToken != null && accessToken.getId() == IToken.PROTECTED;
	}

	@Override
	public boolean isPrivate() {
		return accessToken != null && accessToken.getId() == IToken.PRIVATE;
	}

	public void setAccess(IToken accessToken) {
		this.accessToken = accessToken;
	}

	private GuidToken getGuidValue() {
		return (GuidToken)constant.getToken();
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		IAttribute[] attributes = getAttributes();

		for(IAttribute attribute : attributes)
			attribute.resolveTypes(compilationUnit, declaringType);

		return constant.resolveTypes(compilationUnit, declaringType);
	}

	@Override
	public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveStructure(compilationUnit, declaringType))
			return false;

		if(!constant.resolveStructure(compilationUnit, declaringType))
			return false;

		IMember member = declaringType.findMember(getName());
		IPosition position = nameToken.getPosition();

		if(member != null) {
			if(member.getDeclaringType() == declaringType) {
				setError(position, "Duplicate field " + declaringType.getUserName() + "." + getName());
				setError(member.getPosition(), "Duplicate field " + declaringType.getUserName() + "." + getName());
			} else
				setError(getPosition(), getName() + ": redefinition of " + member.getDeclaringType().getUserName() + "." + getName());

			return false;
		}

		if(constant == null) {
			setFatalError(position, "The member " + nameToken.getRawText() + " must be initialized with guid constant");
			return false;
		}

		if(!(constant.getToken() instanceof GuidToken)) {
			setFatalError(constant.getPosition(), "The type " + constant.getToken().getTypeName() + " can not be used here; use guid");
			return false;
		}

		declaringType.addMember(this);
		compilationUnit.addHyperlink(position, compilationUnit, position, HyperlinkKind.StaticMember);

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		String name = getName();

		if(!Lexer.checkIdentifier(name)) {
			setFatalError(nameToken.getPosition(), "Syntax error on token '" + name + "'. " + name + " is a reserved keyword.");
			return false;
		}

		if(!constant.checkSemantics(compilationUnit, declaringType, declaringMethod, null, null))
			return false;

		for(IType type = declaringType; type != null; type = type.getBaseType()) {
			IMember[] members = type.getMembers();

			for(IMember member : members) {
				if(member instanceof Record) {
					Record element = (Record)member;

					if(element != this && element.getGuidValue().format().equals(getGuidValue().format())) {
						setError(constant.getPosition(), declaringType.getUserName() + "." + getName() + ": id conflicts with " + type.getUserName() + "." + member.getName());
						return false;
					}
				}
			}
		}

		IAttribute[] attributes = getAttributes();

		for(IAttribute attribute : attributes) {
			IMember member = declaringType.findMember(attribute.getName());

			if(member == null) {
				setError(attribute.getNameToken().getPosition(), attribute.getName() + " cannot be resolved to a type member name");
				continue;
			}

			IVariableType variableType = attribute.getVariableType();

			if(variableType == null) {
				setError(attribute.getPosition(), "The value is missing for the attribute " + attribute.getName());
				continue;
			}

			String sqlTypeName = Primary.getSqlTypeName(variableType.getType().getUserName());

			if(sqlTypeName == null) {
				setError(attribute.getPosition(), "The value of type " + variableType.getSignature() + " cannot be converted to " + member.getVariableType().getSignature() + ", (" + member.getDeclaringType().getUserName() + "." + member.getName() + ")");
				continue;
			}

			IType sqlType = Primary.resolveType(compilationUnit, sqlTypeName);

			if(sqlType != null) {
				IVariableType constantType = new VariableType(getCompilationUnit(), sqlType);
				IVariableType memberType = member.getVariableType();

				typeCast = memberType.getCastTo(constantType);

				if(typeCast == null)
					setError(attribute.getPosition(), "The value of type " + variableType.getSignature() + " cannot be converted to " + member.getVariableType().getSignature() + ", (" + member.getDeclaringType().getUserName() + "." + member.getName() + ")");
			}

			IPosition position = attribute.getNameToken().getPosition();
			compilationUnit.addHyperlink(position, member.getCompilationUnit(), member.getPosition(), HyperlinkKind.Record);
		}

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		IType guidType = Primary.resolveType(getCompilationUnit(), Primary.Guid);

		codeGenerator.getCompilationUnit().importType(guidType);
		codeGenerator.append("public static " + guidType.getJavaName() + " " + nameToken.getRawText() + " = new " + guidType.getJavaName() + "(" + getGuidValue().format(true) + ");");
		codeGenerator.breakLine();
	}

	public void getAddRecordCode(CodeGenerator codeGenerator) {
		IAttribute[] attributes = getAttributes();

		if(attributes.length == 0)
			return;

		codeGenerator.indent();
		codeGenerator.append("{");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		String mapType = "java.util.HashMap";

		codeGenerator.indent();
		codeGenerator.append(mapType + " map = new " + mapType + "();");
		codeGenerator.breakLine();

		for(IAttribute attribute : attributes) {
			IVariableType variableType = attribute.getVariableType();

			codeGenerator.getCompilationUnit().importType(variableType.getType());

			codeGenerator.indent();
			codeGenerator.append("map.put(" + attribute.getName() + ".get(), ");
			attribute.getValue().getCode(codeGenerator);
			codeGenerator.append(");");
			codeGenerator.breakLine();
		}

		codeGenerator.indent();
		codeGenerator.append("internalAddRecord(");
		constant.getCode(codeGenerator);
		codeGenerator.append(", map);");
		codeGenerator.breakLine();

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("}");
		codeGenerator.breakLine();
	}
}
