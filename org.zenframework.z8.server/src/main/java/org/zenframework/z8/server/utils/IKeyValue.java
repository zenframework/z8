package org.zenframework.z8.server.utils;

public interface IKeyValue<K, V> {

	void set(K key, V value);

	V get(K key);

}
