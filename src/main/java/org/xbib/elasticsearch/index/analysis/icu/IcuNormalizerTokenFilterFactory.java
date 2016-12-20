package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 * Uses the {@link IcuNormalizer2Filter} to normalize tokens.
 *
 * The <code>name</code> can be used to provide the type of normalization to perform,
 * the <code>mode</code> can be used to provide the mode of normalization.
 */
public class IcuNormalizerTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Normalizer2 normalizer;

    public IcuNormalizerTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        String normalizationName = settings.get("name", "nfkc_cf");
        Normalizer2.Mode normalizationMode;
        switch (settings.get("mode", "compose")) {
            case "compose_contiguous":
                normalizationMode = Normalizer2.Mode.COMPOSE_CONTIGUOUS;
                break;
            case "decompose":
                normalizationMode = Normalizer2.Mode.DECOMPOSE;
                break;
            case "fcd":
                normalizationMode = Normalizer2.Mode.FCD;
                break;
            default:
                normalizationMode = Normalizer2.Mode.COMPOSE;
                break;
        }
        this.normalizer = Normalizer2.getInstance(null, normalizationName, normalizationMode);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new IcuNormalizer2Filter(tokenStream, normalizer);
    }
}
