package org.xbib.elasticsearch.plugin.bundle.test.index.analysis.icu.segmentation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.AttributeFactory;
import org.elasticsearch.testframework.ESTokenStreamTestCase;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.segmentation.DefaultIcuTokenizerConfig;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizer;

/**
 * ICU tokenizer CJK tests.
 */
public class IcuTokenizerCJKTests extends ESTokenStreamTestCase {

    private static Analyzer create() {
        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                return new TokenStreamComponents(new IcuTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY,
                        new DefaultIcuTokenizerConfig(true, true)));
            }
        };
    }

    public static void destroyAnalyzer(Analyzer a) {
        a.close();
    }

    public void testSimpleChinese() throws Exception {
        Analyzer a = create();
        assertAnalyzesTo(a, "我购买了道具和服装。",
                new String[] { "我", "购买", "了", "道具", "和", "服装" }
        );
        destroyAnalyzer(a);
    }

    public void testChineseNumerics() throws Exception {
        Analyzer a = create();
        assertAnalyzesTo(a, "９４８３", new String[] { "９４８３" });
        assertAnalyzesTo(a, "院內分機９４８３。",
                new String[] { "院", "內", "分機", "９４８３" });
        assertAnalyzesTo(a, "院內分機9483。",
                new String[] { "院", "內", "分機", "9483" });
        destroyAnalyzer(a);
    }

    public void testSimpleJapanese() throws Exception {
        Analyzer a = create();
        assertAnalyzesTo(a, "それはまだ実験段階にあります",
                new String[] { "それ", "は", "まだ", "実験", "段階", "に", "あり", "ます"  }
        );
        destroyAnalyzer(a);
    }

    public void testJapaneseTypes() throws Exception {
        Analyzer a = create();
        assertAnalyzesTo(a, "仮名遣い カタカナ",
                new String[] { "仮名遣い", "カタカナ" },
                new String[] { "<IDEOGRAPHIC>", "<IDEOGRAPHIC>" });
        destroyAnalyzer(a);
    }

    public void testKorean() throws Exception {
        Analyzer a = create();
        // Korean words
        assertAnalyzesTo(a, "안녕하세요 한글입니다", new String[]{"안녕하세요", "한글입니다"});
        destroyAnalyzer(a);
    }

    public void testKoreanTypes() throws Exception {
        Analyzer a = create();
        assertAnalyzesTo(a, "훈민정음", new String[] { "훈민정음" }, new String[] { "<HANGUL>" });
        destroyAnalyzer(a);
    }
}
