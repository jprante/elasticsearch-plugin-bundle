package org.xbib.opensearch.plugin.bundle.index.analysis.whitespace;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenizerFactory;

public class WhitespaceTokenizerFactory extends AbstractTokenizerFactory {

    static final String MAX_TOKEN_LENGTH = "max_token_length";

    private final Integer maxTokenLength;

    public WhitespaceTokenizerFactory(IndexSettings indexSettings,
                                      Environment environment,
                                      String name,
                                      Settings settings) {
        super(indexSettings, settings, name);
        maxTokenLength = settings.getAsInt(MAX_TOKEN_LENGTH, StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
    }

    @Override
    public Tokenizer create() {
        return new WhitespaceTokenizer(TokenStream.DEFAULT_TOKEN_ATTRIBUTE_FACTORY, maxTokenLength);
    }
}
