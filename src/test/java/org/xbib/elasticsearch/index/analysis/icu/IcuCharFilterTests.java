package org.xbib.elasticsearch.index.analysis.icu;

import org.elasticsearch.index.analysis.AnalysisService;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.AnalyzerTestUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class IcuCharFilterTests extends Assert {

    @Test
    public void testFoldingCharfilter() throws IOException {
        String source = "Jörg Prante";
        AnalysisService analysisService = AnalyzerTestUtils.createAnalysisService("/org/xbib/elasticsearch/index/analysis/icu/icu_folding.json");
        Reader charFilter = analysisService.charFilter("my_icu_folder").create(new StringReader(source));
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = charFilter.read()) != -1) {
            sb.append((char)ch);
        }
        assertEquals("jorg prante", sb.toString());
    }
}
