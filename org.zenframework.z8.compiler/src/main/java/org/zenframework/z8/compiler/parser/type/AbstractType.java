package org.zenframework.z8.compiler.parser.type;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.ITypeCast;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.BuiltinNative;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.type.members.MemberInit;
import org.zenframework.z8.compiler.parser.type.members.TypeBody;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.util.Set;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public abstract class AbstractType extends LanguageElement implements IType {
	private String userName;
	private String javaName;
	private String javaPackage;

	private ImportBlock importBlock;

	protected IType baseType;
	protected IType containerType;
	protected List<IType> nestedTypes;

	protected boolean recursionFlag;

	private Set<IMember> members;
	private Set<IMethod> methods;
	private List<IMethod> typeCastOperators;

	private Set<IInitializer> initializers;

	private String constructionStage = BuiltinNative.Constructor;

	private boolean typesResolved;
	private boolean structureResolved;
	private boolean semanticsChecked;
	private boolean nestedTypesResolved;

	protected AbstractType() {
	}

	@Override
	public boolean getRecursionFlag() {
		return recursionFlag;
	}

	@Override
	public void setRecursionFlag(boolean recursionFlag) {
		this.recursionFlag = recursionFlag;
	}

	@Override
	public int hashCode() {
		String userName = getUserName();
		return userName == null ? super.hashCode() : userName.hashCode();
	}

	@Override
	public String toString() {
		return getUserName();
	}

	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}

		if(object instanceof IType) {
			IType type = (IType)object;

			String userName = getUserName();

			return getCompilationUnit() == type.getCompilationUnit() && userName != null && userName.equals(type.getUserName());

		}

		return false;
	}

	@Override
	public IToken getNameToken() {
		return null;
	}

	public String getName() {
		return getUserName();
	}

	@Override
	public String getUserName() {
		return userName;
	}

	protected void setUserName(String name) {
		this.userName = name;
	}

	@Override
	public String getJavaName() {
		if(javaName == null) {
			javaName = userName;
		}

		return javaName;
	}

	@Override
	public String getNestedJavaName() {
		IType container = getContainerType();

		if(container != null)
			return container.getNestedJavaName() + '.' + getJavaName();

		return getJavaName();
	}

	@Override
	public String getNestedUserName() {
		IType container = getContainerType();

		if(container != null)
			return container.getNestedUserName() + '.' + getUserName();

		return getUserName();
	}

	@Override
	public String getUserPackage() {
		return getCompilationUnit().getPackage();
	}

	@Override
	public String getJavaPackage() {
		return javaPackage != null ? javaPackage : getCompilationUnit().getPackage();
	}

	@Override
	public String getQualifiedJavaName() {
		String javaPackage = getJavaPackage();
		return javaPackage + (javaPackage.isEmpty() ? "" : '.') + getNestedJavaName();
	}

	@Override
	public String getQualifiedUserName() {
		String userPackage = getJavaPackage();
		return userPackage + (userPackage.isEmpty() ? "" : '.') + getNestedUserName();
	}

	protected void setJavaName(String javaName) {
		this.javaName = javaName;
	}

	@Override
	public IVariableType getVariableType() {
		return new VariableType(getCompilationUnit(), this);
	}

	@Override
	public IType getTopLevelContainerType() {
		IType container = getContainerType();

		if(container == null)
			return this;

		return container.getTopLevelContainerType();
	}

	@Override
	public ImportBlock getImportBlock() {
		return importBlock;
	}

	@Override
	public void setImportBlock(ImportBlock importBlock) {
		this.importBlock = importBlock;
	}

	@Override
	public ImportElement[] getImports() {
		if(importBlock == null)
			return new ImportElement[0];

		return importBlock.getImportElements();
	}

	@Override
	public boolean isPrimary() {
		return getAttribute(IAttribute.Primary) != null;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public boolean extendsPrimary() {
		if(isPrimary())
			return true;

		IType baseType = getBaseType();

		if(baseType != null)
			return baseType.extendsPrimary();

		return false;
	}

	@Override
	public boolean isNative() {
		return isPrimary() || getAttribute(IAttribute.Native) != null;
	}

	@Override
	public boolean isEnum() {
		return false;
	}

	@Override
	public boolean isAbstract() {
		return getAttribute(IAttribute.Abstract) != null;
	}

	@Override
	public IType getBaseType() {
		return baseType;
	}

	@Override
	public void setBaseType(IType baseType) {

		this.baseType = baseType;

		if(this.baseType != null) {
			CompilationUnit compilationUnit = getCompilationUnit();
			compilationUnit.importType(this.baseType.getCompilationUnit().getType());
			compilationUnit.addContributor(this.baseType.getCompilationUnit());
		}
	}

	@Override
	public boolean canBeBaseTypeOf(IType type) {
		return true;
	}

	@Override
	public boolean canBeSubtypeOf(IType type) {
		if(type == null)
			return true;

		return type.canBeBaseTypeOf(this);
	}

	@Override
	public IType getContainerType() {
		return containerType;
	}

	public void setContainerType(IType containerType) {
		this.containerType = containerType;
		getCompilationUnit().addContributor(containerType.getCompilationUnit());
	}

	@Override
	public IType[] getNestedTypes() {
		if(nestedTypes != null) {
			return nestedTypes.toArray(new IType[nestedTypes.size()]);
		}
		return new IType[0];
	}

	@Override
	public IType getNestedType(String name) {
		IType[] nestedTypes = getNestedTypes();

		for(IType nestedType : nestedTypes) {
			if(name.equals(nestedType.getUserName()))
				return nestedType;
		}

		return null;
	}

	@Override
	public IType lookupNestedType(String qualifiedName) {
		if(qualifiedName == null)
			return null;

		List<String> names = new ArrayList<String>();

		while(true) {
			int index = qualifiedName.indexOf(".");

			if(index == -1) {
				names.add(qualifiedName);
				break;
			}

			String name = qualifiedName.substring(0, index);
			qualifiedName = qualifiedName.substring(index + 1);

			names.add(name);
		}

		IType type = this;

		for(String name : names) {
			IType[] nestedTypes = type.getNestedTypes();

			type = null;

			for(IType nestedType : nestedTypes) {
				if(nestedType.getJavaName().equals(name))
					type = nestedType;
			}

			if(type == null)
				return null;
		}

		return type;
	}

	@Override
	public void addNestedType(IType type) {
		if(nestedTypes == null)
			nestedTypes = new ArrayList<IType>();

		nestedTypes.add(type);
	}

	@Override
	public IMember[] getMembers() {
		if(members == null)
			return new IMember[0];
		return members.toArray(new IMember[members.size()]);
	}

	@Override
	public IMember[] getAllMembers() {
		IType type = this;

		Set<IMember> result = new Set<IMember>();

		while(type != null) {
			IMember[] members = type.getMembers();

			for(IMember member : members)
				result.add(member);

			IMethod[] methods = type.getMethods();

			for(IMember method : methods) {
				if(result.get(method) == null)
					result.add(method);
			}

			type = type.getBaseType();
		}

		return result.toArray(new IMember[result.size()]);
	}

	@Override
	public IMethod[] getMethods() {
		if(methods == null)
			return new IMethod[0];
		return methods.toArray(new IMethod[methods.size()]);
	}

	@Override
	public IInitializer[] getInitializers() {
		if(initializers == null)
			return new IInitializer[0];
		return initializers.toArray(new IInitializer[initializers.size()]);
	}

	@Override
	public IInitializer[] getAllInitializers() {
		IType type = this;

		List<IInitializer> result = new ArrayList<IInitializer>();

		while(type != null) {
			IInitializer[] initializers = type.getInitializers();

			for(IInitializer initializer : initializers)
				result.add(0, initializer);

			type = type.getBaseType();
		}

		return result.toArray(new IInitializer[result.size()]);
	}

	@Override
	public void setConstructionStage(String stage) {
		constructionStage = stage;
	}

	@Override
	public String getConstructionStage() {
		return constructionStage == BuiltinNative.Constructor2 ? "" : constructionStage;
	}

	protected void setupNativeAttribute() {
		IAttribute attribute = getAttribute(IAttribute.Native);

		if(attribute == null)
			attribute = getAttribute(IAttribute.Primary);

		if(attribute != null) {
			String qualifiedName = attribute.getValueString();

			int pos = qualifiedName.lastIndexOf('.');

			if(pos != -1 && pos != qualifiedName.length() - 1) {
				javaName = qualifiedName.substring(pos + 1);
				javaPackage = qualifiedName.substring(0, pos);
			} else {
				javaName = qualifiedName;
				javaPackage = "";
			}
		}
	}

	@Override
	public IMember getMember(String name) {
		return members != null ? members.get(name) : null;
	}

	@Override
	public IMethod getMethod(String signature) {
		return methods != null ? methods.get(signature) : null;
	}

	@Override
	public IInitializer getInitializer(String name) {
		return initializers != null ? initializers.get(name) : null;
	}

	@Override
	public void addMember(IMember member) {
		if(members == null)
			members = new Set<IMember>();
		members.add(member);
	}

	@Override
	public void addMethod(IMethod method) {
		if(methods == null)
			methods = new Set<IMethod>();
		methods.add(method);
	}

	@Override
	public IMethod[] getTypeCastOperators() {
		AbstractType type = this;

		Set<IMethod> result = new Set<IMethod>();

		while(type != null) {
			if(type.typeCastOperators != null) {
				for(IMethod method : type.typeCastOperators)
					result.add(method);
			}

			type = (AbstractType)type.getBaseType();
		}

		return result.toArray(new IMethod[result.size()]);
	}

	@Override
	public void addTypeCastOperator(IMethod operator) {
		if(typeCastOperators == null)
			typeCastOperators = new ArrayList<IMethod>();
		typeCastOperators.add(operator);
	}

	@Override
	public void addInitializer(IInitializer initializer) {
		if(initializers == null)
			initializers = new Set<IInitializer>();
		initializers.add(initializer);
	}

	@Override
	public IMember findMember(String name) {
		IMember member = getMember(name);

		if(member != null)
			return member;

		if(getBaseType() != null)
			return getBaseType().findMember(name);

		return null;
	}

	@Override
	public IMethod findMethod(String signature) {
		IMethod method = getMethod(signature);

		if(method != null)
			return method;

		if(getBaseType() != null)
			return getBaseType().findMethod(signature);

		return null;
	}

	@Override
	public IInitializer findInitializer(String name) {
		IInitializer initializer = getInitializer(name);

		if(initializer != null)
			return initializer;

		if(getBaseType() != null)
			return getBaseType().findInitializer(name);

		return null;
	}

	@Override
	public IMember[] getAutoArrays() {
		IType type = this;

		List<IMember> result = new ArrayList<IMember>();

		while(type != null) {
			IMember[] members = type.getMembers();

			for(IMember member : members) {
				if(member.getVariableType().isAuto())
					result.add(member);
			}

			type = type.getBaseType();
		}

		return result.toArray(new IMember[result.size()]);
	}

	@Override
	public IMethod[] getMatchingMethods(String name) {
		return getMatchingMethods(name, -1);
	}

	@Override
	public IMethod[] getMatchingMethods(String name, int parametersCount) {
		IType type = this;

		Set<IMethod> result = new Set<IMethod>();

		while(type != null) {
			IMethod[] methods = type.getMethods();

			for(IMethod method : methods) {
				if(!method.getName().equals(name) || parametersCount != -1 && parametersCount != method.getParametersCount() || result.get(method) != null)
					continue;

				result.add(method);
			}

			type = type.getBaseType();
		}

		return result.toArray(new IMethod[result.size()]);
	}

	@Override
	public boolean isSubtypeOf(IType candidate) {
		if(candidate != null && candidate.equals(baseType))
			return true;

		return baseType != null ? baseType.isSubtypeOf(candidate) : false;
	}

	@Override
	public boolean hasPrivateAccess(IType from) {
		return from == this || isContainerOf(from);
	}

	@Override
	public boolean hasProtectedAccess(IType from) {
		if(from == null)
			return false;

		if(from.isSubtypeOf(this))
			return true;

		IType container = from.getContainerType();

		while(container != null) {
			if(container == this || container.isSubtypeOf(this))
				return true;

			container = container.getContainerType();
		}

		return false;
	}

	@Override
	public boolean isContainerOf(IType candidate) {
		if(candidate == null)
			return false;

		IType container = candidate.getContainerType();

		while(container != null) {
			if(container.equals(this))
				return true;

			container = container.getContainerType();
		}

		return false;
	}

	@Override
	public ITypeCast getCastTo(IVariableType variableType) {
		if(!variableType.isArray()) {
			ITypeCast typeCast = TypeCast.getCastToBaseType(getCompilationUnit(), this, variableType.getType());

			if(typeCast != null)
				return typeCast;
		}

		Set<ITypeCast> candidates = new Set<ITypeCast>();

		IType type = this;

		while(type != null) {
			IMethod[] operators = type.getTypeCastOperators();

			for(IMethod operator : operators) {
				IVariableType candidate = operator.getVariableType();

				ITypeCast typeCast = candidate.getCastTo(type, variableType);

				if(typeCast != null) {
					typeCast.setOperator(operator);
					candidates.add(typeCast);
				}
			}

			type = type.getBaseType();
		}

		ITypeCast[] result = TypeCast.findBestCast(candidates.toArray(new ITypeCast[candidates.size()]));

		return result.length == 0 ? null : result[0];
	}

	protected boolean typesResolved() {
		return typesResolved;
	}

	protected void setTypesResolved(boolean resolved) {
		typesResolved = resolved;
	}

	protected boolean structureResolved() {
		return structureResolved;
	}

	protected void setStructureResolved(boolean resolved) {
		structureResolved = resolved;
	}

	protected boolean semanticsChecked() {
		return semanticsChecked;
	}

	protected void setSemanticsChecked(boolean checked) {
		semanticsChecked = checked;
	}

	protected boolean nestedTypesResolved() {
		return nestedTypesResolved;
	}

	protected void setNestedTypesResolved(boolean resolved) {
		nestedTypesResolved = resolved;
	}

	@Override
	public boolean resolveType(CompilationUnit compilationUnit) {
		setCompilationUnit(compilationUnit);
		return true;
	}

	@Override
	public boolean resolveTypes(CompilationUnit compilationUnit, IType declaringType) {
		if(!super.resolveTypes(compilationUnit, declaringType))
			return false;

		if(importBlock != null)
			return importBlock.resolveTypes(compilationUnit, declaringType);

		return true;
	}

	@Override
	public boolean checkImportUsage(CompilationUnit compilationUnit) {
		if(importBlock != null)
			return importBlock.checkImportUsage(compilationUnit);

		return true;
	}

	@Override
	public void getCode(CodeGenerator codeGenerator) {
		CompilationUnit compilationUnit = getCompilationUnit();

		String packageName = compilationUnit.getPackage();

		if(packageName.length() != 0) {
			codeGenerator.append("package " + packageName + ';');
			codeGenerator.breakLine();
			codeGenerator.breakLine();
		}
	}

	@Override
	public IAttribute findAttribute(String name) {
		IAttribute attribute = super.getAttribute(name);

		if(attribute != null)
			return attribute;

		IType baseType = getBaseType();

		return baseType != null ? baseType.getAttribute(name) : null;
	}

	void getSetAttributesCode(IType type, CodeGenerator codeGenerator) {
		for(IAttribute attribute : type.getAttributes()) {
			codeGenerator.indent();
			codeGenerator.append("setAttribute(" + '"' + attribute.getName() + '"' + ", ");
			attribute.getCode(codeGenerator);
			codeGenerator.append(");");
			codeGenerator.breakLine();
		}
	}

	@Override
	public MemberInit getMemberInit(String left, IType context) {
		IType curr = context;
		while(true) {
			if(curr.getTypeBody().getMembers() != null)
				for(ILanguageElement elem : curr.getTypeBody().getMembers()) {
					if(elem instanceof MemberInit) {
						MemberInit init = (MemberInit)elem;
						if(init.getQualifiedName().toString().equals(left))
							return init;
					}
				}
			if(curr == this)
				break;
			curr = curr.getBaseType();
			if(curr == null)
				break;
		}
		return null;
	}

	@Override
	public TypeBody getTypeBody() {
		return null;
	}

	@Override
	public IInitializer[] findInitializers(String name) {
		IType curr = this;
		List<IInitializer> inits = new ArrayList<IInitializer>();
		while(curr != null) {
			IInitializer init = curr.findInitializer(name);
			if(init != null)
				inits.add(0, init);
			curr = curr.getBaseType();
		}
		return inits.toArray(new IInitializer[inits.size()]);
	}

	@Override
	public IInitializer findInitializerDeep(String name) {
		IInitializer[] inits = findInitializers(name);
		if(inits.length > 0)
			return inits[inits.length - 1];
		else
			return null;
	}

	@Override
	public boolean isSubtypeOf(String typeName) {
		if(typeName.equals(userName))
			return true;

		return baseType != null ? baseType.isSubtypeOf(typeName) : false;

	}

	@Override
	public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
		if(importBlock != null)
			importBlock.replaceTypeName(parent, type, newTypeName);
	}

	@Override
	public void replaceImport(TextEdit parent, IPath oldImport, IPath newImport) {
		if(importBlock != null) {
			importBlock.replaceImport(parent, oldImport, newImport);
		}
	}

	protected void generateClassCode(CodeGenerator codeGenerator) {
		String base = baseType == null ? BuiltinNative.OBJECT : baseType.getNestedJavaName();
		String type = getNestedJavaName();

		codeGenerator.indent();
		codeGenerator.append("public static class CLASS<T extends " + type + "> extends " + base + ".CLASS<T>");
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("{");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		codeGenerator.indent();
		codeGenerator.append("public CLASS" + "(" + BuiltinNative.IObject + " container)");
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("{");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		codeGenerator.indent();
		codeGenerator.append("super(container);");
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("setJavaClass(" + type + ".class);");
		codeGenerator.breakLine();

		getSetAttributesCode(this, codeGenerator);

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("}");
		codeGenerator.breakLine();

		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("public Object" + " newObject(" + BuiltinNative.IObject + " container)");
		codeGenerator.breakLine();
		codeGenerator.indent();
		codeGenerator.append("{");
		codeGenerator.breakLine();

		codeGenerator.incrementIndent();

		codeGenerator.indent();
		codeGenerator.append("return new " + type + "(container);");
		codeGenerator.breakLine();

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("}");
		codeGenerator.breakLine();

		codeGenerator.decrementIndent();

		codeGenerator.indent();
		codeGenerator.append("}");
		codeGenerator.breakLine();
	}
}
