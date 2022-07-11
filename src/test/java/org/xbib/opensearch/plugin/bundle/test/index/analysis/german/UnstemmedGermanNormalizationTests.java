package org.xbib.opensearch.plugin.bundle.test.index.analysis.german;

import org.apache.lucene.analysis.Analyzer;

import org.opensearch.analysis.common.CommonAnalysisPlugin;
import org.opensearch.common.settings.Settings;
import org.opensearch.index.Index;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.io.StringReader;

/**
 * Unstemmed german normalization tests.
 */
public class UnstemmedGermanNormalizationTests extends OpenSearchTokenStreamTestCase {

    public void testOne() throws Exception {
        String source = "Ein Tag in Köln im Café an der Straßenecke mit einer Standard-Nummer ISBN 1-4493-5854-3";
        String[] expected = {
                "tag",
                "koln",
                "cafe",
                "caf",
                "strassenecke",
                "strasseneck",
                "standard-nummer",
                "standardnummer",
                "standard-numm",
                "standardnumm",
                "isbn",
                "1-4493-5854-3",
                "1449358543",
                "978-1-4493-5854-9",
                "9781449358549"
        };
        String resource = "unstemmed.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        Analyzer analyzer = analysis.indexAnalyzers.get("default");
        assertTokenStreamContents(analyzer.tokenStream(null, new StringReader(source)), expected);
    }

    public void testTwo() throws Exception {
        String source = "So wird's was: das Elasticsearch-Buch erscheint beim O'Reilly-Verlag.";
        String[] expected = {
                "wird's",
                "elasticsearch-buch",
                "elasticsearchbuch",
                "erscheint",
                "o'reilly-verlag",
                "o'reillyverlag"
        };
        String resource = "unstemmed.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        Analyzer analyzer = analysis.indexAnalyzers.get("default");
        assertTokenStreamContents(analyzer.tokenStream(null, new StringReader(source)), expected);
    }

    public void testThree() throws Exception {
        String source = "978-1-4493-5854-9";
        String[] expected = {
             "978-1-4493-5854-9",
             "9781449358549"
        };
        String resource = "unstemmed.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        Analyzer analyzer = analysis.indexAnalyzers.get("default");
        assertTokenStreamContents(analyzer.tokenStream(null, new StringReader(source)), expected);
    }

    public void testFour() throws Exception {
        String source = "Prante, Jörg";
        String[] expected = {
                "prante",
                "jorg"
        };
        String resource = "unstemmed.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        Analyzer analyzer = analysis.indexAnalyzers.get("unstemmed");
        assertTokenStreamContents(analyzer.tokenStream("test", new StringReader(source)), expected);
    }

    public void testFive() throws Exception {
        String source = "Schroeder";
        String[] expected = {
                "schroder"
        };
        String resource = "unstemmed.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        Analyzer analyzer = analysis.indexAnalyzers.get("unstemmed");
        assertTokenStreamContents(analyzer.tokenStream("test", new StringReader(source)), expected);
    }

    public void testSix() throws Exception {
        String source = "Programmieren in C++ für Einsteiger";
        String[] expected = {
                "programmieren",
                "programmi",
                "c++",
                "einsteiger",
                "einsteig"
        };
        String resource = "unstemmed.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        Analyzer analyzer = analysis.indexAnalyzers.get("default");
        assertTokenStreamContents(analyzer.tokenStream(null, new StringReader(source)), expected);
    }
}
