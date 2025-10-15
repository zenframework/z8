package org.zenframework.z8.server.ldap;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

public interface Extractor<T> {
	T extract(SearchResult result) throws NamingException;
}
