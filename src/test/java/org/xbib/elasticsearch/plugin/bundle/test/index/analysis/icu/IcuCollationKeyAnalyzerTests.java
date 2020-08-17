package org.xbib.elasticsearch.plugin.bundle.test.index.analysis.icu;

import com.ibm.icu.text.Collator;
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
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.Randomness;
import org.elasticsearch.test.ESTestCase;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.IcuCollationKeyAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * ICU collation key analyzer tests.
 */
public class IcuCollationKeyAnalyzerTests extends ESTestCase {

    public void testFarsiRangeFilterCollating() throws Exception {
        CollationParameters collationParameters = new CollationParameters(new Locale("fa"));
        testFarsiRangeFilterCollating(collationParameters.analyzer,
                collationParameters.firstRangeBeginning, collationParameters.firstRangeEnd,
                collationParameters.secondRangeBeginning, collationParameters.secondRangeEnd);
    }

    public void testFarsiRangeQueryCollating() throws Exception {
        CollationParameters collationParameters = new CollationParameters(new Locale("fa"));
        testFarsiRangeQueryCollating(collationParameters.analyzer,
                collationParameters.firstRangeBeginning, collationParameters.firstRangeEnd,
                collationParameters.secondRangeBeginning, collationParameters.secondRangeEnd);
    }

    public void testFarsiTermRangeQuery() throws Exception {
        CollationParameters collationParameters = new CollationParameters(new Locale("fa"));
        testFarsiTermRangeQuery(collationParameters.analyzer,
                collationParameters.firstRangeBeginning, collationParameters.firstRangeEnd,
                collationParameters.secondRangeBeginning, collationParameters.secondRangeEnd);
    }

    public void testThreadSafe() throws Exception {
        int iters = 20;
        for (int i = 0; i < iters; i++) {
            Locale locale = Locale.GERMAN;
            Collator collator = Collator.getInstance(locale);
            collator.setStrength(Collator.IDENTICAL);
            assertThreadSafe(Randomness.get(), new IcuCollationKeyAnalyzer(collator));
        }
    }

    class CollationParameters {
        Collator collator;

        Analyzer analyzer;

        BytesRef firstRangeBeginning;

        BytesRef firstRangeEnd;

        BytesRef secondRangeBeginning;

        BytesRef secondRangeEnd;

        CollationParameters(Locale locale) {
            collator = Collator.getInstance(locale);
            analyzer = new IcuCollationKeyAnalyzer(collator);
            firstRangeBeginning = new BytesRef(collator.getCollationKey(firstRangeBeginningOriginal).toByteArray());
            firstRangeEnd = new BytesRef(collator.getCollationKey(firstRangeEndOriginal).toByteArray());
            secondRangeBeginning = new BytesRef(collator.getCollationKey(secondRangeBeginningOriginal).toByteArray());
            secondRangeEnd = new BytesRef(collator.getCollationKey(secondRangeEndOriginal).toByteArray());
        }
    }

    private static final String firstRangeBeginningOriginal = "\u062F";

    private static final String firstRangeEndOriginal = "\u0698";

    private static final String secondRangeBeginningOriginal = "\u0633";

    private static final String secondRangeEndOriginal = "\u0638";


    private static int randomIntBetween(Random r, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException( "max must be >= min: " + min + ", " + max);
        }
        long range = (long) max - (long) min;
        if (range < Integer.MAX_VALUE) {
            return min + r.nextInt(1 + (int) range);
        } else {
            return min + (int) Math.round(r.nextDouble() * range);
        }
    }

    private static String randomSimpleString(Random r, int maxLength) {
        final int end = randomIntBetween(r, 0, maxLength);
        if (end == 0) {
            return "";
        }
        final char[] buffer = new char[end];
        for (int i = 0; i < end; i++) {
            buffer[i] = (char) randomIntBetween(r, 'a', 'z');
        }
        return new String(buffer, 0, end);
    }

    private void testFarsiRangeFilterCollating(Analyzer analyzer, BytesRef firstBeg,
                                       BytesRef firstEnd, BytesRef secondBeg,
                                       BytesRef secondEnd) throws Exception {
        Directory dir = new MMapDirectory(Files.createTempDirectory("icutest"));
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
        BooleanQuery.Builder bq = new BooleanQuery.Builder();
        bq.add(query, BooleanClause.Occur.MUST);
        bq.add(new TermRangeQuery("content", firstBeg, firstEnd, true, true), BooleanClause.Occur.FILTER);

        ScoreDoc[] result = searcher.search(bq.build(), 1).scoreDocs;
        assertEquals("The index Term should not be included.", 0, result.length);

        bq = new BooleanQuery.Builder();
        bq.add(query, BooleanClause.Occur.MUST);
        bq.add(new TermRangeQuery("content", secondBeg, secondEnd, true, true), BooleanClause.Occur.FILTER);
        result = searcher.search(bq.build(), 1).scoreDocs;
        assertEquals("The index Term should be included.", 1, result.length);

        reader.close();
        dir.close();
    }

    private void testFarsiRangeQueryCollating(Analyzer analyzer, BytesRef firstBeg,
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

    private void testFarsiTermRangeQuery(Analyzer analyzer, BytesRef firstBeg,
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

    private void assertThreadSafe(final Random random, final Analyzer analyzer) throws Exception {
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
                // ensure we make a copy of the actual bytes too
                map.put(term, BytesRef.deepCopyOf(bytes));
                assertFalse(ts.incrementToken());
                ts.end();
            }
        }

        Thread threads[] = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (Map.Entry<String, BytesRef> mapping : map.entrySet()) {
                        String term = mapping.getKey();
                        BytesRef expected = mapping.getValue();
                        try (TokenStream ts = analyzer.tokenStream("fake", term)) {
                            TermToBytesRefAttribute termAtt = ts.addAttribute(TermToBytesRefAttribute.class);
                            BytesRef bytes = termAtt.getBytesRef();
                            ts.reset();
                            ts.incrementToken();
                            if (!expected.utf8ToString().equals(bytes.utf8ToString())) {
                                throw new IOException("unexpected: bytes=" + bytes.utf8ToString() + " expected=" + expected.utf8ToString());
                            }
                            ts.incrementToken();
                            ts.end();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }
        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }
    }
}
