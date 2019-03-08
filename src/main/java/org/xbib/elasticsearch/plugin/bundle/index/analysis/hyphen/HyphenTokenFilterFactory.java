package org.xbib.elasticsearch.plugin.bundle.index.analysis.hyphen;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

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
