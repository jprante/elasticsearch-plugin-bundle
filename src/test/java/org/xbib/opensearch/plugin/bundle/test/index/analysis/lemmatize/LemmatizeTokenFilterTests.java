package org.xbib.opensearch.plugin.bundle.test.index.analysis.lemmatize;

import org.apache.lucene.analysis.Analyzer;
import org.opensearch.analysis.common.CommonAnalysisPlugin;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.index.Index;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

/**
 * Lemmatize token filter tests.
 */
public class LemmatizeTokenFilterTests extends OpenSearchTokenStreamTestCase {

    public void testLemmatizer() throws Exception {

        String source = "While these texts were previously only available to users of academic libraries " +
                "participating in the partnership, at the end of the first phase of EEBO-TCP the current " +
                "25,000 texts have now been released into the public domain.";
        String[] expected = {
                "While",
                "this",
                "text",
                "be",
                "previously",
                "only",
                "available",
                "to",
                "user",
                "of",
                "academic",
                "library",
                "participate",
                "in",
                "the",
                "partnership",
                "at",
                "end",
                "first",
                "phase",
                "EEBO",
                "TCP",
                "current",
                "25,000",
                "have",
                "now",
                "release",
                "into",
                "public",
                "domain"
        };
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myanalyzer.type", "custom")
                .put("index.analysis.analyzer.myanalyzer.tokenizer", "standard")
                .put("index.analysis.analyzer.myanalyzer.filter.0", "lemmatize")
                .put("index.analysis.analyzer.myanalyzer.filter.1", "unique")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        Analyzer myanalyzer = analysis.indexAnalyzers.get( "myanalyzer");
        assertAnalyzesTo(myanalyzer, source, expected);
    }

    public void testFull() throws Exception {

        String source = "While these texts were previously only available to users of academic libraries " +
                "participating in the partnership, at the end of the first phase of EEBO-TCP the current " +
                "25,000 texts have now been released into the public domain.";
        String[] expected = {
                "While",
                "these",
                "this",
                "texts",
                "text",
                "were",
                "be",
                "previously",
                "only",
                "available",
                "to",
                "users",
                "user",
                "of",
                "academic",
                "libraries",
                "library",
                "participating",
                "participate",
                "in",
                "the",
                "partnership",
                "at",
                "end",
                "first",
                "phase",
                "EEBO",
                "TCP",
                "current",
                "25,000",
                "have",
                "now",
                "been",
                "released",
                "release",
                "into",
                "public",
                "domain"
        };

        Settings settings = Settings.builder()
                .put("index.analysis.filter.myfilter.type", "lemmatize")
                .put("index.analysis.filter.myfilter.lemma_only", "false")
                .put("index.analysis.analyzer.myanalyzer.type", "custom")
                .put("index.analysis.analyzer.myanalyzer.tokenizer", "standard")
                .put("index.analysis.analyzer.myanalyzer.filter.0", "myfilter")
                .put("index.analysis.analyzer.myanalyzer.filter.1", "unique")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        Analyzer myanalyzer =analysis.indexAnalyzers.get("myanalyzer");
        assertAnalyzesTo(myanalyzer, source, expected);
    }

    public void testGermanLemmatizer() throws Exception {

        String source = "Die Würde des Menschen ist unantastbar. " +
                "Sie zu achten und zu schützen ist Verpflichtung aller staatlichen Gewalt. " +
                "Das Deutsche Volk bekennt sich darum zu unverletzlichen und unveräußerlichen Menschenrechten " +
                "als Grundlage jeder menschlichen Gemeinschaft, des Friedens und der Gerechtigkeit in der Welt.";
        String[] expected = {
                "Die",
                "Würde",
                "der",
                "Mensch",
                "mein",  // ?
                "unantastbar",
                "Sie",
                "zu",
                "achten",
                "und",
                "zu",
                "schützen",
                "mein",  // ?
                "Verpflichtung",
                "all",
                "staatlich",
                "Gewalt",
                "Das",
                "deutsch",
                "Volk",
                "bekennen",
                "sich",
                "darum",
                "zu",
                "unverletzlichen", // ?
                "und",
                "unveräußerlichen", // ?
                "Menschenrechten", // ?
                "als",
                "Grundlage",
                "jed",
                "menschlich",
                "Gemeinschaft",
                "der",
                "Friede",
                "und",
                "der",
                "Gerechtigkeit",
                "in",
                "der",
                "Welt"
        };
        Settings settings = Settings.builder()
                .put("index.analysis.filter.myfilter.type", "lemmatize")
                .put("index.analysis.filter.myfilter.language", "de")
                .put("index.analysis.analyzer.myanalyzer.type", "custom")
                .put("index.analysis.analyzer.myanalyzer.tokenizer", "standard")
                .put("index.analysis.analyzer.myanalyzer.filter.0", "myfilter")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY), new CommonAnalysisPlugin());
        Analyzer myanalyzer = analysis.indexAnalyzers.get("myanalyzer");
        assertAnalyzesTo(myanalyzer, source, expected);
    }
}
