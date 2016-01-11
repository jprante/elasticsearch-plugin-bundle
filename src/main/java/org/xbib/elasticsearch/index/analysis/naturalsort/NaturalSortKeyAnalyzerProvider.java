package org.xbib.elasticsearch.index.analysis.naturalsort;

import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettingsService;

import java.text.Collator;
import java.util.Locale;

public class NaturalSortKeyAnalyzerProvider extends AbstractIndexAnalyzerProvider<NaturalSortKeyAnalyzer> {

    private final Collator collator;

    private final int digits;

    private final int maxTokens;

    private final int bufferSize;

    @Inject
    public NaturalSortKeyAnalyzerProvider(Index index,
                                          IndexSettingsService indexSettingsService,
                                          @Assisted String name,
                                          @Assisted Settings settings) {
        super(index, indexSettingsService.indexSettings(), name, settings);
        this.collator = createCollator(settings);
        this.digits = settings.getAsInt("digits", 1);
        this.maxTokens = settings.getAsInt("maxTokens", 2);
        this.bufferSize = settings.getAsInt("bufferSize", KeywordTokenizer.DEFAULT_BUFFER_SIZE);
    }

    @Override
    public NaturalSortKeyAnalyzer get() {
        return new NaturalSortKeyAnalyzer(collator, bufferSize, digits, maxTokens);
    }

    protected static Collator createCollator(Settings settings) {
        return Collator.getInstance(new Locale(settings.get("locale", Locale.getDefault().toString())));
    }
}
