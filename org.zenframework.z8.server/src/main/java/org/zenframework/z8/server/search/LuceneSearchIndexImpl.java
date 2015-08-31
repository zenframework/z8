package org.zenframework.z8.server.search;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;

import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.file;

public class LuceneSearchIndexImpl implements SearchIndex {

    private static final Log LOG = LogFactory.getLog(LuceneSearchIndexImpl.class);

    private static final String FIELD_RECORD_ID = "recordId";
    private static final String FIELD_FULL_TEXT = "fullText";

    private final Analyzer analyzer;
    private final Directory directory;
    private final IndexWriter writer;

    public LuceneSearchIndexImpl(Directory directory) {
        this.directory = directory;
        analyzer = new RussianAnalyzer(Version.LUCENE_48);
        //analyzer = new StandardAnalyzer(Version.LUCENE_48);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        try {
            writer = new IndexWriter(directory, config);
        } catch (IOException e) {
            throw new RuntimeException("Can't create index writer", e);
        }
    }

    //public LuceneIndexImpl() {
    //    this(new RAMDirectory());
    //}

    public LuceneSearchIndexImpl(String indexId) {
        this(getMMapDirectory(indexId));
    }

    @Override
    public Collection<String> search(String target, int hitsPerPage) {
        try {
            org.apache.lucene.search.Query query = new QueryParser(Version.LUCENE_48, FIELD_FULL_TEXT, analyzer)
                    .parse(target);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
            IndexSearcher searcher = getIndexSearcher();
            searcher.search(query, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            RCollection<String> result = new RCollection<String>(hits.length, true);
            for (int i = 0; i < hits.length; ++i) {
                result.add(searcher.doc(hits[i].doc).get(FIELD_RECORD_ID));
            }
            return result;
        } catch (Exception e) {
            LOG.error("Can't find documents for query '" + target + "'", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void clearIndex() {
        try {
            writer.deleteAll();
        } catch (IOException e) {
            LOG.error("Can't clear index", e);
        }
    }

    @Override
    public void commit() {
        try {
            writer.commit();
        } catch (IOException e) {
            LOG.error("Can't commit index " + directory, e);
        }
    }

    @Override
    public boolean deleteDocument(String recordId) {
        try {
            writer.deleteDocuments(getRecordIdTerm(recordId));
            return true;
        } catch (IOException e) {
            LOG.error("Can't update document '" + recordId + "'", e);
            return false;
        }
    }

    @Override
    public boolean updateDocument(String recordId, String fullText) {
        try {
            writer.updateDocument(getRecordIdTerm(recordId), getDocument(recordId, fullText));
            LOG.debug("Document " + recordId + " updated: '" + fullText + "'");
            return true;
        } catch (IOException e) {
            LOG.error("Can't update document '" + recordId + "'", e);
            return false;
        }
    }

    private Document getDocument(String recordId, String fullText) {
        Document doc = new Document();
        doc.add(new StringField(FIELD_RECORD_ID, recordId, org.apache.lucene.document.Field.Store.YES));
        doc.add(new TextField(FIELD_FULL_TEXT, fullText, org.apache.lucene.document.Field.Store.NO));
        return doc;
    }

    private Term getRecordIdTerm(String recordId) {
        return new Term(FIELD_RECORD_ID, recordId);
    }

    private IndexSearcher getIndexSearcher() throws IOException {
        return new IndexSearcher(DirectoryReader.open(directory));
    }

    private static Directory getMMapDirectory(String indexId) {
        try {
            return new MMapDirectory(new File(file.LuceneFolder, indexId));
        } catch (IOException e) {
            throw new RuntimeException("Can't create index in '" + file.LuceneFolder + "'", e);
        }
    }

}
