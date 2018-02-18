package org.xbib.elasticsearch.index.analysis.symbolname;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.ESTokenStreamTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.io.StringReader;

/**
 * Symbol name tokenfilter tests.
 */
public class SymbolnameTokenFilterTests extends ESTokenStreamTestCase {

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
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                Settings.EMPTY,
                new BundlePlugin(Settings.EMPTY));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("symbolname");
        Tokenizer tokenizer = analysis.tokenizer.get("whitespace").create();
        tokenizer.setReader(new StringReader(source));
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
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                Settings.EMPTY,
                new BundlePlugin(Settings.EMPTY));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("symbolname");
        Tokenizer tokenizer = analysis.tokenizer.get("whitespace").create();
        tokenizer.setReader(new StringReader(source));
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
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                Settings.EMPTY,
                new BundlePlugin(Settings.EMPTY));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("symbolname");
        Tokenizer tokenizer = analysis.tokenizer.get("whitespace").create();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }
}
