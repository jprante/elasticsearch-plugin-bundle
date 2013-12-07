package org.xbib.elasticsearch.index.analysis.german;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettings;

public class GermanNormalizationFilterFactory extends AbstractTokenFilterFactory {

    @Inject
    public GermanNormalizationFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new GermanNormalizationFilter(tokenStream);
    }
}
