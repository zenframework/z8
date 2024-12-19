package org.zenframework.z8.server.base.json;

import org.zenframework.z8.server.base.json.parser.JsonObject;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public class Json extends OBJECT {
	final static public string id = org.zenframework.z8.server.json.Json.id;
	final static public string index = org.zenframework.z8.server.json.Json.index;

	final static public string session = org.zenframework.z8.server.json.Json.session;
	final static public string server = org.zenframework.z8.server.json.Json.server;
	final static public string service = org.zenframework.z8.server.json.Json.service;
	final static public string retry = org.zenframework.z8.server.json.Json.retry;
	final static public string ip = org.zenframework.z8.server.json.Json.ip;

	final static public string upload = org.zenframework.z8.server.json.Json.upload;

	final static public string user = org.zenframework.z8.server.json.Json.user;
	final static public string name = org.zenframework.z8.server.json.Json.name;
	final static public string displayName = org.zenframework.z8.server.json.Json.displayName;
	final static public string login = org.zenframework.z8.server.json.Json.login;
	final static public string password = org.zenframework.z8.server.json.Json.password;
	final static public string newPassword = org.zenframework.z8.server.json.Json.newPassword;

	final static public string entries = org.zenframework.z8.server.json.Json.entries;
	final static public string settings = org.zenframework.z8.server.json.Json.settings;

	final static public string isQuery = org.zenframework.z8.server.json.Json.isQuery;
	final static public string isJob = org.zenframework.z8.server.json.Json.isJob;
	final static public string isSection = org.zenframework.z8.server.json.Json.isSection;

	final static public string info = org.zenframework.z8.server.json.Json.info;
	final static public string messages = org.zenframework.z8.server.json.Json.messages;
	final static public string get = org.zenframework.z8.server.json.Json.get;
	final static public string send = org.zenframework.z8.server.json.Json.send;
	final static public string users = org.zenframework.z8.server.json.Json.users;
	final static public string recipient = org.zenframework.z8.server.json.Json.recipient;
	final static public string sender = org.zenframework.z8.server.json.Json.sender;
	final static public string domain = org.zenframework.z8.server.json.Json.domain;

	final static public string request = org.zenframework.z8.server.json.Json.request;
	final static public string requestUrl = org.zenframework.z8.server.json.Json.requestUrl;
	final static public string message = org.zenframework.z8.server.json.Json.message;
	final static public string success = org.zenframework.z8.server.json.Json.success;
	final static public string status = org.zenframework.z8.server.json.Json.status;

	final static public string aggregation = org.zenframework.z8.server.json.Json.aggregation;
	final static public string readOnly = org.zenframework.z8.server.json.Json.readOnly;

	final static public string visible = org.zenframework.z8.server.json.Json.visible;
	final static public string hidden = org.zenframework.z8.server.json.Json.hidden;
	final static public string width = org.zenframework.z8.server.json.Json.width;
	final static public string height = org.zenframework.z8.server.json.Json.height;
	final static public string length = org.zenframework.z8.server.json.Json.length;
	final static public string size = org.zenframework.z8.server.json.Json.size;
	final static public string priority = org.zenframework.z8.server.json.Json.priority;
	final static public string required = org.zenframework.z8.server.json.Json.required;
	final static public string period = org.zenframework.z8.server.json.Json.period;
	final static public string min = org.zenframework.z8.server.json.Json.min;
	final static public string max = org.zenframework.z8.server.json.Json.max;

	final static public string row = org.zenframework.z8.server.json.Json.row;
	final static public string column = org.zenframework.z8.server.json.Json.column;
	final static public string items = org.zenframework.z8.server.json.Json.items;

	final static public string link = org.zenframework.z8.server.json.Json.link;

	final static public string grid = org.zenframework.z8.server.json.Json.grid;
	final static public string tree = org.zenframework.z8.server.json.Json.tree;

	final static public string key = org.zenframework.z8.server.json.Json.key;
	final static public string field = org.zenframework.z8.server.json.Json.field;
	final static public string value = org.zenframework.z8.server.json.Json.value;
	final static public string type = org.zenframework.z8.server.json.Json.type;
	final static public string query = org.zenframework.z8.server.json.Json.query;
	final static public string table = org.zenframework.z8.server.json.Json.table;

	final static public string recordId = org.zenframework.z8.server.json.Json.recordId;
	final static public string parentId = org.zenframework.z8.server.json.Json.parentId;

	final static public string property = org.zenframework.z8.server.json.Json.property;
	final static public string record = org.zenframework.z8.server.json.Json.record;
	final static public string lookupFields = org.zenframework.z8.server.json.Json.lookupFields;
	final static public string lookup = org.zenframework.z8.server.json.Json.lookup;
	final static public string sort = org.zenframework.z8.server.json.Json.sort;
	final static public string direction = org.zenframework.z8.server.json.Json.direction;

	final static public string having = org.zenframework.z8.server.json.Json.having;
	final static public string filter = org.zenframework.z8.server.json.Json.filter;
	final static public string quickFilter = org.zenframework.z8.server.json.Json.quickFilter;
	final static public string operator = org.zenframework.z8.server.json.Json.operator;
	final static public string logical = org.zenframework.z8.server.json.Json.logical;
	final static public string where = org.zenframework.z8.server.json.Json.where;
	final static public string and = org.zenframework.z8.server.json.Json.and;
	final static public string or = org.zenframework.z8.server.json.Json.or;
	final static public string expressions = org.zenframework.z8.server.json.Json.expressions;

	final static public string primaryKey = org.zenframework.z8.server.json.Json.primaryKey;
	final static public string parentKey = org.zenframework.z8.server.json.Json.parentKey;
	final static public string lockKey = org.zenframework.z8.server.json.Json.lockKey;

	final static public string action = org.zenframework.z8.server.json.Json.action;
	final static public string data = org.zenframework.z8.server.json.Json.data;
	final static public string summaryData = org.zenframework.z8.server.json.Json.summaryData;
	final static public string totalsData = org.zenframework.z8.server.json.Json.totalsData;
	final static public string command = org.zenframework.z8.server.json.Json.command;

	final static public string format = org.zenframework.z8.server.json.Json.format;
	final static public string report = org.zenframework.z8.server.json.Json.report;
	final static public string options = org.zenframework.z8.server.json.Json.options;

	final static public string pageFormat = org.zenframework.z8.server.json.Json.pageFormat;
	final static public string pageOrientation = org.zenframework.z8.server.json.Json.pageOrientation;

	final static public string leftMargin = org.zenframework.z8.server.json.Json.leftMargin;
	final static public string rightMargin = org.zenframework.z8.server.json.Json.rightMargin;
	final static public string topMargin = org.zenframework.z8.server.json.Json.topMargin;
	final static public string bottomMargin = org.zenframework.z8.server.json.Json.bottomMargin;

	final static public string total = org.zenframework.z8.server.json.Json.total;
	final static public string start = org.zenframework.z8.server.json.Json.start;
	final static public string finish = org.zenframework.z8.server.json.Json.finish;
	final static public string limit = org.zenframework.z8.server.json.Json.limit;
	final static public string count = org.zenframework.z8.server.json.Json.count;

	final static public string log = org.zenframework.z8.server.json.Json.log;
	final static public string file = org.zenframework.z8.server.json.Json.file;
	final static public string path = org.zenframework.z8.server.json.Json.path;
	final static public string files = org.zenframework.z8.server.json.Json.files;
	final static public string source = org.zenframework.z8.server.json.Json.source;
	final static public string target = org.zenframework.z8.server.json.Json.target;
	final static public string image = org.zenframework.z8.server.json.Json.image;
	final static public string time = org.zenframework.z8.server.json.Json.time;

	final static public string queries = org.zenframework.z8.server.json.Json.queries;
	final static public string records = org.zenframework.z8.server.json.Json.records;

	final static public string fields = org.zenframework.z8.server.json.Json.fields;
	final static public string controls = org.zenframework.z8.server.json.Json.controls;
	final static public string actions = org.zenframework.z8.server.json.Json.actions;
	final static public string columns = org.zenframework.z8.server.json.Json.columns;
	final static public string commands = org.zenframework.z8.server.json.Json.commands;
	final static public string parameters = org.zenframework.z8.server.json.Json.parameters;
	final static public string reports = org.zenframework.z8.server.json.Json.reports;

	final static public string job = org.zenframework.z8.server.json.Json.job;
	final static public string done = org.zenframework.z8.server.json.Json.done;
	final static public string totalWork = org.zenframework.z8.server.json.Json.totalWork;
	final static public string worked = org.zenframework.z8.server.json.Json.worked;
	final static public string feedback = org.zenframework.z8.server.json.Json.feedback;

	final static public string text = org.zenframework.z8.server.json.Json.text;
	final static public string description = org.zenframework.z8.server.json.Json.description;
	final static public string label = org.zenframework.z8.server.json.Json.label;
	final static public string header = org.zenframework.z8.server.json.Json.header;
	final static public string colSpan = org.zenframework.z8.server.json.Json.colSpan;
	final static public string rowSpan = org.zenframework.z8.server.json.Json.rowSpan;
	final static public string icon = org.zenframework.z8.server.json.Json.icon;
	final static public string help = org.zenframework.z8.server.json.Json.help;

	final static public string expanded = org.zenframework.z8.server.json.Json.expanded;
	final static public string loaded = org.zenframework.z8.server.json.Json.loaded;
	final static public string leaf = org.zenframework.z8.server.json.Json.leaf;

	final static public string save = org.zenframework.z8.server.json.Json.save;
	final static public string update = org.zenframework.z8.server.json.Json.update;
	final static public string create = org.zenframework.z8.server.json.Json.create;
	final static public string destroy = org.zenframework.z8.server.json.Json.destroy;

	public static JsonObject.CLASS<JsonObject> z8_getFilter(string field, primary value) {
		return z8_getFilter(field, new string("eq"), value);
	}

	public static JsonObject.CLASS<JsonObject> z8_getFilter(Field.CLASS<? extends Field> field, primary value) {
		return z8_getFilter(field, new string("eq"), value);
	}

	@SuppressWarnings("rawtypes")
	public static JsonObject.CLASS<JsonObject> z8_getFilter(string field, RCollection values) {
		return z8_getFilter(field, new string("eq"), values);
	}

	@SuppressWarnings("rawtypes")
	public static JsonObject.CLASS<JsonObject> z8_getFilter(Field.CLASS<? extends Field> field, RCollection values) {
		return z8_getFilter(field, new string("eq"), values);
	}

	public static JsonObject.CLASS<JsonObject> z8_getFilter(string field, string operator, primary value) {
		return z8_getFilter(field, operator, value, new string("and"));
	}

	public static JsonObject.CLASS<JsonObject> z8_getFilter(Field.CLASS<? extends Field> field, string operator, primary value) {
		return z8_getFilter(field.get().z8_id(), operator, value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static JsonObject.CLASS<JsonObject> z8_getFilter(string field, string operator, RCollection values) {
		return z8_getFilter(field, operator, encode(values));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static JsonObject.CLASS<JsonObject> z8_getFilter(Field.CLASS<? extends Field> field, string operator, RCollection values) {
		return z8_getFilter(field, operator, encode(values));
	}

	public static JsonObject.CLASS<JsonObject> z8_getFilter(string field, string operator, primary value, string andOr) {
		JsonObject.CLASS<JsonObject> filter = new JsonObject.CLASS<JsonObject>(null);
		filter.get().z8_put(org.zenframework.z8.server.json.Json.property, field);
		filter.get().z8_put(org.zenframework.z8.server.json.Json.operator, operator);
		filter.get().z8_put(org.zenframework.z8.server.json.Json.value, value);
		return filter;
	}

	public static JsonObject.CLASS<JsonObject> z8_getFilter(Field.CLASS<? extends Field> field, string operator, primary value, string andOr) {
		return z8_getFilter(field.get().z8_id(), operator, value, andOr);
	}

	public static JsonObject.CLASS<JsonObject> z8_getFilter(string field, string operator, RCollection<primary> values, string andOr) {
		return z8_getFilter(field, operator, encode(values), andOr);
	}

	public static JsonObject.CLASS<JsonObject> z8_getFilter(Field.CLASS<? extends Field> field, string operator, RCollection<primary> values, string andOr) {
		return z8_getFilter(field, operator, encode(values), andOr);
	}

	private static string encode(RCollection<primary> values) {
		org.zenframework.z8.server.json.parser.JsonArray array = new org.zenframework.z8.server.json.parser.JsonArray();
		for(primary value : values) {
			array.put(value);
		}
		return new string(array.toString());
	}

}
