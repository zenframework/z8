package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.internal.ui.actions.StatusInfo;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Z8ProjectNature;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.LanguageElementLabelProvider;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

@SuppressWarnings("restriction")
public abstract class NewContainerWizardPage extends NewElementWizardPage {
    protected static final String CONTAINER = "NewContainerWizardPage.container";

    protected IStatus m_containerStatus;

    private StringButtonDialogField fContainerDialogField;

    private Folder fCurrRoot;
    private IWorkspaceRoot fWorkspaceRoot;

    public NewContainerWizardPage(String name) {
        super(name);
        fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        ContainerFieldAdapter adapter = new ContainerFieldAdapter();
        fContainerDialogField = new StringButtonDialogField(adapter);
        fContainerDialogField.setDialogFieldListener(adapter);
        fContainerDialogField.setLabelText(getContainerLabel());
        fContainerDialogField.setButtonLabel(RefactoringMessages.NewContainerWizardPage_container_button);
        m_containerStatus = new StatusInfo();
        fCurrRoot = null;
    }

    protected String getContainerLabel() {
        return RefactoringMessages.NewContainerWizardPage_container_label;
    }

    protected void initContainerPage(ILanguageElement element) {
        Folder initRoot = null;

        if(element != null) {
            if(element instanceof Resource) {
                initRoot = ((Resource)element).getProject();
            }
            else {
                initRoot = element.getCompilationUnit().getProject();
            }
        }

        setRoot(initRoot, true);
    }

    protected ILanguageElement getInitialElement(IStructuredSelection selection) {
        ILanguageElement element = null;

        if(selection != null && !selection.isEmpty()) {
            Object selectedElement = selection.getFirstElement();

            if(selectedElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable)selectedElement;

                element = (ILanguageElement)adaptable.getAdapter(ILanguageElement.class);

                if(element == null) {
                    IResource resource = (IResource)adaptable.getAdapter(IResource.class);

                    if(resource != null && resource.getType() != IResource.ROOT) {
                        while(element == null && resource.getType() != IResource.PROJECT) {
                            resource = resource.getParent();
                            element = (ILanguageElement)resource.getAdapter(ILanguageElement.class);
                        }
                        if(element == null) {
                            element = Workspace.getInstance().getResource(resource);
                        }
                    }
                }
            }
        }

        if(element == null) {
            IWorkbenchPart part = Plugin.getActivePage().getActivePart();

            if(part instanceof ContentOutline) {
                part = Plugin.getActivePage().getActiveEditor();
            }
        }

        if(element == null) {
            Project[] projects = Workspace.getInstance().getProjects();

            if(projects.length == 1) {
                element = projects[0];
            }
        }
        return element;
    }

    protected ITextSelection getCurrentTextSelection() {
        IWorkbenchPart part = Plugin.getActivePage().getActivePart();

        if(part instanceof IEditorPart) {
            ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
            if(selectionProvider != null) {
                ISelection selection = selectionProvider.getSelection();
                if(selection instanceof ITextSelection) {
                    return (ITextSelection)selection;
                }
            }
        }
        return null;
    }

    protected int getMaxFieldWidth() {
        return convertWidthInCharsToPixels(40);
    }

    public static void setHorizontalGrabbing(Control control) {
        Object ld = control.getLayoutData();
        if(ld instanceof GridData) {
            ((GridData)ld).grabExcessHorizontalSpace = true;
        }
    }

    public static void setWidthHint(Control control, int widthHint) {
        Object ld = control.getLayoutData();
        if(ld instanceof GridData) {
            ((GridData)ld).widthHint = widthHint;
        }
    }

    protected void createContainerControls(Composite parent, int nColumns) {
        fContainerDialogField.doFillIntoGrid(parent, nColumns);
        setWidthHint(fContainerDialogField.getTextControl(null), getMaxFieldWidth());
    }

    protected void setFocusOnContainer() {
        fContainerDialogField.setFocus();
    }

    private class ContainerFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {
        @Override
        public void changeControlPressed(DialogField field) {
            containerChangeControlPressed(field);
        }

        @Override
        public void dialogFieldChanged(DialogField field) {
            containerDialogFieldChanged(field);
        }
    }

    private void containerChangeControlPressed(DialogField field) {
        Folder root = chooseContainer();

        if(root != null) {
            setRoot(root, true);
        }
    }

    private void containerDialogFieldChanged(DialogField field) {
        if(field == fContainerDialogField) {
            m_containerStatus = containerChanged();
        }
        handleFieldChanged(CONTAINER);
    }

    protected IStatus containerChanged() {
        StatusInfo status = new StatusInfo();
        fCurrRoot = null;
        String str = getRootText();

        if(str.length() == 0) {
            status.setError(RefactoringMessages.NewContainerWizardPage_error_EnterContainerName);
            return status;
        }

        IPath path = new Path(str);
        IResource res = fWorkspaceRoot.findMember(path);

        if(res != null) {
            int resType = res.getType();

            if(resType == IResource.PROJECT || resType == IResource.FOLDER) {
                IProject proj = res.getProject();

                if(!proj.isOpen()) {
                    status.setError(Messages.format(RefactoringMessages.NewContainerWizardPage_error_ProjectClosed, proj
                            .getFullPath().toString()));
                    return status;
                }

                Project project = Workspace.getInstance().getProject(proj);

                fCurrRoot = project;

                if(res.exists()) {
                    try {
                        if(!proj.hasNature(Z8ProjectNature.Id)) {
                            if(resType == IResource.PROJECT) {
                                status.setError(RefactoringMessages.NewContainerWizardPage_warning_NotAZ8Project);
                            }
                            else {
                                status.setWarning(RefactoringMessages.NewContainerWizardPage_warning_NotInAZ8Project);
                            }
                            return status;
                        }
                    }
                    catch(CoreException e) {
                        status.setWarning(RefactoringMessages.NewContainerWizardPage_warning_NotAZ8Project);
                    }
                }
                return status;
            }
            else {
                status.setError(Messages.format(RefactoringMessages.NewContainerWizardPage_error_NotAFolder, str));
                return status;
            }
        }
        else {
            status.setError(Messages.format(RefactoringMessages.NewContainerWizardPage_error_ContainerDoesNotExist, str));
            return status;
        }
    }

    protected void handleFieldChanged(String fieldName) {}

    protected IWorkspaceRoot getWorkspaceRoot() {
        return fWorkspaceRoot;
    }

    public Project getProject() {
        Folder root = getRoot();

        if(root != null) {
            return root.getProject();
        }
        return null;
    }

    public Folder getRoot() {
        return fCurrRoot;
    }

    public String getRootText() {
        return fContainerDialogField.getText();
    }

    public void setRoot(Folder root, boolean canBeModified) {
        fCurrRoot = root;
        String str = (root == null) ? "" : root.getPath().makeRelative().toString();
        fContainerDialogField.setText(str);
        fContainerDialogField.setEnabled(canBeModified);
    }

    @SuppressWarnings("unchecked")
    protected Folder chooseContainer() {
        Folder initElement = getRoot();

        TypedElementSelectionValidator validator = new TypedElementSelectionValidator(new Class[] { Project.class }, false) {
            @Override
            public boolean isSelectedValid(Object element) {
                return true;
            }
        };

        ViewerFilter filter = new TypedViewerFilter(new Class[] { Project.class }) {
            @Override
            public boolean select(Viewer viewer, Object parent, Object element) {
                return super.select(viewer, parent, element);
            }
        };

        StandardLanguageElementContentProvider provider = new StandardLanguageElementContentProvider();

        ILabelProvider labelProvider = new LanguageElementLabelProvider(LanguageElementLabelProvider.SHOW_DEFAULT);

        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, provider);

        dialog.setValidator(validator);
        //dialog.setComparator(new LanguageElementComparator());
        dialog.setTitle(RefactoringMessages.NewContainerWizardPage_ChooseSourceContainerDialog_title);
        dialog.setMessage(RefactoringMessages.NewContainerWizardPage_ChooseSourceContainerDialog_description);

        dialog.addFilter(filter);
        dialog.setInput(Workspace.getInstance());
        dialog.setInitialSelection(initElement);
        dialog.setHelpAvailable(false);

        if(dialog.open() == Window.OK) {
            Object element = dialog.getFirstResult();

            if(element instanceof Project) {
                Project project = (Project)element;
                return project;
            }
            return null;
        }
        return null;
    }
}
