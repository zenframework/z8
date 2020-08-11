package org.zenframework.z8.server.apidocs.dto;


public class AttributeInformation {
    private String name;
    private String type;
    private String value;
    private String description;
    private Boolean required;

    public AttributeInformation(String name, String type, String value, String description, Boolean required) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.description = description;
        this.description = description;
        this.required = required;
    }
    public AttributeInformation(String name, String type, String value, String description) {
        this(name, type, value, description, false);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
