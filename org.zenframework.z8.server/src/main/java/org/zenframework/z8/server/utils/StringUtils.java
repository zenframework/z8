package org.zenframework.z8.server.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class StringUtils {
    static public String unescapeJava(String string) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader("key=" + string));
        } catch (IOException e) {
            return string;
        }
        return properties.getProperty("key");
    }
}
