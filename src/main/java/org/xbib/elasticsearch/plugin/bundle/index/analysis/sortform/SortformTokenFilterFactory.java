package org.xbib.elasticsearch.plugin.bundle.index.analysis.sortform;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

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
