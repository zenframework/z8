package org.zenframework.z8.server.apidocs.field_extractor;

import org.zenframework.z8.server.apidocs.dto.FieldDescription;
import org.zenframework.z8.server.base.table.value.*;
import org.zenframework.z8.server.json.Json;

public class FieldExtractorFactory {

    public static FieldExtractor getExtractor(Field field) {
        if (field.getClass() == StringField.class) {
            return field1 -> new FieldDescription(
                    field1.index(),
                    field1.type().name(),
                    field1.getAttribute(Json.apiDescription.toString()))
                    .setLength(field1.length.getInt());
        } else if (field instanceof DatetimeField) {
            return field12 -> new FieldDescription(
                    field12.index(),
                    field12.type().name(),
                    field12.getAttribute(Json.apiDescription.toString()))
                    .setFormat(field12.format.get());
        } else if (field instanceof DecimalExpression) {
            return field15 -> new FieldDescription(
                    field15.index(),
                    field15.type().name(),
                    field15.getAttribute(Json.apiDescription.toString()))
                    .setFormat(field15.format.get());
        } else if (field instanceof Link) {
            return field13 -> new FieldDescription(
                    field13.index(),
                    field13.type().name(),
                    field13.getAttribute(Json.apiDescription.toString()))
                    .setReference(((Link) field13).getQuery().classId());
        } else {
            return field14 -> new FieldDescription(
                    field14.index(),
                    field14.type().name(),
                    field14.getAttribute(Json.apiDescription.toString()));
        }
    }
}
