package org.zenframework.z8.compiler.parser.grammar;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.file.FileException;
import org.zenframework.z8.compiler.parser.Attribute;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.parser.expressions.ArrayAccess;
import org.zenframework.z8.compiler.parser.expressions.ArrayInitializer;
import org.zenframework.z8.compiler.parser.expressions.Assignment;
import org.zenframework.z8.compiler.parser.expressions.BinaryExpression;
import org.zenframework.z8.compiler.parser.expressions.BracedExpression;
import org.zenframework.z8.compiler.parser.expressions.Constant;
import org.zenframework.z8.compiler.parser.expressions.Container;
import org.zenframework.z8.compiler.parser.expressions.ElvisExpression;
import org.zenframework.z8.compiler.parser.expressions.InstanceOf;
import org.zenframework.z8.compiler.parser.expressions.MapElement;
import org.zenframework.z8.compiler.parser.expressions.MethodCall;
import org.zenframework.z8.compiler.parser.expressions.Null;
import org.zenframework.z8.compiler.parser.expressions.OperatorNew;
import org.zenframework.z8.compiler.parser.expressions.Postfix;
import org.zenframework.z8.compiler.parser.expressions.QualifiedName;
import org.zenframework.z8.compiler.parser.expressions.Super;
import org.zenframework.z8.compiler.parser.expressions.TernaryExpression;
import org.zenframework.z8.compiler.parser.expressions.This;
import org.zenframework.z8.compiler.parser.expressions.TypeCastExpression;
import org.zenframework.z8.compiler.parser.expressions.UnaryExpression;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.ConstantToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.DecimalToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.IntegerToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.KeywordToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.OperatorToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.TokenException;
import org.zenframework.z8.compiler.parser.statements.CatchClause;
import org.zenframework.z8.compiler.parser.statements.CompoundStatement;
import org.zenframework.z8.compiler.parser.statements.Declarator;
import org.zenframework.z8.compiler.parser.statements.DoWhileStatement;
import org.zenframework.z8.compiler.parser.statements.ForStatement;
import org.zenframework.z8.compiler.parser.statements.IfStatement;
import org.zenframework.z8.compiler.parser.statements.JumpStatement;
import org.zenframework.z8.compiler.parser.statements.Statement;
import org.zenframework.z8.compiler.parser.statements.ThrowStatement;
import org.zenframework.z8.compiler.parser.statements.TryCatchStatement;
import org.zenframework.z8.compiler.parser.statements.WhileStatement;
import org.zenframework.z8.compiler.parser.type.DeclaratorNestedType;
import org.zenframework.z8.compiler.parser.type.Enum;
import org.zenframework.z8.compiler.parser.type.ImportBlock;
import org.zenframework.z8.compiler.parser.type.ImportElement;
import org.zenframework.z8.compiler.parser.type.MemberNestedType;
import org.zenframework.z8.compiler.parser.type.Type;
import org.zenframework.z8.compiler.parser.type.members.AbstractMethod;
import org.zenframework.z8.compiler.parser.type.members.CastOperator;
import org.zenframework.z8.compiler.parser.type.members.Member;
import org.zenframework.z8.compiler.parser.type.members.MemberInit;
import org.zenframework.z8.compiler.parser.type.members.Method;
import org.zenframework.z8.compiler.parser.type.members.Operator;
import org.zenframework.z8.compiler.parser.type.members.PriorityOperator;
import org.zenframework.z8.compiler.parser.type.members.Record;
import org.zenframework.z8.compiler.parser.type.members.Records;
import org.zenframework.z8.compiler.parser.type.members.TypeBody;
import org.zenframework.z8.compiler.parser.variable.Variable;
import org.zenframework.z8.compiler.parser.variable.VariableType;
import org.zenframework.z8.compiler.util.Set;
import org.zenframework.z8.compiler.workspace.CompilationUnit;

public class Parser {
	protected CompilationUnit compilationUnit;

	protected ImportBlock importBlock;
	protected IType type;

	protected Lexer lexer;
	protected Grammar grammar;
	protected Stack<Object> stack;

	public Parser(CompilationUnit compilationUnit) {
		this.stack = new Stack<Object>();
		this.compilationUnit = compilationUnit;
	}

	public Lexer getLexer() {
		return lexer;
	}

	@Override
	public int hashCode() {
		return lexer != null ? lexer.hashCode() : 0;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public Object peek() {
		return stack.isEmpty() ? null : stack.peek();
	}

	public Object pop() {
		return stack.pop();
	}

	public Object push(Object object) {
		return stack.push(object);
	}

	public IType getType() {
		return type;
	}

	public ImportBlock getImport() {
		return importBlock;
	}

	public boolean parse() {
		return parse(null);
	}

	public boolean parse(char[] content) {
		try {
			grammar = new Grammar();
			lexer = new Lexer(getCompilationUnit(), content);
			return grammar.parse(this) == 0;
		} catch(TokenException e) {
			getCompilationUnit().parseError(e.getPosition(), e.getMessage());
			return false;
		} catch(FileException e) {
			getCompilationUnit().parseError(e);
			return false;
		} catch(UnsupportedEncodingException e) {
			getCompilationUnit().parseError(e);
			return false;
		} finally {
			List<IType> types = new ArrayList<IType>();

			while(stack.size() != 0) {
				Object element = pop();

				if(element instanceof IType)
					types.add((IType)element);

				if(element instanceof ImportBlock)
					importBlock = (ImportBlock)element;
			}

			if(types.size() > 0)
				type = types.remove(types.size() - 1);
		}
	}

	@SuppressWarnings("unchecked")
	void onImport() {
		List<ImportElement> list = (List<ImportElement>)pop();
		push(new ImportBlock(list.toArray(new ImportElement[list.size()])));
	}

	@SuppressWarnings("unchecked")
	void onImportList(boolean create) {
		ImportElement importElement = (ImportElement)pop();

		if(create)
			push(new ArrayList<ImportElement>());

		List<ImportElement> list = (List<ImportElement>)peek();
		list.add(importElement);
	}

	void onImportElement(IToken importToken, IToken semicolon) {
		QualifiedName qualifiedName = (QualifiedName)pop();
		push(new ImportElement(importToken, qualifiedName, semicolon));
	}

	void onAttribute(IToken leftBracket, IToken name, IToken rightBracket) {
		ILanguageElement expression = (ILanguageElement)pop();

		if(expression instanceof UnaryExpression) {
			UnaryExpression unary = (UnaryExpression)expression;

			if(unary.getOperatorToken().getId() == IToken.MINUS && unary.getExpression() instanceof Constant) {
				ConstantToken constantToken = ((Constant)unary.getExpression()).getToken();

				if(constantToken instanceof IntegerToken) {
					long value = ((IntegerToken)constantToken).getValue();
					IntegerToken integerToken = new IntegerToken(-value, unary.getOperatorToken().getPosition().union(constantToken.getPosition()));
					push(new Attribute(leftBracket, name, integerToken, rightBracket));
					return;
				}

				if(constantToken instanceof DecimalToken) {
					BigDecimal value = ((DecimalToken)constantToken).getValue();
					DecimalToken integerToken = new DecimalToken(value.negate(), unary.getOperatorToken().getPosition().union(constantToken.getPosition()));
					push(new Attribute(leftBracket, name, integerToken, rightBracket));
					return;
				}
			}
		}

		push(new Attribute(leftBracket, name, expression, rightBracket));
	}

	void onAttribute(IToken leftBracket, IToken name, IToken value, IToken rightBracket) {
		push(new Attribute(leftBracket, name, (ConstantToken)value, rightBracket));
	}

	void onToken(IToken token) {
		push(token);
	}

	class Modifiers {
		IToken autoToken;
		IToken staticToken;
		IToken accessToken;
		IToken virtualToken;
		Set<IAttribute> attributes;

		public Modifiers() {
		}

		public IToken getFirstToken() {
			IAttribute[] attributes = new IAttribute[0];

			if(this.attributes != null)
				attributes = this.attributes.toArray(new IAttribute[0]);

			IToken attribute = LanguageElement.getFirstToken(attributes);

			return LanguageElement.getFirstToken(attribute, LanguageElement.getFirstToken(virtualToken, LanguageElement.getFirstToken(accessToken, LanguageElement.getFirstToken(autoToken, staticToken))));
		}

		void setToken(IToken token) {
			switch(token.getId()) {
			case IToken.STATIC: {
				if(staticToken == null) {
					staticToken = token;
					return;
				}
				break;
			}
			case IToken.AUTO: {
				if(autoToken == null) {
					autoToken = token;
					return;
				}
				break;
			}
			case IToken.VIRTUAL: {
				if(virtualToken == null) {
					virtualToken = token;
					return;
				}
				break;
			}
			case IToken.PUBLIC:
			case IToken.PROTECTED:
			case IToken.PRIVATE: {
				if(accessToken == null) {
					accessToken = token;
					return;
				}
				break;
			}
			}

			grammar.error(token);
		}

		void addAttribute(IAttribute attribute) {
			if(attributes == null) {
				attributes = new Set<IAttribute>(attribute);
				return;
			}

			if(checkAttribute(attribute))
				attributes.add(attribute);
		}

		boolean checkAttribute(IAttribute attribute) {
			String name = attribute.getName();

			if(attributes.get(name) != null) {
				getCompilationUnit().error(attribute.getNameToken().getPosition(), "The attribute " + name + " is declared more then once");
				return false;
			}

			return true;
		}
	}

	void onModifiers(boolean create) {
		Object modifier = pop();

		if(create)
			push(new Modifiers());

		Modifiers modifiers = (Modifiers)peek();

		if(modifier instanceof IAttribute) {
			IAttribute attribute = (IAttribute)modifier;
			modifiers.addAttribute(attribute);
		} else if(modifier instanceof IToken) {
			IToken token = (IToken)modifier;
			modifiers.setToken(token);
		}
	}

	void onQualifiedName(boolean create, IToken token) {
		if(!create) {
			QualifiedName name = (QualifiedName)peek();
			name.add(token);
		} else
			push(new QualifiedName(token));
	}

	void applyModifiers(Modifiers modifiers, ILanguageElement element) {
		if(modifiers.attributes != null)
			element.setAttributes(modifiers.attributes.toArray(new IAttribute[modifiers.attributes.size()]));

		if(element instanceof Member) {
			Member member = (Member)element;
			member.setStaticToken(modifiers.staticToken);
			member.setAccessToken(modifiers.accessToken);
			member.setAutoToken(modifiers.autoToken);

			if(modifiers.virtualToken != null)
				getCompilationUnit().error(modifiers.virtualToken.getPosition(), "Modifier is illegal in this context");
		} else if(element instanceof AbstractMethod) {
			AbstractMethod method = (AbstractMethod)element;
			method.setStatic(modifiers.staticToken);
			method.setAccess(modifiers.accessToken);

			if(modifiers.staticToken != null && modifiers.virtualToken != null)
				getCompilationUnit().error(modifiers.virtualToken.getPosition(), "Modifier is illegal in this context");
			else
				method.setVirtual(modifiers.virtualToken);

			if(modifiers.autoToken != null)
				getCompilationUnit().error(modifiers.autoToken.getPosition(), "Modifier is illegal in this context");
		} else if(element instanceof MemberNestedType) {
			if(modifiers.accessToken != null)
				getCompilationUnit().error(modifiers.accessToken.getPosition(), "Modifier is illegal in this context");

			if(modifiers.staticToken != null)
				getCompilationUnit().error(modifiers.staticToken.getPosition(), "Modifier is illegal in this context");

			if(modifiers.autoToken != null)
				getCompilationUnit().error(modifiers.autoToken.getPosition(), "Modifier is illegal in this context");

			if(modifiers.virtualToken != null)
				getCompilationUnit().error(modifiers.virtualToken.getPosition(), "Modifier is illegal in this context");
		} else if(element instanceof Record) {
			Record record = (Record)element;
			record.setAccess(modifiers.accessToken);

			if(modifiers.autoToken != null)
				getCompilationUnit().error(modifiers.autoToken.getPosition(), "Modifier is illegal in this context");

			if(modifiers.staticToken != null)
				getCompilationUnit().error(modifiers.staticToken.getPosition(), "Modifier is illegal in this context");

			if(modifiers.virtualToken != null)
				getCompilationUnit().error(modifiers.virtualToken.getPosition(), "Modifier is illegal in this context");
		} else if(element instanceof MemberInit) {
			if(modifiers.accessToken != null)
				getCompilationUnit().error(modifiers.accessToken.getPosition(), "Modifier is illegal in this context");

			if(modifiers.staticToken != null)
				getCompilationUnit().error(modifiers.staticToken.getPosition(), "Modifier is illegal in this context");

			if(modifiers.autoToken != null)
				getCompilationUnit().error(modifiers.autoToken.getPosition(), "Modifier is illegal in this context");

			if(modifiers.virtualToken != null)
				getCompilationUnit().error(modifiers.virtualToken.getPosition(), "Modifier is illegal in this context");
		}
	}

	void applyModifiers() {
		Object object = pop();
		Modifiers modifiers = (Modifiers)pop();

		if(object instanceof VariableDeclarator) {
			VariableDeclarator varaibleDeclarator = (VariableDeclarator)object;
			varaibleDeclarator.setModifiers(modifiers);
		} else if(object instanceof ILanguageElement)
			applyModifiers(modifiers, (ILanguageElement)object);

		push(object);
	}

	void onMissingSemicolon() {
		Object statement = pop();

		IToken token = (statement instanceof VariableDeclarator) ? ((VariableDeclarator)statement).getFirstToken() : ((ILanguageElement)statement).getFirstToken();
		getCompilationUnit().error(token.getPosition(), "Syntax error, ; is missing before this token");

		push(statement);
	}

	void addClassMember() {
		List<Object> objects = new ArrayList<Object>();

		while(!(peek() instanceof TypeBody))
			objects.add(0, pop());


		TypeBody typeBody = (TypeBody)peek();

		for(Object object : objects) {
			if(object instanceof VariableDeclarator) {
				VariableDeclarator variableDeclarator = (VariableDeclarator)object;
				ILanguageElement[] elements = variableDeclarator.getMembers();

				for(ILanguageElement element : elements)
					typeBody.addMember(element);
			} else if(object instanceof ILanguageElement)
				typeBody.addMember((ILanguageElement)object);
		}
	}

	void startEnum(IToken enumToken) {
		push(new Enum(enumToken));
	}

	void setEnumName(IToken name) {
		((Enum)peek()).setNameToken(name);
	}

	void startEnumBody(IToken leftBrace) {
		((Enum)peek()).setLeftBrace(leftBrace);
	}

	void addEnumMember(IToken name) {
		((Enum)peek()).addElement(name);
	}

	void finishEnumBody(IToken rightBrace) {
		((Enum)peek()).setRightBrace(rightBrace);
	}

	void startClass(IToken finalToken, IToken classToken) {
		push(new Type(finalToken, classToken));
	}

	void setClassName(IToken token) {
		Type cls = (Type)peek();
		cls.setTypeNameToken(token);
	}

	void setClassBase() {
		QualifiedName typeName = (QualifiedName)pop();
		Type cls = (Type)peek();
		cls.setBaseTypeName(typeName);
	}

	void finishClass() {
		TypeBody body = (TypeBody)pop();
		Type cls = (Type)peek();
		cls.setBody(body);
	}

	void startClassBody(IToken leftBrace) {
		push(new TypeBody(leftBrace));
	}

	void finishClassBody(IToken rightBrace) {
		TypeBody body = (TypeBody)peek();
		body.setRightBrace(rightBrace);
	}

	class RecordsBody extends ArrayList<Record> {
		private static final long serialVersionUID = 4576452998078559274L;

		IToken leftBrace;
		IToken rightBrace;

		public RecordsBody(IToken leftBrace) {
			this.leftBrace = leftBrace;
		}

		public RecordsBody(Record element) {
			add(element);
		}
	}

	void startRecords(IToken recordsToken) {
		push(new Records(recordsToken));
	}

	void startRecordsBody(IToken leftBrace) {
		push(new RecordsBody(leftBrace));
	}

	void startRecord(IToken name) {
		push(new Record(name));
	}

	void setRecordValue(IToken value) {
		Record record = (Record)peek();
		record.setValue((ConstantToken)value);
	}

	void addRecord() {
		Record record = (Record)pop();
		RecordsBody body = (RecordsBody)peek();
		body.add(record);
	}

	void finishRecordsBody(IToken rightBrace) {
		RecordsBody body = (RecordsBody)peek();
		body.rightBrace = rightBrace;
	}

	void finishRecords() {
		RecordsBody body = (RecordsBody)pop();
		Records records = (Records)peek();
		records.setElements(body.leftBrace, body.toArray(new Record[body.size()]), body.rightBrace);
	}

	class Index {
		protected IToken leftBracket;
		protected IToken rightBracket;
		protected ILanguageElement expression;

		public Index(IToken leftBracket, ILanguageElement expression, IToken rightBracket) {
			this.leftBracket = leftBracket;
			this.expression = expression;
			this.rightBracket = rightBracket;
		}

		public IPosition getSourceRange() {
			if(rightBracket != null)
				return leftBracket.getPosition().union(rightBracket.getPosition());
			else if(expression != null)
				return leftBracket.getPosition().union(expression.getPosition());

			return leftBracket.getPosition();
		}

		public ILanguageElement getExpression() {
			return expression;
		}
	}

	class Indices extends ArrayList<Index> {
		private static final long serialVersionUID = 3642851993356953272L;

		public IPosition getSourceRange() {
			return get(0).getSourceRange().union(get(size() - 1).getSourceRange());
		}
	}

	void onIndex(IToken leftBracket, IToken rightBracket, boolean hasExpression) {
		ILanguageElement expression = hasExpression ? (ILanguageElement)pop() : null;
		push(new Index(leftBracket, expression, rightBracket));
	}

	void onIndices(boolean create) {
		Index index = (Index)pop();

		if(create)
			push(new Indices());

		Indices indices = (Indices)peek();
		indices.add(index);
	}

	boolean isQualifiedName(ILanguageElement element) {
		if(element instanceof QualifiedName)
			return true;

		if(element instanceof Postfix) {
			Postfix postfix = (Postfix)element;
			return postfix.getPrefix() instanceof QualifiedName && postfix.getPostfix() == null;
		}

		return false;
	}

	QualifiedName retrieveQualifiedName(ILanguageElement element) {
		if(element instanceof QualifiedName) {
			return (QualifiedName)element;
		}

		if(element instanceof Postfix) {
			Postfix postfix = (Postfix)element;
			ILanguageElement prefix = postfix.getPrefix();

			if(prefix instanceof QualifiedName && postfix.getPostfix() == null)
				return (QualifiedName)prefix;
		}

		return null;
	}

	class VariableTypeHelper {
		protected QualifiedName qualifiedName;
		protected Indices indices;

		public VariableTypeHelper(QualifiedName qualifiedName, Indices indices) {
			this.qualifiedName = qualifiedName;
			this.indices = indices;
		}

		public VariableType getVariableType() {
			VariableType variableType = new VariableType(qualifiedName);

			if(indices != null) {
				for(Index index : indices) {
					if(index.expression == null) {
						variableType.addKey(null, index.getSourceRange());
						continue;
					}

					QualifiedName indexType = retrieveQualifiedName(index.expression);

					if(indexType == null || indexType.getTokens().length > 1) {
						getCompilationUnit().error(index.expression.getPosition(), "Syntax error, identifier expected");
						variableType.addKey(null, index.getSourceRange());
					} else
						variableType.addKey(indexType.getTokens()[0], index.getSourceRange());
				}
			}

			return variableType;
		}

		public QualifiedName getQualifiedName() {
			return qualifiedName;
		}

		public Indices getIndices() {
			return indices;
		}
	}

	void onVariableType(boolean hasIndices) {
		Indices indices = hasIndices ? (Indices)pop() : null;
		QualifiedName qualifiedName = (QualifiedName)pop();
		push(new VariableTypeHelper(qualifiedName, indices));
	}

	class VariableDeclarator {
		private IToken finalToken;
		private Modifiers modifiers;
		private VariableType variableType;
		private QualifiedName qualifiedName;
		private OperatorToken operatorToken;
		private ILanguageElement initializer;

		public VariableDeclarator(IToken finalToken, VariableType variableType, QualifiedName qualifiedName) {
			this.finalToken = finalToken;
			this.variableType = variableType;
			this.qualifiedName = qualifiedName;
		}

		public IToken getFirstToken() {
			IToken token = modifiers != null ? modifiers.getFirstToken() : null;

			token = LanguageElement.getFirstToken(token, finalToken);

			if(variableType != null)
				token = LanguageElement.getFirstToken(token, variableType.getFirstToken());
			else
				token = LanguageElement.getFirstToken(token, qualifiedName.getFirstToken());

			return token;

		}

		public void setInitializer(OperatorToken operatorToken, ILanguageElement initializer) {
			this.operatorToken = operatorToken;
			this.initializer = initializer;
		}

		public VariableType getVariableType() {
			return variableType;
		}

		public IToken getFinalToken() {
			return finalToken;
		}

		public IToken getIdentifier() {
			if(qualifiedName.getTokenCount() != 1)
				getCompilationUnit().error(qualifiedName.getPosition(), "Syntax error, identifier expected");

			return qualifiedName.getTokens()[0];
		}

		public Modifiers getModifiers() {
			return modifiers;
		}

		public void setModifiers(Modifiers modifiers) {
			this.modifiers = modifiers;
		}

		public ILanguageElement[] getMembers() {
			if(variableType != null) {
				IToken nameToken = getIdentifier();

				if(initializer != null) {
					if(initializer instanceof Type) {
						Type type = (Type)initializer;
						ILanguageElement nestedType = new MemberNestedType(new QualifiedName(nameToken), type.getClassToken(), type.getBody());
						ILanguageElement member = new Member(variableType, nameToken, nestedType);

						if(modifiers != null)
							applyModifiers(modifiers, member);

						return new ILanguageElement[] { member };
					}

					ILanguageElement memberInitializer = new MemberInit(new QualifiedName(nameToken), operatorToken, initializer);
					ILanguageElement member = new Member(variableType, nameToken, memberInitializer);

					if(modifiers != null)
						applyModifiers(modifiers, member);

					return new ILanguageElement[] { member };
				}

				ILanguageElement member = new Member(variableType, nameToken);

				if(modifiers != null)
					applyModifiers(modifiers, member);

				return new ILanguageElement[] { member };
			}

			if(initializer instanceof Type) {
				Type type = (Type)initializer;
				ILanguageElement member = new MemberNestedType(qualifiedName, type.getClassToken(), type.getBody());

				if(modifiers != null)
					applyModifiers(modifiers, member);

				return new ILanguageElement[] { member };
			}

			ILanguageElement memberInit = new MemberInit(qualifiedName, operatorToken, initializer);

			if(modifiers != null)
				applyModifiers(modifiers, memberInit);

			return new ILanguageElement[] { memberInit };
		}

		public ILanguageElement getDeclarator() {
			IToken nameToken = getIdentifier();

			if(initializer instanceof Type) {
				Type type = (Type)initializer;
				return new DeclaratorNestedType(finalToken, variableType, nameToken, type.getBody());
			}

			return new Declarator(finalToken, variableType, nameToken, operatorToken, initializer);
		}
	}

	void onVariableDeclarator(IToken nameToken) {
		onVariableDeclarator(null, nameToken);
	}

	void onVariableDeclarator1() {
		VariableDeclarator variableTypeHelper = (VariableDeclarator)pop();
		pop();
		push(variableTypeHelper);
	}

	void onVariableDeclarator(IToken finalToken, IToken nameToken) {
		VariableTypeHelper variableTypeHelper = (VariableTypeHelper)pop();
		VariableType variableType = variableTypeHelper.getVariableType();
		push(new VariableDeclarator(finalToken, variableType, new QualifiedName(nameToken)));
	}

	void onVariableDeclaratorInit(IToken operatorToken) {
		ILanguageElement initializer = (ILanguageElement)pop();
		VariableDeclarator variableDeclarator = (VariableDeclarator)peek();
		variableDeclarator.setInitializer((OperatorToken)operatorToken, initializer);
	}

	void onVariableInit(IToken operatorToken) {
		ILanguageElement initializer = operatorToken != null ? (ILanguageElement)pop() : null;
		QualifiedName qualifiedName = (QualifiedName)pop();

		VariableDeclarator variableDeclarator = new VariableDeclarator(null, null, qualifiedName);
		variableDeclarator.setInitializer((OperatorToken)operatorToken, initializer);

		push(variableDeclarator);
	}

	void createMethod(IToken nameToken) {
		Parameters parameters = (Parameters)pop();
		VariableTypeHelper variableTypeHelper = (VariableTypeHelper)pop();
		VariableType variableType = variableTypeHelper.getVariableType();
		push(new Method(variableType, nameToken, parameters.toArray(new Variable[parameters.size()]), parameters.leftBrace, parameters.rightBrace));
	}

	void createPriorityOperator(IToken operatorKeyword) {
		Parameters parameters = (Parameters)pop();
		push(new PriorityOperator((KeywordToken)operatorKeyword, parameters.toArray(new Variable[parameters.size()]), parameters.leftBrace, parameters.rightBrace));
	}

	void createCastOperator(IToken operatorKeyword, IToken typeNameToken) {
		Parameters parameters = (Parameters)pop();
		push(new CastOperator((KeywordToken)operatorKeyword, new VariableType(new QualifiedName(typeNameToken)), parameters.toArray(new Variable[parameters.size()]), parameters.leftBrace, parameters.rightBrace));
	}

	void createOperator(IToken operatorKeyword, IToken operatorToken) {
		Parameters parameters = (Parameters)pop();
		VariableTypeHelper variableTypeHelper = (VariableTypeHelper)pop();
		VariableType variableType = variableTypeHelper.getVariableType();
		push(new Operator(variableType, (KeywordToken)operatorKeyword, (OperatorToken)operatorToken, parameters.toArray(new Variable[parameters.size()]), parameters.leftBrace, parameters.rightBrace));
	}

	void setMethodBody() {
		CompoundStatement body = (CompoundStatement)pop();

		if(peek() instanceof AbstractMethod) {
			AbstractMethod method = (AbstractMethod)peek();
			method.setBody(body);
		}
	}

	class Parameter {
		IToken finalToken;
		IToken nameToken;
		VariableType variableType;

		public Parameter(IToken finalToken, VariableType variableType, IToken nameToken) {
			this.finalToken = finalToken;
			this.nameToken = nameToken;
			this.variableType = variableType;
		}
	}

	class Parameters extends Set<Variable> {
		IToken leftBrace;
		IToken rightBrace;

		Parameters(IToken leftBrace) {
			this.leftBrace = leftBrace;
		}

		IPosition getPosition() {
			if(rightBrace != null)
				return leftBrace.getPosition().union(rightBrace.getPosition());
			else if(size() > 0)
				return leftBrace.getPosition().union(lastElement().getPosition());

			return leftBrace.getPosition();
		}

		void add(Parameter parameter) {
			if(parameter.nameToken == null)
				return;


			String name = parameter.nameToken.getRawText();

			if(get(name) != null) {
				getCompilationUnit().error(parameter.nameToken.getPosition(), name + ": method parameter redefinition");
				return;
			}

			add(new Variable(parameter.finalToken, parameter.variableType, parameter.nameToken));
		}
	}

	void startParameters(IToken leftBrace) {
		push(new Parameters(leftBrace));
	}

	void createParameter() {
		VariableDeclarator variableDeclarator = (VariableDeclarator)pop();

		VariableType variableType = variableDeclarator.getVariableType();
		IToken nameToken = variableDeclarator.getIdentifier();
		IToken finalToken = variableDeclarator.getFinalToken();

		Parameters parameters = (Parameters)peek();
		parameters.add(new Parameter(finalToken, variableType, nameToken));
	}

	void finishParameters(IToken rightBrace) {
		Parameters parameters = (Parameters)peek();
		parameters.rightBrace = rightBrace;
	}

	void startArrayInitializer(IToken leftBrace) {
		push(new ArrayInitializer(leftBrace));
	}

	void addArrayInitializer() {
		ILanguageElement element = (ILanguageElement)pop();
		ArrayInitializer initializer = (ArrayInitializer)peek();
		initializer.add(element);
	}

	void finishArrayInitializer(IToken rightBrace) {
		ArrayInitializer initializer = (ArrayInitializer)peek();
		initializer.setRightBrace(rightBrace);
	}

	void onBracedExpression(IToken leftBrace, IToken rightBrace) {
		ILanguageElement expression = (ILanguageElement)pop();
		push(new BracedExpression(leftBrace, expression, rightBrace));
	}

	void onMapElement(IToken leftBrace, IToken rightBrace) {
		ILanguageElement value = (ILanguageElement)pop();
		ILanguageElement key = (ILanguageElement)pop();
		push(new MapElement(leftBrace, key, value, rightBrace));
	}

	void startCompound(IToken leftBrace) {
		push(new CompoundStatement(leftBrace));
	}

	void addStatement() {
		List<ILanguageElement> list = new ArrayList<ILanguageElement>();

		list.add((ILanguageElement)pop());

		while(!(peek() instanceof CompoundStatement))
			list.add(0, (ILanguageElement)pop());

		CompoundStatement compound = (CompoundStatement)peek();

		for(ILanguageElement statement : list)
			compound.addStatement(statement);
	}

	void finishCompound(IToken rightBrace) {
		CompoundStatement compound = (CompoundStatement)peek();
		compound.setRightBrace(rightBrace);
	}

	void onStatement() {
		ILanguageElement expression = (ILanguageElement)pop();
		push(new Statement(expression));
	}

	void onStatement1() {
		ILanguageElement statement = (ILanguageElement)pop();
		ILanguageElement expression = (ILanguageElement)pop();

		IToken token = ((ILanguageElement)statement).getFirstToken();
		getCompilationUnit().error(token.getPosition(), "Syntax error, ; is missing before this token");

		push(new Statement(expression));
		push(statement);
	}

	void onStatement2() {
		ILanguageElement expression1 = (ILanguageElement)pop();
		ILanguageElement expression2 = (ILanguageElement)pop();

		IToken token = ((ILanguageElement)expression1).getFirstToken();
		getCompilationUnit().error(token.getPosition(), "Syntax error, ; is missing before this token");

		push(new Statement(expression2));
		push(new Statement(expression1));
	}

	void onThrowStatement(IToken throwToken) {
		ILanguageElement expression = (ILanguageElement)pop();
		push(new ThrowStatement(throwToken, expression));
	}

	void onTryStatement(IToken tryToken) {
		CompoundStatement compound = (CompoundStatement)pop();
		push(new TryCatchStatement(tryToken, compound));
	}

	void onCatchClause(IToken catchToken) {
		CompoundStatement compound = (CompoundStatement)pop();
		VariableDeclarator variableDeclarator = (VariableDeclarator)pop();
		push(new CatchClause(catchToken, (Declarator)variableDeclarator.getDeclarator(), compound));
	}

	void onFinallyStatement(IToken finallyToken) {
		CompoundStatement compound = (CompoundStatement)pop();
		TryCatchStatement tryCatch = (TryCatchStatement)peek();
		tryCatch.setFinallyStatement(finallyToken, compound);
	}

	void addCatchStatement() {
		CatchClause catchClause = (CatchClause)pop();
		TryCatchStatement tryCatch = (TryCatchStatement)peek();
		tryCatch.addCatchClause(catchClause);
	}

	void onIfStatement(IToken ifToken, IToken elseToken) {
		ILanguageElement elseStatement = elseToken != null ? (ILanguageElement)pop() : null;
		ILanguageElement ifStatement = (ILanguageElement)pop();
		ILanguageElement expression = (ILanguageElement)pop();
		push(new IfStatement(ifToken, expression, ifStatement, elseToken, elseStatement));
	}

	void onWhileStatement(IToken whileToken) {
		ILanguageElement statement = (ILanguageElement)pop();
		BracedExpression expression = (BracedExpression)pop();
		push(new WhileStatement(whileToken, expression, statement));
	}

	void onDoWhileStatement(IToken doToken, IToken whileToken) {
		BracedExpression expression = (BracedExpression)pop();
		ILanguageElement statement = (ILanguageElement)pop();
		push(new DoWhileStatement(doToken, statement, whileToken, expression));
	}

	void onForStatement(IToken forToken, boolean hasInit) {
		ILanguageElement statement = (ILanguageElement)pop();
		ILanguageElement expression = (ILanguageElement)pop();
		ILanguageElement condition = (ILanguageElement)pop();
		ILanguageElement init = hasInit ? (ILanguageElement)pop() : null;
		push(new ForStatement(forToken, init, condition, expression, statement));
	}

	void onJumpStatement(IToken jumpToken, boolean hasExpression) {
		ILanguageElement expression = hasExpression ? (ILanguageElement)pop() : null;
		push(new JumpStatement(jumpToken, expression));
	}

	void onAssignment(IToken operator) {
		ILanguageElement right = (ILanguageElement)pop();
		ILanguageElement left = (ILanguageElement)pop();
		push(new Assignment(left, (OperatorToken)operator, right));
	}

	void onOperator(IToken sign) {
		ILanguageElement right = (ILanguageElement)pop();
		ILanguageElement left = (ILanguageElement)pop();
		push(new BinaryExpression(left, (OperatorToken)sign, right));
	}

	void onOperatorAssign(IToken sign) {
		ILanguageElement right = (ILanguageElement)pop();
		ILanguageElement left = (ILanguageElement)peek();
		push(new BinaryExpression(left, (OperatorToken)sign, right));
	}

	void onInstanceOf(IToken token) {
		VariableTypeHelper variableTypeHelper = (VariableTypeHelper)pop();
		VariableType variableType = variableTypeHelper.getVariableType();
		ILanguageElement left = (ILanguageElement)pop();
		push(new InstanceOf(left, token, variableType));
	}

	void onTernaryOperator(IToken question, IToken colon) {
		ILanguageElement right = (ILanguageElement)pop();
		ILanguageElement left = (ILanguageElement)pop();
		ILanguageElement condition = (ILanguageElement)pop();
		push(new TernaryExpression(condition, left, right));
	}

	void onElvisOperator(IToken elvis) {
		ILanguageElement right = (ILanguageElement)pop();
		ILanguageElement left = (ILanguageElement)pop();
		push(new ElvisExpression(left, right));
	}

	void onEmptyUnary(IToken operatorToken) {
		push(new UnaryExpression((OperatorToken)operatorToken, null));
	}

	void onUnary(IToken operatorToken) {
		ILanguageElement expression = (ILanguageElement)pop();
		push(new UnaryExpression((OperatorToken)operatorToken, expression));
	}

	void onTypeCast() {
		ILanguageElement postfix = (ILanguageElement)pop();

		BracedExpression bracedExpression = (BracedExpression)pop();

		TypeCastExpression typeCastExpression = bracedExpression.toTypeCastExpression();

		if(typeCastExpression == null) {
			getCompilationUnit().error(bracedExpression.getPosition(), "Syntax error, identifier expected");
			push(postfix);
		} else {
			typeCastExpression.setExpression(postfix);
			push(typeCastExpression);
		}
	}

	void onArrayTypeCast() {
		ArrayInitializer initializer = (ArrayInitializer)pop();
		VariableTypeHelper helper = (VariableTypeHelper)pop();

		VariableType variableType = helper.getVariableType();
		push(new TypeCastExpression(variableType, initializer));
	}

	void onNewExpression(IToken newToken, IToken typeNameToken) {
		push(new OperatorNew(newToken, new QualifiedName(typeNameToken)));
	}

	void onConstant(IToken token) {
		push(new Constant((ConstantToken)token));
	}

	void onNull(IToken token) {
		push(new Null(token));
	}

	void onThis(IToken token) {
		push(new This(token));
	}

	void onSuper(IToken token) {
		push(new Super(token));
	}

	void onContainer(IToken token) {
		push(new Container(token));
	}

	void addContainer(IToken token) {
		Container container = (Container)peek();
		container.add(token);
	}

	void onPostfix() {
		ILanguageElement right = (ILanguageElement)pop();
		ILanguageElement left = (ILanguageElement)pop();
		push(new Postfix(left, right));
	}

	void onTypeToPostfix() {

		VariableTypeHelper variableTypeHelper = (VariableTypeHelper)pop();

		Indices indices = variableTypeHelper.getIndices();

		if(indices != null) {
			push(variableTypeHelper.getQualifiedName());
			push(variableTypeHelper.getIndices());
			onArrayAccess();
		} else
			push(new Postfix(variableTypeHelper.getQualifiedName()));
	}

	void onArrayAccess() {
		Indices indices = (Indices)pop();
		ILanguageElement array = (ILanguageElement)pop();

		for(Index index : indices) {
			ILanguageElement expression = index.getExpression();

			if(expression == null) {
				getCompilationUnit().error(index.getSourceRange(), "Syntax error, expression expected");
				continue;
			}

			array = new ArrayAccess(array, expression, index.leftBracket, index.rightBracket);
		}

		push(array);
	}

	class Expressions extends ArrayList<ILanguageElement> {
		protected IToken leftBrace;
		protected IToken rightBrace;

		private static final long serialVersionUID = -6850019400868054918L;

		public Expressions(IToken leftBrace) {
			this.leftBrace = leftBrace;
		}

		IPosition getPosition() {
			if(rightBrace != null)
				return leftBrace.getPosition().union(rightBrace.getPosition());
			else if(size() > 0)
				return leftBrace.getPosition().union(get(size() - 1).getPosition());

			return leftBrace.getPosition();
		}
	}

	void startExpressions(IToken leftBrace) {
		push(new Expressions(leftBrace));
	}

	void addExpression() {
		ILanguageElement expression = (ILanguageElement)pop();
		Expressions expressions = (Expressions)peek();
		expressions.add(expression);
	}

	void finishExpressions(IToken rightBrace) {
		Expressions expressions = (Expressions)peek();
		expressions.rightBrace = rightBrace;
	}

	void onMethodCall(boolean hasPrefix) {
		Expressions expressions = (Expressions)pop();
		QualifiedName qualifiedName = (QualifiedName)pop();
		ILanguageElement prefix = hasPrefix ? (ILanguageElement)pop() : null;

		IToken methodNameToken = qualifiedName.remove(qualifiedName.getTokenCount() - 1);

		if(qualifiedName.getTokenCount() != 0) {
			if(prefix != null)
				prefix = new Postfix(prefix, qualifiedName);
			else
				prefix = qualifiedName;
		}

		ILanguageElement methodCall = new MethodCall(prefix, methodNameToken, expressions.toArray(new ILanguageElement[expressions.size()]), expressions.leftBrace, expressions.rightBrace);

		if(!hasPrefix)
			methodCall = new Postfix(methodCall);

		push(methodCall);
	}

	void onDeclarator() {
		VariableDeclarator variableDeclarator = (VariableDeclarator)pop();
		push(variableDeclarator.getDeclarator());
	}
}