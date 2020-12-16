package org.zenframework.z8.server.routing;

import org.zenframework.z8.server.base.table.system.SystemTools;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.runtime.CLASS;
import org.zenframework.z8.server.runtime.IRuntime;
import org.zenframework.z8.server.runtime.OBJECT;


/**
 * Represents a map of entities aliases.
 * Where the key is an alias of the entity that is used in URL to access the entity,
 * the value is a value of {@link CLASS#classId()}
 *
 * Each entity has a default class name as the first alias and the next is custom,
 * which is set using the {@code uriPath} attribute
 */
public class EntitiesAliasMap extends JsonObject {
	private static EntitiesAliasMap entityAlias;

	public static synchronized EntitiesAliasMap instance() {
		if (entityAlias == null) {
			entityAlias = new EntitiesAliasMap();
		}
		return entityAlias;
	}

	private EntitiesAliasMap() {
		IRuntime runtime = Runtime.instance();
		for (OBJECT.CLASS<? extends OBJECT> entry : runtime.entries()) {
			if (entry.getJavaClass().equals(SystemTools.class)) {
				continue;
			}
			registerAlias(entry);
		}

		// registration aliases from items of the SystemTool menu
		SystemTools systemToolsCLASS = (SystemTools) runtime.getEntry(SystemTools.class.getName()).newInstance();
		for (OBJECT.CLASS<? extends OBJECT> item : systemToolsCLASS.getRunnables()) {
			registerAlias(item);
		}
	}

	private void registerAlias(OBJECT.CLASS<? extends OBJECT> entry ) {
		String uriPath = entry.getAttribute("uriPath");
		if (uriPath != null) {
			put(validateAlias(uriPath), entry.classId());
		}
		String clsName = validateAlias(entry.getJavaClass().getSimpleName());
		put(clsName, entry.classId());
	}

	private String validateAlias(String slug) {
		return slug.toLowerCase().trim();
	}
}
