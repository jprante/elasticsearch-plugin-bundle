package org.xbib.elasticsearch.index.analysis.standardnumber;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.analysis.UniqueTokenFilterFactory;

import java.io.Reader;
import java.util.Arrays;

public class StandardNumberAnalyzer extends Analyzer {

    private final TokenizerFactory tokenizerFactory;
    private final StandardNumberTokenFilterFactory stdnumTokenFilterFactory;
    private final UniqueTokenFilterFactory uniqueTokenFilterFactory;

    public StandardNumberAnalyzer(TokenizerFactory tokenizerFactory,
                                  StandardNumberTokenFilterFactory stdnumTokenFilterFactory,
                                  UniqueTokenFilterFactory uniqueTokenFilterFactory) {
        this.tokenizerFactory = tokenizerFactory;
        this.stdnumTokenFilterFactory = stdnumTokenFilterFactory;
        this.uniqueTokenFilterFactory = uniqueTokenFilterFactory;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer tokenizer = tokenizerFactory.create(reader);
        TokenStream tokenStream = tokenizer;
        for (TokenFilterFactory tokenFilter : Arrays.asList(stdnumTokenFilterFactory, uniqueTokenFilterFactory)) {
            tokenStream = tokenFilter.create(tokenStream);
        }
        return new TokenStreamComponents(tokenizer, tokenStream);
    }

}
