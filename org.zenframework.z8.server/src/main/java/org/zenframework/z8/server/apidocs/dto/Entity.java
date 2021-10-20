package org.zenframework.z8.server.apidocs.dto;

import org.zenframework.z8.server.apidocs.IActionRequest;

import java.util.ArrayList;
import java.util.List;

public class Entity {
    private String entityName;
    private String entityDescription;
    private String entityId;
    private String contentParams;
    private String requestAttributes;
    private List<IActionRequest> actions;
    private List<FieldDescription> entityFields;
    private List<EntityAction> entityActions;
    private List<BaseInfo> relatedEntities;

    public Entity() {
        actions = new ArrayList<>();
        entityFields = new ArrayList<>();
        entityActions = new ArrayList<>();
        relatedEntities = new ArrayList<>();
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityDescription() {
        return entityDescription;
    }

    public void setEntityDescription(String entityDescription) {
        this.entityDescription = entityDescription;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public List<IActionRequest> getActions() {
        return actions;
    }

    public void setActions(List<IActionRequest> actions) {
        this.actions = actions;
    }

    public List<FieldDescription> getEntityFields() {
        return entityFields;
    }

    public void setEntityFields(List<FieldDescription> entityFields) {
        this.entityFields = entityFields;
    }

    public List<EntityAction> getEntityActions() {
        return entityActions;
    }

    public void setEntityActions(List<EntityAction> entityActions) {
        this.entityActions = entityActions;
    }

    public List<BaseInfo> getRelatedEntities() {
        return relatedEntities;
    }

    public String getContentParams() {
        return contentParams;
    }

    public void setContentParams(String contentParams) {
        this.contentParams = contentParams;
    }

    public String getRequestAttributes() {
        return requestAttributes;
    }

    public void setRequestAttributes(String requestAttributes) {
        this.requestAttributes = requestAttributes;
    }
}
