package org.xbib.elasticsearch.index.analysis.decompound.fst;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.ESTokenStreamTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

/**
 * Finite state transducer decompound token filter tests.
 */
public class FstDecompoundTokenFilterTests extends ESTokenStreamTestCase {

    public void testDecompund() throws Exception {

        String source = "Die Jahresfeier der Rechtsanwaltskanzleien auf dem Donaudampfschiff hat viel Ökosteuer gekostet";

        String[] expected = {
                "Die",
                "Jahresfeier",
                "jahres",
                "feier",
                "jahre",
                //"jahr",
                "der",
                "Rechtsanwaltskanzleien",
                "rechts",
                "anwalts",
                "kanzleien",
                //"recht",
                //"anwalt",
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
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer myanalyzer = analysis.indexAnalyzers.get("myanalyzer");
        // TODO(jprante) flaky?
        // inconsistent endOffset 2 pos=1 posLen=1 token=jahres expected:<15> but was:<10>
        // inconsistent endOffset 10 pos=9 posLen=1 token=ökos expected:<86> but was:<81>
        // term 10 expected:<[auf]> but was:<[recht]>
        assertAnalyzesTo(myanalyzer, source, expected);
    }
}
