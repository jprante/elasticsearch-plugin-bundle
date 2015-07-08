package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Collator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.collation.ICUCollationKeyAnalyzer;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

import java.util.Locale;
import java.util.Random;

public class IcuCollationKeyAnalyzerTests extends CollationTestBase {

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
}
