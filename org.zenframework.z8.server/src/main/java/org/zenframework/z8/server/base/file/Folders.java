package org.zenframework.z8.server.base.file;

import java.io.File;

import org.zenframework.z8.server.engine.ApplicationServer;

public class Folders {
    public static final String Files = "files";
    public static final String Storage = "storage";
    public static final String Cache = "pdf.cache";
    public static final String Lucene = "lucene";

    public static File Base = ApplicationServer.workingPath();
}
