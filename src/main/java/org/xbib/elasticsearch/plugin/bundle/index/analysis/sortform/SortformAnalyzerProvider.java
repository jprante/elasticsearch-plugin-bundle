package org.xbib.elasticsearch.plugin.bundle.index.analysis.sortform;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;

/**
 * Sort form analyzer provider.
 */
public class SortformAnalyzerProvider extends AbstractIndexAnalyzerProvider<SortFormAnalyzer> {

    private final TokenizerFactory tokenizerFactory;

    private final SortformTokenFilterFactory filterFactory;

    public SortformAnalyzerProvider(IndexSettings indexSettings,
                                    Environment environment,
                                    String name,
                                    Settings settings) {
        super(indexSettings, name, settings);
        this.tokenizerFactory =
                new SortformTokenizerFactory(indexSettings, name, settings);
        this.filterFactory =
                new SortformTokenFilterFactory(indexSettings, environment, name, settings);
    }

    @Override
    public SortFormAnalyzer get() {
        return new SortFormAnalyzer(tokenizerFactory, filterFactory);
    }
}
