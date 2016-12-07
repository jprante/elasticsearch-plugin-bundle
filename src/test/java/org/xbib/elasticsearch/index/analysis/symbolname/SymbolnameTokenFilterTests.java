package org.xbib.elasticsearch.index.analysis.symbolname;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;

import java.io.IOException;
import java.io.StringReader;

/**
 *
 */
public class SymbolnameTokenFilterTests extends Assert {

    @Test
    public void testSimple() throws IOException {

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
        AnalysisService analysisService = MapperTestUtils.analysisService();
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("symbolname");
        Tokenizer tokenizer = analysisService.tokenizer("whitespace").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }


    @Test
    public void testPunctuation() throws IOException {

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
        AnalysisService analysisService = MapperTestUtils.analysisService();
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("symbolname");
        Tokenizer tokenizer = analysisService.tokenizer("whitespace").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    @Test
    public void testSingleSymbols() throws IOException {

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
        AnalysisService analysisService = MapperTestUtils.analysisService();
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("symbolname");
        Tokenizer tokenizer = analysisService.tokenizer("whitespace").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    private void assertSimpleTSOutput(TokenStream stream, String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        Assert.assertNotNull(termAttr);
        int i = 0;
        while (stream.incrementToken()) {
            //logger.info("'i={}'", termAttr.toString());
            assertTrue(i < expected.length);
            assertEquals("at position " + i, expected[i], termAttr.toString());
            i++;
        }
        assertEquals(i, expected.length);
        stream.close();
    }
}
