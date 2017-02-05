package org.xbib.elasticsearch.index.analysis.decompound.fst;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;

import java.io.IOException;

/**
 *
 */
public class FstDecompoundTokenFilterTests extends BaseTokenStreamTest {

    @Test
    public void test() throws IOException {

        String source = "Die Jahresfeier der Rechtsanwaltskanzleien auf dem Donaudampfschiff hat viel Ökosteuer gekostet";

        String[] expected = {
                "Die",
                "Jahresfeier",
                "jahres",
                "feier",
                "jahre",
                "jahr",
                "der",
                "Rechtsanwaltskanzleien",
                "rechts",
                "anwalts",
                "kanzleien",
                "recht",
                "anwalt",
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
        Analyzer myanalyzer = MapperTestUtils.analyzer(settings, "myanalyzer");
        assertAnalyzesTo(myanalyzer, source, expected);
    }
}
