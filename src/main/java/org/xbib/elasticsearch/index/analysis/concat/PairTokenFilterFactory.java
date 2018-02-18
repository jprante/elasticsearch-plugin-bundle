package org.xbib.elasticsearch.index.analysis.concat;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pair token filter factory.
 */
public class PairTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Map<String, String> pairs;

    public PairTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.pairs = new LinkedHashMap<>();
        Settings pairsSettings = settings.getAsSettings("pairs");
        for (String key: pairsSettings.keySet()) {
            pairs.put(key, pairsSettings.get(key));
        }
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new PairTokenFilter(tokenStream, pairs);

    }
}
