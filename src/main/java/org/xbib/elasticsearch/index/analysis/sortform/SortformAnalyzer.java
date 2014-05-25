
package org.xbib.elasticsearch.index.analysis.sortform;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;

import java.io.Reader;
import java.util.Arrays;

public class SortformAnalyzer extends Analyzer {

    private final TokenizerFactory tokenizerFactory;
    private final SortformTokenFilterFactory sortformTokenFilterFactory;

    public SortformAnalyzer(TokenizerFactory tokenizerFactory,
                            SortformTokenFilterFactory sortformTokenFilterFactory) {
        this.tokenizerFactory = tokenizerFactory;
        this.sortformTokenFilterFactory = sortformTokenFilterFactory;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer tokenizer = tokenizerFactory.create(reader);
        TokenStream tokenStream = tokenizer;
        for (TokenFilterFactory tokenFilter : Arrays.asList(sortformTokenFilterFactory)) {
            tokenStream = tokenFilter.create(tokenStream);
        }
        return new TokenStreamComponents(tokenizer, tokenStream);
    }

}
