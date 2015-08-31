package org.zenframework.z8.server.reports;

public class ReportConstants {
    public static final String ReportOutputFolder = "reports/generated";

    public static final String DEFAULT_REPORT_FOLDER = "reports";
    public static final String REPORT_BINDING_FILENAME = "reportbinding.xml";

    public static final String DEFAULT_DYN_REPORT_FOLDER = "defaultReports";
    public static final String DEFAULT_REPORT_DESIGN = "default_report.rptdesign";

    // for Layout:
    public static String FIRSTPAGE_CAPTIONCENTER = "CaptionCenter";
    public static String REPORT_BODY = "ReportBody";
    // for MasterPage: 
    public static String EACHPAGE_PAGE_NUMBER = "PageNumber";
    public static String EACHPAGE_TIMESTAMP = "DateTimeStamp";
    public static String EACHPAGE_REPORTNAME = "ReportName";

    // others
    public static final String FORMAT_PDF = "pdf";
    public static final String FORMAT_EXCEL = "xls";
    public static final String FORMAT_WORD = "doc";
    public static final String FORMAT_HTML = "html";
    public static final String FORMAT_POWERPOINT = "ppt";

    public static final int DEFAULT_GROUP_INDENTATION = 20;
    public static final int DEFAULT_PAGE_OVERLAPPING = 10;
    public static final int MINIMAL_FONT_SIZE = 6;

    public static final String GroupTotalText = "Report.groupTotal";
    public static final String GroupGrandTotalText = "Report.groupGrandTotal";
}
