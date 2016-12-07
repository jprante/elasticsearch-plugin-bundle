package org.xbib.elasticsearch.index.analysis.naturalsort;

import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

import java.text.Collator;
import java.util.Locale;

/**
 *
 */
public class NaturalSortKeyAnalyzerProvider extends AbstractIndexAnalyzerProvider<NaturalSortKeyAnalyzer> {

    private final Collator collator;

    private final int digits;

    private final int maxTokens;

    private final int bufferSize;

    public NaturalSortKeyAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name,
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
