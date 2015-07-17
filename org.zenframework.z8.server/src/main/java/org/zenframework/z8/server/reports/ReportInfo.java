package org.zenframework.z8.server.reports;

public class ReportInfo {
    private String fileName;
    private String displayName;

    public ReportInfo(String fileName, String displayName) {
        this.fileName = fileName;
        this.displayName = displayName;
    }

    public String fileName() {
        return fileName;
    }

    public String displayName() {
        return displayName;
    }
}
