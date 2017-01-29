package org.zenframework.z8.pde.wizards;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.util.ReaderInputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import org.zenframework.z8.pde.wizards.NewClassExtraPage.CGenerator;

@SuppressWarnings("restriction")
public class NewClassWizard extends BasicNewResourceWizard {
	private WizardNewFileCreationPage mainPage;
	private NewClassExtendsPage superPage;
	private NewClassExtraPage extraPage;

	private List<String> imports;

	private String contents;

	public NewClassWizard() {
		super();
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new WizardNewFileCreationPage("newFilePage1", getSelection()) {
			@Override
			protected InputStream getInitialContents() {
				String name = getFileName();
				String className = name.substring(0, name.length() - 3);
				contents = "";
				imports = new ArrayList<String>();
				List<CGenerator> gens = ((NewClassExtraPage)getPage("newFilePage4")).getGenerators();
				String superT = superPage.getType();
				if(superT != null)
					addImport(superT);
				String preClass = "";
				String conts = "";
				for(CGenerator g : gens) {
					for(String imp : g.getImports())
						addImport(imp);
					preClass += g.getBeforeClass();
					conts += g.getContent();
				}

				contents += "\r\n" + preClass + "class " + className + (superPage.getType() != null ? " extends " + superPage.getType().substring(superPage.getType().lastIndexOf('.') + 1) : "") + " {\r\n" + conts + "\r\n}\r\n";
				return new ReaderInputStream(new StringReader(contents)) {
					@Override
					public synchronized void close() throws IOException {
					}
				};
			}

			@Override
			protected boolean validatePage() {
				String name = getFileName();
				if(name.endsWith(".bl"))
					name = name.substring(0, name.length() - 3);
				((NewClassExtraPage)getPage("newFilePage4")).updateName(name);

				for(int i = 0; i < name.length(); i++) {
					char c = name.charAt(i);
					if(c != '_' && c != '�' && c != '�' && (c < '�' || c > '�') && (c < '�' || c > '�') && (c < '0' || c > '9') && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
						setErrorMessage("������������ ������ � ����� �����: '" + c + "'");
						return false;
					}
				}
				setErrorMessage(null);
				return super.validatePage();
			}

		};
		mainPage.setTitle("����� Z8");
		mainPage.setDescription("�������� ������ ������ Z8");
		addPage(mainPage);
		superPage = new NewClassExtendsPage("newFilePage2", "����� ������������� ������", IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newfile_wiz.png"));
		addPage(superPage);
		extraPage = new NewClassExtraPage("newFilePage4", "�������������", IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newfile_wiz.png"));
		addPage(extraPage);
		mainPage.setFileExtension("bl");
	}

	private void addImport(String imported) {
		if(imports.contains(imported))
			return;
		imports.add(imported);
		contents = "import " + imported + ";\r\n" + contents;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setWindowTitle("�������� ������ ������");
		setNeedsProgressMonitor(true);
	}

	@Override
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newfile_wiz.png");//$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}

	@Override
	public boolean performFinish() {
		IFile file = mainPage.createNewFile();
		if(file == null) {
			return false;
		}

		selectAndReveal(file);

		// Open editor on new file.
		IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
		try {
			if(dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if(page != null) {
					IDE.openEditor(page, file, true);
				}
			}
		} catch(PartInitException e) {
			DialogUtil.openError(dw.getShell(), ResourceMessages.FileResource_errorMessage, e.getMessage(), e);
		}

		return true;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if(page == superPage)
			if(superPage.getType() == null)
				return null;
		return super.getNextPage(page);
	}

	/*
	 * public void updateMethodsPage(){ methodsPage.update(superPage.getType());
	 * }
	 */

}
