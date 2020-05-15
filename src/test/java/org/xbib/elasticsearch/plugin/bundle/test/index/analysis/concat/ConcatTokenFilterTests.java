package org.xbib.elasticsearch.plugin.bundle.test.index.analysis.concat;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.ESTokenStreamTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

/**
 * Concat token filter tests.
 */
public class ConcatTokenFilterTests extends ESTokenStreamTestCase {

    public void testConcat() throws Exception {
        String source = "Das ist ein Schlüsselwort, ein Bindestrichwort";
        String[] expected = {
                "Das ist ein Schlüsselwort ein Bindestrichwort"
        };
        String resource = "concat_analysis.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("concat");
        assertNotNull(analyzer);
        assertTokenStreamContents(analyzer.tokenStream("test-field", source), expected);
    }
}
