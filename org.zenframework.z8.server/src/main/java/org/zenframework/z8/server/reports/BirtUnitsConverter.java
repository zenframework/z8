package org.zenframework.z8.server.reports;

import org.eclipse.birt.report.model.api.DimensionHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.metadata.DimensionValue;

public class BirtUnitsConverter {
	static public float PX_PER_INCH = 72;
	static public float PX_PER_PT = 1;
	static public float PT_PER_PC = 12;
	static public float CM_PER_INCH = (float)2.54;
	static public float MM_PER_INCH = (float)25.4;

	public static float convertToPoints(DimensionHandle valueHandle) {
		DimensionValue value = valueHandle.getAbsoluteValue();

		float measure = (float)value.getMeasure();

		return convertToPoints(measure, value.getUnits());
	}

	public static float convertToPoints(float measure, String units) {
		if(DesignChoiceConstants.UNITS_PX.equalsIgnoreCase(units)) {
			return measure;
		}
		if(DesignChoiceConstants.UNITS_IN.equalsIgnoreCase(units)) {
			return measure * PX_PER_INCH;
		} else if(DesignChoiceConstants.UNITS_PT.equalsIgnoreCase(units)) {
			return measure * PX_PER_PT;
		} else if(DesignChoiceConstants.UNITS_PC.equalsIgnoreCase(units)) {
			return measure * PX_PER_PT * PT_PER_PC;
		} else if(DesignChoiceConstants.UNITS_MM.equalsIgnoreCase(units)) {
			return (measure / MM_PER_INCH) * PX_PER_INCH;
		} else if(DesignChoiceConstants.UNITS_CM.equalsIgnoreCase(units)) {
			return (measure / CM_PER_INCH) * PX_PER_INCH;
		}

		return 0;
	}

}
