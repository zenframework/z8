package org.zenframework.z8.server.apidocs.dto;

public class FieldDescription {
    private String name;
    private String type;
    private Integer length;
    private String format;
    private String description;
    private String reference;

    public FieldDescription(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
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

    public String getReference() {
        return reference;
    }

    public FieldDescription setFormat(String format) {
        this.format = format;
        return this;
    }

    public FieldDescription setLength(Integer length) {
        this.length = length;
        return this;
    }

    public FieldDescription setReference(String reference) {
        this.reference = reference;
        return this;
    }
}
