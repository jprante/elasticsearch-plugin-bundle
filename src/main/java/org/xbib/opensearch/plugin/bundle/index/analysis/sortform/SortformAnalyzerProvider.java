package org.xbib.opensearch.plugin.bundle.index.analysis.sortform;

import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.opensearch.index.analysis.TokenizerFactory;

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
