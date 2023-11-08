package org.xbib.opensearch.plugin.bundle.test.index.analysis.symbolname;

import org.apache.lucene.analysis.Tokenizer;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.index.Index;
import org.opensearch.index.analysis.TokenFilterFactory;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.io.StringReader;

/**
 * Symbol name tokenfilter tests.
 */
public class SymbolnameTokenFilterTests extends OpenSearchTokenStreamTestCase {

    public void testSimple() throws Exception {

        String source = "Programmieren mit C++";

        String[] expected = {
                "Programmieren",
                "mit",
                "C++",
                "C __PLUSSIGN__ __PLUSSIGN__",
                "C",
                "__PLUSSIGN__",
                "__PLUSSIGN__"
        };
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                Settings.EMPTY,
                new BundlePlugin(Settings.EMPTY));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("symbolname");
        Tokenizer tokenizer = whitespaceMockTokenizer(new StringReader(source));
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testPunctuation() throws Exception {

        String source = "Programmieren mit C++ Version 2.0";

        String[] expected = {
                "Programmieren",
                "mit",
                "C++",
                "C __PLUSSIGN__ __PLUSSIGN__",
                "C",
                "__PLUSSIGN__",
                "__PLUSSIGN__",
                "Version",
                "2.0",
                "__DIGITTWO__ __FULLSTOP__ __DIGITZERO__",
                "__DIGITTWO__",
                "__FULLSTOP__",
                "__DIGITZERO__"
        };
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                Settings.EMPTY,
                new BundlePlugin(Settings.EMPTY));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("symbolname");
        Tokenizer tokenizer = whitespaceMockTokenizer(new StringReader(source));
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public void testSingleSymbols() throws Exception {

        String source = "Programmieren mit + und - ist toll, oder?";

        String[] expected = {
                "Programmieren",
                "mit",
                "+",
                "__PLUSSIGN__",
                "und",
                "-",
                "__HYPHENMINUS__",
                "ist",
                "toll,",
                "toll __COMMA__",
                "toll",
                "__COMMA__",
                "oder?",
                "oder __QUESTIONMARK__",
                "oder",
                "__QUESTIONMARK__"
        };
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                Settings.EMPTY,
                new BundlePlugin(Settings.EMPTY));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("symbolname");
        Tokenizer tokenizer = whitespaceMockTokenizer(new StringReader(source));
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }
}
