package org.zenframework.z8.server.reports;

import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonObject;

public class PrintOptions {
	public PageFormat pageFormat = PageFormat.A4;
	public PageOrientation pageOrientation = PageOrientation.Landscape;

	public float leftMargin = 10;
	public float rightMargin = 10;
	public float topMargin = 10;
	public float bottomMargin = 10;

	public PrintOptions() {
	}

	public PrintOptions(String json) {
		if(json == null || json.isEmpty())
			return;

		JsonObject object = new JsonObject(json);

		pageOrientation = PageOrientation.fromString(object.getString(Json.pageOrientation));
		pageFormat = PageFormat.fromString(object.getString(Json.pageFormat));

		leftMargin = (float)object.getDouble(Json.leftMargin);
		rightMargin = (float)object.getDouble(Json.rightMargin);
		topMargin = (float)object.getDouble(Json.topMargin);
		bottomMargin = (float)object.getDouble(Json.bottomMargin);
	}

	public float pageHeight() {
		boolean portrait = pageOrientation == PageOrientation.Portrait;
		float height = portrait ? PageFormat.pageHeight(pageFormat) : PageFormat.pageWidth(pageFormat);
		return UnitsConverter.convertToPoints(height, DesignChoiceConstants.UNITS_MM);
	}

	public float pageWidth() {
		boolean portrait = pageOrientation == PageOrientation.Portrait;
		float width = portrait ? PageFormat.pageWidth(pageFormat) : PageFormat.pageHeight(pageFormat);
		return UnitsConverter.convertToPoints(width, DesignChoiceConstants.UNITS_MM);
	}

	public float leftMargin() {
		return UnitsConverter.convertToPoints(leftMargin, DesignChoiceConstants.UNITS_MM);
	}

	public float rightMargin() {
		return UnitsConverter.convertToPoints(rightMargin, DesignChoiceConstants.UNITS_MM);
	}

	public float topMargin() {
		return UnitsConverter.convertToPoints(topMargin, DesignChoiceConstants.UNITS_MM);
	}

	public float bottomMargin() {
		return UnitsConverter.convertToPoints(bottomMargin, DesignChoiceConstants.UNITS_MM);
	}
}
