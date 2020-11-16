package org.zenframework.z8.server.apidocs.actions;

import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParameter;
import org.zenframework.z8.server.apidocs.parameters.Data;
import org.zenframework.z8.server.apidocs.parameters.Info;
import org.zenframework.z8.server.apidocs.parameters.SimpleParameters;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.json.parser.JsonObject;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAction implements IActionRequest {
    protected List<IRequestParameter> inputParameters;
    protected List<IRequestParameter> outputParameters;
    private String request;
    private String response;

    public BaseAction() {
        inputParameters = new ArrayList<>();
        outputParameters = new ArrayList<>();

        outputParameters.add(new Data());
        outputParameters.add(new Info());
        outputParameters.add(SimpleParameters.request());
        outputParameters.add(SimpleParameters.server());
        outputParameters.add(SimpleParameters.success());

    }

    public JsonObject createRequestParameters(Query query) {
        JsonObject requestJson = new JsonObject();
        for (IRequestParameter param : inputParameters) {
            requestJson.put(param.getKey(), param.getValue(query, this));
        }
        return requestJson;
    }

    public JsonObject createResponseParameters(Query query) {
        JsonObject requestJson = new JsonObject();
        for (IRequestParameter param : outputParameters) {
            requestJson.put(param.getKey(), param.getValue(query, this));
        }
        return requestJson;
    }

    @Override
    public void makeExample(Query query) {
        JsonObject requestJson = createRequestParameters(query);
        request = requestJson.toString(4);
        JsonObject resposneJson = createResponseParameters(query);
        response = resposneJson.toString(4);
    }

    @Override
    public String getRequest() {
        return request;
    }

    @Override
    public String getResponse() {
        return response;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
