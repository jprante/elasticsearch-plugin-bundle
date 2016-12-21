package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.xbib.elasticsearch.MapperTestUtils.tokenFilterFactory;
import static org.xbib.elasticsearch.MapperTestUtils.tokenizerFactory;

/**
 *
 */
public class IcuMeasureFormatTests extends Assert {

    @Ignore
    @Test
    public void testByteMeasureFormat() throws IOException {
        // Sad, doesn't work.
        //  java.lang.UnsupportedOperationException
        // at com.ibm.icu.text.MeasureFormat.parseObject(MeasureFormat.java:346)
        //  at org.xbib.elasticsearch.index.analysis.icu.IcuMeasureFormatTokenFilter.incrementToken(IcuMeasureFormatTokenFilter.java:35)

        String source = "123.45kb";

        String[] expected = {
        };
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_measureformat.json";
        Tokenizer tokenizer = tokenizerFactory(resource, "measure").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = tokenFilterFactory(resource, "measure");
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
