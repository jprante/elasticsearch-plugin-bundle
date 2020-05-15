package org.xbib.elasticsearch.plugin.bundle.test.index.analysis.icu.segmentation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.AttributeFactory;
import org.elasticsearch.test.ESTokenStreamTestCase;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.segmentation.DefaultIcuTokenizerConfig;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizer;

/**
 * Myanmar syllable tests.
 */
public class MyanmarSyllableTests extends ESTokenStreamTestCase {

    private static Analyzer createAnalyzer() {
        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new IcuTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY,
                        new DefaultIcuTokenizerConfig(false, false));
                return new TokenStreamComponents(tokenizer);
            }
        };
    }

    private static void destroyAnalyzer(Analyzer a) {
        a.close();
    }

    public void testBasics() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "သက်ဝင်လှုပ်ရှားစေပြီး", new String[] { "သက်", "ဝင်", "လှုပ်", "ရှား", "စေ", "ပြီး" });
        destroyAnalyzer(a);
    }

    public void testC() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကက", new String[] { "က", "က" });
        destroyAnalyzer(a);
    }

    public void testCF() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကံကံ", new String[] { "ကံ", "ကံ" });
        destroyAnalyzer(a);
    }

    public void testCCA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကင်ကင်", new String[] { "ကင်", "ကင်" });
        destroyAnalyzer(a);
    }

    public void testCCAF() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကင်းကင်း", new String[] { "ကင်း", "ကင်း" });
        destroyAnalyzer(a);
    }

    public void testCV() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကာကာ", new String[] { "ကာ", "ကာ" });
        destroyAnalyzer(a);
    }

    public void testCVF() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကားကား", new String[] { "ကား", "ကား" });
        destroyAnalyzer(a);
    }

    public void testCVVA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကော်ကော်", new String[] { "ကော်", "ကော်" });
        destroyAnalyzer(a);
    }

    public void testCVVCA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကောင်ကောင်", new String[] { "ကောင်", "ကောင်" });
        destroyAnalyzer(a);
    }

    public void testCVVCAF() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကောင်းကောင်း", new String[] { "ကောင်း", "ကောင်း" });
        destroyAnalyzer(a);
    }

    public void testCM() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကျကျ", new String[] { "ကျ", "ကျ" });
        destroyAnalyzer(a);
    }

    public void testCMF() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကျံကျံ", new String[] { "ကျံ", "ကျံ" });
        destroyAnalyzer(a);
    }

    public void testCMCA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကျင်ကျင်", new String[] { "ကျင်", "ကျင်" });
        destroyAnalyzer(a);
    }

    public void testCMCAF() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကျင်းကျင်း", new String[] { "ကျင်း", "ကျင်း" });
        destroyAnalyzer(a);
    }

    public void testCMV() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကျာကျာ", new String[] { "ကျာ", "ကျာ" });
        destroyAnalyzer(a);
    }

    public void testCMVF() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကျားကျား", new String[] { "ကျား", "ကျား" });
        destroyAnalyzer(a);
    }

    public void testCMVVA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကျော်ကျော်", new String[] { "ကျော်", "ကျော်" });
        destroyAnalyzer(a);
    }

    public void testCMVVCA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကြောင်ကြောင်", new String[] { "ကြောင်", "ကြောင်"});
        destroyAnalyzer(a);
    }

    public void testCMVVCAF() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ကြောင်းကြောင်း", new String[] { "ကြောင်း", "ကြောင်း"});
        destroyAnalyzer(a);
    }

    public void testI() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ဪဪ", new String[] { "ဪ", "ဪ" });
        destroyAnalyzer(a);
    }

    public void testE() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ဣဣ", new String[] { "ဣ", "ဣ" });
        destroyAnalyzer(a);
    }
}
