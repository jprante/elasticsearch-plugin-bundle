package org.xbib.opensearch.plugin.bundle.index.analysis.standardnumber;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.opensearch.index.analysis.TokenFilterFactory;
import org.opensearch.index.analysis.TokenizerFactory;

import java.util.Collections;

/**
 * Standard number analyzer.
 */
public class StandardnumberAnalyzer extends Analyzer {

    private final TokenizerFactory tokenizerFactory;
    private final StandardnumberTokenFilterFactory stdnumTokenFilterFactory;

    public StandardnumberAnalyzer(TokenizerFactory tokenizerFactory,
                                  StandardnumberTokenFilterFactory stdnumTokenFilterFactory) {
        this.tokenizerFactory = tokenizerFactory;
        this.stdnumTokenFilterFactory = stdnumTokenFilterFactory;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = tokenizerFactory.create();
        TokenStream tokenStream = tokenizer;
        for (TokenFilterFactory tokenFilter : Collections.singletonList(stdnumTokenFilterFactory)) {
            tokenStream = tokenFilter.create(tokenStream);
        }
        return new TokenStreamComponents(tokenizer, tokenStream);
    }
}
