package org.xbib.elasticsearch.index.analysis.standardnumber;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.analysis.UniqueTokenFilterFactory;

import java.util.Arrays;

/**
 *
 */
public class StandardnumberAnalyzer extends Analyzer {

    private final TokenizerFactory tokenizerFactory;
    private final StandardnumberTokenFilterFactory stdnumTokenFilterFactory;
    private final UniqueTokenFilterFactory uniqueTokenFilterFactory;

    public StandardnumberAnalyzer(TokenizerFactory tokenizerFactory,
                                  StandardnumberTokenFilterFactory stdnumTokenFilterFactory,
                                  UniqueTokenFilterFactory uniqueTokenFilterFactory) {
        this.tokenizerFactory = tokenizerFactory;
        this.stdnumTokenFilterFactory = stdnumTokenFilterFactory;
        this.uniqueTokenFilterFactory = uniqueTokenFilterFactory;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = tokenizerFactory.create();
        TokenStream tokenStream = tokenizer;
        for (TokenFilterFactory tokenFilter : Arrays.asList(stdnumTokenFilterFactory, uniqueTokenFilterFactory)) {
            tokenStream = tokenFilter.create(tokenStream);
        }
        return new TokenStreamComponents(tokenizer, tokenStream);
    }
}
