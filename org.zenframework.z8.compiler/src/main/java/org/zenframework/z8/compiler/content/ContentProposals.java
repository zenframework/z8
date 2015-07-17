package org.zenframework.z8.compiler.content;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

import org.zenframework.z8.compiler.core.IVariableType;

public class ContentProposals {
    private Map<String, Map<Integer, IVariableType>> files;

    public ContentProposals() {}

    public IVariableType get(IPath path, int offset) {
        if(files == null) {
            return null;
        }

        Map<Integer, IVariableType> proposals = files.get(path.toString());

        if(proposals == null) {
            return null;
        }

        return proposals.get(offset);
    }

    public void add(IPath path, int offset, IVariableType type) {
        if(files == null) {
            files = new HashMap<String, Map<Integer, IVariableType>>();
        }

        Map<Integer, IVariableType> proposals = files.get(path.toString());

        if(proposals == null) {
            proposals = new HashMap<Integer, IVariableType>();
            files.put(path.toString(), proposals);
        }

        proposals.put(offset, type);
    }

    public void remove(IPath path) {
        if(files != null) {
            files.remove(path.toString());
        }
    }
}
