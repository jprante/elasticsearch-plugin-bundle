package org.xbib.opensearch.plugin.bundle.index.analysis.concat;

import org.apache.lucene.analysis.TokenStream;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenFilterFactory;

/**
 * Concat token filter factory.
 */
public class ConcatTokenFilterFactory extends AbstractTokenFilterFactory {

    public ConcatTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new ConcatTokenFilter(tokenStream);

    }
}