package org.zenframework.z8.pde.refactoring.reorg;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;

public class PixelConverter {
    private final FontMetrics fFontMetrics;

    public PixelConverter(Control control) {
        this(control.getFont());
    }

    public PixelConverter(Font font) {
        GC gc = new GC(font.getDevice());
        gc.setFont(font);
        fFontMetrics = gc.getFontMetrics();
        gc.dispose();
    }

    public int convertHeightInCharsToPixels(int chars) {
        return Dialog.convertHeightInCharsToPixels(fFontMetrics, chars);
    }

    public int convertHorizontalDLUsToPixels(int dlus) {
        return Dialog.convertHorizontalDLUsToPixels(fFontMetrics, dlus);
    }

    public int convertVerticalDLUsToPixels(int dlus) {
        return Dialog.convertVerticalDLUsToPixels(fFontMetrics, dlus);
    }

    public int convertWidthInCharsToPixels(int chars) {
        return Dialog.convertWidthInCharsToPixels(fFontMetrics, chars);
    }
}
