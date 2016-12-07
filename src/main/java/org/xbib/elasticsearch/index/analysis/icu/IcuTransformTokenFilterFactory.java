package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Transliterator;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.ICUTransformFilter;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 *
 */
public class IcuTransformTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Transliterator transliterator;

    public IcuTransformTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                          Settings settings) {
        super(indexSettings, name, settings);
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
