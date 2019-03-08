package org.xbib.elasticsearch.plugin.bundle.index.analysis.hyphen;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

/**
 * Hyphen tokenizer factory.
 */
public class HyphenTokenizerFactory extends AbstractTokenizerFactory {

    private final Integer maxTokenLength;

    public HyphenTokenizerFactory(IndexSettings indexSettings, Environment environment, String name,
                                  Settings settings) {
        super(indexSettings, name, settings);
        this.maxTokenLength = settings.getAsInt("max_token_length", StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
    }

    @Override
    public Tokenizer create() {
        return new HyphenTokenizer(maxTokenLength);
    }
}
