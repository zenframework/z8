package org.zenframework.z8.server.reports;

import java.util.Collection;

import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;

import org.zenframework.z8.server.base.model.actions.ReadAction;

public class PrintOptions {
    public PageFormat pageFormat = PageFormat.A4;
    public PageOrientation pageOrientation = PageOrientation.Portrait;

    public float leftMargin = 10;
    public float rightMargin = 10;
    public float topMargin = 10;
    public float bottomMargin = 10;

    public PrintOptions() {}

    public ReportOptions getReportOptions(String captionCenter, Collection<ReadAction> actions, String reportFolder,
            String reportTemplate) {
        ReportOptions reportOptions = new ReportOptions();
        reportOptions.actions = actions;
        reportOptions.reportFolder = reportFolder;
        reportOptions.reportTemplate = reportTemplate;

        float width = PageFormat.pageWidth(pageFormat);
        float height = PageFormat.pageHeight(pageFormat);

        if(pageOrientation.equals(PageOrientation.Landscape)) {
            float t = height;
            height = width;
            width = t;
        }

        reportOptions.setPageHeight(BirtUnitsConverter.convertToPoints(height, DesignChoiceConstants.UNITS_MM));
        reportOptions.setPageWidth(BirtUnitsConverter.convertToPoints(width, DesignChoiceConstants.UNITS_MM));
        reportOptions.setLeftMargin(BirtUnitsConverter.convertToPoints(leftMargin, DesignChoiceConstants.UNITS_MM));
        reportOptions.setRightMargin(BirtUnitsConverter.convertToPoints(rightMargin, DesignChoiceConstants.UNITS_MM));
        reportOptions.setTopMargin(BirtUnitsConverter.convertToPoints(topMargin, DesignChoiceConstants.UNITS_MM));
        reportOptions.setBottomMargin(BirtUnitsConverter.convertToPoints(bottomMargin, DesignChoiceConstants.UNITS_MM));

        reportOptions.headers.put(ReportConstants.FIRSTPAGE_CAPTIONCENTER, captionCenter);

        return reportOptions;
    }
}
