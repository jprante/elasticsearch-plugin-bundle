package org.xbib.opensearch.plugin.bundle.test.index.analysis.icu;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.opensearch.common.settings.Settings;
import org.opensearch.index.Index;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.io.Reader;
import java.io.StringReader;

/**
 * ICU folding filter tests.
 */
public class IcuFoldingFilterTests extends OpenSearchTokenStreamTestCase {

    public void testFoldingCharFilter() throws Exception {
        String source = "Jörg Prante";
        String resource = "icu_folding.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Reader charFilter = analysis.charFilter.get("my_icu_folder").create(new StringReader(source));
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = charFilter.read()) != -1) {
            sb.append((char)ch);
        }
        assertEquals("jorg prante", sb.toString());
    }

    public void testFoldingAnalyzer() throws Exception {
        String resource = "icu_folding.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("my_icu_analyzer");
        TokenStream ts = analyzer.tokenStream("test", "Jörg Prante");
        String[] expected = {"jorg", "prante"};
        assertTokenStreamContents(ts, expected);
        assertTokenStreamContents(analyzer.tokenStream("test", "This is a test"), new String[]{ "this", "is", "a", "test" });
        assertTokenStreamContents(analyzer.tokenStream("test", "ΜΆΪΟΣ"), new String[]{  "μαιοσ" });
        assertTokenStreamContents(analyzer.tokenStream("test", "Μάϊος"), new String[] { "μαιοσ" });
        assertTokenStreamContents(analyzer.tokenStream("test", "𐐖"), new String[] { "𐐾" });
        assertTokenStreamContents(analyzer.tokenStream("test", "ﴳﴺﰧ"), new String[] { "طمطمطم" });
        assertTokenStreamContents(analyzer.tokenStream("test", "क्‍ष"), new String[] { "कष" });
        assertTokenStreamContents(analyzer.tokenStream("test", "résumé"), new String[] { "resume" });
        assertTokenStreamContents(analyzer.tokenStream("test", "re\u0301sume\u0301"), new String[] { "resume" });
        assertTokenStreamContents(analyzer.tokenStream("test", "৭০৬"), new String[] { "706" });
        assertTokenStreamContents(analyzer.tokenStream("test", "đis is cræzy"), new String[] { "dis", "is", "craezy" });
        assertTokenStreamContents(analyzer.tokenStream("test",  "ELİF"), new String[] { "elif" });
        assertTokenStreamContents(analyzer.tokenStream("test", "eli\u0307f"), new String[] { "elif" });
        assertTokenStreamContents(analyzer.tokenStream("test", "Ruß"), new String[]{ "russ" });
    }

    public void testFoldingAnalyzerWithExceptions() throws Exception {
        String resource = "icu_folding.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));

        Analyzer analyzer = analysis.indexAnalyzers.get("my_icu_analyzer_with_exceptions");
        TokenStream ts = analyzer.tokenStream("test", "Jörg Prante");
        String[] expected = { "jörg", "prante" };
        assertTokenStreamContents(ts, expected);
    }
}
