package org.zenframework.z8.pde.refactoring;

import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class Z8Status extends Status implements IStatus, Z8StatusConstants, IResourceStatus {
    protected ILanguageElement[] elements = new ILanguageElement[0];

    protected IPath path;
    protected String string;
    protected final static IStatus[] NO_CHILDREN = new IStatus[] {};
    protected final static ILanguageElement[] NO_ELEMENTS = new ILanguageElement[] {};

    protected IStatus[] children = NO_CHILDREN;

    public static final IStatus VERIFIED_OK = new Z8Status(OK, OK, RefactoringMessages.status_OK);

    public Z8Status() {
        super(ERROR, Plugin.PLUGIN_ID, 0, "JavaModelStatus", null);
    }

    public Z8Status(int code) {
        super(ERROR, Plugin.PLUGIN_ID, code, "JavaModelStatus", null);
        this.elements = NO_ELEMENTS;
    }

    public Z8Status(int code, ILanguageElement[] elements) {
        super(ERROR, Plugin.PLUGIN_ID, code, "Z8Status", null);
        this.elements = elements;
        this.path = null;
    }

    public Z8Status(int code, String string) {
        this(ERROR, code, string);
    }

    public Z8Status(int severity, int code, String string) {
        super(severity, Plugin.PLUGIN_ID, code, "Z8Status", null);
        this.elements = NO_ELEMENTS;
        this.path = null;
        this.string = string;
    }

    public Z8Status(int code, Throwable throwable) {
        super(ERROR, Plugin.PLUGIN_ID, code, "Z8Status", throwable);
        this.elements = NO_ELEMENTS;
    }

    public Z8Status(int code, IPath path) {
        super(ERROR, Plugin.PLUGIN_ID, code, "Z8Status", null);
        this.elements = NO_ELEMENTS;
        this.path = path;
    }

    public Z8Status(int code, ILanguageElement element) {
        this(code, new ILanguageElement[] { element });
    }

    public Z8Status(int code, ILanguageElement element, String string) {
        this(code, new ILanguageElement[] { element });
        this.string = string;
    }

    public Z8Status(int code, ILanguageElement element, IPath path) {
        this(code, new ILanguageElement[] { element });
        this.path = path;
    }

    public Z8Status(int code, ILanguageElement element, IPath path, String string) {
        this(code, new ILanguageElement[] { element });
        this.path = path;
        this.string = string;
    }

    public Z8Status(CoreException coreException) {
        super(ERROR, Plugin.PLUGIN_ID, CORE_EXCEPTION, "Z8Status", coreException);
        elements = NO_ELEMENTS;
    }

    protected int getBits() {
        int severity = 1 << (getCode() % 100 / 33);
        int category = 1 << ((getCode() / 100) + 3);
        return severity | category;
    }

    @Override
    public IStatus[] getChildren() {
        return children;
    }

    public ILanguageElement[] getElements() {
        return elements;
    }

    @Override
    public String getMessage() {
        Throwable exception = getException();
        if(exception == null) {
            switch(getCode()) {
            case CORE_EXCEPTION:
                return RefactoringMessages.status_coreException;
            case DEVICE_PATH:
                return Messages.format(RefactoringMessages.status_cannotUseDeviceOnPath, getPath().toString());
            case ELEMENT_DOES_NOT_EXIST:
                return Messages
                        .format(RefactoringMessages.element_doesNotExist, ""/*((JavaElement)elements[0]).toStringWithAncestors()*/);
            case INDEX_OUT_OF_BOUNDS:
                return RefactoringMessages.status_indexOutOfBounds;
            case INVALID_CONTENTS:
                return RefactoringMessages.status_invalidContents;
            case INVALID_DESTINATION:
                return Messages
                        .format(RefactoringMessages.status_invalidDestination, ""/*((JavaElement)elements[0]).toStringWithAncestors()*/);
            case INVALID_ELEMENT_TYPES:
                StringBuffer buff = new StringBuffer(RefactoringMessages.operation_notSupported);
                for(int i = 0; i < elements.length; i++) {
                    //					if(i > 0)
                    //					{
                    //						buff.append(", ");
                    //					}
                    //					buff.append(((JavaElement)elements[i]).toStringWithAncestors());
                }
                return buff.toString();
            case INVALID_NAME:
                return Messages.format(RefactoringMessages.status_invalidName, string);
            case INVALID_FOLDER:
                return Messages.format(RefactoringMessages.status_invalidFolder, string);
            case INVALID_PATH:
                if(string != null) {
                    return string;
                }
                else {
                    return Messages.format(RefactoringMessages.status_invalidPath, new String[] { getPath() == null ? "null"
                            : getPath().toString() });
                }
            case INVALID_PROJECT:
                return Messages.format(RefactoringMessages.status_invalidProject, string);
            case INVALID_RESOURCE:
                return Messages.format(RefactoringMessages.status_invalidResource, string);
            case INVALID_RESOURCE_TYPE:
                return Messages.format(RefactoringMessages.status_invalidResourceType, string);
            case INVALID_SIBLING:
                if(string != null) {
                    return Messages.format(RefactoringMessages.status_invalidSibling, string);
                }
                else {
                    return Messages
                            .format(RefactoringMessages.status_invalidSibling, ""/*((JavaElement)elements[0]).toStringWithAncestors()*/);
                }
            case IO_EXCEPTION:
                return RefactoringMessages.status_IOException;
            case NAME_COLLISION:
                if(string != null) {
                    return string;
                }
                else {
                    return Messages.format(RefactoringMessages.status_nameCollision, "");
                }
            case NO_ELEMENTS_TO_PROCESS:
                return RefactoringMessages.operation_needElements;
            case NULL_NAME:
                return RefactoringMessages.operation_needName;
            case NULL_PATH:
                return RefactoringMessages.operation_needPath;
            case NULL_STRING:
                return RefactoringMessages.operation_needString;
            case PATH_OUTSIDE_PROJECT:
                return Messages.format(RefactoringMessages.operation_pathOutsideProject,
                        new String[] { string, ""/*((JavaElement)elements[0]).toStringWithAncestors()*/});
            case READ_ONLY:
                return Messages.format(RefactoringMessages.status_readOnly, ""/*name*/);
            case RELATIVE_PATH:
                return Messages.format(RefactoringMessages.operation_needAbsolutePath, getPath().toString());
            case UPDATE_CONFLICT:
                return RefactoringMessages.status_updateConflict;
            case NO_LOCAL_CONTENTS:
                return Messages.format(RefactoringMessages.status_noLocalContents, getPath().toString());
            }
            if(string != null) {
                return string;
            }
            else {
                return "";
            }
        }
        else {
            String message = exception.getMessage();
            if(message != null) {
                return message;
            }
            else {
                return exception.toString();
            }
        }
    }

    @Override
    public IPath getPath() {
        return path;
    }

    @Override
    public int getSeverity() {
        if(children == NO_CHILDREN)
            return super.getSeverity();
        int severity = -1;
        for(int i = 0, max = children.length; i < max; i++) {
            int childrenSeverity = children[i].getSeverity();
            if(childrenSeverity > severity) {
                severity = childrenSeverity;
            }
        }
        return severity;
    }

    public boolean isDoesNotExist() {
        int code = getCode();
        return code == ELEMENT_DOES_NOT_EXIST;
    }

    @Override
    public boolean isMultiStatus() {
        return children != NO_CHILDREN;
    }

    @Override
    public boolean isOK() {
        return getCode() == OK;
    }

    @Override
    public boolean matches(int mask) {
        if(!isMultiStatus()) {
            return matches(this, mask);
        }
        else {
            for(int i = 0, max = children.length; i < max; i++) {
                if(matches((Z8Status)children[i], mask))
                    return true;
            }
            return false;
        }
    }

    protected boolean matches(Z8Status status, int mask) {
        int severityMask = mask & 0x7;
        int categoryMask = mask & ~0x7;
        int bits = status.getBits();
        return ((severityMask == 0) || (bits & severityMask) != 0) && ((categoryMask == 0) || (bits & categoryMask) != 0);
    }

    public static Z8Status newMultiStatus(IStatus[] children) {
        Z8Status status = new Z8Status();
        status.children = children;
        return status;
    }

    @Override
    public String toString() {
        if(this == VERIFIED_OK) {
            return "Z8Status[OK]";
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("Z8 Status [");
        buffer.append(getMessage());
        buffer.append("]");
        return buffer.toString();
    }
}
