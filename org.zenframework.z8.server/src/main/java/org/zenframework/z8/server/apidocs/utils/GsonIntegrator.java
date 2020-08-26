package org.zenframework.z8.server.apidocs.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonIntegrator {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static Object fromJson(String json) {
        return gson.fromJson(json, Object.class);
    }
}
