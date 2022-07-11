package org.xbib.opensearch.plugin.bundle.test.index.analysis.icu.segmentation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cjk.CJKBigramFilter;
import org.apache.lucene.util.AttributeFactory;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.junit.After;
import org.junit.Before;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.segmentation.DefaultIcuTokenizerConfig;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizer;

/**
 * CJK bi-gram filter tests.
 */
public class CJKBigramFilterTests extends OpenSearchTokenStreamTestCase {

    private static Analyzer analyzer;

    //private static Analyzer analyzer2;

    @Before
    public void up() {
        analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer source = new IcuTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY,
                        new DefaultIcuTokenizerConfig(false, true));
                TokenStream result = new CJKBigramFilter(source);
                return new TokenStreamComponents(source, new StopFilter(result, CharArraySet.EMPTY_SET));
            }
        };
    }

    @After
    public void down() {
        analyzer.close();
    }

    public void testJa1() throws Exception {
        assertAnalyzesTo(analyzer, "一二三四五六七八九十",
                new String[] { "一二", "二三", "三四", "四五", "五六", "六七", "七八", "八九", "九十" },
                new int[] { 0, 1, 2, 3, 4, 5, 6, 7,  8 },
                new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10 },
                new String[] { "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>" },
                new int[] { 1, 1, 1, 1, 1, 1, 1, 1,  1 });
    }

    public void testJa2() throws Exception {
        assertAnalyzesTo(analyzer, "一 二三四 五六七八九 十",
                new String[] { "一", "二三", "三四", "五六", "六七", "七八", "八九", "十" },
                new int[] { 0, 2, 3, 6, 7,  8,  9, 12 },
                new int[] { 1, 4, 5, 8, 9, 10, 11, 13 },
                new String[] { "<SINGLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<SINGLE>" },
                new int[] { 1, 1, 1, 1, 1,  1,  1,  1 });
    }

    public void testC() throws Exception {
        assertAnalyzesTo(analyzer, "abc defgh ijklmn opqrstu vwxy z",
                new String[] { "abc", "defgh", "ijklmn", "opqrstu", "vwxy", "z" },
                new int[] { 0, 4, 10, 17, 25, 30 },
                new int[] { 3, 9, 16, 24, 29, 31 },
                new String[] { "<ALPHANUM>", "<ALPHANUM>", "<ALPHANUM>", "<ALPHANUM>", "<ALPHANUM>", "<ALPHANUM>" },
                new int[] { 1, 1,  1,  1,  1,  1 });
    }

    public void testFinalOffset() throws Exception {
        assertAnalyzesTo(analyzer, "あい",
                new String[] { "あい" },
                new int[] { 0 },
                new int[] { 2 },
                new String[] { "<DOUBLE>" },
                new int[] { 1 });

        assertAnalyzesTo(analyzer, "あい   ",
                new String[] { "あい" },
                new int[] { 0 },
                new int[] { 2 },
                new String[] { "<DOUBLE>" },
                new int[] { 1 });

        assertAnalyzesTo(analyzer, "test",
                new String[] { "test" },
                new int[] { 0 },
                new int[] { 4 },
                new String[] { "<ALPHANUM>" },
                new int[] { 1 });

        assertAnalyzesTo(analyzer, "test   ",
                new String[] { "test" },
                new int[] { 0 },
                new int[] { 4 },
                new String[] { "<ALPHANUM>" },
                new int[] { 1 });

        assertAnalyzesTo(analyzer, "あいtest",
                new String[] { "あい", "test" },
                new int[] { 0, 2 },
                new int[] { 2, 6 },
                new String[] { "<DOUBLE>", "<ALPHANUM>" },
                new int[] { 1, 1 });

        assertAnalyzesTo(analyzer, "testあい    ",
                new String[] { "test", "あい" },
                new int[] { 0, 4 },
                new int[] { 4, 6 },
                new String[] { "<ALPHANUM>", "<DOUBLE>" },
                new int[] { 1, 1 });
    }

    public void testMix() throws Exception {
        assertAnalyzesTo(analyzer, "あいうえおabcかきくけこ",
                new String[] { "あい", "いう", "うえ", "えお", "abc", "かき", "きく", "くけ", "けこ" },
                new int[] { 0, 1, 2, 3, 5,  8,  9, 10, 11 },
                new int[] { 2, 3, 4, 5, 8, 10, 11, 12, 13 },
                new String[] { "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<ALPHANUM>", "<DOUBLE>", "<DOUBLE>",
                        "<DOUBLE>", "<DOUBLE>" },
                new int[] { 1, 1, 1, 1, 1,  1,  1,  1,  1});
    }

    public void testMix2() throws Exception {
        assertAnalyzesTo(analyzer, "あいうえおabんcかきくけ こ",
                new String[] { "あい", "いう", "うえ", "えお", "ab", "ん", "c", "かき", "きく", "くけ", "こ" },
                new int[] { 0, 1, 2, 3, 5, 7, 8,  9, 10, 11, 14 },
                new int[] { 2, 3, 4, 5, 7, 8, 9, 11, 12, 13, 15 },
                new String[] { "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<ALPHANUM>", "<SINGLE>", "<ALPHANUM>",
                        "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<SINGLE>" },
                new int[] { 1, 1, 1, 1, 1, 1, 1,  1,  1,  1,  1 });
    }

    public void testNonIdeographic() throws Exception {
        assertAnalyzesTo(analyzer, "一 روبرت موير",
                new String[] { "一", "روبرت", "موير" },
                new int[] { 0, 2, 8 },
                new int[] { 1, 7, 12 },
                new String[] { "<SINGLE>", "<ALPHANUM>", "<ALPHANUM>" },
                new int[] { 1, 1, 1 });
    }

    public void testNonIdeographicNonLetter() throws Exception {
        assertAnalyzesTo(analyzer, "一 رُوبرت موير",
                new String[] { "一", "رُوبرت", "موير" },
                new int[] { 0, 2, 9 },
                new int[] { 1, 8, 13 },
                new String[] { "<SINGLE>", "<ALPHANUM>", "<ALPHANUM>" },
                new int[] { 1, 1, 1 });
    }

    public void testSurrogates() throws Exception {
        assertAnalyzesTo(analyzer, "𩬅艱鍟䇹愯瀛",
                new String[] { "𩬅艱", "艱鍟", "鍟䇹", "䇹愯", "愯瀛" },
                new int[] { 0, 2, 3, 4, 5 },
                new int[] { 3, 4, 5, 6, 7 },
                new String[] { "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>" },
                new int[] { 1, 1, 1, 1, 1 });
    }

    public void testReusableTokenStream() throws Exception {
        assertAnalyzesTo(analyzer, "あいうえおabcかきくけこ",
                new String[] { "あい", "いう", "うえ", "えお", "abc", "かき", "きく", "くけ", "けこ" },
                new int[] { 0, 1, 2, 3, 5,  8,  9, 10, 11 },
                new int[] { 2, 3, 4, 5, 8, 10, 11, 12, 13 },
                new String[] { "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<ALPHANUM>", "<DOUBLE>", "<DOUBLE>",
                        "<DOUBLE>", "<DOUBLE>" },
                new int[] { 1, 1, 1, 1, 1,  1,  1,  1,  1});

        assertAnalyzesTo(analyzer, "あいうえおabんcかきくけ こ",
                new String[] { "あい", "いう", "うえ", "えお", "ab", "ん", "c", "かき", "きく", "くけ", "こ" },
                new int[] { 0, 1, 2, 3, 5, 7, 8,  9, 10, 11, 14 },
                new int[] { 2, 3, 4, 5, 7, 8, 9, 11, 12, 13, 15 },
                new String[] { "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<ALPHANUM>", "<SINGLE>", "<ALPHANUM>",
                        "<DOUBLE>", "<DOUBLE>", "<DOUBLE>", "<SINGLE>" },
                new int[] { 1, 1, 1, 1, 1, 1, 1,  1,  1,  1,  1 });
    }

    public void testSingleChar() throws Exception {
        assertAnalyzesTo(analyzer, "一",
                new String[] { "一" },
                new int[] { 0 },
                new int[] { 1 },
                new String[] { "<SINGLE>" },
                new int[] { 1 });
    }

    public void testTokenStream() throws Exception {
        assertAnalyzesTo(analyzer, "一丁丂",
                new String[] { "一丁", "丁丂"},
                new int[] { 0, 1 },
                new int[] { 2, 3 },
                new String[] { "<DOUBLE>", "<DOUBLE>" },
                new int[] { 1, 1 });
    }
}
