package org.xbib.opensearch.plugin.bundle.index.analysis.year;

import org.apache.lucene.analysis.TokenStream;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenFilterFactory;

/**
 * Gregorian year token filter factory.
 */
public class GregorianYearTokenFilterFactory extends AbstractTokenFilterFactory {

    private final String defaultYear;

    public GregorianYearTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                           Settings settings) {
        super(indexSettings, name, settings);
        defaultYear = settings.get("default_year", "0000");
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new GregorianYearTokenFilter(tokenStream, defaultYear);
    }
}
