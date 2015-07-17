package org.zenframework.z8.pde.refactoring.rename;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ISetSelectionTarget;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.UserInterfaceStarter;

public class RenameUserInterfaceStarter extends UserInterfaceStarter {
    private static class SelectionState {
        private Display fDisplay;
        private Object fElement;
        private List<IWorkbenchPart> fParts;
        private List<ISelection> fSelections;

        public SelectionState(Object element) {
            fElement = element;
            fParts = new ArrayList<IWorkbenchPart>();
            fSelections = new ArrayList<ISelection>();
            init();
        }

        private void init() {
            IWorkbenchWindow dw = Plugin.getActiveWorkbenchWindow();

            if(dw == null)
                return;

            fDisplay = dw.getShell().getDisplay();

            IWorkbenchPage page = dw.getActivePage();

            if(page == null)
                return;

            IViewReference vrefs[] = page.getViewReferences();

            for(int i = 0; i < vrefs.length; i++) {
                consider(vrefs[i].getPart(false));
            }

            IEditorReference refs[] = page.getEditorReferences();

            for(int i = 0; i < refs.length; i++) {
                consider(refs[i].getPart(false));
            }
        }

        private void consider(IWorkbenchPart part) {
            if(part == null)
                return;

            ISetSelectionTarget target = null;

            if(!(part instanceof ISetSelectionTarget)) {
                target = (ISetSelectionTarget)part.getAdapter(ISetSelectionTarget.class);
                if(target == null)
                    return;
            }
            else {
                target = (ISetSelectionTarget)part;
            }

            ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();

            if(selectionProvider == null)
                return;

            ISelection s = selectionProvider.getSelection();

            if(!(s instanceof IStructuredSelection))
                return;

            IStructuredSelection selection = (IStructuredSelection)s;

            if(!selection.toList().contains(fElement))
                return;

            fParts.add(part);
            fSelections.add(selection);
        }

        public void restore(Object newElement) {
            if(fDisplay == null)
                return;

            for(int i = 0; i < fParts.size(); i++) {
                boolean changed = false;
                IStructuredSelection currentSelection = (IStructuredSelection)fSelections.get(i);

                final ISetSelectionTarget target = (ISetSelectionTarget)fParts.get(i);
                final IStructuredSelection[] newSelection = new IStructuredSelection[1];

                newSelection[0] = currentSelection;

                if(currentSelection instanceof TreeSelection) {
                    TreeSelection treeSelection = (TreeSelection)currentSelection;
                    TreePath[] paths = treeSelection.getPaths();

                    for(int p = 0; p < paths.length; p++) {
                        TreePath path = paths[p];
                        if(path.getSegmentCount() > 0 && path.getLastSegment().equals(fElement)) {
                            paths[p] = createTreePath(path, newElement);
                            changed = true;
                        }
                    }
                    if(changed) {
                        newSelection[0] = new TreeSelection(paths, treeSelection.getElementComparer());
                    }
                }
                else {
                    Object[] elements = currentSelection.toArray();
                    for(int e = 0; e < elements.length; e++) {
                        if(elements[e].equals(fElement)) {
                            elements[e] = newElement;
                            changed = true;
                        }
                    }
                    if(changed) {
                        newSelection[0] = new StructuredSelection(elements);
                    }
                }
                if(changed) {
                    fDisplay.asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            target.selectReveal(newSelection[0]);
                        }
                    });
                }
            }
        }

        private TreePath createTreePath(TreePath old, Object newElement) {
            int count = old.getSegmentCount();

            Object[] newObjects = new Object[count];

            for(int i = 0; i < count - 1; i++) {
                newObjects[i] = old.getSegment(i);
            }

            newObjects[count - 1] = newElement;

            return new TreePath(newObjects);
        }
    }

    @Override
    public void activate(Refactoring refactoring, Shell parent, boolean save) throws CoreException {
        RenameProcessor processor = (RenameProcessor)refactoring.getAdapter(RenameProcessor.class);

        Object[] elements = processor.getElements();

        SelectionState state = elements.length == 1 ? new SelectionState(elements[0]) : null;

        super.activate(refactoring, parent, save);

        INameUpdating nameUpdating = (INameUpdating)refactoring.getAdapter(INameUpdating.class);

        if(nameUpdating != null && state != null) {
            Object newElement = nameUpdating.getNewElement();

            if(newElement != null) {
                state.restore(newElement);
            }
        }
    }
}
