package org.zenframework.z8.server.apidocs.parameters;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParameter;
import org.zenframework.z8.server.base.query.Query;

public class SimpleParameters {

	public static IRequestParameter request() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "request";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return query.classId();}
		};
	}

	public static IRequestParameter action() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "action";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return action.getName();}
		};
	}

	public static IRequestParameter session() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "session";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return "00000000-0000-0000-0000-000000000000";}
		};
	}

	public static IRequestParameter server() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "server";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return "00000000-0000-0000-0000-000000000000";}
		};
	}

	public static IRequestParameter success() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "success";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return "true";}
		};
	}

	public static IRequestParameter start() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "start";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return "0";}
		};
	}

	public static IRequestParameter limit() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "limit";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return "200";}
		};
	}

	public static IRequestParameter recordId() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "recordId";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return "00000000-0000-0000-0000-000000000000";}
		};
	}

	public static IRequestParameter records() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "records";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return new String[] {"00000000-0000-0000-0000-000000000000"};}
		};
	}

	public static IRequestParameter name() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "name";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return "actionName";}
		};
	}

	public static IRequestParameter format() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "format";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return "pdf";}
		};
	}

	public static IRequestParameter field() {
		return new IRequestParameter() {
			@Override
			public String getKey() { return "field";}
			@Override
			public Object getValue(Query query, IActionRequest action) {return "file_field_name";}
		};
	}
}
