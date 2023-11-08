package org.xbib.opensearch.plugin.bundle.index.analysis.hyphen;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenizerFactory;

/**
 * Hyphen tokenizer factory.
 */
public class HyphenTokenizerFactory extends AbstractTokenizerFactory {

    private final Integer maxTokenLength;

    public HyphenTokenizerFactory(IndexSettings indexSettings, Environment environment, String name,
                                  Settings settings) {
        super(indexSettings, settings, name);
        this.maxTokenLength = settings.getAsInt("max_token_length", StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
    }

    @Override
    public Tokenizer create() {
        return new HyphenTokenizer(maxTokenLength);
    }
}
