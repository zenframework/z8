package org.zenframework.z8.pde.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.ResourceVisitor;
import org.zenframework.z8.compiler.workspace.Workspace;

public class NewClassExtendsPage extends WizardPage {

	private Text nameField;

	private List classesList;

	private Combo baseCombo;

	private java.util.List<IType> allTypes = new ArrayList<IType>();

	private IType baseTypes[] = new IType[0];;

	private IType[] currentTypes;

	private String type = null;

	private static ArrayList<String> fastKeys = new ArrayList<String>();
	private static ArrayList<String> fastValues = new ArrayList<String>();

	public void setResource() {
		final java.util.List<IType> baseTypesList = new ArrayList<IType>();
		allTypes = new ArrayList<IType>(100);
		for(Project p : Workspace.getInstance().getProjects()) {
			p.iterate(new ResourceVisitor() {
				@Override
				public boolean visit(CompilationUnit compilationUnit) {
					IType type = compilationUnit.getType();
					if(type != null) {
						allTypes.add(type);
						if(type.isNative() && !type.isPrimary())
							baseTypesList.add(type);
					}
					return true;
				}
			});
		}
		baseTypes = new IType[baseTypesList.size()];
		baseTypesList.toArray(baseTypes);
		Arrays.sort(baseTypes, new Comparator<IType>() {
			@Override
			public int compare(IType arg0, IType arg1) {
				return arg0.getUserName().compareToIgnoreCase(arg1.getUserName());
			}
		});
		if(baseCombo != null)
			setComboData();
		if(nameField != null)
			updateList();
	}

	protected NewClassExtendsPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	@Override
	public void createControl(Composite parent) {
		final ArrayList<Control> otherControls = new ArrayList<Control>();

		Composite topLevel = new Composite(parent, SWT.NONE);
		GridData layoutData;
		topLevel.setLayout(new GridLayout());
		topLevel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		Label label1 = new Label(topLevel, SWT.NONE);
		label1.setText("�������� ��� ������:");
		for(int i = 0; i < fastKeys.size(); i++) {
			final String value = fastValues.get(i);
			Button b = new Button(topLevel, SWT.RADIO);
			b.setText(fastKeys.get(i));
			b.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					for(Control c : otherControls)
						c.setEnabled(false);
					setType(value);
				}

			});
		}

		Button b = new Button(topLevel, SWT.RADIO);
		b.setText("������:");
		b.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				for(Control c : otherControls)
					c.setEnabled(true);
				setTypeFromList();
			}

		});

		Label label = new Label(topLevel, SWT.NONE);
		label.setText("������ �� ����� ������������� ������:");
		otherControls.add(label);
		nameField = new Text(topLevel, SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		nameField.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateList();
				getContainer().updateButtons();
			}
		});
		otherControls.add(nameField);
		label = new Label(topLevel, SWT.NONE);
		label.setText("������ �� �������� ������:");
		otherControls.add(label);
		baseCombo = new Combo(topLevel, SWT.BORDER | SWT.READ_ONLY);
		layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
		baseCombo.setLayoutData(layoutData);
		baseCombo.setVisibleItemCount(30);
		baseCombo.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateList();
				getContainer().updateButtons();
			}
		});
		otherControls.add(baseCombo);
		label = new Label(topLevel, SWT.NONE);
		label.setText("������������ �����:");
		otherControls.add(label);
		classesList = new List(topLevel, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		layoutData = new GridData(SWT.FILL, SWT.TOP, true, true);
		layoutData.heightHint = 600;
		classesList.setLayoutData(layoutData);
		classesList.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				getContainer().updateButtons();
				setTypeFromList();
			}
		});
		otherControls.add(classesList);
		for(Control c : otherControls)
			c.setEnabled(false);
		setControl(topLevel);
		setResource();
	}

	private void setComboData() {
		String[] strings = new String[baseTypes.length + 1];
		for(int i = 0; i < baseTypes.length; i++)
			strings[i + 1] = baseTypes[i].getUserName();
		strings[0] = "";
		baseCombo.setItems(strings);
		baseCombo.select(0);
	}

	private void updateList() {
		IType baseType = null;
		int index = baseCombo.getSelectionIndex();
		if(index > 0)
			baseType = baseTypes[index - 1];
		String beginning = nameField.getText();
		classesList.deselectAll();
		java.util.List<IType> curr;
		// if (beginning.equals("")) curr = allTypes;
		// else {
		curr = new ArrayList<IType>();
		for(IType type : allTypes) {
			String name = type.getUserName();
			if(baseType != null)
				if(!(type.isSubtypeOf(baseType) || baseType.equals(type)))
					continue;
			if(name.length() < beginning.length())
				continue;
			// String start = name.substring(0, beginning.length());
			// if (beginning.equalsIgnoreCase(start))
			if(name.toLowerCase().contains(beginning.toLowerCase()))
				curr.add(type);
		}
		// }
		int size = curr.size();
		currentTypes = new IType[size];
		curr.toArray(currentTypes);
		Arrays.sort(currentTypes, new Comparator<IType>() {
			@Override
			public int compare(IType arg0, IType arg1) {
				return arg0.getUserName().compareToIgnoreCase(arg1.getUserName());
			}
		});
		String[] strings = new String[size];
		for(int i = 0; i < size; i++)
			strings[i] = currentTypes[i]
					.getUserName()/*
									 * + " - " +
									 * currentTypes[i].getCompilationUnit().
									 * getPackage()
									 */;
		classesList.setItems(strings);
	}

	public String getType() {
		return type;
	}

	private void setType(String type) {
		this.type = type;
		((NewClassExtraPage)getWizard().getPage("newFilePage4")).updateParent(type);
		getContainer().updateButtons();
	}

	private void setTypeFromList() {
		int index = classesList.getSelectionIndex();
		String type1 = null;
		if(index != -1)
			type1 = currentTypes[index].getCompilationUnit().getQualifiedName();
		setType(type1);
	}

}
