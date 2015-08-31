package org.zenframework.z8.compiler.workspace;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class Folder extends Resource {
    public Folder(Resource parent, IResource resource) {
        super(parent, resource);

        if(parent != null) {
            parent.addMember(this);
        }
    }

    public boolean iterate(ResourceVisitor visitor) {
        Resource[] members = getMembers();

        for(Resource resource : members) {
            if(resource instanceof Folder) {
                Folder folder = (Folder)resource;

                if(!folder.iterate(visitor)) {
                    return false;
                }
            }
            else if(resource instanceof CompilationUnit) {
                if(!visitor.visit((CompilationUnit)resource)) {
                    return false;
                }
            }
            else if(resource instanceof NlsUnit) {
                if(!visitor.visit((NlsUnit)resource)) {
                    return false;
                }
            }
        }

        return true;
    }

    public Folder getFolder(IResource resource) {
        return (Folder)getWorkspace().getResource(resource);
    }

    public Folder getFolder(IPath path) {
        Resource resource = getMember(path);

        if(resource instanceof Folder) {
            return (Folder)resource;
        }
        return null;
    }

    public Folder getFolder(String name) {
        return getFolder(new Path(name));
    }

    public Folder[] getFolders() {
        Resource[] members = getMembers();

        List<Folder> result = new ArrayList<Folder>();

        for(Resource member : members) {
            if(member instanceof Folder) {
                result.add((Folder)member);
            }
        }

        return result.toArray(new Folder[result.size()]);
    }

    public CompilationUnit[] getCompilationUnits() {
        Resource[] members = getMembers();

        List<CompilationUnit> result = new ArrayList<CompilationUnit>();

        for(Resource member : members) {
            if(member instanceof CompilationUnit) {
                result.add((CompilationUnit)member);
            }
        }

        return result.toArray(new CompilationUnit[result.size()]);
    }

    public CompilationUnit getCompilationUnit(IResource resource) {
        return getWorkspace().getCompilationUnit(resource);
    }

    public NlsUnit getNLSUnit(IResource resource) {
        return getWorkspace().getNLSUnit(resource);
    }

    public CompilationUnit getCompilationUnit(IPath path) {
        Resource resource = getMember(path);

        if(resource instanceof CompilationUnit) {
            return (CompilationUnit)resource;
        }
        return null;
    }

    public CompilationUnit getCompilationUnit(String name) {
        return getCompilationUnit(new Path(name));
    }

    public boolean hasSubfolders() {
        return getFolders().length != 0;
    }

    public boolean hasCompilationUnits() {
        return getCompilationUnits().length != 0;
    }

    @Override
    public boolean containsError() {
        final boolean[] result = new boolean[] { false };

        iterate(new ResourceVisitor() {
            @Override
            public boolean visit(CompilationUnit compilationUnit) {
                if(compilationUnit.containsError()) {
                    result[0] = true;
                    return false;
                }
                return true;
            }
        });

        return result[0];
    }

    @Override
    public boolean containsWarning() {
        final boolean[] result = new boolean[] { false };

        iterate(new ResourceVisitor() {
            @Override
            public boolean visit(CompilationUnit compilationUnit) {
                if(compilationUnit.containsWarning()) {
                    result[0] = true;
                    return false;
                }
                return true;
            }
        });

        return result[0];
    }

    public Folder createFolder(IResource resource) {
        Folder folder = getFolder(resource);

        if(folder == null) {
            Folder container = getFolder(resource.getParent());
            folder = new Folder(container, resource);
        }

        return folder;
    }

    public void removeFolder(IResource resource) {
        Folder folder = getFolder(resource);

        ResourceVisitor visitor = new ResourceVisitor() {
            @Override
            public boolean visit(CompilationUnit compilationUnit) {
                compilationUnit.contentChanged();
                compilationUnit.updateDependencies();
                return true;
            }

            @Override
            public boolean visit(NlsUnit nlsUnit) {
                nlsUnit.contentChanged();
                nlsUnit.updateDependencies();
                return true;
            }
        };

        if(folder != null) {
            folder.iterate(visitor);
            folder.getContainer().removeMember(folder);
        }
    }

    public CompilationUnit createCompilationUnit(IResource resource) {
        CompilationUnit compilationUnit = getCompilationUnit(resource);

        if(compilationUnit == null) {
            Folder container = getFolder(resource.getParent());
            return new CompilationUnit(container, resource);
        }

        return compilationUnit;
    }

    public NlsUnit createNLSUnit(IResource resource) {
        NlsUnit nlsUnit = getNLSUnit(resource);

        if(nlsUnit == null) {
            Folder container = getFolder(resource.getParent());
            return new NlsUnit(container, resource);
        }

        return nlsUnit;
    }

    public void removeCompilationUnit(IResource resource) {
        CompilationUnit compilationUnit = getCompilationUnit(resource);
        if(compilationUnit != null) {
            compilationUnit.contentChanged();
            compilationUnit.updateDependencies();
            compilationUnit.getContainer().removeMember(compilationUnit);
        }
    }

    public void removeNLSUnit(IResource resource) {
        NlsUnit nlsUnit = getNLSUnit(resource);
        if(nlsUnit != null) {
            nlsUnit.contentChanged();
            nlsUnit.updateDependencies();
            nlsUnit.getContainer().removeMember(nlsUnit);
        }
    }

    public void updateCompilationUnit(IResource resource) {
        CompilationUnit compilationUnit = getCompilationUnit(resource);
        compilationUnit.contentChanged();
    }

    public void updateNLSUnit(IResource resource) {
        NlsUnit nlsUnit = getNLSUnit(resource);
        nlsUnit.contentChanged();
    }
}
