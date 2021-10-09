package org.zenframework.z8.server.apidocs.dto;

import org.zenframework.z8.server.apidocs.IActionRequest;

import java.util.List;

public class Documentation {
	private List<Entity> entities;
	private List<IActionRequest> actions;

	public List<Entity> getEntities() {
		return entities;
	}

	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}

	public List<IActionRequest> getActions() {
		return actions;
	}

	public void setActions(List<IActionRequest> actions) {
		this.actions = actions;
	}
}
