package org.xbib.elasticsearch.index.analysis.hyphen;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

/**
 *
 */
public class HyphenTokenizerFactory extends AbstractTokenizerFactory {

    private final Integer tokenLength;

    public HyphenTokenizerFactory(IndexSettings indexSettings, Environment environment, String name,
                                  Settings settings) {
        super(indexSettings, name, settings);
        this.tokenLength = settings.getAsInt("max_token_length", null);
    }

    @Override
    public Tokenizer create() {
        HyphenTokenizer tokenizer = new HyphenTokenizer();
        if (tokenLength != null) {
            tokenizer.setMaxTokenLength(tokenLength);
        }
        return tokenizer;
    }
}
