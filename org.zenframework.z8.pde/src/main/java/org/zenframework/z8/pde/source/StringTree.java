package org.zenframework.z8.pde.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.ISource;

public class StringTree {
	public StringTree(List<String> s) {
		values = s;
		children = new HashMap<String, StringTree>();
	}

	public StringTree(String s) {
		values = new ArrayList<String>(1);
		values.add(s);
		children = new HashMap<String, StringTree>();
	}

	public StringTree() {
		values = new ArrayList<String>();
		children = new HashMap<String, StringTree>();
	}

	public void addValue(String s) {
		if(!values.contains(s))
			values.add(s);
	}

	public String name() {
		if(values.size() == 0)
			return null;
		return values.get(0);
	}

	private List<String> values;
	private Map<String, StringTree> children;
	private List<String> findPath;
	private List<IPosition> expressions = new ArrayList<IPosition>();
	private ISource source;

	public String root; // FIXME Dirty workaround

	public boolean isLeaf() {
		return children.size() == 0;
	}

	public StringTree find(String s) {
		findPath = new ArrayList<String>(1);
		if(values.contains(s))
			return this;
		for(String key : children.keySet()) {
			findPath.clear();
			findPath.add(key);
			StringTree child = children.get(key);
			StringTree result = child.find(s);
			if(result != null) {
				findPath.addAll(child.findPath);
				return result;
			}
		}
		findPath = null;
		return null;
	}

	public StringTree get(List<String> path) {
		if(path == null)
			return null;
		if(path.size() == 0)
			return this;
		String key = path.get(0);
		List<String> next = path.subList(1, path.size());
		StringTree child = children.get(key);
		if(child == null)
			return null;
		return child.get(next);
	}

	public Map<String, StringTree> getChildren() {
		return children;
	}

	public List<String> getFindPath() {
		return findPath;
	}

	public List<List<String>> getAllPaths() {
		List<List<String>> result = new ArrayList<List<String>>();
		parseForPaths(this, result, new ArrayList<String>());
		return result;
	}

	private void parseForPaths(StringTree t, List<List<String>> paths, List<String> currPath) {
		paths.add(new ArrayList<String>(currPath));
		for(String s : t.getChildren().keySet()) {
			currPath.add(s);
			parseForPaths(t.getChildren().get(s), paths, currPath);
			currPath.remove(currPath.size() - 1);
		}
	}

	public List<String> getValues() {
		return values;
	}

	protected String print(int level) {
		String tab = "";
		for(int i = 0; i < level; i++)
			tab += "-";
		String result = "";
		result += tab;
		for(String value : values)
			result += value + ",";
		result += "\n";
		for(String key : children.keySet()) {
			result += tab + key + ":\n";
			result += children.get(key).print(level + 1);
		}
		return result;
	}

	public String print() {
		return print(0);
	}

	public void addWhereExpression(IPosition source) {
		expressions.add(source);
	}

	public List<IPosition> getWhereExpressions() {
		return expressions;
	}

	public void setSource(ISource source) {
		this.source = source;
	}

	public ISource getSource() {
		return source;
	}

}
