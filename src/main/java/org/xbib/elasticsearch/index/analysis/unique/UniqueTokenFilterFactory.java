package org.xbib.elasticsearch.index.analysis.unique;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

public class UniqueTokenFilterFactory extends AbstractTokenFilterFactory {

    private final boolean onlyOnSamePosition;

    public UniqueTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.onlyOnSamePosition = settings.getAsBoolean("only_on_same_position", false);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new UniqueTokenFilter(tokenStream, onlyOnSamePosition);
    }
}
