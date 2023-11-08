package org.xbib.opensearch.plugin.bundle.index.analysis.naturalsort;

import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractIndexAnalyzerProvider;

import java.text.Collator;
import java.util.Locale;

/**
 * Natural sort key analyzer provider.
 */
public class NaturalSortKeyAnalyzerProvider extends AbstractIndexAnalyzerProvider<NaturalSortKeyAnalyzer> {

    private final Collator collator;

    private final int digits;

    private final int maxTokens;

    private final int bufferSize;

    public NaturalSortKeyAnalyzerProvider(IndexSettings indexSettings,
                                          Environment environment,
                                          String name,
                                          Settings settings) {
        super(indexSettings, name, settings);
        this.collator = createCollator(settings);
        this.digits = settings.getAsInt("digits", 1);
        this.maxTokens = settings.getAsInt("maxTokens", 2);
        this.bufferSize = settings.getAsInt("bufferSize", KeywordTokenizer.DEFAULT_BUFFER_SIZE);
    }

    protected static Collator createCollator(Settings settings) {
        return Collator.getInstance(new Locale(settings.get("locale", Locale.getDefault().toString())));
    }

    @Override
    public NaturalSortKeyAnalyzer get() {
        return new NaturalSortKeyAnalyzer(collator, bufferSize, digits, maxTokens);
    }
}
