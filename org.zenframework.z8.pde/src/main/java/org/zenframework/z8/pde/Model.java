package org.zenframework.z8.pde;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IAttributed;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.expressions.Constant;
import org.zenframework.z8.compiler.parser.type.members.EnumElement;
import org.zenframework.z8.compiler.parser.type.members.Record;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.build.ReconcileMessageConsumer;
import org.zenframework.z8.pde.source.IMergeable;
import org.zenframework.z8.pde.source.Init2;
import org.zenframework.z8.pde.source.InitHelper;
import org.zenframework.z8.pde.source.MultipleTransactions;
import org.zenframework.z8.pde.source.ObjectWithProperties;
import org.zenframework.z8.pde.source.Transaction;

public class Model extends ObjectWithProperties implements IPropertySource, IMergeable {

	private Map<Object, Object> cachedProperties = new HashMap<Object, Object>();

	protected interface IDocProv {
		public IDocument getDocument();

		public void release();
	}

	private class SimpleDocProv implements IDocProv {
		private IDocument doc;

		public SimpleDocProv(IDocument d) {
			doc = d;
		}

		@Override
		public IDocument getDocument() {
			return doc;
		}

		@Override
		public void release() {
		}

	}

	private class CUDocProv implements IDocProv {
		private CompilationUnit compilationUnit;

		private IDocumentProvider prov;
		private IDocument doc;
		private FileEditorInput input;

		public CUDocProv(CompilationUnit cu) {
			compilationUnit = cu;
			prov = DocumentProviderRegistry.getDefault().getDocumentProvider(".bl");
			input = new FileEditorInput((IFile)compilationUnit.getResource());
		}

		@Override
		public IDocument getDocument() {
			IDocument d = prov.getDocument(input);

			if(d != null)
				return d;
			try {
				prov.connect(input);
			} catch(Exception e) {
				return null;
			}
			doc = prov.getDocument(input);
			return doc;

		}

		@Override
		public void release() {
			try {
				if(doc == null)
					return;
				prov.saveDocument(null, input, doc, true);
				prov.disconnect(input);
			} catch(Exception e) {
			}
		}
	}

	private final static String NOT_SET_ATTRIBUTE = "<not set>";

	protected final static int PROPERTY_TYPE_BOOLEAN = 0;
	protected final static int PROPERTY_TYPE_ENUM = 1;
	protected final static int PROPERTY_TYPE_INT = 2;
	protected final static int PROPERTY_TYPE_STRING = 3;
	protected final static int PROPERTY_TYPE_OTHER = 4;

	private class AttributedPropertySource implements IPropertySource {
		@Override
		public Object getEditableValue() {
			return "";
		}

		@Override
		public IPropertyDescriptor[] getPropertyDescriptors() {
			return getAttributeProperties().toArray(new IPropertyDescriptor[0]);
		}

		@Override
		public Object getPropertyValue(Object id) {
			// IType t =
			// getHelper().context.getCompilationUnit().getReconciledType();
			if(cachedProperties.containsKey(id)) {
				Object result = cachedProperties.get(id);
				cachedProperties.remove(id);
				return result;
			}
			AttributedProperty att = (AttributedProperty)id;
			IAttribute a = getHelper().findAttribute(att.attribute);
			if(att.isBoolean) {
				if(a == null)
					return 0;
				else
					return 1;
			} else {
				if(a == null)
					return NOT_SET_ATTRIBUTE;
				return a.getValueString();
			}
		}

		@Override
		public boolean isPropertySet(Object id) {
			// return id instanceof AttributedProperty;
			return !getPropertyValue(id).equals(NOT_SET_ATTRIBUTE);
		}

		@Override
		public void resetPropertyValue(Object id) {
			IDocProv dp = getDocProv();
			if(dp == null)
				return;
			// if
			// (getHelper().context!=getHelper().context.getCompilationUnit().getReconciledType())
			// return;
			AttributedProperty att = (AttributedProperty)id;
			// unset boolean
			MultipleTransactions mt = createMultipleTransactions(dp);
			boolean execute = false;
			IAttribute attribute = getHelper().findAttribute(att.attribute);
			if(attribute != null && attribute.getCompilationUnit() == getHelper().context.getCompilationUnit()) {
				IPosition oldValue = attribute.getPosition();
				mt.add(new Transaction(oldValue.getLength(), oldValue.getOffset(), ""));
				execute = true;
			} else {
				// not set
			}
			if(execute) {
				mt.execute();
			}
			dp.release();
		}

		@Override
		public void setPropertyValue(Object id, Object value) {
			IDocProv dp = getDocProv();
			if(dp == null)
				return;
			// if
			// (getHelper().context!=getHelper().context.getCompilationUnit().getReconciledType())
			// return;
			AttributedProperty att = (AttributedProperty)id;
			// unset boolean
			MultipleTransactions mt = createMultipleTransactions(dp);
			boolean execute = false;
			if(att.isBoolean && ((Integer)value).intValue() == 0) {
				IAttribute attribute = getHelper().findAttribute(att.attribute);

				if(attribute != null && attribute.getCompilationUnit() == getHelper().context.getCompilationUnit()) {
					IPosition oldValue = attribute.getPosition();
					mt.add(new Transaction(oldValue.getLength(), oldValue.getOffset(), ""));
					execute = true;
				}
			}
			// set attribute
			else {
				String newValue;
				if(att.isBoolean) {
					newValue = "[" + att.attribute + "]";
				} else {
					newValue = (String)value;
					newValue = "[" + att.attribute + " \"" + newValue.replace("\"", "\\\"") + "\"]";
				}
				IAttribute attribute = getHelper().findAttribute(att.attribute);
				List<IAttributed> attributed = getHelper().findAttrubuted();
				if(attribute != null && attribute.getCompilationUnit() == getHelper().context.getCompilationUnit()) {
					IPosition oldValue = attribute.getPosition();
					mt.add(new Transaction(oldValue.getLength(), oldValue.getOffset(), newValue));
				} else if(attributed.size() > 0 && ((ILanguageElement)attributed.get(0)).getCompilationUnit() == getHelper().context.getCompilationUnit()) {
					ILanguageElement aaa = (ILanguageElement)attributed.get(0);
					mt.add(new Transaction(aaa.getSourceRange().getOffset(), newValue));
				} else
					mt.add(getHelper().getAddAttributeTransaction(newValue));
				execute = true;
			}
			if(execute) {
				cachedProperties.put(id, value);
				mt.execute();
			}
			dp.release();
		}

	}

	private final AttributedPropertySource m_attributedPropertySource = new AttributedPropertySource();

	private final static Object m_attributedId = new Object();

	private final static Object m_descriptionId = new Object();

	private static class Property {

		public int propertyType;
		public String what;

		public Property(int type, String what) {
			this.propertyType = type;
			this.what = what;
		}

		@Override
		public boolean equals(Object other) {
			if(other == null)
				return false;
			if(other instanceof Property) {
				Property prop = (Property)other;
				return what.equals(prop.what);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return what.hashCode() + 1;
		}
	}

	private static class AttributedProperty {
		public String attribute;
		public boolean isBoolean;

		public AttributedProperty(String attribute, boolean isBoolean) {
			this.attribute = attribute;
			this.isBoolean = isBoolean;
		}

		@Override
		public boolean equals(Object other) {
			if(other == null)
				return false;
			if(other instanceof AttributedProperty) {
				AttributedProperty att = (AttributedProperty)other;
				return attribute.equals(att.attribute);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return attribute.hashCode() + 2;
		}
	}

	private static class SubProperty {
		public String what;

		public SubProperty(String what) {
			this.what = what;
		}

		@Override
		public boolean equals(Object other) {
			if(other == null)
				return false;
			if(other instanceof SubProperty) {
				SubProperty att = (SubProperty)other;
				return what.equals(att.what);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return what.hashCode() + 3;
		}
	}

	private InitHelper m_helper;

	private IDocProv docProv;

	public void setDocument(IDocument d) {
		docProv = new SimpleDocProv(d);
	}

	public void setCompilationUnit(CompilationUnit cu) {
		docProv = new CUDocProv(cu);
	}

	private MultipleTransactions createMultipleTransactions(IDocProv p) {
		return new MultipleTransactions(p.getDocument());
	}

	public Model(InitHelper helper) {
		if(helper != null)
			setHelper(helper);
	}

	public static List<IPropertyDescriptor> collectProperties(IType t) {
		List<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
		for(IMember m : t.getAllMembers()) {
			if(m instanceof IMethod)
				continue;
			if(m.isStatic())
				continue;
			IVariableType vt = m.getVariableType();
			if(vt.isArray())
				continue;
			IType type = vt.getType();
			String newName = m.getName();
			if(type.isEnum())
				properties.add(createEnumPropertyDescriptor(type, newName));
			else if(type.isPrimary())
				properties.add(createPrimaryPropertyDescriptor(type, newName));
			else
				properties.add(new PropertyDescriptor(new SubProperty(newName), newName));
		}
		return properties;
	}

	public List<IPropertyDescriptor> collectRecordAttributeProperties() {
		IType t = getHelper().getMember().getDeclaringType();
		List<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
		for(IMember m : t.getAllMembers()) {
			if(m instanceof IMethod)
				continue;
			IVariableType vt = m.getVariableType();
			if(vt.isArray())
				continue;
			if(vt.getType().isSubtypeOf("Field"))
				properties.add(new TextPropertyDescriptor(new AttributedProperty(m.getName(), false), m.getName()));
		}
		return properties;
	}

	public static IPropertyDescriptor createBooleanAttributePropertyDescriptor(String attribute) {
		return new ComboBoxPropertyDescriptor(new AttributedProperty(attribute, true), "[" + attribute + "]", new String[] { "false", "true" });
	}

	public List<IPropertyDescriptor> collectAttributeProperties() {
		List<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
		IPropertyDescriptor d;
		d = new TextPropertyDescriptor(new AttributedProperty(IAttribute.DisplayName, false), "[" + IAttribute.DisplayName + "]");
		properties.add(d);
		d = createBooleanAttributePropertyDescriptor(IAttribute.Entry);
		if(getHelper().getType().isSubtypeOf("Runnable"))
			properties.add(d);
		d = createBooleanAttributePropertyDescriptor(IAttribute.Generatable);
		if(getHelper().getType().isSubtypeOf("Table"))
			properties.add(d);
		d = new TextPropertyDescriptor(new AttributedProperty(IAttribute.Name, false), "[" + IAttribute.Name + "]");
		((TextPropertyDescriptor)d).setValidator(new ICellEditorValidator() {

			@Override
			public String isValid(Object value) {
				String newText = (String)value;
				boolean namedOk = true;
				if(!newText.toUpperCase().equals(newText))
					namedOk = false;
				if(namedOk)
					for(char c : newText.toLowerCase().toCharArray())
						if((c < 'a' || c > 'z') && c != '_' && (c < '0' || c > '9')) {
							namedOk = false;
							break;
						}
				if(!namedOk)
					return "������ �������� ��� �������� named.";
				return null;
			}

		});
		if(getHelper().getType().isSubtypeOf("TableBase") || getHelper().getType().isSubtypeOf("Field"))
			properties.add(d);
		d = new TextPropertyDescriptor(new AttributedProperty(IAttribute.Job, false), "[" + IAttribute.Job + "]");
		if(getHelper().getType().isSubtypeOf("Runnable"))
			properties.add(d);
		return properties;
	}

	protected final void setHelper(InitHelper other) {
		m_helper = other;
	}

	protected List<IPropertyDescriptor> m_properties = new ArrayList<IPropertyDescriptor>(1);
	private List<IPropertyDescriptor> m_attributeProperties = null;

	private IPropertyDescriptor m_attributedPropertyDescriptor = new PropertyDescriptor(m_attributedId, "[��������]");

	private boolean showDescriptionProperty = true;

	@Override
	public Object getEditableValue() {
		if(getHelper() == null)
			return "";
		if(getHelper().what.isEmpty())
			return "class " + getHelper().context.getUserName();
		else
			return getHelper().toQname() + " (" + getHelper().getMemberType().getUserName() + ")";
	}

	protected List<IPropertyDescriptor> getAttributeProperties() {
		if(m_attributeProperties != null)
			return m_attributeProperties;
		if(getHelper().what.size() == 1)
			if(getHelper().getMember() instanceof Record) {
				m_attributeProperties = collectRecordAttributeProperties();
				return m_attributeProperties;
			}
		m_attributeProperties = collectAttributeProperties();
		return m_attributeProperties;
	}

	private List<IPropertyDescriptor> getCollectedProperties() {
		// if (m_collectedProperties != null)
		// return m_collectedProperties;
		if(getHelper() == null)
			return new ArrayList<IPropertyDescriptor>();
		Init2 init2 = getHelper().findInitDeep();
		IType toCollect = null;
		if(init2 == null) {
			if(getHelper().what.size() == 0)
				toCollect = getHelper().context;
			else {
				getHelper().findInitDeep();
				return new ArrayList<IPropertyDescriptor>();
			}
		} else {
			InitHelper h1 = InitHelper.createHelper(init2);
			if(h1 == null || h1.what.size() > 0) {
				getHelper().findInitDeep();
				return new ArrayList<IPropertyDescriptor>();
			}
			toCollect = h1.context;
		}
		if(toCollect == null) {
			getHelper().findInitDeep();
			return new ArrayList<IPropertyDescriptor>();
		}
		// m_collectedProperties = collectProperties(toCollect);
		// return m_collectedProperties;
		return collectProperties(toCollect);
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {

		List<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
		properties.addAll(m_properties);
		properties.addAll(getCollectedProperties());
		if(getHelper() != null) {
			properties.add(m_attributedPropertyDescriptor);
			if(showDescriptionProperty)
				properties.add(new PropertyDescriptor(m_descriptionId, ""));
		}
		return properties.toArray(new IPropertyDescriptor[m_properties.size()]);
	}

	@Override
	public Object getPropertyValue(Object id) {
		if(id instanceof Property) {
			if(cachedProperties.containsKey(id)) {
				Object result = cachedProperties.get(id);
				cachedProperties.remove(id);
				return result;
			}
			Property p = (Property)id;
			Init2 init = getHelper().add(p.what).findInitDeep();
			if(p.propertyType == PROPERTY_TYPE_ENUM) {
				if(init != null && init.element != null)
					if(init.element instanceof EnumElement) {
						EnumElement ee = (EnumElement)init.element;
						List<String> e = new ArrayList<String>();
						IType t = getHelper().add(p.what).getType();
						for(IMember m : t.getMembers())
							e.add(m.getName());
						return e.indexOf(ee.getName());
					}
				return 0;
			} else {
				if(init != null && init.element != null)
					if(init.element instanceof Constant) {
						Constant cnst = (Constant)init.element;
						if(p.propertyType == PROPERTY_TYPE_BOOLEAN)
							return cnst.getToken().format(false).equals("true") ? 1 : 0;
						return cnst.getToken().format(false);
					}
				return "";
			}
		} else if(id instanceof SubProperty) {
			if(cachedProperties.containsKey(id)) {
				Object result = cachedProperties.get(id);
				return result;
			}
			SubProperty sp = (SubProperty)id;
			Model model = new LinkedModel(this, sp.what);
			model.showDescriptionProperty = false;
			// model.setDocument(getDocument());
			cachedProperties.put(sp, model);
			return model;
		} else if(id == m_attributedId) {
			return m_attributedPropertySource;
		} else if(id == m_descriptionId) {
			return getEditableValue();
		} else
			return null;
	}

	@Override
	public boolean isPropertySet(Object id) {
		return id instanceof Property || id instanceof SubProperty;
	}

	@Override
	public void resetPropertyValue(Object id) {
		if(id instanceof Property) {
			IDocProv dp = getDocProv();
			if(dp == null)
				return;
			Property p = (Property)id;
			MultipleTransactions mt = createMultipleTransactions(dp);

			Init2 init = getHelper().add(p.what).findInit();
			if(init != null && init.element != null && init.element.getCompilationUnit() == getHelper().context.getCompilationUnit()) {
				IDocument d = dp.getDocument();
				int pos = init.element.getSourceRange().getOffset() - 1;
				try {
					while(Character.isWhitespace(d.getChar(pos)))
						pos--;
					if(d.getChar(pos) == '=') {
						pos--;
						while(Character.isWhitespace(d.getChar(pos)))
							pos--;
						if(Character.isJavaIdentifierPart(d.getChar(pos))) {
							pos--;
							while(Character.isJavaIdentifierPart(d.getChar(pos)) || d.getChar(pos) == '.')
								pos--;
							while(Character.isWhitespace(d.getChar(pos)))
								pos--;
							if(!Character.isJavaIdentifierPart(d.getChar(pos))) {
								int end = init.element.getSourceRange().getOffset() + init.element.getSourceRange().getLength();
								while(Character.isWhitespace(d.getChar(end)))
									end++;
								if(d.getChar(end) == ';') {
									end++;
									mt.add(new Transaction(end - pos - 1, pos + 1));
								}
							}
						}

					}
				} catch(Exception e) {
				}
				;
			} else {
				// property not set
			}
			if(!mt.getTransactions().isEmpty()) {
				mt.execute();
			}
			dp.release();
		}
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		// if
		// (getHelper().context!=getHelper().context.getCompilationUnit().getReconciledType())
		// return;
		if(id instanceof Property) {
			IDocProv dp = getDocProv();
			if(dp == null)
				return;
			Property p = (Property)id;
			IType t = getHelper().add(p.what).getType();
			MultipleTransactions mt = createMultipleTransactions(dp);
			String resultString = null;
			if(p.propertyType == PROPERTY_TYPE_ENUM) {
				List<String> e = new ArrayList<String>();
				InitHelper.checkImport(mt, getHelper().context, t.getCompilationUnit().getQualifiedName());
				for(IMember m : t.getMembers())
					e.add(m.getName());
				resultString = t.getUserName() + "." + e.get((Integer)value);
			} else if(p.propertyType == PROPERTY_TYPE_BOOLEAN) {
				resultString = value.equals(1) ? "true" : "false";
			} else {
				String newValue = (String)value;
				if(p.propertyType == PROPERTY_TYPE_STRING)
					resultString = "\"" + newValue.replace("\"", "\\\"") + "\"";
				else if(p.propertyType == PROPERTY_TYPE_INT)
					resultString = newValue;
				else
					resultString = newValue;
			}
			Init2 init = getHelper().add(p.what).findInit();
			if(init != null && init.element != null && init.element.getCompilationUnit() == getHelper().context.getCompilationUnit()) {
				mt.add(new Transaction(init.element.getSourceRange().getLength(), init.element.getSourceRange().getOffset(), resultString));
			} else {
				mt.add(getHelper().add(p.what).getAddInitTransaction(resultString));
			}
			if(!mt.getTransactions().isEmpty()) {
				cachedProperties.put(id, value);
				mt.execute();
			}
			dp.release();
		}
	}

	public InitHelper getHelper() {
		return m_helper;
	}

	public static IPropertyDescriptor createBooleanPropertyDescriptor(String name) {
		return new ComboBoxPropertyDescriptor(new Property(PROPERTY_TYPE_BOOLEAN, name), name, new String[] { "false", "true" });
	}

	protected static IPropertyDescriptor createPrimaryPropertyDescriptor(IType type, String name) {
		if(type.getUserName().equals("bool"))
			return createBooleanPropertyDescriptor(name);
		int propertyType;
		if(type.getUserName().equals("string"))
			propertyType = PROPERTY_TYPE_STRING;
		else if(type.getUserName().equals("int") || type.getUserName().equals("decimal"))
			propertyType = PROPERTY_TYPE_INT;
		else
			propertyType = PROPERTY_TYPE_OTHER;
		PropertyDescriptor d = new TextPropertyDescriptor(new Property(propertyType, name), name);
		String typeString = type.getUserName();
		if(typeString.equals("int"))
			d.setValidator(new ICellEditorValidator() {

				@Override
				public String isValid(Object value) {
					String s = (String)value;
					for(char c : s.toCharArray())
						if(!Character.isDigit(c))
							return "Not a number";
					return null;
				}

			});
		if(typeString.equals("decimal"))
			d.setValidator(new ICellEditorValidator() {

				@Override
				public String isValid(Object value) {
					String s = (String)value;
					int dotCount = 0;
					for(char c : s.toCharArray())
						if(!Character.isDigit(c)) {
							if(c == '.') {
								dotCount++;

							} else
								return "Not a decimal number";
						}
					if(dotCount > 1)
						return "Not a decimal number";
					return null;
				}

			});
		return d;
	}

	protected static IPropertyDescriptor createEnumPropertyDescriptor(IType type, String name) {
		List<String> list = new ArrayList<String>();
		for(IMember m : type.getMembers())
			list.add(m.getName());
		return new ComboBoxPropertyDescriptor(new Property(PROPERTY_TYPE_ENUM, name), name, list.toArray(new String[0]));
	}

	@Override
	public boolean equals(Object other) {
		if(other == null)
			return false;
		if(!getClass().equals(other.getClass()))
			return false;
		if(getHelper() == null)
			return false;
		return getHelper().equals(((Model)other).getHelper());
	}

	protected IDocProv getDocProv() {
		return docProv;
	}

	public String getAttributeStringValue(String attribute) {
		String result = (String)m_attributedPropertySource.getPropertyValue(new AttributedProperty(attribute, false));
		if(result.equals(NOT_SET_ATTRIBUTE))
			return null;
		return result;
	}

	public boolean getAttributeBooleanValue(String attribute) {
		return ((Integer)m_attributedPropertySource.getPropertyValue(new AttributedProperty(attribute, true))).intValue() == 1;
	}

	public boolean getBooleanPropertyValue(String what) {
		return ((Integer)getPropertyValue(new Property(PROPERTY_TYPE_BOOLEAN, what))).intValue() == 1;
	}

	public int getIntPropertyValue(String what) {
		String s = (String)getPropertyValue(new Property(PROPERTY_TYPE_INT, what));
		return new Integer(s);
	}

	public String getStringPropertyValue(String what) {
		return (String)getPropertyValue(new Property(PROPERTY_TYPE_STRING, what));
	}

	public IMember getEnumPropertyValue(String what) {
		IType t = getHelper().add(what).getType();
		return t.getMembers()[(Integer)getPropertyValue(new Property(PROPERTY_TYPE_ENUM, what))];
	}

	@Override
	public int hashCode() {
		if(getHelper() == null)
			return super.hashCode();
		return getHelper().hashCode();
	}

	@Override
	public void merge(IMergeable other) {
		Model m = (Model)other;
		setHelper(m.m_helper);
	}

	public static IType getTypeFromQualifiedName(String qName) {
		CompilationUnit cu = null;
		for(Project p : Workspace.getInstance().getProjects()) {
			cu = p.getCompilationUnit(qName.replace('.', '/') + ".bl");
			if(cu != null)
				break;
		}
		if(cu == null)
			return null;
		final CompilationUnit cUnit = cu;
		if(cu.getType() == null) {
			BusyIndicator.showWhile(Display.getCurrent(), new java.lang.Runnable() {
				@Override
				public void run() {
					ReconcileMessageConsumer consumer = new ReconcileMessageConsumer();
					cUnit.getProject().reconcile(cUnit.getResource(), null, consumer);
				}
			});
		}
		return cUnit.getType();
	}

}
