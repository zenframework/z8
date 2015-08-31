package org.zenframework.z8.pde.refactoring.reorg;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.internal.ui.actions.StatusInfo;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.pde.refactoring.LanguageConventions;
import org.zenframework.z8.pde.refactoring.Z8Status;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.operations.CreateFolderOperation;
import org.zenframework.z8.pde.refactoring.rename.TextFieldNavigationHandler;

@SuppressWarnings("restriction")
public class NewFolderWizardPage extends NewContainerWizardPage {
    private static final String PAGE_NAME = "NewFolderWizardPage";
    private static final String FOLDER = "NewFolderWizardPage.folder";
    private StringDialogField m_folderDialogField;

    private IStatus m_folderStatus;
    private Folder m_createdFolder;

    public NewFolderWizardPage() {
        super(PAGE_NAME);
        setTitle(RefactoringMessages.NewFolderWizardPage_title);
        setDescription(RefactoringMessages.NewFolderWizardPage_description);
        m_createdFolder = null;
        FolderFieldAdapter adapter = new FolderFieldAdapter();
        m_folderDialogField = new StringDialogField();
        m_folderDialogField.setDialogFieldListener(adapter);
        m_folderDialogField.setLabelText(RefactoringMessages.NewFolderWizardPage_folder_label);
        m_folderStatus = new StatusInfo();
    }

    public void init(IStructuredSelection selection) {
        ILanguageElement element = getInitialElement(selection);

        initContainerPage(element);

        String folderName = "";

        if(element != null) {
            Folder folder = null;

            if(element instanceof Resource) {
                folder = ((Resource)element).getFolder();
            }
            else {
                folder = element.getCompilationUnit().getFolder();
            }

            if(folder != null) {
                folderName = folder.getName();
            }
        }

        setFolderText(folderName, true);

        updateStatus(new IStatus[] { m_containerStatus, m_folderStatus });
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        int nColumns = 3;
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);
        Label label = new Label(composite, SWT.WRAP);
        label.setText(RefactoringMessages.NewFolderWizardPage_info);
        GridData gd = new GridData();
        gd.widthHint = convertWidthInCharsToPixels(60);
        gd.horizontalSpan = 3;
        label.setLayoutData(gd);
        createContainerControls(composite, nColumns);
        createFolderControls(composite, nColumns);
        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if(visible) {
            setFocus();
        }
    }

    protected void setFocus() {
        m_folderDialogField.setFocus();
    }

    private void createFolderControls(Composite composite, int nColumns) {
        m_folderDialogField.doFillIntoGrid(composite, nColumns - 1);
        Text text = m_folderDialogField.getTextControl(null);
        setWidthHint(text, getMaxFieldWidth());
        setHorizontalGrabbing(text);
        DialogField.createEmptySpace(composite);
        TextFieldNavigationHandler.install(text);
    }

    private class FolderFieldAdapter implements IDialogFieldListener {
        @Override
        public void dialogFieldChanged(DialogField field) {
            m_folderStatus = folderChanged();
            handleFieldChanged(FOLDER);
        }
    }

    @Override
    protected void handleFieldChanged(String fieldName) {
        super.handleFieldChanged(fieldName);

        if(fieldName == CONTAINER) {
            m_folderStatus = folderChanged();
        }

        updateStatus(new IStatus[] { m_containerStatus, m_folderStatus });
    }

    private IStatus validateFolderName(String text) {
        return LanguageConventions.validateFolderName(text);
    }

    private IStatus folderChanged() {
        StatusInfo status = new StatusInfo();

        String folderName = getFolderText();

        if(folderName.length() > 0) {
            IStatus val = validateFolderName(folderName);

            if(val.getSeverity() == IStatus.ERROR) {
                status.setError(Messages.format(RefactoringMessages.NewFolderWizardPage_error_InvalidFolderName,
                        val.getMessage()));
                return status;
            }
            else if(val.getSeverity() == IStatus.WARNING) {
                status.setWarning(Messages.format(RefactoringMessages.NewFolderWizardPage_warning_DiscouragedFolderName,
                        val.getMessage()));
            }
        }
        else {
            status.setError(RefactoringMessages.NewFolderWizardPage_error_EnterName);
            return status;
        }

        Folder root = getRoot();

        if(root != null && root.getProject().getResource().exists()) {
            Folder folder = root.getFolder(folderName);

            IPath rootPath = root.getPath();
            IPath outputPath = root.getProject().getOutputPath();

            if(rootPath.isPrefixOf(outputPath) && !rootPath.equals(outputPath)) {
                IPath folderPath = folder.getPath();

                if(outputPath.isPrefixOf(folderPath)) {
                    status.setError(RefactoringMessages.NewFolderWizardPage_error_IsOutputFolder);
                    return status;
                }
            }

            if(folder.getResource().exists()) {
                if(folder.hasCompilationUnits() || !folder.hasSubfolders()) {
                    status.setError(RefactoringMessages.NewFolderWizardPage_error_FolderExists);
                }
                else {
                    status.setError(RefactoringMessages.NewFolderWizardPage_error_FolderNotShown);
                }
            }
            else {
                URI location = folder.getResource().getLocationURI();
                if(location != null) {
                    try {
                        IFileStore store = EFS.getStore(location);

                        if(store.fetchInfo().exists()) {
                            status.setError(RefactoringMessages.NewFolderWizardPage_error_FolderExistsDifferentCase);
                        }
                    }
                    catch(CoreException e) {
                        return new Z8Status(e);
                    }
                }
            }

        }
        return status;
    }

    public String getFolderText() {
        return m_folderDialogField.getText();
    }

    public void setFolderText(String str, boolean canBeModified) {
        m_folderDialogField.setText(str);
        m_folderDialogField.setEnabled(canBeModified);
    }

    public IResource getModifiedResource() {
        Folder root = getRoot();
        if(root != null) {
            return root.getFolder(getFolderText()).getResource();
        }
        return null;
    }

    public IRunnableWithProgress getRunnable() {
        return new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                try {
                    createFolder(monitor);
                }
                catch(CoreException e) {
                    throw new InvocationTargetException(e);
                }
            }
        };
    }

    public Folder getNewFolder() {
        return m_createdFolder;
    }

    public Folder createFolder(Folder root, String folderName, boolean force, IProgressMonitor monitor) throws CoreException {
        CreateFolderOperation op = new CreateFolderOperation(root, folderName, force);
        op.runOperation(monitor);
        return root.getFolder(folderName);
    }

    public void createFolder(IProgressMonitor monitor) throws CoreException, InterruptedException {
        if(monitor == null) {
            monitor = new NullProgressMonitor();
        }

        Folder root = getRoot();
        String folderName = getFolderText();

        m_createdFolder = createFolder(root, folderName, true, monitor);

        if(monitor.isCanceled()) {
            throw new InterruptedException();
        }
    }
}
