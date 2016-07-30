package org.xbib.elasticsearch.index.analysis.concat;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettingsService;

import java.util.Map;

public class PairTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Map<String, String> pairs;

    @Inject
    public PairTokenFilterFactory(Index index,
                                  IndexSettingsService indexSettingsService,
                                  @Assisted String name,
                                  @Assisted Settings settings) {
        super(index, indexSettingsService.indexSettings(), name, settings);
        this.pairs = settings.getAsSettings("pairs").getAsMap();

    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new PairTokenFilter(tokenStream, pairs);

    }
}
