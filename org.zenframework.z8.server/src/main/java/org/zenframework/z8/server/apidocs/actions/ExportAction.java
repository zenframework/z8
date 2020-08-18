package org.zenframework.z8.server.apidocs.actions;

import org.zenframework.z8.server.apidocs.request_parameters.*;

import java.util.ArrayList;

public class ExportAction extends BaseAction {

    public ExportAction(){
        super();
        inputParameters.add(SimpleParameters.request());
        inputParameters.add(SimpleParameters.action());
        inputParameters.add(SimpleParameters.format());
        inputParameters.add(new Columns());
        inputParameters.add(new Filter());
        inputParameters.add(new Sort());
        inputParameters.add(SimpleParameters.session());

        outputParameters = new ArrayList<>();

        outputParameters.add(new Info());
        outputParameters.add(SimpleParameters.request());
        outputParameters.add(SimpleParameters.server());
        outputParameters.add(SimpleParameters.success());
    }

    @Override
    public String getName() {
        return "export";
    }
}
