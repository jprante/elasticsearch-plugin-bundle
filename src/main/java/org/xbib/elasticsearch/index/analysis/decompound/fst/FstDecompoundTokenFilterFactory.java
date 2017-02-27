package org.xbib.elasticsearch.index.analysis.decompound.fst;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.xbib.elasticsearch.common.decompound.fst.FstDecompounder;

import java.io.IOException;

/**
 *
 */
public class FstDecompoundTokenFilterFactory extends AbstractTokenFilterFactory {

    private final FstDecompounder decompounder;

    public FstDecompoundTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                           Settings settings) {
        super(indexSettings, name, settings);
        this.decompounder = createDecompounder(settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new FstDecompoundTokenFilter(tokenStream, decompounder);
    }

    private FstDecompounder createDecompounder(Settings settings) {
        try {
            String words = settings.get("fst", "/decompound/fst/words.fst");
            String[] glueMorphs = settings.getAsArray("glue_morphs");
            return new FstDecompounder(getClass().getResourceAsStream(words), glueMorphs);
        } catch (IOException e) {
            throw new IllegalArgumentException("fst decompounder resources in settings not found: " + settings, e);
        }
    }
}
