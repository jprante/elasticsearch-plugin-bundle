package org.xbib.elasticsearch.index.analysis.icu.segmentation;

import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;

/**
 *
 */
public class IcuTokenizerFactoryTests extends BaseTokenStreamTest {

    class TestIcuTokenizerFactory extends IcuTokenizerFactory {

        public TestIcuTokenizerFactory(Settings settings) {
            super(indexSettings(), null, "test", settings);
        }
    }

    private static IndexSettings indexSettings() {
        Settings settings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .build();
        IndexMetaData indexMetaData = IndexMetaData.builder("test")
                .settings(settings)
                .numberOfShards(1)
                .numberOfReplicas(1)
                .build();
        return new IndexSettings(indexMetaData, settings);
    }

    @Test
    public void testMixedText() throws Exception {
        Reader reader = new StringReader("การที่ได้ต้องแสดงว่างานดี  This is a test ກວ່າດອກ");
        IcuTokenizerFactory factory = new TestIcuTokenizerFactory(Settings.EMPTY);
        Tokenizer stream = factory.create();
        stream.setReader(reader);
        assertTokenStreamContents(stream,
                new String[] { "การ", "ที่", "ได้", "ต้อง", "แสดง", "ว่า", "งาน", "ดี",
                        "This", "is", "a", "test", "ກວ່າ", "ດອກ"});
    }

    @Test
    public void testTokenizeLatinOnWhitespaceOnly() throws Exception {
        Reader reader = new StringReader
                ("  Don't,break.at?/(punct)!  \u201Cnice\u201D\r\n\r\n85_At:all; `really\" +2=3$5,&813 !@#%$^)(*@#$   ");
        Settings settings = Settings.builder()
                .put("rulefiles", "Latn:icu/Latin-break-only-on-whitespace.rbbi")
                .build();
        IcuTokenizerFactory factory = new TestIcuTokenizerFactory(settings);
        Tokenizer stream = factory.create();
        stream.setReader(reader);
        assertTokenStreamContents(stream,
                new String[] { "Don't,break.at?/(punct)!", "\u201Cnice\u201D", "85_At:all;", "`really\"",  "+2=3$5,&813", "!@#%$^)(*@#$" },
                new String[] { "<ALPHANUM>",               "<ALPHANUM>",       "<ALPHANUM>", "<ALPHANUM>", "<NUM>",       "<OTHER>" });
    }

    @Test
    public void testTokenizeLatinDontBreakOnHyphens() throws Exception {
        Reader reader = new StringReader
                ("One-two punch.  Brang-, not brung-it.  This one--not that one--is the right one, -ish.");
        Settings settings = Settings.builder()
                .put("rulefiles", "Latn:icu/Latin-dont-break-on-hyphens.rbbi")
                .build();
        IcuTokenizerFactory factory = new TestIcuTokenizerFactory(settings);
        Tokenizer stream = factory.create();
        stream.setReader(reader);
        assertTokenStreamContents(stream,
                new String[] { "One-two", "punch",
                        "Brang", "not", "brung-it",
                        "This", "one", "not", "that", "one", "is", "the", "right", "one", "ish" });
    }

    @Test
    public void testKeywordTokenizeCyrillicAndThai() throws Exception {
        Reader reader = new StringReader
                ("Some English.  Немного русский.  ข้อความภาษาไทยเล็ก ๆ น้อย ๆ  More English.");
        Settings settings = Settings.builder()
                .put("rulefiles", "Cyrl:icu/KeywordTokenizer.rbbi,Thai:icu/KeywordTokenizer.rbbi")
                .build();
        IcuTokenizerFactory factory = new TestIcuTokenizerFactory(settings);
        Tokenizer stream = factory.create();
        stream.setReader(reader);
        assertTokenStreamContents(stream, new String[] { "Some", "English",
                "Немного русский.  ",
                "ข้อความภาษาไทยเล็ก ๆ น้อย ๆ  ",
                "More", "English" });
    }
}