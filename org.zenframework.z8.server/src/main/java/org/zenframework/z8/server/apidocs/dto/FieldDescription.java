package org.zenframework.z8.server.apidocs.dto;

public class FieldDescription extends BaseInfo{
    private String type;
    private Integer length;
    private String format;
    private String reference;

    public FieldDescription(String name, String type, String description) {
        super(name, description);
        this.type = type;
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
