package org.xbib.opensearch.plugin.bundle.index.analysis.sortform;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.opensearch.index.analysis.TokenFilterFactory;
import org.opensearch.index.analysis.TokenizerFactory;

import java.util.Collections;

public class SortFormAnalyzer extends Analyzer {

    private final TokenizerFactory tokenizerFactory;

    private final SortformTokenFilterFactory tokenFilterFactory;

    public SortFormAnalyzer(TokenizerFactory tokenizerFactory,
                            SortformTokenFilterFactory tokenFilterFactory) {
        this.tokenizerFactory = tokenizerFactory;
        this.tokenFilterFactory = tokenFilterFactory;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = tokenizerFactory.create();
        TokenStream tokenStream = tokenizer;
        for (TokenFilterFactory tokenFilter : Collections.singletonList(tokenFilterFactory)) {
            tokenStream = tokenFilter.create(tokenStream);
        }
        return new TokenStreamComponents(tokenizer, tokenStream);
    }
}
