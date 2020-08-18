package org.zenframework.z8.server.apidocs.actions;

import org.zenframework.z8.server.apidocs.request_parameters.Fields;
import org.zenframework.z8.server.apidocs.request_parameters.SimpleParameters;

public class DetachAction extends BaseAction {

    public DetachAction(){
        super();
        inputParameters.add(SimpleParameters.request());
        inputParameters.add(SimpleParameters.action());
        inputParameters.add(SimpleParameters.recordId());
        inputParameters.add(SimpleParameters.field());
        inputParameters.add(new Fields());
        inputParameters.add(SimpleParameters.session());
    }

    @Override
    public String getName() {
        return "detach";
    }
}
