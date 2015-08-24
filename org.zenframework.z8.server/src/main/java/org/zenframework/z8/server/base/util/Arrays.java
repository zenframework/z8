package org.zenframework.z8.server.base.util;

import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.string;

public class Arrays {

    private Arrays() {}

    @SuppressWarnings("rawtypes")
    public static final string z8_toString(RCollection array) {
        return new string(array.toString());
    }

}
