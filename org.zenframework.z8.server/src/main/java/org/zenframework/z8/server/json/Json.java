package org.zenframework.z8.server.json;

import org.zenframework.z8.server.types.string;

public interface Json {
	static public string id = new string("id");
	static public string name = new string("name");
	static public string displayName = new string("displayName");
	static public string index = new string("index");
	static public string sourceCode = new string("sourceCode");

	static public string request = new string("request");
	static public string session = new string("session");
	static public string server = new string("server");
	static public string service = new string("service");
	static public string retry = new string("retry");
	static public string ip = new string("ip");

	static public string upload = new string("upload");
	static public string download = new string("download");
	static public string maxUploadSize = new string("maxUploadSize");

	static public string user = new string("user");
	static public string account = new string("account");
	static public string login = new string("login");
	static public string firstName = new string("firstName");
	static public string middleName = new string("middleName");
	static public string lastName = new string("lastName");
	static public string password = new string("password");
	static public string newPassword = new string("newPassword");
	static public string email = new string("email");
	static public string phone = new string("phone");

	static public string menu = new string("menu");
	static public string entries = new string("entries");
	static public string settings = new string("settings");

	static public string isQuery = new string("isQuery");
	static public string isJob = new string("isJob");

	static public string isForm = new string("isForm");
	static public string isSection = new string("isSection");
	static public string isFieldset = new string("isFieldset");
	static public string isListbox = new string("isListbox");
	static public string isCombobox = new string("isCombobox");
	static public string isText = new string("isText");
	static public string isTab = new string("isTab");
	static public string isTabControl = new string("isTabControl");
	static public string isAttachment = new string("isAttachment");
	static public string valueFrom = new string("valueFrom");
	static public string valueFor = new string("valueFor");

	static public string info = new string("info");
	static public string warning = new string("warning");
	static public string error = new string("error");
	static public string fatal = new string("fatal");
	static public string messages = new string("messages");
	static public string get = new string("get");
	static public string send = new string("send");
	static public string users = new string("users");
	static public string recipient = new string("recipient");
	static public string sender = new string("sender");

	static public string requestUrl = new string("requestUrl");
	static public string message = new string("message");
	static public string success = new string("success");
	static public string status = new string("status");

	static public string aggregation = new string("aggregation");
	static public string readOnly = new string("readOnly");

	static public string visible = new string("visible");
	static public string hidden = new string("hidden");
	static public string width = new string("width");
	static public string height = new string("height");
	static public string length = new string("length");
	static public string size = new string("size");
	static public string priority = new string("priority");
	static public string required = new string("required");
	static public string editable = new string("editable");
	static public string important = new string("important");
	static public string period = new string("period");
	static public string min = new string("min");
	static public string max = new string("max");

	static public string html = new string("html");
	static public string row = new string("row");
	static public string column = new string("column");
	static public string items = new string("items");
	static public string tabs = new string("tabs");
	static public string totals = new string("totals");

	static public string dependsOn = new string("dependsOn");
	static public string dependency = new string("dependency");
	static public string dependencies = new string("dependencies");

	static public string link = new string("link");
	static public string isLink = new string("isLink");
	static public string isBackward = new string("isBackward");

	static public string key = new string("key");
	static public string field = new string("field");
	static public string value = new string("value");
	static public string values = new string("values");
	static public string type = new string("type");
	static public string query = new string("query");
	static public string table = new string("table");

	static public string recordId = new string("recordId");
	static public string parentId = new string("parentId");

	static public string property = new string("property");
	static public string record = new string("record");
	static public string sort = new string("sort");
	static public string group = new string("group");
	static public string direction = new string("direction");
	static public string lookupFields = new string("lookupFields");
	static public string lookup = new string("lookup");

	static public string filter = new string("filter");
	static public string quickFilter = new string("quickFilter");
	static public string where = new string("where");
	static public string operator = new string("operator");
	static public string logical = new string("logical");
	static public string and = new string("and");
	static public string or = new string("or");
	static public string expressions = new string("expressions");

/* backward compatibility */
	static public string filter1 = new string("filter1");
	static public string comparison = new string("comparison");
	static public string andOr = new string("andOr");
/* backward compatibility */

	static public string primaryKey = new string("primaryKey");
	static public string lockKey = new string("lockKey");
	static public string attachmentsKey = new string("attachmentsKey");
	static public string periodKey = new string("periodKey");
	static public string parentKey = new string("parentKey");
	static public string parentKeys = new string("parentKeys");
	static public string hasChildren = new string("hasChildren");
	static public string level = new string("level");

	static public string isPrimaryKey = new string("isPrimaryKey");
	static public string isParentKey = new string("isParentKey");

	static public string action = new string("action");
	static public string data = new string("data");
	static public string summaryData = new string("summaryData");
	static public string totalsData = new string("totalsData");
	static public string command = new string("command");

	static public string format = new string("format");
	static public string report = new string("report");
	static public string options = new string("options");

	static public string pageFormat = new string("pageFormat");
	static public string pageOrientation = new string("pageOrientation");

	static public string leftMargin = new string("leftMargin");
	static public string rightMargin = new string("rightMargin");
	static public string topMargin = new string("topMargin");
	static public string bottomMargin = new string("bottomMargin");

	static public string total = new string("total");
	static public string start = new string("start");
	static public string finish = new string("finish");
	static public string limit = new string("limit");
	static public string count = new string("count");
	static public string duration = new string("duration");

	static public string log = new string("log");
	static public string file = new string("file");
	static public string path = new string("path");
	static public string url = new string("url");
	static public string files = new string("files");
	static public string source = new string("source");
	static public string target = new string("target");
	static public string image = new string("image");
	static public string time = new string("time");
	static public string author = new string("author");
	static public string details = new string("details");

	static public string records = new string("records");

	static public string queries = new string("queries");
	static public string fields = new string("fields");
	static public string columnCount = new string("columnCount");
	static public string columns = new string("columns");
	static public string sections = new string("sections");
	static public string controls = new string("controls");
	static public string nameFields = new string("nameFields");
	static public string quickFilters = new string("quickFilters");

	static public string grid = new string("grid");
	static public string tree = new string("tree");
	static public string actions = new string("actions");
	static public string commands = new string("commands");
	static public string parameters = new string("parameters");
	static public string reports = new string("reports");

	static public string job = new string("job");
	static public string done = new string("done");
	static public string totalWork = new string("totalWork");
	static public string worked = new string("worked");
	static public string feedback = new string("feedback");
	static public string scheduled = new string("scheduled");

	static public string text = new string("text");
	static public string description = new string("description");
	static public string ui = new string("ui");
	static public string label = new string("label");
	static public string legend = new string("legend");
	static public string header = new string("header");
	static public string colspan = new string("colspan");
	static public string rowspan = new string("rowspan");
	static public string flex = new string("flex");
	static public string icon = new string("icon");
	static public string help = new string("help");

	static public string expanded = new string("expanded");
	static public string loaded = new string("loaded");
	static public string leaf = new string("leaf");

	static public string save = new string("save");
	static public string update = new string("update");
	static public string create = new string("create");
	static public string destroy = new string("destroy");

	static public string experimental = new string("experimental");
}
