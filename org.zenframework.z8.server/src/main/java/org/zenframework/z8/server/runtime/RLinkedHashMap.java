package org.zenframework.z8.server.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.integer;

public class RLinkedHashMap<Key, Value> extends LinkedHashMap<Key, Value> {

    private static final long serialVersionUID = 1195983270713776247L;

    private boolean modified = false;
    
    public RLinkedHashMap() {}

    public RLinkedHashMap(Key[] keys, Value[] values) {
        assert (keys.length == values.length);

        for(int i = 0; i < keys.length; i++) {
            put(keys[i], values[i]);
        }
    }

    public void operatorAssign(Object map) {
        clear();
        operatorAddAssign(map);
    }

    @SuppressWarnings("unchecked")
    public void operatorAddAssign(Object map) {
        putAll((RLinkedHashMap<Key, Value>)map);
    }

    public RLinkedHashMap<Key, Value> operatorAdd(Object map) {
        RLinkedHashMap<Key, Value> result = new RLinkedHashMap<Key, Value>();
        result.operatorAddAssign(this);
        result.operatorAddAssign(map);
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

    public bool z8_containsKey(Key key) {
        return new bool(containsKey(key));
    }

    public bool z8_containsValue(Value value) {
        return new bool(containsValue(value));
    }

    public Value z8_get(Key key) {
        if(!containsKey(key)) {
            throw new RuntimeException("Key '" + key + "' was not found");
        }
        return get(key);
    }

    public RCollection<Key> z8_keys() {
        return new RCollection<Key>(keySet());
    }

    public RCollection<Value> z8_values() {
        return new RCollection<Value>(values());
    }

    @Override
    public void clear() {
        modified();
        super.clear();
    }

    @Override
    public Value put(Key key, Value value) {
        modified();
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends Key, ? extends Value> m) {
        modified();
        super.putAll(m);
    }

    @Override
    public Value remove(Object key) {
        modified();
        return super.remove(key);
    }
    
    public boolean isModified() {
        return modified;
    }
    
    private void modified() {
        if (!modified) {
            modified = true;
        }
    }

}
