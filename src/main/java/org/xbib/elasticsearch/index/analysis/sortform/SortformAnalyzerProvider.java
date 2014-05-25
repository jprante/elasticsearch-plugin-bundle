package org.xbib.elasticsearch.index.analysis.sortform;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.KeywordTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettings;

public class SortformAnalyzerProvider extends AbstractIndexAnalyzerProvider<SortformAnalyzer> {

    private final SortformAnalyzer analyzer;

    @Inject
    public SortformAnalyzerProvider(Index index, @IndexSettings Settings indexSettings,
                                    KeywordTokenizerFactory tokenizerFactory,
                                    SortformTokenFilterFactory sortformTokenFilterFactory,
                                    @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        this.analyzer = new SortformAnalyzer(tokenizerFactory, sortformTokenFilterFactory);
    }

    @Override
    public SortformAnalyzer get() {
        return this.analyzer;
    }
}
