package org.xbib.elasticsearch.index.analysis.baseform;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.TokenFilterFactory;

import org.junit.Assert;
import org.junit.Test;

import org.xbib.elasticsearch.index.analysis.AnalyzerTestUtils;

import java.io.IOException;
import java.io.StringReader;

public class BaseformTokenFilterTests extends Assert {

    @Test
    public void testOne() throws IOException {

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
        AnalysisService analysisService = AnalyzerTestUtils.createAnalysisService();
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("baseform");
        Tokenizer tokenizer = analysisService.tokenizer("standard").create(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    @Test
    public void testTwo() throws IOException {

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
        AnalysisService analysisService = AnalyzerTestUtils.createAnalysisService();
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("baseform");
        Tokenizer tokenizer = analysisService.tokenizer("standard").create(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }


    @Test
    public void testThree() throws IOException {

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
        AnalysisService analysisService = AnalyzerTestUtils.createAnalysisService();
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("baseform");
        Tokenizer tokenizer = analysisService.tokenizer("standard").create(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    private void assertSimpleTSOutput(TokenStream stream, String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        assertNotNull(termAttr);
        int i = 0;
        while (stream.incrementToken()) {
            assertTrue(i < expected.length);
            assertEquals(expected[i], termAttr.toString());
            i++;
        }
        assertEquals(i, expected.length);
        stream.close();
    }
}
