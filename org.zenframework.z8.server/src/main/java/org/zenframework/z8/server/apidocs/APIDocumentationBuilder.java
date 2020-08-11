package org.zenframework.z8.server.apidocs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import org.zenframework.z8.server.apidocs.dto.EntityDocumentation;
import org.zenframework.z8.server.apidocs.field_extractor.FieldExtractor;
import org.zenframework.z8.server.apidocs.field_extractor.FieldExtractorFactory;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.expressions.Operation;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.runtime.OBJECT;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class APIDocumentationBuilder {

    private static final String linkToFieldsTmpl = "<a href=\"#%s\">перечень полей определяется представлением</a>";
    private static final List<Map<String, String>> sortParamExample = Collections.singletonList(new LinkedTreeMap<>());
    private static final List<Map<String, String>> filterParamExample = Collections.singletonList(new LinkedTreeMap<>());

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Map<String, List<EntityDocumentation>> build (Collection<OBJECT.CLASS<? extends OBJECT>> entities) {
        List<EntityDocumentation> resultData = new ArrayList<>();
        for(OBJECT.CLASS<? extends OBJECT> entity : entities) {
            Query query = (Query) entity.newInstance();
            EntityDocumentation entityDocumentation = new EntityDocumentation();

            entityDocumentation.setName(query.getAttribute("name"));
            entityDocumentation.setEntityId(entity.classId());

            // set a list of available fields of the entity
            for(Field field : query.getDataFields()) {
                if (field.hasAttribute("APIDescription")) {
                    FieldExtractor fieldExtractor = FieldExtractorFactory.getExtractor(field);
                    entityDocumentation.getEntityFields().add(fieldExtractor.extract(field));
                }
            }

            // set value for param "request"
            entityDocumentation.getRequest().setValue(entity.classId());
            // set value for param "requestResponse"
            entityDocumentation.getRequestResponse().setValue(entity.classId());

            prepareFieldsParam(entityDocumentation, entity);
            prepareSortParam(entityDocumentation, entity);
            prepareFilterParam(entityDocumentation, entity);
            prepareDataParam(entityDocumentation, entity);

            resultData.add(entityDocumentation);
        }
        return Collections.singletonMap("entities", resultData);
    }

    private void prepareFieldsParam(EntityDocumentation entityDocumentation, OBJECT.CLASS<? extends OBJECT> entity) {
        Query query = (Query) entity.newInstance();
        entityDocumentation.getFields().setValue(
                gson.toJson(query.getDataFields()
                        .stream()
                        .filter(field -> field.hasAttribute("APIDescription"))
                        .map(Field::index)
                        .collect(Collectors.toList())
                ));
    }

    private void prepareSortParam(EntityDocumentation entityDocumentation, OBJECT.CLASS<? extends OBJECT> entity) {
        Query query = (Query) entity.newInstance();
        sortParamExample.get(0).put("property", query.primaryKey().index());
        sortParamExample.get(0).put("direction", "asc");
        entityDocumentation.getSort().setValue(gson.toJson(sortParamExample));

        String linkToFields = String.format(linkToFieldsTmpl, entityDocumentation.getEntityId());
        entityDocumentation.getSort().setDescription(
                String.format(entityDocumentation.getSort().getDescription(), linkToFields));

    }

    private void prepareFilterParam(EntityDocumentation entityDocumentation, OBJECT.CLASS<? extends OBJECT> entity) {
        Query query = (Query) entity.newInstance();
        filterParamExample.get(0).put("property", query.primaryKey().index());
        filterParamExample.get(0).put("operator", Operation.Eq.toString());
        filterParamExample.get(0).put("value", "3468CA6A-853F-42A1-9DF7-F92FC951AB20");
        entityDocumentation.getFilter().setValue(gson.toJson(filterParamExample));

        String operationList = Stream.of(Operation.values()).map(Operation::toString).collect(Collectors.joining("\n    -"));
        String linkToFields = String.format(linkToFieldsTmpl, entityDocumentation.getEntityId());
        entityDocumentation.getFilter().setDescription(
                String.format(entityDocumentation.getFilter().getDescription(), linkToFields, operationList));
    }

    private void prepareDataParam(EntityDocumentation entityDocumentation, OBJECT.CLASS<? extends OBJECT> entity) {
        Query query = (Query) entity.newInstance();
        JsonWriter writer = new JsonWriter();
        writer.startObject();
        writer.startArray(Json.data);
        writer.startObject();

        query.getDataFields()
                .stream()
                .filter(field -> field.hasAttribute("APIDescription"))
                .forEach(field -> field.writeData(writer));

        writer.finishObject();
        writer.finishArray();
        writer.finishObject();
        // toJson then fromJson the reason is just to make pretty json
        entityDocumentation.getData().setValue(gson.toJson(gson.fromJson(writer.toString(), Object.class)));
    }
}
