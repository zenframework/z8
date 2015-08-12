package org.zenframework.z8.pde;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.dialogs.ListDialog;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.type.ImportBlock;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.Workspace;

@SuppressWarnings("deprecation")
public class OrganizeAllImportsAction implements IWorkbenchWindowActionDelegate {

    protected static Image RESOURCE_IMAGE;

    static {
        try {
            RESOURCE_IMAGE = new Image((Device)null, Plugin.getDefault().openStream(new Path("icons/obj16/z8.gif")));
        }
        catch(Exception e) {
            Plugin.log(e);
        }
    }

    private IWorkbenchWindow m_window;

    @Override
    public void dispose() {}

    @Override
    public void init(IWorkbenchWindow window) {
        m_window = window;
    }

    @Override
    public void run(IAction action) {
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) {
                    try {
                        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                    }
                    catch(Exception e) {
                        Plugin.log(e);
                    }
                }

            });
        }
        catch(Exception e) {
            Plugin.log(e);
        }

        int count = 0;
        for(Project pr : Workspace.getInstance().getProjects())
            count += countUnits(pr);
        final int units = count;

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) {
                    monitor.beginTask("Organizing imports", units);
                    for(Project pr : Workspace.getInstance().getProjects())
                        organizeTree(pr, monitor);
                    monitor.subTask("Refreshing workspace");
                    RefreshAction raction = new RefreshAction(m_window);
                    raction.refreshAll();
                    monitor.done();
                }

            });
        }
        catch(Exception e) {
            Plugin.log(e);
        }

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) {
                    try {
                        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                    }
                    catch(Exception e) {
                        Plugin.log(e);
                    }
                }

            });
        }
        catch(Exception e) {
            Plugin.log(e);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {}

    private void organizeTree(Folder folder, IProgressMonitor monitor) {
        for(Resource r : folder.getMembers()) {
            if(r instanceof CompilationUnit) {
                CompilationUnit unit = (CompilationUnit)r;
                monitor.subTask("Organizing imports in " + unit.getName());
                organizeOne(unit);
                monitor.worked(1);
            }
            if(r instanceof Folder) {
                Folder fold = (Folder)r;
                organizeTree(fold, monitor);
            }
        }

    }

    private int countUnits(Folder folder) {
        int result = 0;
        for(Resource r : folder.getMembers()) {
            if(r instanceof CompilationUnit) {
                result++;
            }
            if(r instanceof Folder) {
                Folder fold = (Folder)r;
                result += countUnits(fold);
            }
        }
        return result;
    }

    private void organizeOne(CompilationUnit compilationUnit) {
        IFile file = (IFile)compilationUnit.getResource();
        IType type = compilationUnit.getReconciledType();
        if(type == null)
            return;
        ImportBlock importBlock = type.getImportBlock();

        String[] unresolvedTypes = compilationUnit.getUnresolvedTypes();

        String imports = "";

        for(String typeName : unresolvedTypes) {
            List<CompilationUnit> otherCompilationUnits = new ArrayList<CompilationUnit>();
            for(Project p : Workspace.getInstance().getProjects()) {
                CompilationUnit[] other = p.lookupCompilationUnits(typeName);
                if(other != null)
                    for(int i = 0; i < other.length; i++)
                        otherCompilationUnits.add(other[i]);
            }

            if(otherCompilationUnits.size() == 0)
                continue;

            String qualifiedName = null;// otherCompilationUnits[0].getQualifiedName();

            if(otherCompilationUnits.size() == 1) {
                qualifiedName = otherCompilationUnits.get(0).getQualifiedName();
            }
            else {
                ListDialog dialog = new ListDialog(m_window.getShell());
                dialog.setContentProvider(new IStructuredContentProvider() {
                    @Override
                    public Object[] getElements(Object inputElement) {
                        return (Object[])inputElement;
                    }

                    @Override
                    public void dispose() {}

                    @Override
                    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
                });
                dialog.setLabelProvider(new LabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return ((CompilationUnit)element).getQualifiedName();
                    }

                    @Override
                    public Image getImage(Object element) {
                        return RESOURCE_IMAGE;
                    }
                });
                dialog.setInput(otherCompilationUnits);
                dialog.setTitle("Организовать импорты");
                dialog.setMessage("Выберите подходящий import для " + compilationUnit.getName());
                dialog.setInitialSelections(new Object[] { otherCompilationUnits.get(0) });
                int open = dialog.open();
                if(Dialog.CANCEL == open)
                    continue;
                else {
                    Object[] result = dialog.getResult();
                    if(result.length != 1)
                        continue;
                    Object first = result[0];
                    if(first instanceof IType) {
                        IType typ = (IType)first;
                        qualifiedName = typ.getCompilationUnit().getQualifiedName();
                    }
                    if(first instanceof CompilationUnit) {
                        CompilationUnit unit = (CompilationUnit)first;
                        qualifiedName = unit.getQualifiedName();
                    }
                }
            }
            if(importBlock == null || importBlock.getImportedUnit(typeName) == null) {
                if(qualifiedName != null)
                    imports += (imports.length() > 0 ? "\r\n" : "") + "import " + qualifiedName + ";";
            }
        }
        if(importBlock != null) {
            List<String> qualifiedNames = importBlock.getResolvedNames();
            for(String name : qualifiedNames) {
                imports += (imports.length() > 0 ? "\r\n" : "") + "import " + name + ";";
            }
        }
        try {
            InputStream in = file.getContents();
            // String s = "";
            StringBuffer s = new StringBuffer();
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0) {
                s.append(new String(buf, 0, len, file.getCharset()));
                // s = s + new String(buf, 0, len, "CP1251");
            }
            in.close();

            if(importBlock == null) {
                s.replace(0, 0, imports + (imports.length() > 0 ? "\r\n" : ""));
            }
            else {
                s.replace(importBlock.getPosition().getOffset(), importBlock.getPosition().getLength()
                        + importBlock.getPosition().getOffset(), imports);
            }
            boolean readonly = file.isReadOnly();
            file.setReadOnly(false);
            file.setContents(new ByteArrayInputStream(s.toString().getBytes(file.getCharset())), true, true, null);
            file.setReadOnly(readonly);
        }
        catch(CoreException e) {
            Plugin.log(e);
        }
        catch(IOException e) {
            Plugin.log(e);
        }

    }

}
