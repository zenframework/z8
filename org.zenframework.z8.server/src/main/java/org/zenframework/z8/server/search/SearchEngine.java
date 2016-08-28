package org.zenframework.z8.server.search;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.exception;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;

public class SearchEngine {
	static private class strings {
		static String ErrorIndexUndefined = "SearchEngine.errorIndexUndefined";
	}

	static public final SearchEngine INSTANCE = new SearchEngine();

	private final Map<String, SearchIndex> indicies = new HashMap<String, SearchIndex>();

	public SearchEngine() {
	}

	public void updateRecord(Query query, String recordId) {
		query.saveState();

		if(query.readFirst(query.getSearchFields(), new Equ(query.getSearchId(), recordId))) {
			String fullText = query.getRecordFullText();
			if(fullText != null && !fullText.isEmpty()) {
				try {
					SearchIndex index = getIndex(query);
					Field searchId = query.getSearchId();
					String searchRecordId = searchId != null ? searchId.get().toString() : null;
					index.updateDocument(searchRecordId == null || searchRecordId.equals(guid.NULL) ? recordId : searchRecordId, fullText);
					index.commit();
				} catch(Throwable e) {
					Trace.logEvent(e);
				}
			}
		}

		query.restoreState();
	}

	public void deleteRecord(Query query, String recordId) {
		try {
			SearchIndex index = getIndex(query);
			index.deleteDocument(recordId);
			index.commit();
		} catch(Throwable e) {
			Trace.logEvent(e);
		}
	}

	public Collection<String> searchRecords(Query query, String target) {
		SearchIndex index = getIndex(query);
		return (target == null || target.isEmpty() || index == null) ? Collections.<String> emptyList() : index.search(target, 50);
	}

	private Table findTable(String index) {
		Collection<Table.CLASS<? extends Table>> classes = Runtime.instance().tables();

		for(Table.CLASS<? extends Table> cls : classes) {
			if(index.equals(cls.getAttribute(IObject.SearchIndex))) {
				Table table = cls.newInstance();
				if(!table.searchFields.isEmpty())
					return table;
			}
		}

		return null;
	}

	public void rebuildIndex(String indexId) {
		Table table = findTable(indexId);

		if(table != null) {
			SearchIndex index = getIndex(indexId);
			index.clearIndex();
			updateIndex(index, table);
		}
	}

	public void updateIndex(Query query) {
		updateIndex(getIndex(query), query);
	}

	private void updateIndex(SearchIndex index, Query query) {
		Collection<Field> fields = query.getSearchFields();

		if(fields.isEmpty())
			return;

		query.saveState();

		query.read(fields);

		while(query.next())
			index.updateDocument(query.getSearchId().get().toString(), query.getRecordFullText());

		index.commit();

		query.restoreState();
	}

	private SearchIndex getIndex(Query query) {
		String indexId = query.getAttribute(IObject.SearchIndex);
		if(indexId == null)
			throw new exception(Resources.format(strings.ErrorIndexUndefined, query.classId()));
		return getIndex(indexId);
	}

	private SearchIndex getIndex(String indexId) {
		SearchIndex index = indicies.get(indexId);
		if(index == null) {
			index = new LuceneSearchIndexImpl(indexId);
			indicies.put(indexId, index);
		}
		return index;
	}

	static public void z8_rebuildIndex(string indexId) {
		INSTANCE.rebuildIndex(indexId.get());
	}

	static public void z8_updateIndex(Query.CLASS<? extends Query> queryClass) {
		INSTANCE.updateIndex(queryClass.get());
	}

	static public RCollection<string> z8_searchRecords(Query.CLASS<? extends Query> queryClass, string target) {
		Collection<String> recordIds = INSTANCE.searchRecords(queryClass.get(), target.toString());
		RCollection<string> result = new RCollection<string>(recordIds.size(), true);
		for(String recordId : recordIds)
			result.add(new string(recordId));
		return result;
	}

	static public void z8_updateRecord(Query.CLASS<? extends Query> queryClass, string recordId) {
		INSTANCE.updateRecord(queryClass.get(), recordId.get());
	}

	static public void z8_deleteRecord(Query.CLASS<? extends Query> queryClass, string recordId) {
		INSTANCE.deleteRecord(queryClass.get(), recordId.get());
	}
}
