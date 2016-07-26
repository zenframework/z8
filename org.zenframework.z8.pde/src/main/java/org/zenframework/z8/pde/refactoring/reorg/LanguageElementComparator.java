package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.type.ImportBlock;
import org.zenframework.z8.compiler.parser.type.ImportElement;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.Resource;

public class LanguageElementComparator extends ViewerComparator {
    private static final int PROJECTS = 1;
    private static final int FOLDER = 3;
    private static final int COMPILATIONUNITS = 4;
    private static final int RESOURCEFOLDERS = 7;
    private static final int RESOURCES = 8;
    private static final int IMPORT_CONTAINER = 11;
    private static final int IMPORT_DECLARATION = 12;

    private static final int LANGUAGE_ELEMENTS = 50;
    private static final int OTHERS = 51;

    public static final int TYPE_INDEX = 0;
    public static final int CONSTRUCTORS_INDEX = 1;
    public static final int METHOD_INDEX = 2;
    public static final int FIELDS_INDEX = 3;
    public static final int INIT_INDEX = 4;
    public static final int STATIC_FIELDS_INDEX = 5;
    public static final int STATIC_INIT_INDEX = 6;
    public static final int STATIC_METHODS_INDEX = 7;
    public static final int ENUM_CONSTANTS_INDEX = 8;
    public static final int N_CATEGORIES = ENUM_CONSTANTS_INDEX + 1;

    public LanguageElementComparator() {
        super(null); // delay initialization of collator
    }

    @Override
    public int category(Object object) {
        if(object instanceof ILanguageElement) {
            if(object instanceof IMethod) {
                IMethod method = (IMethod)object;
                return method.isStatic() ? STATIC_METHODS_INDEX : METHOD_INDEX;
            }
            else if(object instanceof IMember) {
                IMember member = (IMember)object;

                if(member.getVariableType().isEnum()) {
                    return ENUM_CONSTANTS_INDEX;
                }

                return member.isStatic() ? STATIC_FIELDS_INDEX : FIELDS_INDEX;
            }
            else if(object instanceof IType) {
                return TYPE_INDEX;
            }
            else if(object instanceof ImportBlock) {
                return IMPORT_CONTAINER;
            }
            else if(object instanceof ImportElement) {
                return IMPORT_DECLARATION;
            }
            else if(object instanceof Project) {
                return PROJECTS;
            }
            else if(object instanceof Folder) {
                return FOLDER;
            }
            else if(object instanceof CompilationUnit) {
                return COMPILATIONUNITS;
            }
            return LANGUAGE_ELEMENTS;
        }
        else if(object instanceof IFile) {
            return RESOURCES;
        }
        else if(object instanceof IProject) {
            return PROJECTS;
        }
        else if(object instanceof IContainer) {
            return RESOURCEFOLDERS;
        }
        return OTHERS;
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int cat1 = category(e1);
        int cat2 = category(e2);

        if(cat1 != cat2)
            return cat1 - cat2;

        if(cat1 == PROJECTS || cat1 == RESOURCES || cat1 == RESOURCEFOLDERS || cat1 == OTHERS) {
            String name1 = getNonJavaElementLabel(viewer, e1);
            String name2 = getNonJavaElementLabel(viewer, e2);

            if(name1 != null && name2 != null) {
                return getComparator().compare(name1, name2);
            }
            return 0;
        }

        if(e1 instanceof IMember) {
            IMember member1 = (IMember)e1;
            IMember member2 = (IMember)e2;

            if(member1.isPublic()) {
                return member2.isPublic() ? 0 : -1;
            }

            if(member1.isProtected()) {
                if(member2.isPublic())
                    return 1;

                if(member2.isPrivate())
                    return -1;

                return 0;
            }

            if(member1.isPrivate()) {
                return member2.isPrivate() ? 0 : 1;
            }
        }

        return getComparator().compare(getName(e1), getName(e2));
    }

    private String getNonJavaElementLabel(Viewer viewer, Object element) {
        if(element instanceof IResource) {
            return ((IResource)element).getName();
        }
        if(element instanceof IStorage) {
            return ((IStorage)element).getName();
        }
        if(element instanceof IAdaptable) {
            IWorkbenchAdapter adapter = (IWorkbenchAdapter)((IAdaptable)element).getAdapter(IWorkbenchAdapter.class);
            if(adapter != null) {
                return adapter.getLabel(element);
            }
        }
        if(viewer instanceof ContentViewer) {
            IBaseLabelProvider prov = ((ContentViewer)viewer).getLabelProvider();
            if(prov instanceof ILabelProvider) {
                return ((ILabelProvider)prov).getText(element);
            }
        }
        return null;
    }

    private String getName(Object object) {
        if(object instanceof IMethod) {
            IMethod method = (IMethod)object;
            return method.getName();
        }
        else if(object instanceof IMember) {
            IMember member = (IMember)object;
            return member.getName();
        }
        else if(object instanceof IType) {
            IType type = (IType)object;
            return type.getUserName();
        }
        else if(object instanceof Resource) {
            Resource resource = (Resource)object;
            return resource.getName();
        }
        return object.toString();
    }
}
