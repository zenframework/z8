package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.LanguageElementLabelProvider;
import org.zenframework.z8.pde.refactoring.messages.LanguageElementLabels;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

abstract class ReorgUserInputPage extends UserInputWizardPage {
    private static final long LABEL_FLAGS = LanguageElementLabels.ALL_DEFAULT | LanguageElementLabels.M_PRE_RETURNTYPE
            | LanguageElementLabels.M_PARAMETER_NAMES | LanguageElementLabels.F_PRE_TYPE_SIGNATURE;

    private TreeViewer fViewer;

    public ReorgUserInputPage(String pageName) {
        super(pageName);
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite result = new Composite(parent, SWT.NONE);
        setControl(result);
        result.setLayout(new GridLayout());
        Object initialSelection = getInitiallySelectedElement();
        verifyDestination(initialSelection, true);
        addLabel(result);
        fViewer = createViewer(result);
        fViewer.setSelection(new StructuredSelection(initialSelection), true);
        fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ReorgUserInputPage.this.viewerSelectionChanged(event);
            }
        });
        Dialog.applyDialogFont(result);
    }

    protected Control addLabel(Composite parent) {
        Label label = new Label(parent, SWT.WRAP);
        String text;

        int resources = getResources().length;
        int javaElements = getElements().length;

        if(resources == 0 && javaElements == 1) {
            text = Messages.format(RefactoringMessages.ReorgUserInputPage_choose_destination_single,
                    LanguageElementLabels.getElementLabel(getElements()[0], LABEL_FLAGS));
        }
        else if(resources == 1 && javaElements == 0) {
            text = Messages.format(RefactoringMessages.ReorgUserInputPage_choose_destination_single,
                    getResources()[0].getName());
        }
        else {
            text = Messages.format(RefactoringMessages.ReorgUserInputPage_choose_destination_multi,
                    String.valueOf(resources + javaElements));
        }

        label.setText(text);

        GridData data = new GridData(SWT.FILL, SWT.END, true, false);
        data.widthHint = convertWidthInCharsToPixels(50);
        label.setLayoutData(data);

        return label;
    }

    private void viewerSelectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if(!(selection instanceof IStructuredSelection))
            return;
        IStructuredSelection ss = (IStructuredSelection)selection;
        verifyDestination(ss.getFirstElement(), false);
    }

    protected abstract Object getInitiallySelectedElement();

    protected abstract RefactoringStatus verifyDestination(Object selected) throws CoreException;

    protected abstract IResource[] getResources();

    protected abstract ILanguageElement[] getElements();

    protected abstract IReorgDestinationValidator getDestinationValidator();

    private final void verifyDestination(Object selected, boolean initialVerification) {
        try {
            RefactoringStatus status = verifyDestination(selected);

            if(initialVerification) {
                setPageComplete(status.isOK());
            }
            else {
                setPageComplete(status);
            }
        }
        catch(CoreException e) {
            Plugin.log(e);
            setPageComplete(false);
        }
    }

    private TreeViewer createViewer(Composite parent) {
        TreeViewer treeViewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = convertWidthInCharsToPixels(40);
        gd.heightHint = convertHeightInCharsToPixels(15);
        treeViewer.getTree().setLayoutData(gd);
        treeViewer.setLabelProvider(new LanguageElementLabelProvider(LanguageElementLabelProvider.SHOW_SMALL_ICONS));
        treeViewer.setContentProvider(new DestinationContentProvider(getDestinationValidator()));
        //treeViewer.setComparator(new LanguageElementComparator());
        treeViewer.setInput(Workspace.getInstance());
        return treeViewer;
    }

    protected TreeViewer getTreeViewer() {
        return fViewer;
    }
}
