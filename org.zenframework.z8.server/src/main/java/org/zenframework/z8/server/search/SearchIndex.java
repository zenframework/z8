package org.zenframework.z8.server.search;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: volkov
 * Date: 25.12.14
 * Time: 16:02
 * To change this template use File | Settings | File Templates.
 */
public interface SearchIndex {

    boolean updateDocument(String recordId, String fullText);
    
    boolean deleteDocument(String recordId);
    
    void commit();

    void clearIndex();

    Collection<String> search(String target, int hitsPerPage);

}
