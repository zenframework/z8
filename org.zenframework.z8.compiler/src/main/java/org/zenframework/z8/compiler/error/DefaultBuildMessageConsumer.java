package org.zenframework.z8.compiler.error;

import org.zenframework.z8.compiler.workspace.Resource;

public class DefaultBuildMessageConsumer implements IBuildMessageConsumer {
    private int errors = 0;
    private int warnings = 0;

    @Override
    final public int getErrorCount() {
        return errors;
    }

    @Override
    final public int getWarningCount() {
        return warnings;
    }

    @Override
    public void consume(BuildMessage message) {
        String status = "";

        if(message instanceof BuildError) {
            errors++;
            status = "ERROR: ";
        }
        else if(message instanceof BuildWarning) {
            warnings++;
            status = "WARNING: ";
        }

        System.out.println(status + message.format());
    }

    @Override
    public void report(Resource resource, BuildMessage[] messages) {
        clearMessages(resource);

        for(BuildMessage message : messages) {
            consume(message);
        }
    }

    @Override
    public void clearMessages(Resource resource) {}
}
