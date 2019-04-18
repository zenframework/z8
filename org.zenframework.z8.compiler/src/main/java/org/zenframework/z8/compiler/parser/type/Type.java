package org.zenframework.z8.compiler.parser.type;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.BuiltinNative;
import org.zenframework.z8.compiler.parser.expressions.QualifiedName;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.type.members.TypeBody;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.util.Set;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;

public class Type extends AbstractType {
	private IToken finalToken;
	private IToken classToken;
	private IToken typeNameToken;
	@SuppressWarnings("unused")
	private QualifiedName baseTypeName;

	private VariableType baseVariableType;

	private TypeBody body;

	public Type(IToken finalToken, IToken classToken) {
		this.finalToken = finalToken;
		this.classToken = classToken;
	}

	@Override
	public ILanguageElement getParent() {
		return getCompilationUnit();
	}

	@Override
	public IPosition getSourceRange() {
		IPosition start = super.getSourceRange();

		if(start == null)
			start = classToken.getPosition();

		if(body != null)
			return start.union(body.getSourceRange());
		else if(baseVariableType != null)
			return start.union(baseVariableType.getPosition());

		return start.union(typeNameToken.getPosition());
	}

	@Override
	public IPosition getPosition() {
		return typeNameToken != null ? typeNameToken.getPosition() : classToken.getPosition();
	}

	@Override
	public IToken getFirstToken() {
		return getFirstToken(super.getFirstToken(), classToken);
	}

	@Override
	public ILanguageElement getElementAt(IPosition position) {
		if(getSourceRange().contains(position))
			return this;

		return null;
	}

	public IToken getClassToken() {
		return classToken;
	}

	@Override
	public IToken getNameToken() {
		return typeNameToken;
	}

	public void setTypeNameToken(IToken typeNameToken) {
		this.typeNameToken = typeNameToken;
		setUserName(typeNameToken.getRawText());
	}

	@Override
	public boolean isFinal() {
		return finalToken != null;
	}

	public void setBaseTypeName(QualifiedName baseTypeName) {
		this.baseTypeName = baseTypeName;
		baseVariableType = new VariableType(baseTypeName);
	}

	public TypeBody getBody() {
		return body;
	}

	public void setBody(TypeBody body) {
		this.body = body;
	}

	@Override
	public Project getProject() {
		return getCompilationUnit().getProject();
	}

	@Override
	public boolean resolveType(CompilationUnit compilationUnit) {
		if(!super.resolveType(compilationUnit))
			return false;

		String typeName = getUserName();

		if(typeName == null)
			return false;

		if(!isNative() && !Lexer.checkIdentifier(typeName)) {
			setFatalError(typeNameToken.getPosition(), "Syntax error on token '" + typeName + "'. " + typeName + " is a reserved keyword.");
			return false;
		}

		if(!compilationUnit.getSimpleName().equals(typeName)) {
			setError(typeNameToken.getPosition(), "The type " + typeName + " must be defined in its own file");
			return false;
		}

		return true;
	}

	@Override
	public boolean canBeBaseTypeOf(IType type) {
		if(equals(type))
			return false;

		if(baseVariableType == null)
			return true;

		IType baseType = baseVariableType.getType();

		if(type.equals(baseType))
			return false;

		return baseType != null ? baseType.canBeBaseTypeOf(type) : true;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(typesResolved()) {
			return true;
		}

		setTypesResolved(true);

		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		if(baseVariableType != null) {
			if(baseVariableType.resolveTypes(compilationUnit, this)) {
				IType baseType = baseVariableType.getType();

				if(baseType.equals(this) || baseType.isSubtypeOf(this)) {
					setError(baseVariableType.getPosition(), "Cycle detected: the type " + baseVariableType.getSignature() + " cannot extend/implement itself");
					baseType = null;
				} else if(baseType.isEnum()) {
					setError(baseVariableType.getPosition(), "The enum " + baseVariableType.getSignature() + " cannot be the supertype of " + getUserName() + "; a supertype must be a type");
					baseType = null;
				} else if(baseType.isFinal()) {
					setError(baseVariableType.getPosition(), "The type " + baseVariableType.getSignature() + " is final and cannot be the supertype of " + getUserName());
					baseType = null;
				}

				setBaseVariableType(baseVariableType);

			}
		}

		setupNativeAttribute();

		if(body != null)
			body.resolveTypes(compilationUnit, this);

		return true;
	}

	@Override
	public boolean resolveStructure(CompilationUnit compilationUnit, IType declaringType) {
		if(structureResolved())
			return true;

		setStructureResolved(true);

		if(!super.resolveStructure(compilationUnit, declaringType))
			return false;

		IType baseType = getBaseType();

		if(baseType != null && !canBeSubtypeOf(baseType)) {
			setError(typeNameToken.getPosition(), "The hierarchy of the type " + getUserName() + " is inconsistent");
			setBaseVariableType(null);
		}

		if(baseType != null)
			baseType.resolveStructure(baseType.getCompilationUnit(), baseType);

		if(body != null)
			body.resolveStructure(compilationUnit, this);

		return true;
	}

	@Override
	public boolean checkSemantics(CompilationUnit compilationUnit, IType declaringType, IMethod declaringMethod, IVariable leftHandValue, IVariableType context) {
		if(semanticsChecked())
			return true;

		setSemanticsChecked(true);

		if(!super.checkSemantics(compilationUnit, declaringType, declaringMethod, leftHandValue, context))
			return false;

		IType baseType = getBaseType();

		if(baseType != null)
			baseType.checkSemantics(baseType.getCompilationUnit(), baseType, null, null, null);

		if(body != null)
			body.checkSemantics(compilationUnit, this, null, null, null);

		if(getBaseType() != null)
			compilationUnit.addHyperlink(baseVariableType.getPosition(), getBaseType());

		if(typeNameToken != null)
			compilationUnit.addHyperlink(typeNameToken.getPosition(), this);

		return true;
	}

	@Override
	public boolean resolveNestedTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(nestedTypesResolved()) {
			return true;
		}
		setNestedTypesResolved(true);

		if(!super.resolveNestedTypes(compilationUnit, declaringType))
			return false;

		IType baseType = getBaseType();

		if(baseType != null)
			baseType.resolveNestedTypes(baseType.getCompilationUnit(), baseType);


		if(body != null)
			body.resolveNestedTypes(compilationUnit, declaringType);

		return true;
	}

	class MemberInitializationInfo {
		String name;
		IMember member;

		IType type;
		Set<ILanguageElement> initializers;

		MemberInitializationInfo(String name) {
			this.name = name;
		}

		IVariableType getVariableType() {
			if(type != null)
				return type.getVariableType();

			if(member != null)
				return member.getVariableType();

			throw new UnsupportedOperationException();
		}

		String getQualifiedJavaName() {
			return getVariableType().getType().getQualifiedJavaName();
		}

		ILanguageElement[] getInitializers() {
			return initializers != null ? initializers.toArray(new ILanguageElement[0]) : new ILanguageElement[0];
		}

		void addInitializer(ILanguageElement initializer) {
			if(initializers == null)
				initializers = new Set<ILanguageElement>();
			initializers.add(initializer);
		}
	}

	@SuppressWarnings("unused")
	private MemberInitializationInfo[] getMemberInitilization() {
		Map<String, MemberInitializationInfo> result = new HashMap<String, MemberInitializationInfo>();

		initMembersMap(result, this);
		collectInitializers(result, this);
		collectNestedTypes(result, this);

		return result.values().toArray(new MemberInitializationInfo[0]);
	}

	private void initMembersMap(Map<String, MemberInitializationInfo> result, IType type) {
		IMember[] members = type.getAllMembers();

		for(IMember member : members) {
			if(!(member instanceof IMethod) && member.getVariableType().isReference()) {
				String name = member.getDeclaringType().getNestedUserName() + '.' + member.getName();
				MemberInitializationInfo info = new MemberInitializationInfo(name);
				info.member = member;
				result.put(name, info);
			}
		}
	}

	private void collectNestedTypes(Map<String, MemberInitializationInfo> result, IType type) {
		if(type.getRecursionFlag())
			return;

		type.setRecursionFlag(true);

		IType baseType = type.getBaseType();

		if(baseType != null)
			collectNestedTypes(result, baseType);

		IType[] nestedTypes = type.getNestedTypes();

		for(IType nestedType : nestedTypes) {
			if(nestedType instanceof DeclaratorNestedType) {
				String name = nestedType.getNestedUserName();

				MemberInitializationInfo info = result.get(name);

				if(info == null) {
					info = new MemberInitializationInfo(name);
					result.put(name, info);
				}

				info.type = nestedType;
				collectNestedTypes(result, nestedType);
			}

			initMembersMap(result, nestedType);
			collectInitializers(result, nestedType);
		}

		type.setRecursionFlag(false);
	}

	private void collectInitializers(Map<String, MemberInitializationInfo> result, IType type) {
		if(type.getRecursionFlag())
			return;

		type.setRecursionFlag(true);

		IType baseType = type.getBaseType();

		if(baseType != null)
			collectInitializers(result, baseType);

		IInitializer[] initializers = type.getInitializers();

		for(IInitializer initializer : initializers) {
			String name = type.getNestedUserName() + '.' + initializer.getLeftName();

			MemberInitializationInfo info = result.get(name);

			if(initializer instanceof IType) {
				if(!type.isNative()) {
					if(info == null) {
						info = new MemberInitializationInfo(name);
						result.put(name, info);
					}

					info.type = (IType)initializer;
				}

				collectNestedTypes(result, (IType)initializer);
			}

			if(info != null)
				info.addInitializer(initializer);
		}

		type.setRecursionFlag(false);
	}

	@Override
	public void getCode(CodeGenerator headerGenerator) {
		CodeGenerator codeGenerator = new CodeGenerator(getCompilationUnit());

		codeGenerator.breakLine();

		IVariableType baseVariableType = getBaseVariableType();
		IType baseType = getBaseType();

		String base = baseVariableType == null ? BuiltinNative.OBJECT : (baseVariableType.isQualified() ? baseType.getQualifiedJavaName() : baseType.getJavaName());

		codeGenerator.indent();
		codeGenerator.append("@SuppressWarnings(\"all\")");
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("public class " + getJavaName() + " extends " + base + " {");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		if(!extendsPrimary())
			generateClassCode(codeGenerator);

		if(body != null) {
			codeGenerator.breakLine();
			body.getCode(codeGenerator);
		}

		codeGenerator.append("}");
		codeGenerator.breakLine();

		codeGenerator.decrementIndent();

		// Import block code
		super.getCode(headerGenerator);

		for(String standardImport : BuiltinNative.StandardImports) {
			headerGenerator.append(standardImport);
			headerGenerator.breakLine();
		}

		CompilationUnit compilationUnit = getCompilationUnit();

		IType[] usedTypes = compilationUnit.getImportedTypes();

		for(IType type : usedTypes) {
			headerGenerator.append("import " + type.getQualifiedJavaName() + ";");
			headerGenerator.breakLine();
		}

		compilationUnit.setTargetLinesOffset(headerGenerator.getCurrentLine());

		headerGenerator.append(codeGenerator);
	}

	@Override
	public TypeBody getTypeBody() {
		return body;
	}
}
