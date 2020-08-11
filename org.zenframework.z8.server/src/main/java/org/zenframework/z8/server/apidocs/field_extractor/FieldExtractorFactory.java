package org.zenframework.z8.server.apidocs.field_extractor;

import org.zenframework.z8.server.apidocs.dto.FieldDescription;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.StringField;

public class FieldExtractorFactory {

    public static FieldExtractor getExtractor(Field field) {
        if (field.getClass() == StringField.class) {
            return field1 -> new FieldDescription(
                    field1.index(),
                    field1.type().name(),
                    field1.getAttribute("APIDescription"),
                    field1.length.getInt());
        } else if (field instanceof DatetimeField) {
            return field12 -> new FieldDescription(
                    field12.index(),
                    field12.type().name(),
                    field12.getAttribute("APIDescription"),
                    field12.format.get());
        } else {
            return field13 -> new FieldDescription(
                    field13.index(),
                    field13.type().name(),
                    field13.getAttribute("APIDescription"));
        }
    }
}
