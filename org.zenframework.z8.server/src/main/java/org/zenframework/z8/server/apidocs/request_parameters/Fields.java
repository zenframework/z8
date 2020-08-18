package org.zenframework.z8.server.apidocs.request_parameters;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParametr;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import java.util.stream.Collectors;

public class Fields implements IRequestParametr {

    @Override
    public String getKey() {
        return "fields";
    }

    @Override
    public Object getValue(Query query, IActionRequest action) {
        return query.getDataFields()
                .stream()
                .filter(field -> field.hasAttribute("APIDescription"))
                .map(Field::index)
                .collect(Collectors.toList());
    }
}
