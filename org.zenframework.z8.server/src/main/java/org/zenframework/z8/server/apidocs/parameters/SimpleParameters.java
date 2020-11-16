package org.zenframework.z8.server.apidocs.parameters;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParameter;
import org.zenframework.z8.server.base.query.Query;

public class SimpleParameters {

    public static IRequestParameter request() {
        return new IRequestParameter() {
            public String getKey() { return "request";}
            public Object getValue(Query query, IActionRequest action) {return query.classId();}
        };
    }

    public static IRequestParameter action() {
        return new IRequestParameter() {
            public String getKey() { return "action";}
            public Object getValue(Query query, IActionRequest action) {return action.getName();}
        };
    }

    public static IRequestParameter session() {
        return new IRequestParameter() {
            public String getKey() { return "session";}
            public Object getValue(Query query, IActionRequest action) {return "00000000-0000-0000-0000-000000000000";}
        };
    }

    public static IRequestParameter server() {
        return new IRequestParameter() {
            public String getKey() { return "server";}
            public Object getValue(Query query, IActionRequest action) {return "00000000-0000-0000-0000-000000000000";}
        };
    }

    public static IRequestParameter success() {
        return new IRequestParameter() {
            public String getKey() { return "success";}
            public Object getValue(Query query, IActionRequest action) {return "true";}
        };
    }

    public static IRequestParameter start() {
        return new IRequestParameter() {
            public String getKey() { return "start";}
            public Object getValue(Query query, IActionRequest action) {return "0";}
        };
    }

    public static IRequestParameter limit() {
        return new IRequestParameter() {
            public String getKey() { return "limit";}
            public Object getValue(Query query, IActionRequest action) {return "200";}
        };
    }

    public static IRequestParameter recordId() {
        return new IRequestParameter() {
            public String getKey() { return "recordId";}
            public Object getValue(Query query, IActionRequest action) {return "00000000-0000-0000-0000-000000000000";}
        };
    }

    public static IRequestParameter records() {
        return new IRequestParameter() {
            public String getKey() { return "records";}
            public Object getValue(Query query, IActionRequest action) {return new String[] {"00000000-0000-0000-0000-000000000000"};}
        };
    }

    public static IRequestParameter name() {
        return new IRequestParameter() {
            public String getKey() { return "name";}
            public Object getValue(Query query, IActionRequest action) {return "actionName";}
        };
    }

    public static IRequestParameter format() {
        return new IRequestParameter() {
            public String getKey() { return "format";}
            public Object getValue(Query query, IActionRequest action) {return "pdf";}
        };
    }

    public static IRequestParameter field() {
        return new IRequestParameter() {
            public String getKey() { return "field";}
            public Object getValue(Query query, IActionRequest action) {return "file_field_name";}
        };
    }
}
