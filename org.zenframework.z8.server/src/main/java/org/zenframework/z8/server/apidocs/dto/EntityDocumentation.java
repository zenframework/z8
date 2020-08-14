package org.zenframework.z8.server.apidocs.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityDocumentation {
    private String entityName;
    private String entityDescription;
    private String entityId;
    private AttributeInformation request;
    private AttributeInformation query;
    private AttributeInformation session;
    private AttributeInformation action;
    private AttributeInformation start;
    private AttributeInformation limit;
    private AttributeInformation fields;
    private AttributeInformation count;
    private AttributeInformation sort;
    private AttributeInformation filter;
    private AttributeInformation name;
    private AttributeInformation values;
    private AttributeInformation recordId;

    private AttributeInformation success;
    private AttributeInformation status;
    private AttributeInformation server;
    private AttributeInformation data;
    private AttributeInformation total;

    private String codeSnippet;

    private final List<FieldDescription> entityFields;
    private final List<BaseInfo> actions;
    private final List<AttributeInformation> input;
    private final List<AttributeInformation> output;

    public EntityDocumentation() {
        entityFields = new ArrayList<>();
        actions = new ArrayList<>();

        // common parameters that exist in both sides of a request
        request = new AttributeInformation("request", "string", "",
                "уникальная строка, идентифицирующая представление данных, к которому формируется запрос;", true);
        data = new AttributeInformation("data", "object", "",
                "Массив объектов-записей");
        // endregion

        // region request parameters
        query = new AttributeInformation("query", "string", "", 
                "Обязательно в отдельных случаях, уникальная строка, идентифицирующая запрос в рамках представления данных;");

        session = new AttributeInformation("session", "guid", "68322EBA-F143-4997-A7DA-CC67E476B9EA", 
                "Обязательно, идентификатор сессии, полученный в ответе на\n" +
                "запрос авторизации", true);

        action = new AttributeInformation("action", "string", "read", 
                "Действие запроса, один из вариантов:\n" +
                        "create - создать запись в базе данных\n" +
                        "copy - скопировать запись(создать новую и скопировать все значения полей из исходной записи)\n" +
                        "read - прочитать записи из базе данных\n" +
                        "update - изменить поля записи в базе данных\n" +
                        "destroy - удалить записи из базе данных\n" +
                        "export -\n" +
                        "report - запустить создание отчета, результатом является файл\n" +
                        "preview - загрузить указанный файл в режиме preview (файл на лету конвертируется в PDF)\n" +
                        "action - выполнить команду\n" +
                        "attach - прикрепить файл к записи\n" +
                        "detach - открепить файл от записи\n" +
                        "content - получить или записать бинарные данные", true);

        start = new AttributeInformation("start", "integer", "0",
                "Опционально, номер первой записи в запрашиваемой выборке (странице) данных");

        limit = new AttributeInformation("limit", "integer", "100",
                "Опционально, количество записей в выборке (странице)");

        fields = new AttributeInformation("fields", "object", "",
                "Обязательно, если указан query, JSON-массив названий полей запроса в представлении");

        count = new AttributeInformation("count", "boolean", "true",
                "Опционально, если указано, ответ содержит общее количество записей в выборке");

        sort = new AttributeInformation("sort", "object", "",
                "Опционально, JSON-массив параметров сортировки выборки, каждый параметр описывается объектом со следующей структурой:\n" +
                        "◦ property: <имя поля> — имя поля сортировки (%s);\n" +
                        "◦ direction: <направление> — направление сортировки: по возрастанию «asc», либо по убыванию «desc»");

        filter = new AttributeInformation("filter", "object", "",
                "Опционально, JSON-массив объектов-фильтров, каждый объект-фильтр имеет следующую структуру:\n" +
                        "◦ property: <имя поля> — имя поля сортировки (%s);\n" +
                        "◦ operator: <операция> — операция сравнения, одна из:\n" +
                        "%s\n" +
                        "◦ value: <значение> — значение, с которым сравнивается поле.");

        name = new AttributeInformation("name", "string", "",
                "Обязательно в случаях, когда параметр [action=\"action\"]. Наименование команды представления");

        values = new AttributeInformation("values", "object", "",
                "Массив значений по умолчанию объекта. Используется когда параметр [action=\"copy\"]");

        recordId = new AttributeInformation("recordId", "string", "",
                "Идентификтор записи. Используется когда параметр [action=\"copy|attach|detach|preview|report\"]");
        // endregion

        //region response parameters
        success = new AttributeInformation("success", "boolean", "true",
                "Результат обработки запроса — успешно / неуспешно");

        status = new AttributeInformation("status", "integer", "",
                "Целочисленный код ошибки (возвращается в случае неуспешной обработки запроса)");

        server = new AttributeInformation("server", "guid", "3A05EC0E-86BB-4F00-A3D3-270236847D28",
                "Идентификатор сервера в формате UUID, сформировавшего ответ (возвращается в случае успешной обработки запроса)");

        total = new AttributeInformation("total", "integer", "",
                "Количество записей в выборке (только для параметра count: true)");
        // endregion

        input = Arrays.asList(
                request,
                query,
                session,
                action,
                start,
                limit,
                fields,
                count,
                sort,
                filter,
                name,
                data,
                values
        );
        output = Arrays.asList(
                success,
                status,
                server,
                request,
                data,
                total
        );

        codeSnippet = "curl -X POST http://127.0.0.1:9080/request.json \\\n" +
                "   -H 'content-type: application/x-www-form-urlencoded' \\\n" +
                "   -d 'action=read&request=%s&start=0&limit=500&session=8D285D67-B8BD-4856-8FE3-647E4D916289'";
    }

    public List<FieldDescription> getEntityFields() {
        return entityFields;
    }

    public List<BaseInfo> getActions() {
        return actions;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityDescription() {
        return entityDescription;
    }

    public void setEntityDescription(String entityDescription) {
        this.entityDescription = entityDescription;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public AttributeInformation getRequest() {
        return request;
    }

    public void setRequest(AttributeInformation request) {
        this.request = request;
    }

    public AttributeInformation getQuery() {
        return query;
    }

    public void setQuery(AttributeInformation query) {
        this.query = query;
    }

    public AttributeInformation getSession() {
        return session;
    }

    public void setSession(AttributeInformation session) {
        this.session = session;
    }

    public AttributeInformation getAction() {
        return action;
    }

    public void setAction(AttributeInformation action) {
        this.action = action;
    }

    public AttributeInformation getStart() {
        return start;
    }

    public void setStart(AttributeInformation start) {
        this.start = start;
    }

    public AttributeInformation getLimit() {
        return limit;
    }

    public void setLimit(AttributeInformation limit) {
        this.limit = limit;
    }

    public AttributeInformation getFields() {
        return fields;
    }

    public void setFields(AttributeInformation fields) {
        this.fields = fields;
    }

    public AttributeInformation getCount() {
        return count;
    }

    public void setCount(AttributeInformation count) {
        this.count = count;
    }

    public AttributeInformation getSort() {
        return sort;
    }

    public void setSort(AttributeInformation sort) {
        this.sort = sort;
    }

    public AttributeInformation getFilter() {
        return filter;
    }

    public void setFilter(AttributeInformation filter) {
        this.filter = filter;
    }

    public AttributeInformation getSuccess() {
        return success;
    }

    public void setSuccess(AttributeInformation success) {
        this.success = success;
    }

    public AttributeInformation getStatus() {
        return status;
    }

    public void setStatus(AttributeInformation status) {
        this.status = status;
    }

    public AttributeInformation getServer() {
        return server;
    }

    public void setServer(AttributeInformation server) {
        this.server = server;
    }

    public AttributeInformation getData() {
        return data;
    }

    public void setData(AttributeInformation data) {
        this.data = data;
    }

    public AttributeInformation getTotal() {
        return total;
    }

    public void setTotal(AttributeInformation total) {
        this.total = total;
    }

    public List<AttributeInformation> getInput() {
        return input;
    }

    public List<AttributeInformation> getOutput() {
        return output;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public AttributeInformation getValues() {
        return values;
    }
}
