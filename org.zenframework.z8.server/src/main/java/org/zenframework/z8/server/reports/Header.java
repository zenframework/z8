package org.zenframework.z8.server.reports;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.string;

public class Header extends OBJECT {
    public static class CLASS extends OBJECT.CLASS<Header> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Header.class);
            setAttribute(Native, Header.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Header(container);
        }
    }

    public string caption = new string();
    public string value = new string();

    public Header(IObject container) {
        super(container);
    }

    public Header(string caption, string value) {
        this(null);
        this.caption = caption;
        this.value = value;
    }
}
