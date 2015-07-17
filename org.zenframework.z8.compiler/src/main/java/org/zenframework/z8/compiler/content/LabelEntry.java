package org.zenframework.z8.compiler.content;

import org.zenframework.z8.compiler.workspace.NlsUnit;

public class LabelEntry {
    private String string;
    private NlsUnit nlsUnit;

    public LabelEntry(String string, NlsUnit nlsUnit) {
        this.string = string;
        this.nlsUnit = nlsUnit;
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    public NlsUnit getNLSUnit() {
        return nlsUnit;
    }

    public String getString() {
        return string;
    }
}
