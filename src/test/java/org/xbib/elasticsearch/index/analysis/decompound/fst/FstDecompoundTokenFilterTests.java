package org.xbib.elasticsearch.index.analysis.decompound.fst;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

public class FstDecompoundTokenFilterTests extends Assert {

    @Test
    public void test() throws IOException {

        String source = "Die Jahresfeier der Rechtsanwaltskanzleien auf dem Donaudampfschiff hat viel Ökosteuer gekostet";

        String[] expected = {
            "Die",
            "Jahresfeier",
            "jahres",
            "feier",
            "der",
            "Rechtsanwaltskanzleien",
            "rechts",
            "anwalts",
            "kanzleien",
            "auf",
            "dem",
            "Donaudampfschiff",
            "donau",
            "dampf",
            "schiff",
            "hat",
            "hat",
            "viel",
            "viel",
            "Ökosteuer",
            "ökos",
            "teuer",
            "gekostet",
            "gekostet"
        };
        AnalysisService analysisService = MapperTestUtils.analysisService("org/xbib/elasticsearch/index/analysis/decompound/fst/decompound_analysis.json");
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("decomp");
        assertNotNull(tokenFilter);
        Tokenizer tokenizer = analysisService.tokenizer("standard").create();
        tokenizer.setReader(new StringReader(source));
        assertNotNull(tokenizer);
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    private void assertSimpleTSOutput(TokenStream stream, String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        Assert.assertNotNull(termAttr);
        int i = 0;
        while (stream.incrementToken()) {
            assertTrue(i < expected.length);
            assertEquals(expected[i], termAttr.toString());
            //logger.info("{}", termAttr.toString());
            i++;
        }
        assertEquals(i, expected.length);
        stream.close();
    }

    private final static Logger logger = LogManager.getLogger("test");
}
