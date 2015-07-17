package org.zenframework.z8.pde.refactoring.reorg;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ltk.internal.ui.refactoring.WorkbenchRunnableAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.ExceptionHandler;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

@SuppressWarnings("restriction")
public abstract class NewElementWizard extends Wizard implements INewWizard {
    private IWorkbench fWorkbench;
    private IStructuredSelection fSelection;

    public NewElementWizard() {
        setNeedsProgressMonitor(true);
    }

    protected void openResource(final IFile resource) {
        final IWorkbenchPage activePage = Plugin.getActivePage();

        if(activePage != null) {
            final Display display = getShell().getDisplay();

            if(display != null) {
                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IDE.openEditor(activePage, resource, true);
                        }
                        catch(PartInitException e) {
                            Plugin.log(e);
                        }
                    }
                });
            }
        }
    }

    protected abstract void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException;

    protected ISchedulingRule getSchedulingRule() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    protected boolean canRunForked() {
        return true;
    }

    public abstract ILanguageElement getCreatedElement();

    protected void handleFinishException(Shell shell, InvocationTargetException e) {
        String title = RefactoringMessages.NewElementWizard_op_error_title;
        String message = RefactoringMessages.NewElementWizard_op_error_message;
        ExceptionHandler.handle(e, shell, title, message);
    }

    @Override
    public boolean performFinish() {
        IWorkspaceRunnable op = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
                try {
                    finishPage(monitor);
                }
                catch(InterruptedException e) {
                    throw new OperationCanceledException(e.getMessage());
                }
            }
        };
        try {
            ISchedulingRule rule = null;
            Job job = Job.getJobManager().currentJob();
            if(job != null)
                rule = job.getRule();
            IRunnableWithProgress runnable = null;
            if(rule != null)
                runnable = new WorkbenchRunnableAdapter(op, rule, true);
            else
                runnable = new WorkbenchRunnableAdapter(op, getSchedulingRule());
            getContainer().run(canRunForked(), true, runnable);
        }
        catch(InvocationTargetException e) {
            handleFinishException(getShell(), e);
            return false;
        }
        catch(InterruptedException e) {
            return false;
        }
        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        fWorkbench = workbench;
        fSelection = currentSelection;
    }

    public IStructuredSelection getSelection() {
        return fSelection;
    }

    public IWorkbench getWorkbench() {
        return fWorkbench;
    }

    protected void selectAndReveal(IResource newResource) {
        BasicNewResourceWizard.selectAndReveal(newResource, fWorkbench.getActiveWorkbenchWindow());
    }
}
