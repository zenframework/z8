package org.zenframework.z8.server.security;

public class Form implements IForm {
    private static final long serialVersionUID = -4771966559756775051L;
    private String id;
    private String name;
    private String description;
    private String ownerName;
    private String ownerId;
    private String groupName;
    private String groupId;

    private IAccess access = new Access();

    public Form() {}

    @Override
    public String toString() {
        return id + groupId;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public IAccess getAccess() {
        return access;
    }

    public void setAccess(IAccess access) {
        this.access = access;
    }
}
