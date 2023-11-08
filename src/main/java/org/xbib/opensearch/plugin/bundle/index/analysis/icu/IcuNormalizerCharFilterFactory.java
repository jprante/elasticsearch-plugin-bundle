package org.xbib.opensearch.plugin.bundle.index.analysis.icu;

import com.ibm.icu.text.FilteredNormalizer2;
import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.UnicodeSet;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractCharFilterFactory;
import org.opensearch.index.analysis.NormalizingCharFilterFactory;

import java.io.InputStream;
import java.io.Reader;

/**
 * ICU normalizer char filter factory.
 */
public class IcuNormalizerCharFilterFactory
        extends AbstractCharFilterFactory  implements NormalizingCharFilterFactory {

    private final Normalizer2 normalizer;

    public IcuNormalizerCharFilterFactory(IndexSettings indexSettings,
                                          Environment environment,
                                          String name,
                                          Settings settings) {
        super(indexSettings, name);
        Normalizer2 base = Normalizer2.getInstance(getNormalizationResource(settings),
                getNormalizationName(settings), getNormalizationMode(settings));
        String unicodeSetFilter = settings.get("unicode_set_filter");
        this.normalizer = unicodeSetFilter != null ?
                new FilteredNormalizer2(base, new UnicodeSet(unicodeSetFilter).freeze()) : base;
    }

    @Override
    public Reader create(Reader reader) {
        return new IcuNormalizerCharFilter(reader, normalizer);
    }

    protected InputStream getNormalizationResource(Settings settings) {
        String string = getNormalizationName(settings);
        return getClass().getResourceAsStream(string + ".nrm");
    }

    protected String getNormalizationName(Settings settings) {
        return settings.get("normalization_name", "nfkc_cf");
    }

    protected Normalizer2.Mode getNormalizationMode(Settings settings) {
        Normalizer2.Mode normalizationMode;
        switch (settings.get("normalization_mode", "compose")) {
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
        return normalizationMode;
    }
}
