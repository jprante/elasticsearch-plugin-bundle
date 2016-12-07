package org.xbib.elasticsearch.index.analysis.concat;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

import java.util.Map;

/**
 *
 */
public class PairTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Map<String, String> pairs;

    public PairTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.pairs = settings.getAsSettings("pairs").getAsMap();

    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new PairTokenFilter(tokenStream, pairs);

    }
}
