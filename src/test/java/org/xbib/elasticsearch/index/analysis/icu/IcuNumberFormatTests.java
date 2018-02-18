package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.ESTokenStreamTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.io.StringReader;

/**
 * ICU number format tests.
 */
public class IcuNumberFormatTests extends ESTokenStreamTestCase {

    public void testGermanNumberFormat() throws Exception {

        String source = "Muss Rudi Völler fünftausend oder 10000 EUR Strafe zahlen?";

        String[] expected = {
                "Muss",
                "Rudi",
                "Völler",
                "fünftausend",
                "oder",
                "zehntausend",
                "EUR",
                "Strafe",
                "zahlen"
        };
        String resource = "/org/xbib/elasticsearch/index/analysis/icu/icu_numberformat.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("spellout_de");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }

    public void testAmericanEnglish() throws Exception {

        String source = "You will never get 100,000 US dollars of salary per year.";

        String[] expected = {
                "You",
                "will",
                "never",
                "get",
                "onehundredthousand",
                "US",
                "dollars",
                "of",
                "salary",
                "per",
                "year"
        };
        String resource = "/org/xbib/elasticsearch/index/analysis/icu/icu_numberformat.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("spellout_en");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }
}
