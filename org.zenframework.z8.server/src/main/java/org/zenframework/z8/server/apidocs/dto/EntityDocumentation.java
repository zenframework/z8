package org.zenframework.z8.server.apidocs.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityDocumentation {
    private String name;
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

    private AttributeInformation success;
    private AttributeInformation status;
    private AttributeInformation server;
    private AttributeInformation requestResponse;
    private AttributeInformation data;
    private AttributeInformation total;

    private final List<FieldDescription> entityFields;
    private final List<AttributeInformation> input;
    private final List<AttributeInformation> output;

    public EntityDocumentation() {
        entityFields = new ArrayList<>();
        request = new AttributeInformation("request", "string", "",
                "Обязательно, уникальная строка, идентифицирующая представление данных, к которому формируется запрос;", true);
        query = new AttributeInformation("query", "string", "", 
                "Обязательно в отдельных случаях, уникальная строка, идентифицирующая запрос в рамках представления данных;");
        session = new AttributeInformation("session", "guid", "68322EBA-F143-4997-A7DA-CC67E476B9EA", 
                "Обязательно, идентификатор сессии, полученный в ответе на\n" +
                "запрос авторизации", true);
        action = new AttributeInformation("action", "string", "read", 
                "Действие запроса, один из вариантов:\n" +
                        "new -\n" +
                        "create -\n" +
                        "copy -\n" +
                        "read -\n" +
                        "update -\n" +
                        "destroy -\n" +
                        "export -\n" +
                        "report -\n" +
                        "preview -\n" +
                        "action -\n" +
                        "attach -\n" +
                        "detach -\n" +
                        "content -", true);
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

        success = new AttributeInformation("success", "boolean", "true",
                "Результат обработки запроса — успешно / неуспешно");
        status = new AttributeInformation("status", "integer", "",
                "Целочисленный код ошибки (возвращается в случае неуспешной обработки запроса)");
        server = new AttributeInformation("server", "guid", "3A05EC0E-86BB-4F00-A3D3-270236847D28",
                "Идентификатор сервера в формате UUID, сформировавшего ответ (возвращается в случае успешной обработки запроса)");
        requestResponse = new AttributeInformation("requestResponse", "string", "",
                "Идентификатор представления, сформировавшего ответ");
        data = new AttributeInformation("data", "object", "",
                "Массив объектов-записей, формат записи зависит от заданного представления (если не задан параметр count: true);");
        total = new AttributeInformation("total", "integer", "",
                "Количество записей в выборке (только для параметра count: true)");

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
                filter
        );
        output = Arrays.asList(
                success,
                status,
                server,
                requestResponse,
                data,
                total
        );
    }

    public List<FieldDescription> getEntityFields() {
        return entityFields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public AttributeInformation getRequestResponse() {
        return requestResponse;
    }

    public void setRequestResponse(AttributeInformation requestResponse) {
        this.requestResponse = requestResponse;
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
}
