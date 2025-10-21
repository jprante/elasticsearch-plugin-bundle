package org.xbib.opensearch.plugin.bundle.index.analysis.german;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenFilterFactory;

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
