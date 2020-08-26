package org.zenframework.z8.server.apidocs.actions;

import org.zenframework.z8.server.apidocs.request_parameters.Fields;
import org.zenframework.z8.server.apidocs.request_parameters.Filter;
import org.zenframework.z8.server.apidocs.request_parameters.SimpleParameters;
import org.zenframework.z8.server.apidocs.request_parameters.Sort;

public class ReadAction extends BaseAction {

    public ReadAction(){
        super();
        inputParameters.add(SimpleParameters.action());
        inputParameters.add(SimpleParameters.request());
        inputParameters.add(new Fields());
        inputParameters.add(new Filter());
        inputParameters.add(new Sort());
        inputParameters.add(SimpleParameters.start());
        inputParameters.add(SimpleParameters.limit());
        inputParameters.add(SimpleParameters.session());
    }

    @Override
    public String getName() {
        return "read";
    }

}
