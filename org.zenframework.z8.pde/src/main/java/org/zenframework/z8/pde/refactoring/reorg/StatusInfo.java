package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.core.runtime.IStatus;

import org.zenframework.z8.pde.Plugin;

public class StatusInfo implements IStatus {
    public static final IStatus OK_STATUS = new StatusInfo();
    private String m_statusMessage;
    private int m_severity;

    public StatusInfo() {
        this(OK, null);
    }

    public StatusInfo(int severity, String message) {
        m_statusMessage = message;
        m_severity = severity;
    }

    @Override
    public boolean isOK() {
        return m_severity == IStatus.OK;
    }

    public boolean isWarning() {
        return m_severity == IStatus.WARNING;
    }

    public boolean isInfo() {
        return m_severity == IStatus.INFO;
    }

    public boolean isError() {
        return m_severity == IStatus.ERROR;
    }

    @Override
    public String getMessage() {
        return m_statusMessage;
    }

    public void setError(String errorMessage) {
        m_statusMessage = errorMessage;
        m_severity = IStatus.ERROR;
    }

    public void setWarning(String warningMessage) {
        m_statusMessage = warningMessage;
        m_severity = IStatus.WARNING;
    }

    public void setInfo(String infoMessage) {
        m_statusMessage = infoMessage;
        m_severity = IStatus.INFO;
    }

    public void setOK() {
        m_statusMessage = null;
        m_severity = IStatus.OK;
    }

    @Override
    public boolean matches(int severityMask) {
        return (m_severity & severityMask) != 0;
    }

    @Override
    public boolean isMultiStatus() {
        return false;
    }

    @Override
    public int getSeverity() {
        return m_severity;
    }

    @Override
    public String getPlugin() {
        return Plugin.PLUGIN_ID;
    }

    @Override
    public Throwable getException() {
        return null;
    }

    @Override
    public int getCode() {
        return m_severity;
    }

    @Override
    public IStatus[] getChildren() {
        return new IStatus[0];
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("StatusInfo ");

        if(m_severity == OK) {
            buf.append("OK");
        }
        else if(m_severity == ERROR) {
            buf.append("ERROR");
        }
        else if(m_severity == WARNING) {
            buf.append("WARNING");
        }
        else if(m_severity == INFO) {
            buf.append("INFO");
        }
        else {
            buf.append("severity=");
            buf.append(m_severity);
        }

        buf.append(": ");
        buf.append(m_statusMessage);

        return buf.toString();
    }
}
