package org.xbib.elasticsearch.index.analysis.concat;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.index.analysis.AnalysisService;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;

import java.io.IOException;

public class ConcatTokenFilterTests extends Assert {

    @Test
    public void testConcat() throws IOException {
        String source = "Das ist ein Schlüsselwort, ein Bindestrichwort";
        String[] expected = {
                "Das ist ein Schlüsselwort ein Bindestrichwort"
        };
        AnalysisService analysisService = MapperTestUtils.analysisService("org/xbib/elasticsearch/index/analysis/concat/concat_analysis.json");
        Analyzer analyzer = analysisService.analyzer("concat");
        assertNotNull(analyzer);
        assertSimpleTSOutput(analyzer.tokenStream("test-field", source), expected);
    }

    private void assertSimpleTSOutput(TokenStream stream, String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        Assert.assertNotNull(termAttr);
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
