package org.zenframework.z8.server.apidocs.actions;

import org.zenframework.z8.server.apidocs.request_parameters.Data;
import org.zenframework.z8.server.apidocs.request_parameters.Info;
import org.zenframework.z8.server.apidocs.request_parameters.SimpleParameters;

import java.util.ArrayList;

public class DestroyAction extends BaseAction {

    public DestroyAction(){
        super();
        inputParameters.add(SimpleParameters.request());
        inputParameters.add(SimpleParameters.action());
        inputParameters.add(new Data());
        inputParameters.add(SimpleParameters.session());

        outputParameters = new ArrayList<>();

        outputParameters.add(new Info());
        outputParameters.add(SimpleParameters.request());
        outputParameters.add(SimpleParameters.server());
        outputParameters.add(SimpleParameters.success());
    }

    @Override
    public String getName() {
        return "destroy";
    }
}
