package org.zenframework.z8.server.apidocs.actions;

import org.zenframework.z8.server.apidocs.request_parameters.Data;
import org.zenframework.z8.server.apidocs.request_parameters.SimpleParameters;

public class CopyAction extends BaseAction {

    public CopyAction(){
        super();
        inputParameters.add(SimpleParameters.request());
        inputParameters.add(SimpleParameters.action());
        inputParameters.add(new Data());
        inputParameters.add(SimpleParameters.recordId());
        inputParameters.add(SimpleParameters.session());
    }

    @Override
    public String getName() {
        return "copy";
    }
}
