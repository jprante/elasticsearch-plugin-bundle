package org.xbib.opensearch.plugin.bundle.test.index.analysis.decompound.fst;

import org.apache.lucene.analysis.Analyzer;
import org.junit.Ignore;
import org.opensearch.common.settings.Settings;
import org.opensearch.index.Index;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

/**
 * Finite state transducer decompound token filter tests.
 */
@Ignore
public class FstDecompoundTokenFilterTests extends OpenSearchTokenStreamTestCase {

    public void testDecompound() throws Exception {

        String source = "Die Jahresfeier der Rechtsanwaltskanzleien auf dem Donaudampfschiff hat viel Ökosteuer gekostet";

        String[] expected = {
                "Die",
                "Jahresfeier",
                "jahres",
                "feier",
                "der",
                "Rechtsanwaltskanzleien",
                "rechts",
                "anwalts",
                "kanzleien",
                "auf",
                "dem",
                "Donaudampfschiff",
                "donau",
                "dampf",
                "schiff",
                "hat",
                "viel",
                "Ökosteuer",
                "ökos",
                "teuer",
                "gekostet"
        };

        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myanalyzer.type", "custom")
                .put("index.analysis.analyzer.myanalyzer.tokenizer", "standard")
                .put("index.analysis.analyzer.myanalyzer.filter.0", "fst_decompound")
                .put("index.analysis.analyzer.myanalyzer.filter.1", "unique")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY)/*, new CommonAnalysisPlugin()*/);
        Analyzer myanalyzer = analysis.indexAnalyzers.get("myanalyzer");
        assertAnalyzesTo(myanalyzer, source, expected);
    }
}
