
package org.xbib.elasticsearch.index.analysis.icu;

import org.elasticsearch.index.analysis.AnalysisModule;

/**
 */
public class IcuAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processTokenizers(TokenizersBindings tokenizersBindings) {
        tokenizersBindings.processTokenizer("icu_tokenizer", IcuTokenizerFactory.class);
    }

    @Override
    public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
        tokenFiltersBindings.processTokenFilter("icu_normalizer", IcuNormalizerTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("icu_folding", IcuFoldingTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("icu_collation", IcuCollationTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("icu_transform", IcuTransformTokenFilterFactory.class);
    }

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
        analyzersBindings.processAnalyzer("icu_collation", IcuCollationAnalyzerProvider.class);
    }
}
