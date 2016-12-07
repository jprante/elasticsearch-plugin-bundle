package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Normalizer2;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractCharFilterFactory;
import org.elasticsearch.index.analysis.MultiTermAwareComponent;

import java.io.Reader;

/**
 *
 */
public class IcuNormalizerCharFilterFactory extends AbstractCharFilterFactory implements MultiTermAwareComponent {

    private final Normalizer2 normalizer;

    public IcuNormalizerCharFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name);
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
    public Reader create(Reader reader) {
        return new ICUNormalizer2CharFilter(reader, normalizer);
    }

    @Override
    public Object getMultiTermComponent() {
        return this;
    }
}
