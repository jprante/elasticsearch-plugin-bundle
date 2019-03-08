package org.xbib.elasticsearch.plugin.bundle.index.analysis.decompound.fst;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.xbib.elasticsearch.plugin.bundle.common.decompound.fst.FstDecompounder;

import java.io.IOException;
import java.util.List;

/**
 * Finite state decompound token filter factory.
 */
public class FstDecompoundTokenFilterFactory extends AbstractTokenFilterFactory {

    private final FstDecompounder decompounder;

    private final Boolean respectKeywords;

    private final Boolean subwordsonly;

    public FstDecompoundTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                           Settings settings) {
        super(indexSettings, name, settings);
        this.decompounder = createDecompounder(settings);
        this.respectKeywords = settings.getAsBoolean("respect_keywords", false);
        this.subwordsonly = settings.getAsBoolean("subwords_only", false);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new FstDecompoundTokenFilter(tokenStream, decompounder, respectKeywords, subwordsonly);
    }

    private FstDecompounder createDecompounder(Settings settings) {
        try {
            String words = settings.get("fst", "words.fst");
            List<String> glueMorphs = settings.getAsList("glue_morphs");
            return new FstDecompounder(getClass().getResourceAsStream(words), glueMorphs);
        } catch (IOException e) {
            throw new IllegalArgumentException("fst decompounder resources in settings not found: " + settings, e);
        }
    }
}
