package org.zenframework.z8.pde.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class Resources {
    private Resources() {}

    public static IStatus checkInSync(IResource resource) {
        return checkInSync(new IResource[] { resource });
    }

    public static IStatus checkInSync(IResource[] resources) {
        IStatus result = null;

        for(int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            if(!resource.isSynchronized(IResource.DEPTH_INFINITE)) {
                result = addOutOfSync(result, resource);
            }
        }
        if(result != null)
            return result;

        return new Status(IStatus.OK, Plugin.PLUGIN_ID, IStatus.OK, "", null);
    }

    public static IStatus makeCommittable(IResource resource, Object context) {
        return makeCommittable(new IResource[] { resource }, context);
    }

    public static IStatus makeCommittable(IResource[] resources, Object context) {
        List<IFile> readOnlyFiles = new ArrayList<IFile>();

        for(int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            if(resource.getType() == IResource.FILE && isReadOnly(resource))
                readOnlyFiles.add((IFile)resource);
        }

        if(readOnlyFiles.size() == 0)
            return new Status(IStatus.OK, Plugin.PLUGIN_ID, IStatus.OK, "", null);

        Map<IFile, Long> oldTimeStamps = createModificationStampMap(readOnlyFiles);

        IStatus status = ResourcesPlugin.getWorkspace().validateEdit(
                (IFile[])readOnlyFiles.toArray(new IFile[readOnlyFiles.size()]), context);

        if(!status.isOK())
            return status;

        IStatus modified = null;

        Map<IFile, Long> newTimeStamps = createModificationStampMap(readOnlyFiles);

        for(IFile file : oldTimeStamps.keySet()) {
            if(!oldTimeStamps.get(file).equals(newTimeStamps.get(file)))
                modified = addModified(modified, file);
        }

        if(modified != null)
            return modified;

        return new Status(IStatus.OK, Plugin.PLUGIN_ID, IStatus.OK, "", null);
    }

    private static Map<IFile, Long> createModificationStampMap(List<IFile> files) {
        Map<IFile, Long> map = new HashMap<IFile, Long>();

        for(IFile file : files) {
            map.put(file, new Long(file.getModificationStamp()));
        }
        return map;
    }

    private static IStatus addModified(IStatus status, IFile file) {
        IStatus entry = new Status(IStatus.ERROR, Plugin.PLUGIN_ID, IStatus.ERROR, Messages.format(
                RefactoringMessages.Resources_fileModified, file.getFullPath().toString()), null);

        if(status == null) {
            return entry;
        }
        else if(status.isMultiStatus()) {
            ((MultiStatus)status).add(entry);
            return status;
        }
        else {
            MultiStatus result = new MultiStatus(Plugin.PLUGIN_ID, IStatus.ERROR,
                    RefactoringMessages.Resources_modifiedResources, null);
            result.add(status);
            result.add(entry);
            return result;
        }
    }

    private static IStatus addOutOfSync(IStatus status, IResource resource) {
        IStatus entry = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IResourceStatus.OUT_OF_SYNC_LOCAL,
                Messages.format(RefactoringMessages.Resources_outOfSync, resource.getFullPath().toString()), null);
        if(status == null) {
            return entry;
        }
        else if(status.isMultiStatus()) {
            ((MultiStatus)status).add(entry);
            return status;
        }
        else {
            MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.OUT_OF_SYNC_LOCAL,
                    RefactoringMessages.Resources_outOfSyncResources, null);
            result.add(status);
            result.add(entry);
            return result;
        }
    }

    public static String[] getLocationOSStrings(IResource[] resources) {
        List<String> result = new ArrayList<String>(resources.length);

        for(int i = 0; i < resources.length; i++) {
            IPath location = resources[i].getLocation();
            if(location != null)
                result.add(location.toOSString());
        }
        return result.toArray(new String[result.size()]);
    }

    public static boolean isReadOnly(IResource resource) {
        ResourceAttributes resourceAttributes = resource.getResourceAttributes();

        if(resourceAttributes == null)
            return false;

        return resourceAttributes.isReadOnly();
    }

    static void setReadOnly(IResource resource, boolean readOnly) {
        ResourceAttributes resourceAttributes = resource.getResourceAttributes();

        if(resourceAttributes == null)
            return;

        resourceAttributes.setReadOnly(readOnly);

        try {
            resource.setResourceAttributes(resourceAttributes);
        }
        catch(CoreException e) {
            Plugin.log(e);
        }
    }
}
