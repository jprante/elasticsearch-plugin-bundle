package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettings;


/**
 * Uses the {@link org.apache.lucene.analysis.icu.ICUNormalizer2Filter} to normalize tokens.
 * <p/>
 * <p>The <tt>name</tt> can be used to provide the type of normalization to perform.
 */
public class IcuNormalizerTokenFilterFactory extends AbstractTokenFilterFactory {

    private final String name;

    @Inject
    public IcuNormalizerTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        this.name = settings.get("name", "nfkc_cf");
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new org.apache.lucene.analysis.icu.ICUNormalizer2Filter(tokenStream, Normalizer2.getInstance(null, name, Normalizer2.Mode.COMPOSE));
    }
}