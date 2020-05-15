package org.xbib.elasticsearch.plugin.bundle.test.index.analysis.icu;

import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.elasticsearch.test.ESTokenStreamTestCase;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.IcuNormalizerFilter;

/**
 * ICU normalizer filter tests.
 */
public class IcuNormalizerFilterTests extends ESTokenStreamTestCase {

    public void testDefaults() throws Exception {
        Analyzer a = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                return new TokenStreamComponents(tokenizer,
                        new IcuNormalizerFilter(tokenizer,
                                Normalizer2.getInstance(null, "nfkc_cf", Normalizer2.Mode.COMPOSE)));
            }
        };
        assertAnalyzesTo(a, "This is a test", new String[] { "this", "is", "a", "test" });
        assertAnalyzesTo(a, "Ruß", new String[] { "russ" });
        assertAnalyzesTo(a, "ΜΆΪΟΣ", new String[] { "μάϊοσ" });
        assertAnalyzesTo(a, "Μάϊος", new String[] { "μάϊοσ" });
        assertAnalyzesTo(a, "𐐖", new String[] { "𐐾" });
        assertAnalyzesTo(a, "ﴳﴺﰧ", new String[] { "طمطمطم" });
        assertAnalyzesTo(a, "क्‍ष", new String[] { "क्ष" });
        a.close();
    }

    public void testAlternate() throws Exception {
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

    public void testEmptyTerm() throws Exception {
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
