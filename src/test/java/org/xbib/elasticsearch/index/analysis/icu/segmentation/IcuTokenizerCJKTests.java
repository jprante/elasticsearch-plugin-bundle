package org.xbib.elasticsearch.index.analysis.icu.segmentation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.AttributeFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;

/**
 *
 */
public class IcuTokenizerCJKTests extends BaseTokenStreamTest {

    private static Analyzer a;

    @BeforeClass
    public static void setUp() throws Exception {
        a = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                return new TokenStreamComponents(new IcuTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY,
                        new DefaultIcuTokenizerConfig(true, true)));
            }
        };
    }

    @AfterClass
    public static void tearDown() throws Exception {
        a.close();
    }

    @Test
    public void testSimpleChinese() throws Exception {
        assertAnalyzesTo(a, "我购买了道具和服装。",
                new String[] { "我", "购买", "了", "道具", "和", "服装" }
        );
    }

    @Test
    public void testChineseNumerics() throws Exception {
        assertAnalyzesTo(a, "９４８３", new String[] { "９４８３" });
        assertAnalyzesTo(a, "院內分機９４８３。",
                new String[] { "院", "內", "分機", "９４８３" });
        assertAnalyzesTo(a, "院內分機9483。",
                new String[] { "院", "內", "分機", "9483" });
    }

    @Test
    public void testSimpleJapanese() throws Exception {
        assertAnalyzesTo(a, "それはまだ実験段階にあります",
                new String[] { "それ", "は", "まだ", "実験", "段階", "に", "あり", "ます"  }
        );
    }

    @Test
    public void testJapaneseTypes() throws Exception {
        assertAnalyzesTo(a, "仮名遣い カタカナ",
                new String[] { "仮名遣い", "カタカナ" },
                new String[] { "<IDEOGRAPHIC>", "<IDEOGRAPHIC>" });
    }

    @Test
    public void testKorean() throws Exception {
        // Korean words
        assertAnalyzesTo(a, "안녕하세요 한글입니다", new String[]{"안녕하세요", "한글입니다"});
    }

    @Test
    public void testKoreanTypes() throws Exception {
        assertAnalyzesTo(a, "훈민정음", new String[] { "훈민정음" }, new String[] { "<HANGUL>" });
    }
}