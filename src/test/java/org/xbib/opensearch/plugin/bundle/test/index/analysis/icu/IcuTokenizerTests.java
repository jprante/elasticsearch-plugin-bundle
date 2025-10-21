package org.xbib.opensearch.plugin.bundle.test.index.analysis.icu;

import org.apache.lucene.analysis.Tokenizer;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.index.Index;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.io.StringReader;

/**
 * ICU tokenizer tests.
 */
public class IcuTokenizerTests extends OpenSearchTokenStreamTestCase {

    public void testLetterNonBreak() throws Exception {

        String source = "Das ist ein Bindestrich-Wort, oder etwa nicht? Jetzt kommen wir zum Ende.";

        String[] expected = {
                "Das",
                "ist",
                "ein",
                "Bindestrich-Wort",
                "oder",
                "etwa",
                "nicht",
                "Jetzt",
                "kommen",
                "wir",
                "zum",
                "Ende"
        };
        String resource = "icu_tokenizer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_hyphen_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenizer, expected);
    }

    public void testIdentifierNonBreak() throws Exception {
        String source = "ISBN 3-428-84350-9";
        String[] expected = {"ISBN", "3-428-84350-9"};
        String resource = "icu_tokenizer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_whitespace_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenizer, expected);
    }

    public void testIdentifierNonBreakSingleToken() throws Exception {
        String source = "3-428-84350-9 is an ISBN";
        String[] expected = {"3-428-84350-9", "is", "an", "ISBN"};
        String resource = "icu_tokenizer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_whitespace_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenizer, expected);
    }

    public void testIdentifierNonBreakSpaceTwoTokens() throws Exception {
        String source = "Binde-strich-wort 3-428-84350-9";
        String[] expected = {"Binde-strich-wort", "3-428-84350-9"};
        String resource = "icu_tokenizer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_whitespace_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenizer, expected);
    }
}
