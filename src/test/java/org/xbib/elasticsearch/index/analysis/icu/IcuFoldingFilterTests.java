package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.xbib.elasticsearch.MapperTestUtils.charFilterFactory;

/**
 *
 */
public class IcuFoldingFilterTests extends Assert {

    @Test
    public void testFoldingCharFilter() throws IOException {
        String source = "Jörg Prante";
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_folding.json";
        Reader charFilter = charFilterFactory(resource, "my_icu_folder").create(new StringReader(source));
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = charFilter.read()) != -1) {
            sb.append((char)ch);
        }
        assertEquals("jorg prante", sb.toString());
    }

    @Test
    public void testFoldingTokenFilter() throws IOException {
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_folding.json";
        Analyzer analyzer = MapperTestUtils.analyzer(resource,"my_icu_analyzer");
        TokenStream ts = analyzer.tokenStream("test", "Jörg Prante");
        String[] expected = {
                "jorg",
                "prante"
        };
        assertSimpleTSOutput(ts, expected);
    }

    @Test
    public void testFoldingTokenFilterWithExceptions() throws IOException {
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_folding.json";
        Analyzer analyzer = MapperTestUtils.analyzer(resource, "my_icu_analyzer_with_exceptions");
        TokenStream ts = analyzer.tokenStream("test", "Jörg Prante");
        String[] expected = {
                "jörg",
                "prante"
        };
        assertSimpleTSOutput(ts, expected);
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
