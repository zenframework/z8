package org.zenframework.z8.server.engine;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.Link;
import org.zenframework.z8.server.logs.Trace;

public class ModelGraph {

	private final Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
	private final Map<String, Integer> priorities = new LinkedHashMap<String, Integer>();

	private ModelGraph() {}

	public int getTablePriority(String name) {
		Integer priority = priorities.get(name);
		return priority == null ? 0 : priority;
	}

	public int getTablePriority(Table table) {
		return getTablePriority(table.name());
	}

	public void print(PrintStream out) {
		for (Map.Entry<String, Integer> entry : priorities.entrySet())
			out.println(entry.getKey() + "[" + entry.getValue() + "] -> " + graph.get(entry.getKey()));
	}
	
	@SuppressWarnings("unchecked")
	private void buildGraph(Collection<Table.CLASS<? extends Table>> tables) {
		for (Table.CLASS<? extends Table> table : tables) {
			String tableName = table.name();
			Set<String> links = new HashSet<String>();
			for (Field.CLASS<? extends Field> field : table.get().getLinks()) {
				if (field instanceof Link.CLASS && field.foreignKey()) {
					String referencedTable = ((Link.CLASS<Link>) field).get().getReferencedTable().name();
					if (!tableName.equals(referencedTable))
						links.add(referencedTable);
				}
			}
			graph.put(tableName, links);
		}
	}
	
	private void fillPriorities() {
		Map<String, Set<String>> graph = copy(this.graph);
		int priority = 0;
		while (!graph.isEmpty()) {
			Set<String> outside = new HashSet<String>();

			Iterator<Map.Entry<String, Set<String>>> it = graph.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Set<String>> entry = it.next();
				if (entry.getValue().isEmpty()) {
					outside.add(entry.getKey());
					priorities.put(entry.getKey(), priority);
					it.remove();
				}
			}

			priority++;
			if (outside.isEmpty()) {
				Trace.logEvent("Tables priorities calculation error: DB schema contains deadlocks\n" + graph);
				break;
			}

			it = graph.entrySet().iterator();
			while (it.hasNext())
				it.next().getValue().removeAll(outside);
		}
		
		Iterator<String> it = graph.keySet().iterator();
		while (it.hasNext())
			priorities.put(it.next(), priority);
	}

	public static ModelGraph newModelGraph(Collection<Table.CLASS<? extends Table>> tables) {
		ModelGraph modelGraph = new ModelGraph();
		modelGraph.buildGraph(tables);
		modelGraph.fillPriorities();
		return modelGraph;
	}

	private static Map<String, Set<String>> copy(Map<String, Set<String>> map) {
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		for (Map.Entry<String, Set<String>> entry : map.entrySet())
			result.put(entry.getKey(), new HashSet<String>(entry.getValue()));
		return result;
	}
	
}
