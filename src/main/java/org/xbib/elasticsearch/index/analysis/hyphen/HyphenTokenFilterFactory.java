package org.xbib.elasticsearch.index.analysis.hyphen;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettings;

public class HyphenTokenFilterFactory extends AbstractTokenFilterFactory {

    private final char[] hyphenchars;

    @Inject
    public HyphenTokenFilterFactory(Index index,
                                    @IndexSettings Settings indexSettings,
                                    @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        // by default, only '-' is used for token filtering
        this.hyphenchars = settings.get("hyphens") != null ? settings.get("hyphens").toCharArray() : null;
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return hyphenchars != null ? new HyphenTokenFilter(tokenStream, hyphenchars) : new HyphenTokenFilter(tokenStream);
    }
}
