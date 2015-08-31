package org.zenframework.z8.pde.refactoring.reorg;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CreateTargetExecutionLog {
    private Map<Object, Object> fCreations = new LinkedHashMap<Object, Object>(2);

    public Object getCreatedElement(Object selection) {
        return fCreations.get(selection);
    }

    public Object[] getCreatedElements() {
        return fCreations.values().toArray();
    }

    public Object[] getSelectedElements() {
        return fCreations.keySet().toArray();
    }

    public void markAsCreated(Object selection, Object element) {
        fCreations.put(selection, element);
    }
}