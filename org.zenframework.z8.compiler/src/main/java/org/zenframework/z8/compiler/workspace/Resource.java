package org.zenframework.z8.compiler.workspace;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.error.BuildError;
import org.zenframework.z8.compiler.error.BuildMessage;
import org.zenframework.z8.compiler.error.BuildWarning;
import org.zenframework.z8.compiler.error.IBuildMessageConsumer;
import org.zenframework.z8.compiler.parser.LanguageElement;
import org.zenframework.z8.compiler.util.Set;

public class Resource extends LanguageElement {
    static final String FileExtension = "bl";
    static final String NlsFileExtension = "nls";

    private Project project;
    private Resource container;
    private List<Resource> members;
    private IResource resource;

    private boolean hasError = false;
    private boolean hasWarning = false;
    private boolean hasParseError = false;

    private Set<ResourceListener> listeners;

    private List<BuildMessage> messages;

    static public boolean isBLResource(IResource resource) {
        // TODO Сделать поддержку папок с исходниками
        return FileExtension.equals(resource.getProjectRelativePath().getFileExtension());
    }

    static public boolean isNLSResource(IResource resource) {
        return NlsFileExtension.equals(resource.getProjectRelativePath().getFileExtension());
    }

    protected Resource(Resource container, IResource resource) {
        this.container = container;
        this.resource = resource;
        setParent(container);
    }

    public IResource getResource() {
        return resource;
    }

    public String getName() {
        return resource.getName();
    }

    public IPath getPath() {
        return resource.getProjectRelativePath();
    }

    public IPath getAbsolutePath() {
        return resource.getLocation();
    }

    public Resource getContainer() {
        return container;
    }

    public boolean isDescendantOf(Resource resource) {
        Resource container = getContainer();

        while(container != null) {
            if(container == resource)
                return true;

            container = container.getContainer();
        }

        return false;
    }

    protected void addMember(Resource member) {
        if(members == null) {
            members = new ArrayList<Resource>();
        }

        members.add(member);
        getWorkspace().addResource(member);

        member.fireResourceEvent(ResourceListener.RESOURCE_ADDED, member, null);
    }

    protected void removeMember(Resource member) {
        assert (members != null && member.getContainer() == this);

        member.removeAllMembers();

        member.fireResourceEvent(ResourceListener.RESOURCE_REMOVED, member, null);

        members.remove(member);
        getWorkspace().removeResource(member);

    }

    protected void removeAllMembers() {

        for(Resource member : getMembers()) {
            removeMember(member);
        }

        members = null;
    }

    public Resource[] getMembers() {
        if(members == null) {
            return new Resource[0];
        }
        return members.toArray(new Resource[members.size()]);
    }

    public Resource getMember(IPath path) {
        Resource resource = this;

        for(int segment = 0; segment < path.segmentCount(); segment++) {
            resource = resource.getMember(path.segment(segment));

            if(resource == null)
                return null;
        }
        return resource;
    }

    public Resource getMember(String name) {
        if(members != null) {
            for(Resource member : members) {
                if(name.equals(member.getName())) {
                    return member;
                }
            }
        }
        return null;
    }

    public Folder getFolder() {
        return (Folder)getContainer();
    }

    @Override
    public Project getProject() {
        if(project == null) {
            Resource container = this;

            while(container != null) {
                if(container instanceof Project) {
                    project = (Project)container;
                    return project;
                }
                container = container.getContainer();
            }

            assert (false);
            return null;
        }

        return project;
    }

    public Workspace getWorkspace() {
        return Workspace.getInstance();
    }

    public void installResourceListener(ResourceListener listener) {
        if(listeners == null) {
            listeners = new Set<ResourceListener>();
        }

        listeners.add(listener);
    }

    public void uninstallResourceListener(ResourceListener listener) {
        if(listeners != null) {
            listeners.remove(listener);

            if(listeners.size() == 0) {
                listeners = null;
            }
        }
    }

    protected ResourceListener[] getResourceListeners() {
        if(listeners != null) {
            return listeners.toArray(new ResourceListener[listeners.size()]);
        }

        return new ResourceListener[0];
    }

    protected void fireResourceEvent(int kind, Resource resource, Object object) {
        ResourceListener[] listeners = getResourceListeners();

        for(ResourceListener listener : listeners) {
            listener.event(kind, resource, object);
        }

        Resource container = getContainer();

        if(container != null) {
            container.fireResourceEvent(kind, resource, object);
        }
    }

    public BuildMessage[] getMessages() {
        if(messages == null) {
            return new BuildMessage[0];
        }
        return messages.toArray(new BuildMessage[messages.size()]);
    }

    protected void clearMessages() {
        hasError = false;
        hasWarning = false;
        hasParseError = false;
        messages = null;
    }

    protected void message(BuildMessage message) {
        if(messages == null) {
            messages = new ArrayList<BuildMessage>();
        }
        messages.add(message);
    }

    public void reportMessages() {
        IBuildMessageConsumer consumer = getProject().getMessageConsumer();

        if(consumer != null) {
            consumer.report(this, getMessages());
        }
    }

    public boolean containsError() {
        return hasError;
    }

    public boolean containsWarning() {
        return hasWarning;
    }

    public boolean containsParseError() {
        return hasParseError;
    }

    public void error(Throwable throwable) {
        error(null, throwable.getMessage(), throwable);
    }

    public void parseError(Throwable throwable) {
        hasParseError = true;
        error(null, throwable.getMessage(), throwable);
    }

    public void error(String message) {
        error(null, message, null);
    }

    public void parseError(IPosition position, String message) {
        hasParseError = true;
        error(position, message, null);
    }

    public void error(IPosition position, String message) {
        error(position, message, null);
    }

    public void error(IPosition position, String message, Throwable throwable) {
        hasError = true;
        message(new BuildError(getResource(), position, message, throwable));
    }

    public void warning(String message) {
        warning(null, message);
    }

    public void warning(IPosition position, String message) {
        hasWarning = true;
        message(new BuildWarning(getResource(), position, message));
    }
}
