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
        ReportOptions options = new ReportOptions();
        options.actions = actions;
        options.reportFolder = reportFolder;
        options.reportTemplate = reportTemplate;

        float width = PageFormat.pageWidth(pageFormat);
        float height = PageFormat.pageHeight(pageFormat);

        if(pageOrientation.equals(PageOrientation.Landscape)) {
            float t = height;
            height = width;
            width = t;
        }

        options.setPageHeight(BirtUnitsConverter.convertToPoints(height, DesignChoiceConstants.UNITS_MM));
        options.setPageWidth(BirtUnitsConverter.convertToPoints(width, DesignChoiceConstants.UNITS_MM));
        options.setLeftMargin(BirtUnitsConverter.convertToPoints(leftMargin, DesignChoiceConstants.UNITS_MM));
        options.setRightMargin(BirtUnitsConverter.convertToPoints(rightMargin, DesignChoiceConstants.UNITS_MM));
        options.setTopMargin(BirtUnitsConverter.convertToPoints(topMargin, DesignChoiceConstants.UNITS_MM));
        options.setBottomMargin(BirtUnitsConverter.convertToPoints(bottomMargin, DesignChoiceConstants.UNITS_MM));

        options.headers.put(Reports.FIRSTPAGE_CAPTIONCENTER, captionCenter);

        return options;
    }
}
