package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ltk.internal.ui.refactoring.util.SWTUtil;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.processors.move.Z8MoveProcessor;

@SuppressWarnings("restriction")
public class ReorgMoveWizard extends RefactoringWizard {
    public ReorgMoveWizard(MoveRefactoring ref) {
        super(ref, DIALOG_BASED_USER_INTERFACE | computeHasPreviewPage(ref));
        if(isTextualMove(ref))
            setDefaultPageTitle(RefactoringMessages.ReorgMoveWizard_textual_move);
        else
            setDefaultPageTitle(RefactoringMessages.ReorgMoveWizard_3);
    }

    private static boolean isTextualMove(MoveRefactoring ref) {
        Z8MoveProcessor moveProcessor = (Z8MoveProcessor)ref.getAdapter(Z8MoveProcessor.class);
        return moveProcessor.isTextualMove();
    }

    private static int computeHasPreviewPage(MoveRefactoring refactoring) {
        Z8MoveProcessor processor = (Z8MoveProcessor)refactoring.getAdapter(Z8MoveProcessor.class);
        if(processor.canUpdateReferences())
            return NONE;
        return NO_PREVIEW_PAGE;
    }

    @Override
    protected void addUserInputPages() {
        addPage(new MoveInputPage());
    }

    private static class MoveInputPage extends ReorgUserInputPage {
        private static final String PAGE_NAME = "MoveInputPage";
        private Button fReferenceCheckbox;
        private ICreateTargetQuery fCreateTargetQuery;
        private Object fDestination;

        public MoveInputPage() {
            super(PAGE_NAME);
        }

        private Z8MoveProcessor getMoveProcessor() {
            return (Z8MoveProcessor)getRefactoring().getAdapter(Z8MoveProcessor.class);
        }

        @Override
        protected Object getInitiallySelectedElement() {
            return getMoveProcessor().getCommonParentForInputElements();
        }

        @Override
        protected ILanguageElement[] getElements() {
            return getMoveProcessor().getLanguageElements();
        }

        @Override
        protected IResource[] getResources() {
            return getMoveProcessor().getResources();
        }

        @Override
        protected IReorgDestinationValidator getDestinationValidator() {
            return getMoveProcessor();
        }

        @Override
        protected boolean performFinish() {
            return super.performFinish() || getMoveProcessor().wasCanceled();
        }

        @Override
        protected RefactoringStatus verifyDestination(Object selected) throws CoreException {
            Z8MoveProcessor processor = getMoveProcessor();

            RefactoringStatus refactoringStatus;

            if(selected instanceof ILanguageElement)
                refactoringStatus = processor.setDestination((ILanguageElement)selected);
            else if(selected instanceof IResource)
                refactoringStatus = processor.setDestination((IResource)selected);
            else
                refactoringStatus = RefactoringStatus.createFatalErrorStatus(RefactoringMessages.ReorgMoveWizard_4);

            updateUIStatus();
            fDestination = selected;

            return refactoringStatus;
        }

        private void updateUIStatus() {
            getRefactoringWizard().setForcePreviewReview(false);

            Z8MoveProcessor processor = getMoveProcessor();

            if(fReferenceCheckbox != null) {
                fReferenceCheckbox.setEnabled(canUpdateReferences());
                processor.setUpdateReferences(fReferenceCheckbox.getEnabled() && fReferenceCheckbox.getSelection());
            }
        }

        private void addUpdateReferenceComponent(Composite result) {
            final Z8MoveProcessor processor = getMoveProcessor();

            if(!processor.canUpdateReferences())
                return;

            fReferenceCheckbox = new Button(result, SWT.CHECK);
            fReferenceCheckbox.setText(RefactoringMessages.MoveAction_update_references);
            fReferenceCheckbox.setSelection(processor.getUpdateReferences());
            fReferenceCheckbox.setEnabled(canUpdateReferences());

            fReferenceCheckbox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    processor.setUpdateReferences(((Button)e.widget).getSelection());
                    updateUIStatus();
                }
            });
        }

        @Override
        public void createControl(Composite parent) {
            Composite result;

            boolean showDestinationTree = !getMoveProcessor().hasDestinationSet();

            if(showDestinationTree) {
                fCreateTargetQuery = getMoveProcessor().getCreateTargetQuery();
                super.createControl(parent);
                getTreeViewer().getTree().setFocus();
                result = (Composite)super.getControl();
            }
            else {
                initializeDialogUnits(parent);
                result = new Composite(parent, SWT.NONE);
                setControl(result);
                result.setLayout(new GridLayout());
                Dialog.applyDialogFont(result);
            }

            addUpdateReferenceComponent(result);
            setControl(result);
            Dialog.applyDialogFont(result);
        }

        @Override
        protected Control addLabel(Composite parent) {
            if(fCreateTargetQuery != null) {
                Composite firstLine = new Composite(parent, SWT.NONE);
                GridLayout layout = new GridLayout(2, false);
                layout.marginHeight = layout.marginWidth = 0;
                firstLine.setLayout(layout);
                firstLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                Control label = super.addLabel(firstLine);
                label.addTraverseListener(new TraverseListener() {
                    @Override
                    public void keyTraversed(TraverseEvent e) {
                        if(e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
                            e.detail = SWT.TRAVERSE_NONE;
                            getTreeViewer().getTree().setFocus();
                        }
                    }
                });
                Button newButton = new Button(firstLine, SWT.PUSH);
                newButton.setText(fCreateTargetQuery.getNewButtonLabel());
                GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL
                        | GridData.VERTICAL_ALIGN_BEGINNING);
                gd.widthHint = SWTUtil.getButtonWidthHint(newButton);
                newButton.setLayoutData(gd);
                newButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        doNewButtonPressed();
                    }
                });
                return firstLine;
            }
            else {
                return super.addLabel(parent);
            }
        }

        private boolean canUpdateReferences() {
            return getMoveProcessor().canUpdateReferences();
        }

        private void doNewButtonPressed() {
            Object newElement = fCreateTargetQuery.getCreatedTarget(fDestination);
            if(newElement != null) {
                TreeViewer viewer = getTreeViewer();
                ITreeContentProvider contentProvider = (ITreeContentProvider)viewer.getContentProvider();
                viewer.refresh(contentProvider.getParent(newElement));
                viewer.setSelection(new StructuredSelection(newElement), true);
                viewer.getTree().setFocus();
            }
        }
    }
}
