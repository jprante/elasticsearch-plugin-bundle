package org.xbib.opensearch.plugin.bundle.index.analysis.sortform;

import org.apache.lucene.analysis.TokenStream;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenFilterFactory;

/**
 * Sort form token filter factory.
 */
public class SortformTokenFilterFactory extends AbstractTokenFilterFactory {

    public SortformTokenFilterFactory(IndexSettings indexSettings,
                                      Environment environment,
                                      String name,
                                      Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new SortformTokenFilter(tokenStream);
    }

}
