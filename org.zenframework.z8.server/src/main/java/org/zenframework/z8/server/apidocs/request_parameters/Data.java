package org.zenframework.z8.server.apidocs.request_parameters;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParametr;
import org.zenframework.z8.server.apidocs.utils.GsonIntegrator;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.JsonWriter;

public class Data implements IRequestParametr {

    @Override
    public String getKey() {
        return "data";
    }

    @Override
    public Object getValue(Query query, IActionRequest action) {
        JsonWriter writer = new JsonWriter();
        writer.startArray();
        writer.startObject();

        query.getDataFields()
                .stream()
                .filter(field -> field.hasAttribute("APIDescription"))
                .forEach(field -> field.writeData(writer));

        writer.finishObject();
        writer.finishArray();
        return GsonIntegrator.fromJson(writer.toString());
    }
}
