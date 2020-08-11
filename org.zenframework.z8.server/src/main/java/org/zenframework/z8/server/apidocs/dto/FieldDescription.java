package org.zenframework.z8.server.apidocs.dto;

public class FieldDescription {
    private String name;
    private String type;
    private Integer length;
    private String format;
    private String description;

    public FieldDescription(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public FieldDescription(String name, String type, String description, Integer length) {
        this(name, type,description);
        this.length = length;
    }

    public FieldDescription(String name, String type, String description, String format) {
        this(name, type,description);
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Integer getLength() {
        return length;
    }

    public String getFormat() {
        return format;
    }

    public String getDescription() {
        return description;
    }
}
