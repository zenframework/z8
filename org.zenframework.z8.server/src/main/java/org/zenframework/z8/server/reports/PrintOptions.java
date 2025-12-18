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

	public String detailRowHeight = null;

	public PrintOptions() {
	}

	public PrintOptions(String json) {
		if(json == null || json.isEmpty())
			return;

		JsonObject object = new JsonObject(json);

		pageFormat = object.has(Json.pageFormat) ? PageFormat.fromString(object.getString(Json.pageFormat)) : pageFormat;
		pageOrientation = object.has(Json.pageOrientation) ? PageOrientation.fromString(object.getString(Json.pageOrientation)) : pageOrientation;

		leftMargin = object.has(Json.leftMargin) ? (float)object.getDouble(Json.leftMargin) : leftMargin;
		rightMargin = object.has(Json.rightMargin) ? (float)object.getDouble(Json.rightMargin) : rightMargin;
		topMargin = object.has(Json.topMargin) ? (float)object.getDouble(Json.topMargin) : topMargin;
		bottomMargin = object.has(Json.bottomMargin) ? (float)object.getDouble(Json.bottomMargin) : bottomMargin;

		detailRowHeight = object.has(Json.detailRowHeight) ? object.getString(Json.detailRowHeight) : detailRowHeight;
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
