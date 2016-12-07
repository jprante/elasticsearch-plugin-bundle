package org.xbib.elasticsearch.index.analysis.icu;

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
public class IcuMeasureFormatTests extends Assert {

    //  java.lang.UnsupportedOperationException
    // at com.ibm.icu.text.MeasureFormat.parseObject(MeasureFormat.java:346)

    public void testByteMeasureFormat() throws IOException {

        String source = "123.45kb";

        String[] expected = {
        };
        AnalysisService analysisService =
                MapperTestUtils.analysisService("org/xbib/elasticsearch/index/analysis/icu/icu_measureformat.json");
        Tokenizer tokenizer = analysisService.tokenizer("measure").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("measure");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
    }

    private void assertSimpleTSOutput(TokenStream stream, String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        assertNotNull(termAttr);
        int i = 0;
        while (stream.incrementToken()) {
            System.err.println(termAttr.toString());
            //assertTrue(i < expected.length);
            //assertEquals(expected[i], termAttr.toString());
            i++;
        }
        //assertEquals(i, expected.length);
        stream.close();
    }

}
