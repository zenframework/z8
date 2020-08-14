package org.zenframework.z8.server.apidocs.dto;


public class AttributeInformation extends BaseInfo {
    private String type;
    private String value;
    private Boolean required;

    public AttributeInformation(String name, String type, String value, String description, Boolean required) {
        super(name, description);
        this.type = type;
        this.value = value;
        this.required = required;
    }
    
    public AttributeInformation(String name, String type, String value, String description) {
        this(name, type, value, description, false);
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
    
    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
