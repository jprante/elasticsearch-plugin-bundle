package org.xbib.opensearch.plugin.bundle.index.analysis.hyphen;

import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractIndexAnalyzerProvider;

/**
 * A Hyphen analyzer provider.
 */
public class HyphenAnalyzerProvider extends AbstractIndexAnalyzerProvider<HyphenAnalyzer> {

    private final HyphenTokenizerFactory tokenizerFactory;

    private final HyphenTokenFilterFactory tokenFilterFactory;

    public HyphenAnalyzerProvider(IndexSettings indexSettings,
                                  Environment environment,
                                  String name,
                                  Settings settings) {
        super(indexSettings, name, settings);
        this.tokenizerFactory = new HyphenTokenizerFactory(indexSettings, environment, name, settings);
        this.tokenFilterFactory = new HyphenTokenFilterFactory(indexSettings, environment, name, settings);
    }

    @Override
    public HyphenAnalyzer get() {
        return new HyphenAnalyzer(tokenizerFactory, tokenFilterFactory);
    }
}
