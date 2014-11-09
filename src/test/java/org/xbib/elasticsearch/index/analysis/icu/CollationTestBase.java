package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.xbib.elasticsearch.index.analysis.LuceneTestCase;
import org.xbib.elasticsearch.index.analysis.MockAnalyzer;
import org.xbib.elasticsearch.index.analysis.MockTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Base test class for testing Unicode collation.
 */
public abstract class CollationTestBase extends LuceneTestCase {

    protected Version TEST_VERSION_CURRENT = Version.LATEST;

    protected String firstRangeBeginningOriginal = "\u062F";
    protected String firstRangeEndOriginal = "\u0698";

    protected String secondRangeBeginningOriginal = "\u0633";
    protected String secondRangeEndOriginal = "\u0638";

    public void testFarsiRangeFilterCollating(Analyzer analyzer, BytesRef firstBeg,
                                              BytesRef firstEnd, BytesRef secondBeg,
                                              BytesRef secondEnd) throws Exception {
        Directory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
                TEST_VERSION_CURRENT, analyzer));
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
        ScoreDoc[] result = searcher.search(query, new TermRangeFilter("content", firstBeg, firstEnd, true, true), 1).scoreDocs;
        assertEquals("The index Term should not be included.", 0, result.length);

        result = searcher.search(query, new TermRangeFilter("content", secondBeg, secondEnd, true, true), 1).scoreDocs;
        assertEquals("The index Term should be included.", 1, result.length);

        reader.close();
        dir.close();
    }

    public void testFarsiRangeQueryCollating(Analyzer analyzer, BytesRef firstBeg,
                                             BytesRef firstEnd, BytesRef secondBeg,
                                             BytesRef secondEnd) throws Exception {
        Directory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
                TEST_VERSION_CURRENT, analyzer));
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
        ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
        assertEquals("The index Term should not be included.", 0, hits.length);

        query = new TermRangeQuery("content", secondBeg, secondEnd, true, true);
        hits = searcher.search(query, null, 1000).scoreDocs;
        assertEquals("The index Term should be included.", 1, hits.length);
        reader.close();
        dir.close();
    }

    public void testFarsiTermRangeQuery(Analyzer analyzer, BytesRef firstBeg,
                                        BytesRef firstEnd, BytesRef secondBeg, BytesRef secondEnd) throws Exception {

        Directory farsiIndex = new RAMDirectory();
        IndexWriter writer = new IndexWriter(farsiIndex, new IndexWriterConfig(
                TEST_VERSION_CURRENT, analyzer));
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
        ScoreDoc[] result = search.search(csrq, null, 1000).scoreDocs;
        assertEquals("The index Term should not be included.", 0, result.length);

        csrq = new TermRangeQuery
                ("content", secondBeg, secondEnd, true, true);
        result = search.search(csrq, null, 1000).scoreDocs;
        assertEquals("The index Term should be included.", 1, result.length);
        reader.close();
        farsiIndex.close();
    }

    public void testCollationKeySort(Analyzer usAnalyzer,
                                     Analyzer franceAnalyzer,
                                     Analyzer swedenAnalyzer,
                                     Analyzer denmarkAnalyzer,
                                     String usResult,
                                     String frResult,
                                     String svResult,
                                     String dkResult) throws Exception {
        Directory indexStore = new RAMDirectory();
        IndexWriter writer = new IndexWriter(indexStore,
                new IndexWriterConfig(TEST_VERSION_CURRENT, new MockAnalyzer(MockTokenizer.WHITESPACE, false)));

        String[][] sortData = new String[][]{
                // tracer contents US                 France             Sweden (sv_SE)     Denmark (da_DK)
                {"A", "x", "p\u00EAche", "p\u00EAche", "p\u00EAche", "p\u00EAche"},
                {"B", "y", "HAT", "HAT", "HAT", "HAT"},
                {"C", "x", "p\u00E9ch\u00E9", "p\u00E9ch\u00E9", "p\u00E9ch\u00E9", "p\u00E9ch\u00E9"},
                {"D", "y", "HUT", "HUT", "HUT", "HUT"},
                {"E", "x", "peach", "peach", "peach", "peach"},
                {"F", "y", "H\u00C5T", "H\u00C5T", "H\u00C5T", "H\u00C5T"},
                {"G", "x", "sin", "sin", "sin", "sin"},
                {"H", "y", "H\u00D8T", "H\u00D8T", "H\u00D8T", "H\u00D8T"},
                {"I", "x", "s\u00EDn", "s\u00EDn", "s\u00EDn", "s\u00EDn"},
                {"J", "y", "HOT", "HOT", "HOT", "HOT"},
        };

        FieldType customType = new FieldType();
        customType.setStored(true);

        for (String[] s : sortData) {
            Document doc = new Document();
            doc.add(new Field("tracer", s[0], customType));
            doc.add(new TextField("contents", s[1], Field.Store.NO));
            if (s[2] != null) {
                doc.add(new TextField("US", usAnalyzer.tokenStream("US", s[2])));
            }
            if (s[3] != null) {
                doc.add(new TextField("France", franceAnalyzer.tokenStream("France", s[3])));
            }
            if (s[4] != null) {
                doc.add(new TextField("Sweden", swedenAnalyzer.tokenStream("Sweden", s[4])));
            }
            if (s[5] != null) {
                doc.add(new TextField("Denmark", denmarkAnalyzer.tokenStream("Denmark", s[5])));
            }
            writer.addDocument(doc);
        }
        writer.forceMerge(1);
        writer.close();
        IndexReader reader = DirectoryReader.open(indexStore);
        IndexSearcher searcher = new IndexSearcher(reader);

        Sort sort = new Sort();
        Query queryX = new TermQuery(new Term("contents", "x"));
        Query queryY = new TermQuery(new Term("contents", "y"));

        sort.setSort(new SortField("US", SortField.Type.STRING));
        assertMatches(searcher, queryY, sort, usResult);

        sort.setSort(new SortField("France", SortField.Type.STRING));
        assertMatches(searcher, queryX, sort, frResult);

        sort.setSort(new SortField("Sweden", SortField.Type.STRING));
        assertMatches(searcher, queryY, sort, svResult);

        sort.setSort(new SortField("Denmark", SortField.Type.STRING));
        assertMatches(searcher, queryY, sort, dkResult);
        reader.close();
        indexStore.close();
    }

    private void assertMatches(IndexSearcher searcher, Query query, Sort sort,
                               String expectedResult) throws IOException {
        ScoreDoc[] result = searcher.search(query, null, 1000, sort).scoreDocs;
        StringBuilder sb = new StringBuilder(10);
        for (ScoreDoc sd : result) {
            Document doc = searcher.doc(sd.doc);
            IndexableField[] v = doc.getFields("tracer");
            for (IndexableField aV : v) {
                sb.append(aV.stringValue());
            }
        }
        assertEquals(expectedResult, sb.toString());
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
