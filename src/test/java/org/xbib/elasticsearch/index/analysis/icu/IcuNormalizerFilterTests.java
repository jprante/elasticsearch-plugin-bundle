package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;
import org.xbib.elasticsearch.index.analysis.MockTokenizer;

import java.io.IOException;

/**
 *
 */
public class IcuNormalizerFilterTests extends BaseTokenStreamTest {

    private static Analyzer a;

    @BeforeClass
    public static void setUp() throws Exception {
        a = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                return new TokenStreamComponents(tokenizer,
                        new IcuNormalizerFilter(tokenizer,
                                Normalizer2.getInstance(null, "nfkc_cf", Normalizer2.Mode.COMPOSE)));
            }
        };
    }

    @AfterClass
    public static void tearDown() throws Exception {
        a.close();
    }

    @Test
    public void testDefaults() throws IOException {
        assertAnalyzesTo(a, "This is a test", new String[] { "this", "is", "a", "test" });
        assertAnalyzesTo(a, "Ruß", new String[] { "russ" });
        assertAnalyzesTo(a, "ΜΆΪΟΣ", new String[] { "μάϊοσ" });
        assertAnalyzesTo(a, "Μάϊος", new String[] { "μάϊοσ" });
        assertAnalyzesTo(a, "𐐖", new String[] { "𐐾" });
        assertAnalyzesTo(a, "ﴳﴺﰧ", new String[] { "طمطمطم" });
        assertAnalyzesTo(a, "क्‍ष", new String[] { "क्ष" });
    }

    @Test
    public void testAlternate() throws IOException {
        Analyzer a = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                return new TokenStreamComponents(tokenizer, new IcuNormalizerFilter(
                        tokenizer,
                        Normalizer2.getInstance(null, "nfc", Normalizer2.Mode.DECOMPOSE)));
            }
        };
        assertAnalyzesTo(a, "\u00E9", new String[] { "\u0065\u0301" });
        a.close();
    }

    @Test
    public void testEmptyTerm() throws IOException {
        Analyzer a = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new KeywordTokenizer();
                return new TokenStreamComponents(tokenizer,
                        new IcuNormalizerFilter(tokenizer,
                                Normalizer2.getInstance(null, "nfkc_cf", Normalizer2.Mode.COMPOSE)));
            }
        };
        checkOneTerm(a, "", "");
        a.close();
    }
}