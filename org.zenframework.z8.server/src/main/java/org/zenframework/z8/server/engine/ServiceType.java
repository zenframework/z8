package org.zenframework.z8.server.engine;

public enum ServiceType {
    Report(ServiceTypes.Report),
    Import(ServiceTypes.Import);

    class ServiceTypes {
        static protected final String Report = "report";
        static protected final String Import = "import";
    }

    private String fName = null;

    ServiceType(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName;
    }

    static public ServiceType fromString(String string) {
        if(ServiceTypes.Report.equals(string)) {
            return ServiceType.Report;
        }
        else if(ServiceTypes.Import.equals(string)) {
            return ServiceType.Import;
        }
        else {
            throw new RuntimeException("Unsupported service type: '" + string + "'");
        }
    }
}
