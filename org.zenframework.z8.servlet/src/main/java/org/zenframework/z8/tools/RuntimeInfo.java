package org.zenframework.z8.tools;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.zenframework.z8.auth.AuthorityCenter;
import org.zenframework.z8.auth.AuthorityCenterMain;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.ApplicationServerMain;
import org.zenframework.z8.server.ie.Import;
import org.zenframework.z8.server.json.parser.JsonObject;

public class RuntimeInfo {

    private static final String OPT_CONFIG = "config";
    private static final String OPT_STRUCTURE = "structure";

    private static final Options OPTIONS = getOptions();

    public static void main(String[] args) throws Exception {
        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(OPTIONS, args);
            ServerConfig config = null;
            if (cmd.hasOption(OPT_CONFIG)) {
                config = new ServerConfig(cmd.getOptionValue(OPT_CONFIG));
            }
            if (cmd.hasOption(OPT_STRUCTURE)) {
                AuthorityCenterMain.start(config);
                ApplicationServerMain.start(config);
                JsonObject structure = Import.getTablesStructure();
                System.out.println(structure.toString(4));
                ApplicationServer.get().stop();
                AuthorityCenter.get().stop();
                System.exit(0);
            } else {
                throw new Exception("Incorrect arguments");
            }
        } catch (Exception e) {
            e.printStackTrace();
            new HelpFormatter().printHelp("java " + RuntimeInfo.class.getCanonicalName(), OPTIONS);
            System.exit(0);
        }
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption(OPT_CONFIG, true, "Path to project.xml");
        options.addOption(OPT_STRUCTURE, false, "Prints tables structure in JSON format");
        return options;
    }
}
