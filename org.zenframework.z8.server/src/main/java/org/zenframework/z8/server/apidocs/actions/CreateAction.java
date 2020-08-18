package org.zenframework.z8.server.apidocs.actions;

import org.zenframework.z8.server.apidocs.request_parameters.*;

public class CreateAction extends BaseAction {

    public CreateAction(){
        super();
        inputParameters.add(SimpleParameters.request());
        inputParameters.add(SimpleParameters.action());
        inputParameters.add(new Data());
        inputParameters.add(SimpleParameters.session());
    }

    @Override
    public String getName() {
        return "create";
    }
}
