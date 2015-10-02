package org.xbib.elasticsearch.index.analysis.combo;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalysisService;
import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.AnalyzerTestUtils;

import static org.junit.Assert.assertTrue;

public class SimpleComboAnalysisTests {

    @Test
    public void testDefaultComboAnalysis() {
        AnalysisService analysisService = AnalyzerTestUtils.createAnalysisService();
        Analyzer analyzer = analysisService.analyzer("combo").analyzer();
        assertTrue(analyzer instanceof ComboAnalyzerWrapper);

    }
}
