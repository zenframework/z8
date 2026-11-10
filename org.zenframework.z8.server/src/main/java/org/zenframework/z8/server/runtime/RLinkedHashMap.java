package org.zenframework.z8.server.runtime;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;

public class RLinkedHashMap<Key, Value> extends HashMap<Key, Value> {

	private static final long serialVersionUID = 1195983270713776247L;

	private META meta;

	public RLinkedHashMap() {
	}

	public RLinkedHashMap(Key[] keys, Value[] values) {
		addAll(keys, values);
	}

	protected RLinkedHashMap<Key, Value> setMeta(META meta) {
		this.meta = meta;
		return this;
	}

	@SuppressWarnings("unchecked")
	public RLinkedHashMap<Key, Value> setMeta(Class<?> javaClass, Class<? extends primary>... keys) {
		return setMeta(META.meta(javaClass, keys));
	}

	public META getMeta() {
		return meta;
	}

	@SuppressWarnings("unchecked")
	public void addAll(Object map) {
		putAll((Map<Key, Value>)map);
	}

	@SuppressWarnings("unchecked")
	public void addAll(Object[] keys, Object[] values) {
		for(int i = 0; i < keys.length; i++)
			put((Key)keys[i], (Value)values[i]);
	}

	public RLinkedHashMap<Key, Value> operatorAdd(Object map) {
		RLinkedHashMap<Key, Value> result = new RLinkedHashMap<Key, Value>();
		result.addAll(this);
		result.addAll(map);
		return result;
	}

	public RLinkedHashMap<Key, Value> operatorAdd(Object[] keys, Object[] values) {
		RLinkedHashMap<Key, Value> result = new RLinkedHashMap<Key, Value>();
		result.addAll(this);
		result.addAll(keys, values);
		return result;
	}

	public integer z8_size() {
		return new integer(size());
	}

	public void z8_clear() {
		clear();
	}

	public bool z8_isEmpty() {
		return new bool(isEmpty());
	}

	public Value z8_add(Key key, Value value) {
		return put(key, value);
	}

	public void z8_add(RLinkedHashMap<Key, Value> m) {
		putAll(m);
	}

	public void z8_add(Object[] keys, Object[] values) {
		addAll(keys, values);
	}

	public bool z8_containsKey(Key key) {
		return new bool(containsKey(key));
	}

	public bool z8_containsValue(Value value) {
		return new bool(containsValue(value));
	}

	public Value z8_get(Key key) {
		return get(key);
	}

	@SuppressWarnings("rawtypes")
	public RCollection z8_keys() {
		return new RCollection<Key>(keySet());
	}

	@SuppressWarnings("rawtypes")
	public RCollection z8_values() {
		return new RCollection<Value>(values());
	}

	public Value z8_remove(Key key) {
		return remove(key);
	}
}
