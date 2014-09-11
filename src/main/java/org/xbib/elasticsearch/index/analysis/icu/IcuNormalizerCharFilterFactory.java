package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.icu.ICUNormalizer2CharFilter;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractCharFilterFactory;
import org.elasticsearch.index.settings.IndexSettings;

import java.io.Reader;

public class IcuNormalizerCharFilterFactory extends AbstractCharFilterFactory {

    private final Normalizer2 normalizer;

    @Inject
    public IcuNormalizerCharFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name);
        String normalizationName = settings.get("name", "nfkc_cf");
        Normalizer2.Mode normalizationMode;
        switch (settings.get("mode", "compose")) {
            case "compose_contiguous" : normalizationMode = Normalizer2.Mode.COMPOSE_CONTIGUOUS; break;
            case "decompose" : normalizationMode = Normalizer2.Mode.DECOMPOSE; break;
            case "fcd" : normalizationMode = Normalizer2.Mode.FCD; break;
            default: normalizationMode = Normalizer2.Mode.COMPOSE; break;
        }
        this.normalizer =  Normalizer2.getInstance(null, normalizationName, normalizationMode);
    }

    @Override
    public Reader create(Reader reader) {
        return new ICUNormalizer2CharFilter(reader, normalizer);
    }
}