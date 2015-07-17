package org.zenframework.z8.server.security;

public class Access implements IAccess {
    private static final long serialVersionUID = 2326082915397406009L;

    private boolean readAccess = true;
    private boolean writeAccess = true;
    private boolean deleteAccess = true;
    private boolean importAccess = false;

    @Override
    public boolean getRead() {
        return readAccess;
    }

    public void setRead(boolean readAccess) {
        this.readAccess = readAccess;
    }

    @Override
    public boolean getWrite() {
        return writeAccess;
    }

    public void setWrite(boolean writeAccess) {
        this.writeAccess = writeAccess;
    }

    @Override
    public boolean getDelete() {
        return deleteAccess;
    }

    public void setDelete(boolean deleteAccess) {
        this.deleteAccess = deleteAccess;
    }

    @Override
    public boolean getImport() {
        return importAccess;
    }

    public void setImport(boolean importAccess) {
        this.importAccess = importAccess;
    }
}
