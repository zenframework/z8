package org.zenframework.z8.pde.refactoring.action;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;

import org.zenframework.z8.compiler.util.Set;

public class WorkspaceTracker {
    public final static WorkspaceTracker INSTANCE = new WorkspaceTracker();

    public interface Listener {
        public void workspaceChanged();
    }

    private class ResourceListener implements IResourceChangeListener {
        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            workspaceChanged();
        }
    }

    private Set<Listener> m_listeners;
    private ResourceListener m_resourceListener;

    private WorkspaceTracker() {
        m_listeners = new Set<Listener>();
    }

    private void workspaceChanged() {
        Object[] listeners = m_listeners.toArray(new Listener[0]);
        for(int i = 0; i < listeners.length; i++) {
            ((Listener)listeners[i]).workspaceChanged();
        }
    }

    public void addListener(Listener l) {
        m_listeners.add(l);

        if(m_resourceListener == null) {
            m_resourceListener = new ResourceListener();
            ResourcesPlugin.getWorkspace().addResourceChangeListener(m_resourceListener);
        }
    }

    public void removeListener(Listener l) {
        if(m_listeners.size() == 0)
            return;
        m_listeners.remove(l);
        if(m_listeners.size() == 0) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_resourceListener);
            m_resourceListener = null;
        }
    }
}
