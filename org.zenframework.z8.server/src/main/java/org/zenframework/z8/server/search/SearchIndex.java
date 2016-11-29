package org.zenframework.z8.server.search;

import java.util.Collection;

public interface SearchIndex {

	boolean updateDocument(String recordId, String fullText);

	boolean deleteDocument(String recordId);

	void commit();

	void clearIndex();

	Collection<String> search(String target, int hitsPerPage);

}