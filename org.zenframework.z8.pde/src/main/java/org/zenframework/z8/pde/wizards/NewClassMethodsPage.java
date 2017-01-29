package org.zenframework.z8.pde.wizards;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;

public class NewClassMethodsPage extends WizardPage {

	private List methodsList;

	private java.util.List<IMethod> methods;

	protected NewClassMethodsPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	@Override
	public void createControl(Composite parent) {
		Composite topLevel = new Composite(parent, SWT.NONE);
		GridData data;
		topLevel.setLayout(new GridLayout());
		topLevel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		Label label = new Label(topLevel, SWT.NONE);
		label.setText("�������� ����������� ������: (ctrl - ������������� ���������)");
		methodsList = new List(topLevel, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		data = new GridData(SWT.FILL, SWT.TOP, true, false);
		data.heightHint = 500;
		data.widthHint = 300;
		methodsList.setLayoutData(data);
		setControl(topLevel);
	}

	public void update(IType type) {
		if(type == null) {
			methodsList.setItems(new String[0]);
			return;
		}
		java.util.List<String> methodsStrings = new ArrayList<String>();
		methods = new ArrayList<IMethod>();
		IType curr = type.getBaseType();
		while(curr != null) {
			for(IMethod m : curr.getMethods())
				if(m.getBody() == null && m.isVirtual()) {
					IType curr1 = type.getBaseType();
					boolean found = false;
					while(curr1 != curr) {
						for(IMethod m1 : curr1.getMethods())
							if(m1.getSignature().equals(m.getSignature())) {
								found = true;
								break;
							}
						curr1 = curr1.getBaseType();
					}
					if(!found) {
						String method = m.getVariableType().getSignature() + " " + m.getName() + " (";
						boolean first = true;
						for(IVariable v : m.getParameters()) {
							method += (first ? "" : ", ") + v.getSignature() + " " + v.getName();
							first = false;
						}
						method += ")";
						methodsStrings.add(method);
						methods.add(m);
					}
				}
			curr = curr.getBaseType();
		}
		String[] ms = new String[methodsStrings.size()];
		methodsStrings.toArray(ms);
		Arrays.sort(ms);
		methodsList.setItems(ms);

	}

	public String[] getMethodsStrings() {
		return methodsList.getSelection();
	}

	public java.util.List<IMethod> getMethods() {
		String[] strings = methodsList.getSelection();
		java.util.List<IMethod> result = new ArrayList<IMethod>(strings.length);
		for(String string : strings)
			result.add(methods.get(methodsList.indexOf(string)));
		return result;
	}

}
