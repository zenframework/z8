package org.zenframework.z8.server.config;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;

public class FreeMarkerConfiguration {
    private static Configuration freeMarkerCfg;

    private FreeMarkerConfiguration() {}

    public static Configuration getFreeMarkerCfg() throws IOException {
        if (freeMarkerCfg == null) {
            buildFreeMarkerConfiguration();
        }
        return freeMarkerCfg;
    }

    private static void buildFreeMarkerConfiguration() throws IOException {
        freeMarkerCfg = new Configuration(Configuration.VERSION_2_3_30);
        freeMarkerCfg.setClassForTemplateLoading(FreeMarkerConfiguration.class, "/templates/");
        freeMarkerCfg.setDefaultEncoding("UTF-8");
        freeMarkerCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freeMarkerCfg.setLogTemplateExceptions(false);
        freeMarkerCfg.setWrapUncheckedExceptions(true);
        freeMarkerCfg.setFallbackOnNullLoopVariable(false);
    }
}
