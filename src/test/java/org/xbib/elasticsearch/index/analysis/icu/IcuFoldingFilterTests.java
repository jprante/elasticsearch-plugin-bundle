package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.*;
import static org.xbib.elasticsearch.MapperTestUtils.analyzer;
import static org.xbib.elasticsearch.MapperTestUtils.charFilterFactory;

/**
 *
 */
public class IcuFoldingFilterTests {

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
    public void testFoldingAnalyzer() throws IOException {
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_folding.json";
        Analyzer analyzer = analyzer(resource,"my_icu_analyzer");
        TokenStream ts = analyzer.tokenStream("test", "Jörg Prante");
        String[] expected = {"jorg", "prante"};
        assertSimpleTSOutput(ts, expected);
        assertSimpleTSOutput(analyzer.tokenStream("test", "This is a test"), new String[]{ "this", "is", "a", "test" });
        assertSimpleTSOutput(analyzer.tokenStream("test", "Ruß"), new String[]{ "russ" });
        assertSimpleTSOutput(analyzer.tokenStream("test", "ΜΆΪΟΣ"), new String[]{  "μαιοσ" });
        assertSimpleTSOutput(analyzer.tokenStream("test", "Μάϊος"), new String[] { "μαιοσ" });
        assertSimpleTSOutput(analyzer.tokenStream("test", "𐐖"), new String[] { "𐐾" });
        assertSimpleTSOutput(analyzer.tokenStream("test", "ﴳﴺﰧ"), new String[] { "طمطمطم" });
        assertSimpleTSOutput(analyzer.tokenStream("test", "क्‍ष"), new String[] { "कष" });
        assertSimpleTSOutput(analyzer.tokenStream("test", "résumé"), new String[] { "resume" });
        assertSimpleTSOutput(analyzer.tokenStream("test", "re\u0301sume\u0301"), new String[] { "resume" });
        assertSimpleTSOutput(analyzer.tokenStream("test", "৭০৬"), new String[] { "706" });
        assertSimpleTSOutput(analyzer.tokenStream("test", "đis is cræzy"), new String[] { "dis", "is", "craezy" });
        assertSimpleTSOutput(analyzer.tokenStream("test",  "ELİF"), new String[] { "elif" });
        assertSimpleTSOutput(analyzer.tokenStream("test", "eli\u0307f"), new String[] { "elif" });
    }

    @Test
    public void testFoldingAnalyzerrWithExceptions() throws IOException {
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_folding.json";
        Analyzer analyzer = analyzer(resource, "my_icu_analyzer_with_exceptions");
        TokenStream ts = analyzer.tokenStream("test", "Jörg Prante");
        String[] expected = { "jörg", "prante" };
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
