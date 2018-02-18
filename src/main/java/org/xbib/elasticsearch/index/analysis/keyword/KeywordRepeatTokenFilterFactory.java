package org.xbib.elasticsearch.index.analysis.keyword;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.KeywordRepeatFilter;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 * Factory for keyword repeat token filter.
 */
public class KeywordRepeatTokenFilterFactory extends AbstractTokenFilterFactory {

    public KeywordRepeatTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new KeywordRepeatFilter(tokenStream);
    }
}
