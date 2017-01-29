package org.zenframework.z8.pde;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorProvider {
	public static final RGB MULTI_LINE_COMMENT = new RGB(128, 0, 0);
	public static final RGB SINGLE_LINE_COMMENT = new RGB(128, 128, 0);
	public static final RGB KEYWORD = new RGB(0, 0, 255);
	public static final RGB ATTRIBUTE = new RGB(50, 128, 128);
	public static final RGB TYPE = new RGB(0, 0, 255);
	public static final RGB STRING = new RGB(0, 128, 0);
	public static final RGB DEFAULT = new RGB(0, 0, 0);
	public static final RGB DOC_KEYWORD = new RGB(0, 128, 0);
	public static final RGB DOC_TAG = new RGB(128, 128, 128);
	public static final RGB DOC_LINK = new RGB(128, 128, 128);
	public static final RGB DOC_DEFAULT = new RGB(0, 128, 128);

	protected Map<RGB, Color> fColorTable = new HashMap<RGB, Color>(11);

	public void dispose() {
		for(Color color : fColorTable.values()) {
			color.dispose();
		}
	}

	public Color getColor(RGB rgb) {
		Color color = (Color)fColorTable.get(rgb);
		if(color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
}
