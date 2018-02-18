package org.xbib.elasticsearch.index.analysis.german;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 * German normalization filter factory.
 */
public class GermanNormalizationFilterFactory extends AbstractTokenFilterFactory {

    public GermanNormalizationFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                            Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new GermanNormalizationFilter(tokenStream);
    }
}
