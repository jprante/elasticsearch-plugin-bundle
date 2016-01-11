package org.xbib.elasticsearch.index.analysis.naturalsort;

import org.elasticsearch.index.analysis.AnalysisModule;

public class NaturalSortAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
        analyzersBindings.processAnalyzer("naturalsort", NaturalSortKeyAnalyzerProvider.class);
    }

}


