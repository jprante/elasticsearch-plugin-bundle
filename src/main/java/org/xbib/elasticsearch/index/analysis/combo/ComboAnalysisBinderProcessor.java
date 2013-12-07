package org.xbib.elasticsearch.index.analysis.combo;

import org.elasticsearch.index.analysis.AnalysisModule;

public class ComboAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
        analyzersBindings.processAnalyzer(ComboAnalyzerWrapper.NAME, ComboAnalyzerProvider.class);
    }

}
