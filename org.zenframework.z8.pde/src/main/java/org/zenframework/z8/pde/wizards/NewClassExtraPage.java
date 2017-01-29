package org.zenframework.z8.pde.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.pde.Model;

public class NewClassExtraPage extends WizardPage {

	private Composite topLevel;

	private String name = null;

	private List<CGenerator> generators = new ArrayList<CGenerator>();

	public class CGenerator {
		List<String> getImports() {
			return new ArrayList<String>(0);
		};

		String getBeforeClass() {
			return "";
		};

		String getContent() {
			return "";
		};

		void updateName(String name) {
		};

		void dispose() {
		};
	}

	private class BoolAttributeGenerator extends CGenerator {
		String attr;

		Composite c;

		Button b;

		public BoolAttributeGenerator(String a, Composite c1, boolean enabled) {
			this.attr = a;
			c = new Composite(c1, SWT.NONE);
			c.setLayout(new GridLayout());
			c.setLayoutData(new GridData());
			b = new Button(c, SWT.CHECK);
			b.setSelection(enabled);
			b.setText("������� " + attr);
			b.setLayoutData(new GridData());
		}

		@Override
		String getBeforeClass() {
			if(b.getSelection()) {
				return "[" + attr + "]\r\n";
			}
			return super.getBeforeClass();
		}

		@Override
		void dispose() {
			c.dispose();
		}
	}

	private class StringAttributeGenerator extends CGenerator {
		String attr;

		Composite c;

		Button b;

		Text t;

		public StringAttributeGenerator(String a, Composite c1, boolean enabled) {
			this.attr = a;
			c = new Composite(c1, SWT.NONE);
			c.setLayout(new GridLayout(2, false));
			c.setLayoutData(new GridData());
			b = new Button(c, SWT.CHECK);
			b.setSelection(enabled);
			b.setText("������� " + attr + ":");
			b.setLayoutData(new GridData());
			t = new Text(c, SWT.BORDER);
			t.setLayoutData(new GridData(150, SWT.DEFAULT));
			t.setEnabled(enabled);
			b.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					t.setEnabled(b.getSelection());
				}
			});
			updateName(name);
		}

		void setName(String name, Text t) {
			t.setText(name);
		}

		@Override
		void updateName(String name) {
			setName(name, t);
		}

		@Override
		String getBeforeClass() {
			if(b.getSelection()) {
				return "[" + attr + " \"" + t.getText() + "\"]\r\n";
			}
			return super.getBeforeClass();
		}

		@Override
		void dispose() {
			c.dispose();
		}
	}

	private class NamedAttributeGenerator extends StringAttributeGenerator {

		public NamedAttributeGenerator(String a, Composite c1, boolean enabled) {
			super(a, c1, enabled);
		}

		@Override
		void setName(String name, Text t) {
			t.setText(name.toUpperCase());
		}

	}

	protected NewClassExtraPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	@Override
	public void createControl(Composite parent) {
		topLevel = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		topLevel.setLayout(gl);
		topLevel.setData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		setControl(topLevel);
	}

	public void updateParent(String type) {
		for(CGenerator g : generators)
			g.dispose();
		generators.clear();
		if(type != null) {
			IType t = Model.getTypeFromQualifiedName(type);
			addGenerators(t);
		}
		topLevel.layout(true);
	}

	public void updateName(String name) {
		this.name = name;
		for(CGenerator g : generators)
			g.updateName(name);
	}

	public List<CGenerator> getGenerators() {
		return generators;
	}

	private void addGenerators(IType t) {
		generators.add(new StringAttributeGenerator(IAttribute.DisplayName, topLevel, true));
		if(t.isSubtypeOf("Runnable")) {
			generators.add(new StringAttributeGenerator(IAttribute.Job, topLevel, false));
			generators.add(new BoolAttributeGenerator(IAttribute.Entry, topLevel, false));

		}
		if(t.isSubtypeOf("TableBase") || t.isSubtypeOf("Field"))
			generators.add(new NamedAttributeGenerator(IAttribute.Name, topLevel, true));
		if(t.isSubtypeOf("Table"))
			generators.add(new BoolAttributeGenerator(IAttribute.Generatable, topLevel, true));
	}

}
