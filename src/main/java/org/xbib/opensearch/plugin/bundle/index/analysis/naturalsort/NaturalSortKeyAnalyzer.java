package org.xbib.opensearch.plugin.bundle.index.analysis.naturalsort;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordTokenizer;

import java.text.Collator;

/**
 * Natural sort key analyzer.
 */
public class NaturalSortKeyAnalyzer extends Analyzer {

    private final NaturalSortKeyAttributeFactory factory;

    private final int bufferSize;

    public NaturalSortKeyAnalyzer(Collator collator, int bufferSize, int digits, int maxtoken) {
        this.factory = new NaturalSortKeyAttributeFactory(collator, digits, maxtoken);
        this.bufferSize = bufferSize;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        KeywordTokenizer tokenizer = new KeywordTokenizer(factory, bufferSize);
        return new TokenStreamComponents(tokenizer, tokenizer);
    }

}
