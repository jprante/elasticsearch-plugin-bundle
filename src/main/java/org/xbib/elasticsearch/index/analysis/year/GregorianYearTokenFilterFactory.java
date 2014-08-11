package org.xbib.elasticsearch.index.analysis.year;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettings;

public class GregorianYearTokenFilterFactory extends AbstractTokenFilterFactory {

    private final String defaultYear;

    @Inject
    public GregorianYearTokenFilterFactory(Index index,
                                           @IndexSettings Settings indexSettings,
                                           @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        defaultYear = settings.get("default_year", "0000");
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new GregorianYearTokenFilter(tokenStream, defaultYear);
    }

}
