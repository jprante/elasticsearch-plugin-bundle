package org.xbib.opensearch.plugin.bundle.index.analysis.hyphen;

import org.apache.lucene.analysis.TokenStream;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenFilterFactory;

/**
 * Hyphen token filter factory.
 */
public class HyphenTokenFilterFactory extends AbstractTokenFilterFactory {

    private final char[] hyphenchars;

    private final boolean subwords;

    private final boolean respectKeywords;

    public HyphenTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                    Settings settings) {
        super(indexSettings, name, settings);
        this.hyphenchars = settings.get("hyphens") != null ? settings.get("hyphens").toCharArray() : HyphenTokenFilter.HYPHEN;
        this.subwords = settings.getAsBoolean("subwords", true);
        this.respectKeywords = settings.getAsBoolean("respect_keywords", false);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new HyphenTokenFilter(tokenStream, hyphenchars, subwords, respectKeywords);
    }
}
