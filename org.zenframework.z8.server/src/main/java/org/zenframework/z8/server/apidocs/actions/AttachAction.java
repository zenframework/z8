package org.zenframework.z8.server.apidocs.actions;

import org.zenframework.z8.server.apidocs.request_parameters.Fields;
import org.zenframework.z8.server.apidocs.request_parameters.SimpleParameters;

public class AttachAction extends BaseAction {

    public AttachAction(){
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
        return "attach";
    }

    @Override
    public String getDescription() {
        return "При выполнении запроса в заголовках указывается: \"Content-Type: multipart/form-data\". " +
                "В теле запрос так же прикрепляется загружаемый файл";
    }
}
