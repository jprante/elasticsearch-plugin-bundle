package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.xbib.elasticsearch.index.analysis.LuceneTestCase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Base test class for testing Unicode collation.
 */
public abstract class CollationTestBase extends LuceneTestCase {

    protected String firstRangeBeginningOriginal = "\u062F";
    protected String firstRangeEndOriginal = "\u0698";

    protected String secondRangeBeginningOriginal = "\u0633";
    protected String secondRangeEndOriginal = "\u0638";

    public void testFarsiRangeFilterCollating(Analyzer analyzer, BytesRef firstBeg,
                                              BytesRef firstEnd, BytesRef secondBeg,
                                              BytesRef secondEnd) throws Exception {
        Directory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(analyzer));
        Document doc = new Document();
        doc.add(new TextField("content", "\u0633\u0627\u0628", Field.Store.YES));
        doc.add(new StringField("body", "body", Field.Store.YES));
        writer.addDocument(doc);
        writer.close();
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new TermQuery(new Term("body", "body"));

        // Unicode order would include U+0633 in [ U+062F - U+0698 ], but Farsi
        // orders the U+0698 character before the U+0633 character, so the single
        // index Term below should NOT be returned by a TermRangeFilter with a Farsi
        // Collator (or an Arabic one for the case when Farsi searcher not
        // supported).
        BooleanQuery bq = new BooleanQuery();
        bq.add(query, BooleanClause.Occur.MUST);
        bq.add(new TermRangeQuery("content", firstBeg, firstEnd, true, true), BooleanClause.Occur.FILTER);

        ScoreDoc[] result = searcher.search(bq, 1).scoreDocs;
        assertEquals("The index Term should not be included.", 0, result.length);

        bq = new BooleanQuery();
        bq.add(query, BooleanClause.Occur.MUST);
        bq.add(new TermRangeQuery("content", secondBeg, secondEnd, true, true), BooleanClause.Occur.FILTER);
        result = searcher.search(bq, 1).scoreDocs;
        assertEquals("The index Term should be included.", 1, result.length);

        reader.close();
        dir.close();
    }

    public void testFarsiRangeQueryCollating(Analyzer analyzer, BytesRef firstBeg,
                                             BytesRef firstEnd, BytesRef secondBeg,
                                             BytesRef secondEnd) throws Exception {
        Directory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(analyzer));
        Document doc = new Document();

        // Unicode order would include U+0633 in [ U+062F - U+0698 ], but Farsi
        // orders the U+0698 character before the U+0633 character, so the single
        // index Term below should NOT be returned by a TermRangeQuery with a Farsi
        // Collator (or an Arabic one for the case when Farsi is not supported).
        doc.add(new TextField("content", "\u0633\u0627\u0628", Field.Store.YES));
        writer.addDocument(doc);
        writer.close();
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);

        Query query = new TermRangeQuery("content", firstBeg, firstEnd, true, true);
        ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;
        assertEquals("The index Term should not be included.", 0, hits.length);

        query = new TermRangeQuery("content", secondBeg, secondEnd, true, true);
        hits = searcher.search(query, 1000).scoreDocs;
        assertEquals("The index Term should be included.", 1, hits.length);
        reader.close();
        dir.close();
    }

    public void testFarsiTermRangeQuery(Analyzer analyzer, BytesRef firstBeg,
                                        BytesRef firstEnd, BytesRef secondBeg, BytesRef secondEnd) throws Exception {

        Directory farsiIndex = new RAMDirectory();
        IndexWriter writer = new IndexWriter(farsiIndex, new IndexWriterConfig(analyzer));
        Document doc = new Document();
        doc.add(new TextField("content", "\u0633\u0627\u0628", Field.Store.YES));
        doc.add(new StringField("body", "body", Field.Store.YES));
        writer.addDocument(doc);
        writer.close();

        IndexReader reader = DirectoryReader.open(farsiIndex);
        IndexSearcher search = newSearcher(reader);

        // Unicode order would include U+0633 in [ U+062F - U+0698 ], but Farsi
        // orders the U+0698 character before the U+0633 character, so the single
        // index Term below should NOT be returned by a TermRangeQuery
        // with a Farsi Collator (or an Arabic one for the case when Farsi is
        // not supported).
        Query csrq = new TermRangeQuery("content", firstBeg, firstEnd, true, true);
        ScoreDoc[] result = search.search(csrq, 1000).scoreDocs;
        assertEquals("The index Term should not be included.", 0, result.length);

        csrq = new TermRangeQuery("content", secondBeg, secondEnd, true, true);
        result = search.search(csrq, 1000).scoreDocs;
        assertEquals("The index Term should be included.", 1, result.length);
        reader.close();
        farsiIndex.close();
    }

    public void assertThreadSafe(final Random random, final Analyzer analyzer) throws Exception {
        int numTestPoints = 100;
        int numThreads = randomIntBetween(random, 3, 5);
        final HashMap<String, BytesRef> map = new HashMap<>();
        for (int i = 0; i < numTestPoints; i++) {
            String term = randomSimpleString(random, 10);
            try (TokenStream ts = analyzer.tokenStream("fake", term)) {
                TermToBytesRefAttribute termAtt = ts.addAttribute(TermToBytesRefAttribute.class);
                BytesRef bytes = termAtt.getBytesRef();
                ts.reset();
                assertTrue(ts.incrementToken());
                termAtt.fillBytesRef();
                // ensure we make a copy of the actual bytes too
                map.put(term, BytesRef.deepCopyOf(bytes));
                assertFalse(ts.incrementToken());
                ts.end();
            }
        }

        Thread threads[] = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread() {
                @Override
                public void run() {
                    try {
                        for (Map.Entry<String, BytesRef> mapping : map.entrySet()) {
                            String term = mapping.getKey();
                            BytesRef expected = mapping.getValue();
                            try (TokenStream ts = analyzer.tokenStream("fake", term)) {
                                TermToBytesRefAttribute termAtt = ts.addAttribute(TermToBytesRefAttribute.class);
                                BytesRef bytes = termAtt.getBytesRef();
                                ts.reset();
                                assertTrue(ts.incrementToken());
                                termAtt.fillBytesRef();
                                assertEquals(expected, bytes);
                                assertFalse(ts.incrementToken());
                                ts.end();
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }
        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }
    }
}
