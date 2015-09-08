package org.zenframework.z8.server.json;

import java.util.Collection;
import java.util.Map;

import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonException;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.json.parser.JsonTokener;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Json {

    final static public string eventId = new string("eventId");

    final static public string id = new string("id");
    final static public string index = new string("index");

    final static public string sessionId = new string("sessionId");
    final static public string serverId = new string("serverId");
    final static public string service = new string("service");
    final static public string retry = new string("retry");

    final static public string upload = new string("upload");

    final static public string user = new string("user");
    final static public string name = new string("name");
    final static public string displayName = new string("displayName");
    final static public string login = new string("login");
    final static public string password = new string("password");
    final static public string newPassword = new string("newPassword");

    final static public string components = new string("components");
    final static public string settings = new string("settings");

    final static public string isQuery = new string("isQuery");
    final static public string isJob = new string("isJob");
    final static public string isSection = new string("isSection");

    final static public string info = new string("info");
    final static public string messages = new string("messages");
    final static public string get = new string("get");
    final static public string send = new string("send");
    final static public string users = new string("users");
    final static public string recipient = new string("recipient");
    final static public string sender = new string("sender");

    final static public string requestId = new string("requestId");
    final static public string requestUrl = new string("requestUrl");
    final static public string formToOpen = new string("formToOpen");
    final static public string formId = new string("formId");
    final static public string message = new string("message");
    final static public string success = new string("success");
    final static public string status = new string("status");

    final static public string serverType = new string("serverType");
    final static public string aggregation = new string("aggregation");
    final static public string readOnly = new string("readOnly");
    final static public string writeAccess = new string("writeAccess");
    final static public string deleteAccess = new string("deleteAccess");
    final static public string importAccess = new string("importAccess");

    final static public string visible = new string("visible");
    final static public string hidden = new string("hidden");
    final static public string hidable = new string("hidable");
    final static public string formula = new string("formula");
    final static public string evaluations = new string("evaluations");
    final static public string dependencies = new string("dependencies");
    final static public string width = new string("width");
    final static public string height = new string("height");
    final static public string length = new string("length");
    final static public string size = new string("size");
    final static public string lines = new string("lines");
    final static public string stretch = new string("stretch");
    final static public string priority = new string("priority");
    final static public string required = new string("required");
    final static public string period = new string("period");
    final static public string min = new string("min");
    final static public string max = new string("max");

    final static public string row = new string("row");
    final static public string column = new string("column");
    final static public string items = new string("items");

    final static public string columnWidth = new string("columnWidth");
    final static public string labelWidth = new string("labelWidth");

    final static public string link = new string("link");
    final static public string editWith = new string("editWith");
    final static public string editWithText = new string("editWithText");

    final static public string grid = new string("grid");
    final static public string tree = new string("tree");
    final static public string parentsSelectable = new string("parentsSelectable");

    final static public string key = new string("key");
    final static public string field = new string("field");
    final static public string value = new string("value");
    final static public string type = new string("type");
    final static public string query = new string("query");
    final static public string table = new string("table");

    final static public string external = new string("external");
    final static public string internal = new string("internal");

    final static public string queryId = new string("queryId");
    final static public string groupId = new string("groupId");
    final static public string fieldId = new string("fieldId");
    final static public string linkId = new string("linkId");
    final static public string recordId = new string("recordId");
    final static public string parentId = new string("parentId");
    final static public string linked = new string("linked");
    final static public string linkedVia = new string("linkedVia");
    final static public string depth = new string("depth");
    final static public string anchor = new string("anchor");
    final static public string anchorPolicy = new string("anchorPolicy");
    final static public string ids = new string("ids");

    final static public string property = new string("property");
    final static public string record = new string("record");
    final static public string lookupFields = new string("lookupFields");
    final static public string lookup = new string("lookup");
    final static public string sort = new string("sort");
    final static public string groupBy = new string("groupBy");
    final static public string totalsBy = new string("totalsBy");
    final static public string dir = new string("dir");
    final static public string direction = new string("direction");
    final static public string groupDir = new string("groupDir");
    final static public string groupValue = new string("groupValue");
    final static public string groups = new string("groups");
    final static public string __search_text__ = new string("__search_text__");

    final static public string filterBy = new string("filterBy");

    final static public string filter = new string("filter");
    final static public string filter1 = new string("filter1");
    final static public string operator = new string("operator");
    final static public string comparison = new string("comparison");
    final static public string andOr = new string("andOr");

    final static public string primaryKey = new string("primaryKey");
    final static public string parentKey = new string("parentKey");
    final static public string children = new string("children");
    final static public string lockKey = new string("lockKey");
    final static public string attachments = new string("attachments");

    final static public string action = new string("xaction");
    final static public string data = new string("data");
    final static public string summaryData = new string("summaryData");
    final static public string totalsData = new string("totalsData");
    final static public string command = new string("command");
    final static public string attach = new string("attach");

    final static public string format = new string("format");
    final static public string report = new string("report");
    final static public string options = new string("options");

    final static public string pageFormat = new string("pageFormat");
    final static public string pageOrientation = new string("pageOrientation");

    final static public string leftMargin = new string("leftMargin");
    final static public string rightMargin = new string("rightMargin");
    final static public string topMargin = new string("topMargin");
    final static public string bottomMargin = new string("bottomMargin");

    final static public string total = new string("total");
    final static public string start = new string("start");
    final static public string finish = new string("finish");
    final static public string limit = new string("limit");
    final static public string count = new string("count");

    final static public string log = new string("log");
    final static public string file = new string("file");
    final static public string path = new string("path");
    final static public string files = new string("files");
    final static public string source = new string("source");
    final static public string target = new string("target");
    final static public string image = new string("image");
    final static public string time = new string("time");

    final static public string refresh = new string("refresh");
    final static public string queries = new string("queries");
    final static public string records = new string("records");

    final static public string fields = new string("fields");
    final static public string section = new string("section");
    final static public string controls = new string("controls");
    final static public string actions = new string("actions");
    final static public string columns = new string("columns");
    final static public string backwards = new string("backwards");
    final static public string commands = new string("commands");
    final static public string parameters = new string("parameters");
    final static public string reports = new string("reports");
    final static public string collapseGroups = new string("collapseGroups");
    final static public string showTotals = new string("showTotals");
    final static public string viewMode = new string("viewMode");
    final static public string fieldsToShow = new string("fieldsToShow");

    final static public string chartType = new string("chartType");
    final static public string chartSeries = new string("chartSeries");

    final static public string style = new string("style");
    final static public string color = new string("color");
    final static public string background = new string("background");
    final static public string bold = new string("bold");

    final static public string jobId = new string("jobId");
    final static public string done = new string("done");
    final static public string totalWork = new string("totalWork");
    final static public string worked = new string("worked");
    final static public string feedback = new string("feedback");

    final static public string text = new string("text");
    final static public string description = new string("description");
    final static public string label = new string("label");
    final static public string header = new string("header");
    final static public string colspan = new string("colspan");
    final static public string rowspan = new string("rowspan");
    final static public string showLabel = new string("showLabel");
    final static public string icon = new string("icon");
    final static public string help = new string("help");

    final static public string expanded = new string("expanded");
    final static public string loaded = new string("loaded");
    final static public string leaf = new string("leaf");

    final static public string save = new string("save");
    final static public string update = new string("update");
    final static public string create = new string("create");
    final static public string destroy = new string("destroy");

    private Object object;

    public Json() {
        this(null);
    }

    public Json(Object object) {
        if (object == null || object.equals(null)) {
            this.object = org.zenframework.z8.server.json.parser.JsonObject.NULL;
        } else if (object instanceof String) {
            String str = (String) object;
            JsonTokener tokener = new JsonTokener(str);
            char c = tokener.nextClean();
            tokener.back();
            if (c == '{') {
                this.object = new org.zenframework.z8.server.json.parser.JsonObject(tokener);
            } else if (c == '[') {
                this.object = new JsonArray(tokener);
            } else {
                this.object = object;
            }
        } else if (object instanceof bool) {
            this.object = ((bool) object).get();
        } else if (object instanceof integer) {
            this.object = ((integer) object).get();
        } else if (object instanceof decimal) {
            this.object = ((decimal) object).get();
        } else if (object instanceof date) {
            this.object = ((date) object).get();
        } else if (object instanceof datetime) {
            this.object = ((datetime) object).get();
        } else if (object instanceof Map) {
            this.object = new org.zenframework.z8.server.json.parser.JsonObject((Map<?, ?>) object);
        } else if (object instanceof Collection) {
            this.object = new JsonArray((Collection<?>) object);
        } else if (object.getClass().isArray()) {
            this.object = new JsonArray(object);
        } else {
            this.object = object;
        }
    }

    public Object get() {
        return object;
    }

    public String getString() throws JsonException {
        return object == org.zenframework.z8.server.json.parser.JsonObject.NULL ? null : object.toString();
    }

    public boolean getBoolean() throws JsonException {
        if (object.equals(Boolean.FALSE) || (object instanceof String && ((String) object).equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE) || (object instanceof String && ((String) object).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new JsonException("Is not a Boolean.");
    }

    public double getDouble() throws JsonException {
        try {
            return object instanceof Number ? ((Number) object).doubleValue() : Double.parseDouble((String) object);
        } catch (Exception e) {
            throw new JsonException("Is not a number.");
        }
    }

    public int getInt() throws JsonException {
        try {
            return object instanceof Number ? ((Number) object).intValue() : Integer.parseInt((String) object);
        } catch (Exception e) {
            throw new JsonException("Is not an int.");
        }
    }

    public JsonArray getJsonArray() throws JsonException {
        if (object instanceof JsonArray) {
            return (JsonArray) object;
        }
        throw new JsonException("Is not a JSONArray.");
    }

    public JsonObject getJsonObject() throws JsonException {
        if (object instanceof JsonObject) {
            return (JsonObject) object;
        }
        throw new JsonException("Is not a JSONObject.");
    }

    public long getLong() throws JsonException {
        try {
            return object instanceof Number ? ((Number) object).longValue() : Long.parseLong((String) object);
        } catch (Exception e) {
            throw new JsonException("Is not a long.");
        }
    }

    public guid getGuid() throws JsonException {
        return object instanceof guid ? (guid) object : new guid(object.toString());
    }

    public Json put(Object value) throws JsonException {
        this.object = JsonObject.wrap(value);
        return this;
    }

    @Override
    public String toString() {
        return JsonObject.valueToString(object);
    }

}
