package org.xbib.elasticsearch.index.analysis.icu.segmentation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.AttributeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;

/**
 *
 */
public class MyanmarSyllableTests extends BaseTokenStreamTest {

    private static Analyzer a;

    @BeforeClass
    public static void setUp() throws Exception {
        a = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new IcuTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY,
                        new DefaultIcuTokenizerConfig(false, false));
                return new TokenStreamComponents(tokenizer);
            }
        };
    }

    @AfterClass
    public static void tearDown() throws Exception {
        a.close();
    }

    @Test
    public void testBasics() throws Exception {
        assertAnalyzesTo(a, "သက်ဝင်လှုပ်ရှားစေပြီး", new String[] { "သက်", "ဝင်", "လှုပ်", "ရှား", "စေ", "ပြီး" });
    }

    @Test
    public void testC() throws Exception {
        assertAnalyzesTo(a, "ကက", new String[] { "က", "က" });
    }

    @Test
    public void testCF() throws Exception {
        assertAnalyzesTo(a, "ကံကံ", new String[] { "ကံ", "ကံ" });
    }

    @Test
    public void testCCA() throws Exception {
        assertAnalyzesTo(a, "ကင်ကင်", new String[] { "ကင်", "ကင်" });
    }

    @Test
    public void testCCAF() throws Exception {
        assertAnalyzesTo(a, "ကင်းကင်း", new String[] { "ကင်း", "ကင်း" });
    }

    @Test
    public void testCV() throws Exception {
        assertAnalyzesTo(a, "ကာကာ", new String[] { "ကာ", "ကာ" });
    }

    @Test
    public void testCVF() throws Exception {
        assertAnalyzesTo(a, "ကားကား", new String[] { "ကား", "ကား" });
    }

    @Test
    public void testCVVA() throws Exception {
        assertAnalyzesTo(a, "ကော်ကော်", new String[] { "ကော်", "ကော်" });
    }

    @Test
    public void testCVVCA() throws Exception {
        assertAnalyzesTo(a, "ကောင်ကောင်", new String[] { "ကောင်", "ကောင်" });
    }

    @Test
    public void testCVVCAF() throws Exception {
        assertAnalyzesTo(a, "ကောင်းကောင်း", new String[] { "ကောင်း", "ကောင်း" });
    }

    @Test
    public void testCM() throws Exception {
        assertAnalyzesTo(a, "ကျကျ", new String[] { "ကျ", "ကျ" });
    }

    @Test
    public void testCMF() throws Exception {
        assertAnalyzesTo(a, "ကျံကျံ", new String[] { "ကျံ", "ကျံ" });
    }

    @Test
    public void testCMCA() throws Exception {
        assertAnalyzesTo(a, "ကျင်ကျင်", new String[] { "ကျင်", "ကျင်" });
    }

    @Test
    public void testCMCAF() throws Exception {
        assertAnalyzesTo(a, "ကျင်းကျင်း", new String[] { "ကျင်း", "ကျင်း" });
    }

    @Test
    public void testCMV() throws Exception {
        assertAnalyzesTo(a, "ကျာကျာ", new String[] { "ကျာ", "ကျာ" });
    }

    @Test
    public void testCMVF() throws Exception {
        assertAnalyzesTo(a, "ကျားကျား", new String[] { "ကျား", "ကျား" });
    }

    @Test
    public void testCMVVA() throws Exception {
        assertAnalyzesTo(a, "ကျော်ကျော်", new String[] { "ကျော်", "ကျော်" });
    }

    @Test
    public void testCMVVCA() throws Exception {
        assertAnalyzesTo(a, "ကြောင်ကြောင်", new String[] { "ကြောင်", "ကြောင်"});
    }

    @Test
    public void testCMVVCAF() throws Exception {
        assertAnalyzesTo(a, "ကြောင်းကြောင်း", new String[] { "ကြောင်း", "ကြောင်း"});
    }

    @Test
    public void testI() throws Exception {
        assertAnalyzesTo(a, "ဪဪ", new String[] { "ဪ", "ဪ" });
    }

    @Test
    public void testE() throws Exception {
        assertAnalyzesTo(a, "ဣဣ", new String[] { "ဣ", "ဣ" });
    }
}
