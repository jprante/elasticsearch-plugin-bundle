package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Transliterator;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.ICUTransformFilter;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettings;

/**
 */
public class IcuTransformTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Transliterator transliterator;

    @Inject
    public IcuTransformTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        String id = settings.get("id", "Null");
        String s = settings.get("dir", "forward");
        int dir = "forward".equals(s) ? Transliterator.FORWARD : Transliterator.REVERSE;
        this.transliterator = Transliterator.getInstance(id, dir);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new ICUTransformFilter(tokenStream, transliterator);
    }
}