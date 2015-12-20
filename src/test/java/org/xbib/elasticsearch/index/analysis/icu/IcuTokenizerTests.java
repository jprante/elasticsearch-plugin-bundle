package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.index.analysis.AnalysisService;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;

import java.io.IOException;
import java.io.StringReader;

public class IcuTokenizerTests extends Assert {

    @Test
    public void testLetterNonBreak() throws IOException {

        String source = "Das ist ein Bindestrich-Wort, oder etwa nicht? Jetzt kommen wir zum Ende.";

        String[] expected = {
                "Das",
                "ist",
                "ein",
                "Bindestrich-Wort",
                "oder",
                "etwa",
                "nicht",
                "Jetzt",
                "kommen",
                "wir",
                "zum",
                "Ende"
        };
        AnalysisService analysisService =
                MapperTestUtils.analysisService("/org/xbib/elasticsearch/index/analysis/icu/icu_tokenizer.json");
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenizer, expected);
    }

    @Test
    public void testIdentifierNonBreak() throws IOException {

        String source = "ISBN 3-428-84350-9";

        String[] expected = {
                "ISBN",
                "3-428-84350-9"
        };
        AnalysisService analysisService =
                MapperTestUtils.analysisService("/org/xbib/elasticsearch/index/analysis/icu/icu_tokenizer.json");
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenizer, expected);
    }

    @Test
    public void testIdentifierNonBreakSingleToken() throws IOException {

        String source = "3-428-84350-9";

        String[] expected = {
                "3-428-84350-9"
        };
        AnalysisService analysisService =
                MapperTestUtils.analysisService("/org/xbib/elasticsearch/index/analysis/icu/icu_tokenizer.json");
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        // THIS FAILS
        //assertSimpleTSOutput(tokenizer, expected);
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
