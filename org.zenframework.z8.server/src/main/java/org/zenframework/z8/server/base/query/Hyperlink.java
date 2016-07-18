package org.zenframework.z8.server.base.query;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class Hyperlink extends OBJECT {
    public static class CLASS<T extends Hyperlink> extends OBJECT.CLASS<T> {
        public CLASS() {
            this(null);
        }

        public CLASS(IObject container) {
            super(container);
            setJavaClass(Hyperlink.class);
        }

        @Override
        public Object newObject(IObject container) {
            return new Hyperlink(container);
        }
    }

    static String tagStartHRef = "{a id=";
    static String tagHRefRecordId = " recordId=";
    static String tagHRefValue = " a}";
    static String tagEndHRef = "{\\a}";

    public Query.CLASS<? extends Query> query = null;
    public guid recordId = new guid();
    public string text = new string();

    protected Hyperlink(IObject container) {
        super(container);
    }

    static public Hyperlink.CLASS<? extends Hyperlink> z8_create(Query.CLASS<? extends Query> query, string text) {
        return z8_create(query, null, text);
    }

    static public Hyperlink.CLASS<? extends Hyperlink> z8_create(Query.CLASS<? extends Query> query, guid recordId, string text) {
        Hyperlink.CLASS<Hyperlink> href = new Hyperlink.CLASS<Hyperlink>();
        href.get().query = query;
        href.get().text.set(text);
        href.get().recordId.set(recordId);
        return href;
    }

    static public string z8_externalLink(Query.CLASS<? extends Query> query, guid recordId, string text) {
        String url = ApplicationServer.getRequest().getParameter(Json.requestUrl) + "?" + Json.formToOpen
                + "=" + query.classId() + "&" + Json.formId + "=" + recordId;

        return new string("<a href='" + url + "'>" + text + "</a>");
    }

    public string string() {
        String result = tagStartHRef + '"' + query.classId() + '"';

        if(!recordId.isNull()) {
            result += tagHRefRecordId + '"' + recordId.toString() + '"';
        }

        result += tagHRefValue + text + tagEndHRef;

        return new string(result);
    }

    static public String extract(String text) {
        String ret = "";
        int fromIndex = 0;

        while(fromIndex >= 0) {
            int tagStartInd = text.indexOf(tagStartHRef, fromIndex);
            if(tagStartInd >= 0) {
                int tagValueInd = text.indexOf(tagHRefValue, tagStartInd);
                int tagEndInd = text.indexOf(tagEndHRef, tagStartInd);

                String value = text.substring(tagValueInd + tagHRefValue.length(), tagEndInd);
                ret = text.substring(fromIndex, tagStartInd) + value;

                fromIndex = tagEndInd + tagEndHRef.length();
            }
            else {
                if(fromIndex > 0)
                    ret = ret + text.substring(fromIndex);
                fromIndex = -1;
            }
        }

        if(ret.isEmpty())
            return text;

        return ret;
    }
}
