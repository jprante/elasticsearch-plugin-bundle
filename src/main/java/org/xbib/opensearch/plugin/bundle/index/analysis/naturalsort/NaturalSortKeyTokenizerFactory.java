package org.xbib.opensearch.plugin.bundle.index.analysis.naturalsort;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenizerFactory;

import java.text.Collator;

/**
 * Natural sort key tokenizer factory.
 */
public class NaturalSortKeyTokenizerFactory extends AbstractTokenizerFactory {

    private final NaturalSortKeyAttributeFactory factory;

    private final int bufferSize;

    public NaturalSortKeyTokenizerFactory(IndexSettings indexSettings, Environment environment, String name,
                                          Settings settings) {
        super(indexSettings, settings, name);
        Collator collator = NaturalSortKeyAnalyzerProvider.createCollator(settings);
        int digits = settings.getAsInt("digits", 1);
        int maxTokens = settings.getAsInt("maxTokens", 2);
        this.factory = new NaturalSortKeyAttributeFactory(collator, digits, maxTokens);
        this.bufferSize = settings.getAsInt("bufferSize", KeywordTokenizer.DEFAULT_BUFFER_SIZE);
    }

    @Override
    public Tokenizer create() {
        return new KeywordTokenizer(factory, bufferSize);
    }
}
