package org.zenframework.z8.server.apidocs.field.extractor;

import org.zenframework.z8.server.apidocs.dto.FieldDescription;
import org.zenframework.z8.server.base.table.value.Field;

public interface FieldExtractor {
    FieldDescription extract(Field field);
}