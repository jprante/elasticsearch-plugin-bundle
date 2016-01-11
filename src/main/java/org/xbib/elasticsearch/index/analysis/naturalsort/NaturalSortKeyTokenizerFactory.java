package org.xbib.elasticsearch.index.analysis.naturalsort;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettingsService;

import java.text.Collator;

public class NaturalSortKeyTokenizerFactory extends AbstractTokenizerFactory {

    private final NaturalSortKeyAttributeFactory factory;

    private final int bufferSize;

    @Inject
    public NaturalSortKeyTokenizerFactory(Index index,
                                          IndexSettingsService indexSettingsService,
                                          @Assisted String name,
                                          @Assisted Settings settings) {
        super(index, indexSettingsService.indexSettings(), name, settings);
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
