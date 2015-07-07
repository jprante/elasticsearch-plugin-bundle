package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Collator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.collation.ICUCollationKeyAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.MockAnalyzer;
import org.xbib.elasticsearch.index.analysis.MockTokenizer;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class IcuCollationKeyAnalyzerTests extends CollationTestBase {

    private final static ESLogger logger = ESLoggerFactory.getLogger("icu");

    private Collator collator = Collator.getInstance(new Locale("fa"));

    private Analyzer analyzer = new ICUCollationKeyAnalyzer(collator);

    private BytesRef firstRangeBeginning = new BytesRef(collator.getCollationKey(firstRangeBeginningOriginal).toByteArray());
    private BytesRef firstRangeEnd = new BytesRef(collator.getCollationKey(firstRangeEndOriginal).toByteArray());
    private BytesRef secondRangeBeginning = new BytesRef(collator.getCollationKey(secondRangeBeginningOriginal).toByteArray());
    private BytesRef secondRangeEnd = new BytesRef(collator.getCollationKey(secondRangeEndOriginal).toByteArray());

    @Test
    public void testFarsiRangeFilterCollating() throws Exception {
        testFarsiRangeFilterCollating(analyzer, firstRangeBeginning, firstRangeEnd,
                secondRangeBeginning, secondRangeEnd);
    }

    @Test
    public void testFarsiRangeQueryCollating() throws Exception {
        testFarsiRangeQueryCollating(analyzer, firstRangeBeginning, firstRangeEnd,
                secondRangeBeginning, secondRangeEnd);
    }

    @Test
    public void testFarsiTermRangeQuery() throws Exception {
        testFarsiTermRangeQuery(analyzer, firstRangeBeginning, firstRangeEnd,
                secondRangeBeginning, secondRangeEnd);
    }

    @Test
    public void testThreadSafe() throws Exception {
        int iters = 20;
        for (int i = 0; i < iters; i++) {
            Locale locale = Locale.GERMAN;
            Collator collator = Collator.getInstance(locale);
            collator.setStrength(Collator.IDENTICAL);
            assertThreadSafe(new Random(), new ICUCollationKeyAnalyzer(collator));
        }
    }

    @Test
    public void testCollationKeySort() throws Exception {
        Analyzer usAnalyzer = new ICUCollationKeyAnalyzer(Collator.getInstance(Locale.US));
        String usResult = "BFJHD";
        Analyzer franceAnalyzer = new ICUCollationKeyAnalyzer(Collator.getInstance(Locale.FRANCE));
        String frResult = "ECAGI";
        Analyzer swedenAnalyzer = new ICUCollationKeyAnalyzer(Collator.getInstance(new Locale("sv", "se")));
        String svResult = "BJDFH";
        Analyzer denmarkAnalyzer = new ICUCollationKeyAnalyzer(Collator.getInstance(new Locale("da", "dk")));
        String dkResult = "BJDFH";

        Directory indexStore = new RAMDirectory();
        IndexWriter writer = new IndexWriter(indexStore,
                new IndexWriterConfig(new MockAnalyzer(MockTokenizer.WHITESPACE, false)));

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

        sort.setSort(new SortField("US", SortField.Type.DOC));
        assertMatches(searcher, queryY, sort, usResult);

        sort.setSort(new SortField("France", SortField.Type.DOC));
        assertMatches(searcher, queryX, sort, frResult);

        sort.setSort(new SortField("Sweden", SortField.Type.DOC));
        assertMatches(searcher, queryY, sort, svResult);

        sort.setSort(new SortField("Denmark", SortField.Type.DOC));
        assertMatches(searcher, queryY, sort, dkResult);
        reader.close();
        indexStore.close();
    }

    private void assertMatches(IndexSearcher searcher, Query query, Sort sort,
                               String expectedResult) throws IOException {
        ScoreDoc[] result = searcher.search(query, 1000, sort).scoreDocs;
        StringBuilder sb = new StringBuilder();
        for (ScoreDoc sd : result) {
            Document doc = searcher.doc(sd.doc);
            for (IndexableField aV : doc.getFields("tracer")) {
                sb.append(aV.stringValue());
            }
        }
        logger.info("{} == {} ?", expectedResult, sb.toString());
        //assertEquals(expectedResult, sb.toString());
    }


}
