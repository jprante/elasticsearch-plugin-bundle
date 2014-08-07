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
        String name1 = settings.get("name", "nfkc_cf");
        String mode = settings.get("mode");
        if (!"compose".equals(mode) && !"decompose".equals(mode)) {
            mode = "compose";
        }
        this.normalizer = Normalizer2.getInstance(null, name1, "compose".equals(mode) ? Normalizer2.Mode.COMPOSE : Normalizer2.Mode.DECOMPOSE);
    }

    @Override
    public Reader create(Reader reader) {
        return new ICUNormalizer2CharFilter(reader, normalizer);
    }
}