package org.zenframework.z8.server.apidocs.request_parameters;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParametr;
import org.zenframework.z8.server.base.query.Query;

public class SimpleParameters {

    public static IRequestParametr request() {
        return new IRequestParametr() {
            public String getKey() { return "request";}
            public Object getValue(Query query, IActionRequest action) {return query.classId();}
        };
    }

    public static IRequestParametr action() {
        return new IRequestParametr() {
            public String getKey() { return "action";}
            public Object getValue(Query query, IActionRequest action) {return action.getName();}
        };
    }

    public static IRequestParametr session() {
        return new IRequestParametr() {
            public String getKey() { return "session";}
            public Object getValue(Query query, IActionRequest action) {return "00000000-0000-0000-0000-000000000000";}
        };
    }

    public static IRequestParametr server() {
        return new IRequestParametr() {
            public String getKey() { return "server";}
            public Object getValue(Query query, IActionRequest action) {return "00000000-0000-0000-0000-000000000000";}
        };
    }

    public static IRequestParametr success() {
        return new IRequestParametr() {
            public String getKey() { return "success";}
            public Object getValue(Query query, IActionRequest action) {return "true";}
        };
    }

    public static IRequestParametr start() {
        return new IRequestParametr() {
            public String getKey() { return "start";}
            public Object getValue(Query query, IActionRequest action) {return "0";}
        };
    }

    public static IRequestParametr limit() {
        return new IRequestParametr() {
            public String getKey() { return "limit";}
            public Object getValue(Query query, IActionRequest action) {return "200";}
        };
    }

    public static IRequestParametr recordId() {
        return new IRequestParametr() {
            public String getKey() { return "recordId";}
            public Object getValue(Query query, IActionRequest action) {return "00000000-0000-0000-0000-000000000000";}
        };
    }

    public static IRequestParametr records() {
        return new IRequestParametr() {
            public String getKey() { return "records";}
            public Object getValue(Query query, IActionRequest action) {return new String[] {"00000000-0000-0000-0000-000000000000"};}
        };
    }

    public static IRequestParametr name() {
        return new IRequestParametr() {
            public String getKey() { return "name";}
            public Object getValue(Query query, IActionRequest action) {return "actionName";}
        };
    }

    public static IRequestParametr format() {
        return new IRequestParametr() {
            public String getKey() { return "format";}
            public Object getValue(Query query, IActionRequest action) {return "pdf";}
        };
    }

    public static IRequestParametr field() {
        return new IRequestParametr() {
            public String getKey() { return "field";}
            public Object getValue(Query query, IActionRequest action) {return "file_field_name";}
        };
    }
}
