package org.xbib.elasticsearch.plugin.bundle.index.analysis.hyphen;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import java.util.Collections;

/**
 * Hyphen analyzer.
 */
public class HyphenAnalyzer extends Analyzer {

    private final HyphenTokenizerFactory tokenizerFactory;

    private final HyphenTokenFilterFactory filterFactory;

    public HyphenAnalyzer(HyphenTokenizerFactory tokenizerFactory,
                          HyphenTokenFilterFactory filterFactory) {
        this.tokenizerFactory = tokenizerFactory;
        this.filterFactory = filterFactory;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = tokenizerFactory.create();
        TokenStream tokenStream = tokenizer;
        for (TokenFilterFactory tokenFilter : Collections.singletonList(filterFactory)) {
            tokenStream = tokenFilter.create(tokenStream);
        }
        return new TokenStreamComponents(tokenizer, tokenStream);
    }
}
