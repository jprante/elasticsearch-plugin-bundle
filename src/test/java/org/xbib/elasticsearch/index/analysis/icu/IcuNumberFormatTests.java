package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.xbib.elasticsearch.MapperTestUtils.tokenFilterFactory;
import static org.xbib.elasticsearch.MapperTestUtils.tokenizerFactory;

/**
 *
 */
public class IcuNumberFormatTests {

    @Test
    public void testGermanNumberFormat() throws IOException {

        String source = "Muss Rudi Völler fünftausend oder 10000 EUR Strafe zahlen?";

        String[] expected = {
                "Muss",
                "Rudi",
                "Völler",
                "fünftausend",
                "oder",
                "zehntausend",
                "EUR",
                "Strafe",
                "zahlen"
        };
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_numberformat.json";
        Tokenizer tokenizer = tokenizerFactory(resource, "my_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = tokenFilterFactory(resource, "spellout_de");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
    }

    @Test
    public void testAmericanEnglish() throws IOException {

        String source = "You will never get 100,000 US dollars of salary per year.";

        String[] expected = {
                "You",
                "will",
                "never",
                "get",
                "onehundredthousand",
                "US",
                "dollars",
                "of",
                "salary",
                "per",
                "year"
        };
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_numberformat.json";
        Tokenizer tokenizer = tokenizerFactory(resource, "my_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = tokenFilterFactory(resource,"spellout_en");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
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
