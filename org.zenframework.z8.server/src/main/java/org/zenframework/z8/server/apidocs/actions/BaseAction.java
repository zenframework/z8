package org.zenframework.z8.server.apidocs.actions;

import com.google.gson.internal.LinkedTreeMap;
import org.zenframework.z8.server.apidocs.IActionRequest;
import org.zenframework.z8.server.apidocs.IRequestParametr;
import org.zenframework.z8.server.apidocs.request_parameters.Data;
import org.zenframework.z8.server.apidocs.request_parameters.Info;
import org.zenframework.z8.server.apidocs.request_parameters.SimpleParameters;
import org.zenframework.z8.server.apidocs.utils.GsonIntegrator;
import org.zenframework.z8.server.base.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseAction implements IActionRequest {
    protected List<IRequestParametr> inputParameters;
    protected List<IRequestParametr> outputParameters;
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

    public Map<String, Object> createRequestParameters(Query query) {
        return makeMap(inputParameters, query);
    }

    public Map<String, Object> createResponseParameters(Query query) {
        return makeMap(outputParameters, query);
    }

    protected Map<String, Object> makeMap(List<IRequestParametr> parametrs, Query query) {
        Map<String, Object> data = new LinkedTreeMap<>();
        for (IRequestParametr param : parametrs) {
            data.put(param.getKey(), param.getValue(query, this));
        }
        return data;
    }

    @Override
    public void makeExample(Query query) {
        request = GsonIntegrator.toJson(createRequestParameters(query));
        response = GsonIntegrator.toJson(createResponseParameters(query));
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
