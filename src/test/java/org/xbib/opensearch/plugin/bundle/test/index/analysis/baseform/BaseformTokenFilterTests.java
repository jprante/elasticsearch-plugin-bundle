package org.xbib.opensearch.plugin.bundle.test.index.analysis.baseform;

import org.apache.lucene.analysis.Tokenizer;
import org.opensearch.common.settings.Settings;
import org.opensearch.index.Index;
import org.opensearch.index.analysis.TokenFilterFactory;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.io.StringReader;

/**
 * Base form token filter tests.
 */
public class BaseformTokenFilterTests extends OpenSearchTokenStreamTestCase {

    public void testOne() throws Exception {

        String source = "Die Jahresfeier der Rechtsanwaltskanzleien auf dem Donaudampfschiff hat viel Ökosteuer gekostet";

        String[] expected = {
            "Die",
            "Die",
            "Jahresfeier",
            "Jahresfeier",
            "der",
            "der",
            "Rechtsanwaltskanzleien",
            "Rechtsanwaltskanzlei",
            "auf",
            "auf",
            "dem",
            "der",
            "Donaudampfschiff",
            "Donaudampfschiff",
            "hat",
            "haben",
            "viel",
            "viel",
            "Ökosteuer",
            "Ökosteuer",
            "gekostet",
            "kosten"
        };
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                Settings.EMPTY,
                new BundlePlugin(Settings.EMPTY));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("baseform");
        Tokenizer tokenizer = analysis.tokenizer.get("standard").create();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testTwo() throws Exception {

        String source = "Das sind Autos, die Nudeln transportieren.";

        String[] expected = {
                "Das",
                "Das",
                "sind",
                "sind",
                "Autos",
                "Auto",
                "die",
                "der",
                "Nudeln",
                "Nudel",
                "transportieren",
                "transportieren"
        };
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                Settings.EMPTY,
                new BundlePlugin(Settings.EMPTY));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("baseform");
        Tokenizer tokenizer = analysis.tokenizer.get("standard").create();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testThree() throws Exception {

        String source = "wurde zum tollen gemacht";

        String[] expected = {
                "wurde",
                "werden",
                "zum",
                "zum",
                "tollen",
                "tollen",
                "gemacht",
                "machen"
        };
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                Settings.EMPTY,
                new BundlePlugin(Settings.EMPTY));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("baseform");
        Tokenizer tokenizer = analysis.tokenizer.get("standard").create();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }
}
