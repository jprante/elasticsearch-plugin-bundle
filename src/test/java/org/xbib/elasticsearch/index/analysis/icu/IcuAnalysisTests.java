package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public class IcuAnalysisTests {

    @Test
    public void testDefaultsIcuAnalysis() {

        AnalysisService analysisService = MapperTestUtils.analysisService();
        TokenizerFactory tokenizerFactory = analysisService.tokenizer("icu_tokenizer");
        assertThat(tokenizerFactory, instanceOf(IcuTokenizerFactory.class));

        TokenFilterFactory filterFactory = analysisService.tokenFilter("icu_normalizer");
        assertThat(filterFactory, instanceOf(IcuNormalizerTokenFilterFactory.class));

        filterFactory = analysisService.tokenFilter("icu_folding");
        assertThat(filterFactory, instanceOf(IcuFoldingTokenFilterFactory.class));

        filterFactory = analysisService.tokenFilter("icu_transform");
        assertThat(filterFactory, instanceOf(IcuTransformTokenFilterFactory.class));

        Analyzer analyzer = analysisService.analyzer("icu_collation");
        assertThat(analyzer, instanceOf(NamedAnalyzer.class));

    }
}
