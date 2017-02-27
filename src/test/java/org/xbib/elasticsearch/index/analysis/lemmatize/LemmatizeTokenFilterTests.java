package org.xbib.elasticsearch.index.analysis.lemmatize;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;

import java.io.IOException;

/**
 *
 */
public class LemmatizeTokenFilterTests extends BaseTokenStreamTest {

    @Test
    public void testLemmatizer() throws IOException {

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
        Analyzer myanalyzer = MapperTestUtils.analyzer(settings, "myanalyzer");
        assertAnalyzesTo(myanalyzer, source, expected);
    }

    @Test
    public void testFull() throws IOException {

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
        Analyzer myanalyzer = MapperTestUtils.analyzer(settings, "myanalyzer");
        assertAnalyzesTo(myanalyzer, source, expected);
    }

    @Test
    public void testGermanLemmatizer() throws IOException {

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
        Analyzer myanalyzer = MapperTestUtils.analyzer(settings, "myanalyzer");
        assertAnalyzesTo(myanalyzer, source, expected);
    }
}
