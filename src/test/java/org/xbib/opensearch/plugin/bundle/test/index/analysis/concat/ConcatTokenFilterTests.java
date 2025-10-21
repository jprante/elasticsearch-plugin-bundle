package org.xbib.opensearch.plugin.bundle.test.index.analysis.concat;

import org.apache.lucene.analysis.Analyzer;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.index.Index;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

/**
 * Concat token filter tests.
 */
public class ConcatTokenFilterTests extends OpenSearchTokenStreamTestCase {

    public void testConcat() throws Exception {
        String source = "Das ist ein Schlüsselwort, ein Bindestrichwort";
        String[] expected = {
                "Das ist ein Schlüsselwort ein Bindestrichwort"
        };
        String resource = "concat_analysis.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("concat");
        assertNotNull(analyzer);
        assertTokenStreamContents(analyzer.tokenStream("test-field", source), expected);
    }
}
