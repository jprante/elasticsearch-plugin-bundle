package org.xbib.elasticsearch.index.analysis.expansion;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;

import java.io.IOException;

/**
 *
 */
public class FstExpansionTokenFilterTests extends BaseTokenStreamTest {

    @Test
    public void test() throws IOException {

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
                .put("index.analysis.analyzer.myanalyzer.type", "custom")
                .put("index.analysis.analyzer.myanalyzer.tokenizer", "standard")
                .put("index.analysis.analyzer.myanalyzer.filter.0", "expansion")
                .put("index.analysis.analyzer.myanalyzer.filter.1", "unique")
                .build();
        Analyzer myanalyzer = MapperTestUtils.analyzer(settings, "myanalyzer");
        assertAnalyzesTo(myanalyzer, source, expected);
    }
}
