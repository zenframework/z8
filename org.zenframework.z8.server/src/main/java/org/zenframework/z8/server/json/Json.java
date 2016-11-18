package org.zenframework.z8.server.json;

import org.zenframework.z8.server.types.string;

public interface Json {
	string id = new string("id");
	string index = new string("index");
	string instanceId = new string("instanceId");

	string sessionId = new string("sessionId");
	string serverId = new string("serverId");
	string service = new string("service");
	string retry = new string("retry");
	string ip = new string("ip");

	string upload = new string("upload");
	string download = new string("download");

	string user = new string("user");
	string name = new string("name");
	string displayName = new string("displayName");
	string login = new string("login");
	string password = new string("password");
	string newPassword = new string("newPassword");
	string email = new string("email");
	string phone = new string("phone");

	string menu = new string("menu");
	string components = new string("components");
	string settings = new string("settings");

	string isQuery = new string("isQuery");
	string isJob = new string("isJob");
	string isSection = new string("isSection");
	string isFieldset = new string("isFieldset");
	string isListbox = new string("isListbox");
	string isCombobox = new string("isCombobox");
	string isText = new string("isText");
	string isTab = new string("isTab");
	string isTabControl = new string("isTabControl");

	string info = new string("info");
	string messages = new string("messages");
	string get = new string("get");
	string send = new string("send");
	string users = new string("users");
	string recipient = new string("recipient");
	string sender = new string("sender");

	string requestId = new string("requestId");
	string requestUrl = new string("requestUrl");
	string message = new string("message");
	string success = new string("success");
	string status = new string("status");

	string serverType = new string("serverType");
	string aggregation = new string("aggregation");
	string readOnly = new string("readOnly");

	string visible = new string("visible");
	string hidden = new string("hidden");
	string width = new string("width");
	string height = new string("height");
	string length = new string("length");
	string size = new string("size");
	string priority = new string("priority");
	string required = new string("required");
	string period = new string("period");
	string min = new string("min");
	string max = new string("max");

	string html = new string("html");
	string row = new string("row");
	string column = new string("column");
	string items = new string("items");
	string tabs = new string("tabs");

	string dependsOn = new string("dependsOn");
	string dependencies = new string("dependencies");

	string link = new string("link");
	string isLink = new string("isLink");

	string key = new string("key");
	string field = new string("field");
	string value = new string("value");
	string type = new string("type");
	string query = new string("query");
	string table = new string("table");

	string external = new string("external");
	string internal = new string("internal");

	string recordId = new string("recordId");
	string parentId = new string("parentId");

	string property = new string("property");
	string record = new string("record");
	string sort = new string("sort");
	string group = new string("group");
	string direction = new string("direction");
	string lookupFields = new string("lookupFields");
	string lookup = new string("lookup");

	string filter = new string("filter");
	string quickFilter = new string("quickFilter");
	string where = new string("where");
	string operator = new string("operator");
	string logical = new string("logical");
	string and = new string("and");
	string or = new string("or");
	string expressions = new string("expressions");

	string filter1 = new string("filter1");
	string comparison = new string("comparison");
	string andOr = new string("andOr");

	string __search_text__ = new string("__search_text__");

	/* Ext 3.1 values */
	string groupBy = new string("groupBy");
	string totalsBy = new string("totalsBy");
	string dir = new string("dir");
	string groupDir = new string("groupDir");
	string groupValue = new string("groupValue");
	string groups = new string("groups");
	string remoteSort = new string("remoteSort");
	/* Ext 3.1 values */

	string primaryKey = new string("primaryKey");
	string parentKey = new string("parentKey");
	string children = new string("children");
	string lockKey = new string("lockKey");
	string attachments = new string("attachments");

	string action = new string("xaction");
	string data = new string("data");
	string summaryData = new string("summaryData");
	string totalsData = new string("totalsData");
	string command = new string("command");

	string format = new string("format");
	string report = new string("report");
	string options = new string("options");

	string pageFormat = new string("pageFormat");
	string pageOrientation = new string("pageOrientation");

	string leftMargin = new string("leftMargin");
	string rightMargin = new string("rightMargin");
	string topMargin = new string("topMargin");
	string bottomMargin = new string("bottomMargin");

	string total = new string("total");
	string start = new string("start");
	string finish = new string("finish");
	string limit = new string("limit");
	string count = new string("count");

	string log = new string("log");
	string file = new string("file");
	string path = new string("path");
	string files = new string("files");
	string source = new string("source");
	string target = new string("target");
	string image = new string("image");
	string time = new string("time");
	string details = new string("details");

	string records = new string("records");

	string queries = new string("queries");
	string fields = new string("fields");
	string sections = new string("sections");
	string controls = new string("controls");
	string gridFields = new string("gridFields");
	string nameFields = new string("nameFields");
	string quickFilterFields = new string("quickFilterFields");

	string grid = new string("grid");
	string tree = new string("tree");
	string actions = new string("actions");
	string columns = new string("columns");
	string commands = new string("commands");
	string parameters = new string("parameters");
	string reports = new string("reports");

	string showTotals = new string("showTotals");

	string chartType = new string("chartType");
	string chartSeries = new string("chartSeries");

	string style = new string("style");
	string color = new string("color");
	string background = new string("background");
	string bold = new string("bold");

	string jobId = new string("jobId");
	string done = new string("done");
	string totalWork = new string("totalWork");
	string worked = new string("worked");
	string feedback = new string("feedback");
	string scheduled = new string("scheduled");

	string text = new string("text");
	string description = new string("description");
	string label = new string("label");
	string legend = new string("legend");
	string header = new string("header");
	string colspan = new string("colspan");
	string rowspan = new string("rowspan");
	string icon = new string("icon");
	string help = new string("help");

	string expanded = new string("expanded");
	string loaded = new string("loaded");
	string leaf = new string("leaf");

	string save = new string("save");
	string update = new string("update");
	string create = new string("create");
	string destroy = new string("destroy");

	string experimental = new string("experimental");
}
