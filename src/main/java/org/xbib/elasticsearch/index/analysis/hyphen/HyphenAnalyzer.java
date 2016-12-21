package org.xbib.elasticsearch.index.analysis.hyphen;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

/**
 *
 */
public class HyphenAnalyzer extends Analyzer {

    private final HyphenTokenizerFactory tokenizerFactory;

    public HyphenAnalyzer(HyphenTokenizerFactory tokenizerFactory) {
        this.tokenizerFactory = tokenizerFactory;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = tokenizerFactory.create();
        return new TokenStreamComponents(tokenizer, tokenizer);
    }

}
