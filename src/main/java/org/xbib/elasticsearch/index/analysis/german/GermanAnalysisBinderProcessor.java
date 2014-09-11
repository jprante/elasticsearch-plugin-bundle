package org.xbib.elasticsearch.index.analysis.german;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.xbib.elasticsearch.index.analysis.baseform.BaseformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.concat.ConcatTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.decompound.DecompoundTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.hyphen.HyphenTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.sortform.SortformAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.sortform.SortformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.worddelimiter.WordDelimiterFilter2Factory;
import org.xbib.elasticsearch.index.analysis.worddelimiter.WordDelimiterFilterFactory;
import org.xbib.elasticsearch.index.analysis.year.GregorianYearTokenFilterFactory;

public class GermanAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
        tokenFiltersBindings.processTokenFilter("german_normalize", GermanNormalizationFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("decompound", DecompoundTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("baseform", BaseformTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("worddelimiter", WordDelimiterFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("worddelimiter2", WordDelimiterFilter2Factory.class);
        tokenFiltersBindings.processTokenFilter("sortform", SortformTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("concat", ConcatTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("year", GregorianYearTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("hyphen", HyphenTokenFilterFactory.class);
    }

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
        analyzersBindings.processAnalyzer("sort_form", SortformAnalyzerProvider.class);
    }
}
