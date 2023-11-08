package org.xbib.opensearch.plugin.bundle.test.index.analysis.hyphen;

import org.apache.lucene.analysis.Analyzer;
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
 * Hyphen tokenizer tests.
 */
public class HyphenTokenizerTests extends OpenSearchTokenStreamTestCase {

    public void testOne() throws Exception {

        String source = "Das ist ein Bindestrich-Wort.";

        String[] expected = {
                "Das",
                "ist",
                "ein",
                "Bindestrich-Wort",
                "BindestrichWort",
                "Wort",
                "Bindestrich"
        };
        String resource = "hyphen_tokenizer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("hyphen");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }

    public void testTwo() throws Exception {

        String source = "Das E-Book muss dringend zum Buchbinder.";

        String[] expected = {
                "Das",
                "E-Book",
                "EBook",
                "Book",
                "muss",
                "dringend",
                "zum",
                "Buchbinder"
        };
        String resource = "hyphen_tokenizer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("hyphen");
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testThree() throws Exception {

        String source = "Ich will nicht als Service-Center-Mitarbeiterin, sondern 100-prozentig als Dipl.-Ing. arbeiten!";

        String[] expected = {
                "Ich",
                "will",
                "nicht",
                "als",
                "Service-Center-Mitarbeiterin",
                "ServiceCenterMitarbeiterin",
                "Mitarbeiterin",
                "ServiceCenter",
                "ServiceCenter-Mitarbeiterin",
                "Center-Mitarbeiterin",
                "Service",
                "sondern",
                "100-prozentig",
                "als",
                "Dipl",
                "Ing",
                "arbeiten"
        };
        String resource = "hyphen_tokenizer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("hyphen");
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testFour() throws Exception {

        String source = "So wird's was: das Elasticsearch-Buch erscheint beim O'Reilly-Verlag.";

        String[] expected = {
                "So",
                "wird's",
                "was",
                "das",
                "Elasticsearch-Buch",
                "ElasticsearchBuch",
                "Buch",
                "Elasticsearch",
                "erscheint",
                "beim",
                "O'Reilly-Verlag"
        };
        String resource = "hyphen_tokenizer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("hyphen");
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testFive() throws Exception {

        String source = "978-1-4493-5854-9";

        String[] expected = {
                "978-1-4493-5854-9"
        };

        String resource = "hyphen_tokenizer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("hyphen");
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testSix() throws Exception {

        String source = "E-Book";

        String[] expected = {
                "E-Book",
                "EBook",
                "Book"
        };

        String resource = "hyphen_tokenizer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("hyphen");
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testSeven() throws Exception {
        String source = "Procter & Gamble ist Procter&Gamble. Schwarz - weiss ist schwarz-weiss";

        String[] expected = {
                "Procter",
                "Gamble",
                "ist",
                "Procter&Gamble",
                "Schwarz",
                "weiss",
                "ist",
                "schwarz-weiss",
                "schwarzweiss",
                "weiss",
                "schwarz"
        };

        String resource = "hyphen_tokenizer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("hyphen");
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testEight() throws Exception {

        String source = "Ich will nicht als Service-Center-Mitarbeiterin mit C++, sondern 100-prozentig als Dipl.-Ing. arbeiten!";

        String[] expected = {
                "Ich",
                "will",
                "nicht",
                "als",
                "Service-Center-Mitarbeiterin",
                "ServiceCenterMitarbeiterin",
                "mit",
                "C++",
                "sondern",
                "100-prozentig",
                "100prozentig",
                "als",
                "Dipl",
                "Ing",
                "arbeiten"
        };
        String resource = "hyphen_tokenizer_without_subwords.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_hyphen_tokenfilter");
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testNine() throws Exception {

        String source = "Das ist ein Punkt. Und noch ein Punkt für U.S.A. Oder? Nicht doch.";

        String[] expected = {
                "Das",
                "ist",
                "ein",
                "Punkt",
                "Und",
                "noch",
                "ein",
                "Punkt",
                "für",
                "U.S.A",
                "Oder",
                "Nicht",
                "doch"

        };
        String resource = "hyphen_tokenizer_without_subwords.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_hyphen_tokenfilter");
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testTen() throws Exception {

        String source = "Das ist ein Punkt. Und noch ein Punkt für U.S.A. Oder? Nicht doch.";

        String[] expected = {
                "Das",
                "ist",
                "ein",
                "Punkt",
                "Und",
                "noch",
                "ein",
                "Punkt",
                "für",
                "U.S.A",
                "Oder",
                "Nicht",
                "doch"

        };
        String resource = "hyphen_analyzer.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("my_hyphen_analyzer");
        assertTokenStreamContents(analyzer.tokenStream("text", new StringReader(source)), expected);
    }
}
