package org.xbib.opensearch.plugin.bundle.test.index.analysis.icu;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.index.Index;
import org.opensearch.index.analysis.TokenFilterFactory;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.io.StringReader;

/**
 * ICU number format tests.
 */
public class IcuNumberFormatTests extends OpenSearchTokenStreamTestCase {

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
        String resource = "icu_numberformat.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
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
        String resource = "icu_numberformat.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("spellout_en");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }
}
