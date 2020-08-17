package org.xbib.elasticsearch.plugin.bundle.test.index.analysis.icu.segmentation;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.test.ESTokenStreamTestCase;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizerFactory;

import java.io.Reader;
import java.io.StringReader;

/**
 * ICU tokenizer factory tests.
 */
public class IcuTokenizerFactoryTests extends ESTokenStreamTestCase {

    class TestIcuTokenizerFactory extends IcuTokenizerFactory {

        TestIcuTokenizerFactory(Settings settings) {
            super(indexSettings(), null, "test", settings);
        }
    }

    private static IndexSettings indexSettings() {
        Settings settings = Settings.builder()
                .put(IndexMetadata.SETTING_VERSION_CREATED, Version.CURRENT) // required!
                .build();
        IndexMetadata indexMetadata = IndexMetadata.builder("test")
                .settings(settings)
                .numberOfShards(1)
                .numberOfReplicas(1)
                .build();
        return new IndexSettings(indexMetadata, settings);
    }

    public void testMixedText() throws Exception {
        Reader reader = new StringReader("การที่ได้ต้องแสดงว่างานดี  This is a test ກວ່າດອກ");
        IcuTokenizerFactory factory = new TestIcuTokenizerFactory(Settings.EMPTY);
        Tokenizer stream = factory.create();
        stream.setReader(reader);
        assertTokenStreamContents(stream,
                new String[] { "การ", "ที่", "ได้", "ต้อง", "แสดง", "ว่า", "งาน", "ดี",
                        "This", "is", "a", "test", "ກວ່າ", "ດອກ"});
    }

    public void testTokenizeLatinOnWhitespaceOnly() throws Exception {
        Reader reader = new StringReader
                ("  Don't,break.at?/(punct)!  \u201Cnice\u201D\r\n\r\n85_At:all; `really\" +2=3$5,&813 !@#%$^)(*@#$   ");
        Settings settings = Settings.builder()
                .put("rulefiles", "Latn:Latin-break-only-on-whitespace.rbbi")
                .build();
        IcuTokenizerFactory factory = new TestIcuTokenizerFactory(settings);
        Tokenizer stream = factory.create();
        stream.setReader(reader);
        assertTokenStreamContents(stream,
                new String[] { "Don't,break.at?/(punct)!", "\u201Cnice\u201D", "85_At:all;", "`really\"",
                        "+2=3$5,&813", "!@#%$^)(*@#$" },
                new String[] { "<ALPHANUM>",               "<ALPHANUM>",       "<ALPHANUM>", "<ALPHANUM>",
                        "<NUM>",       "<OTHER>" });
    }

    public void testTokenizeLatinDontBreakOnHyphens() throws Exception {
        Reader reader = new StringReader
                ("One-two punch.  Brang-, not brung-it.  This one--not that one--is the right one, -ish.");
        Settings settings = Settings.builder()
                .put("rulefiles", "Latn:Latin-dont-break-on-hyphens.rbbi")
                .build();
        IcuTokenizerFactory factory = new TestIcuTokenizerFactory(settings);
        Tokenizer stream = factory.create();
        stream.setReader(reader);
        assertTokenStreamContents(stream,
                new String[] { "One-two", "punch",
                        "Brang", "not", "brung-it",
                        "This", "one", "not", "that", "one", "is", "the", "right", "one", "ish" });
    }

    public void testKeywordTokenizeCyrillicAndThai() throws Exception {
        Reader reader = new StringReader
                ("Some English.  Немного русский.  ข้อความภาษาไทยเล็ก ๆ น้อย ๆ  More English.");
        Settings settings = Settings.builder()
                .put("rulefiles", "Cyrl:KeywordTokenizer.rbbi,Thai:KeywordTokenizer.rbbi")
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