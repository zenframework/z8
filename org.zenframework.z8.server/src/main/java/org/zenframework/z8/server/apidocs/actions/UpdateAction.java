package org.zenframework.z8.server.apidocs.actions;

import org.zenframework.z8.server.apidocs.parameters.Data;
import org.zenframework.z8.server.apidocs.parameters.SimpleParameters;

public class UpdateAction extends BaseAction {

    public UpdateAction(){
        super();
        inputParameters.add(SimpleParameters.request());
        inputParameters.add(SimpleParameters.action());
        inputParameters.add(new Data());
        inputParameters.add(SimpleParameters.session());
    }

    @Override
    public String getName() {
        return "update";
    }
}
