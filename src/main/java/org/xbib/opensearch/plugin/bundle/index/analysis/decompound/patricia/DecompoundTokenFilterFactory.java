package org.xbib.opensearch.plugin.bundle.index.analysis.decompound.patricia;

import org.apache.lucene.analysis.TokenStream;
import org.opensearch.OpenSearchException;
import org.opensearch.common.inject.assistedinject.Assisted;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.common.decompound.patricia.Decompounder;
import org.xbib.opensearch.plugin.bundle.common.decompound.patricia.LFUCache;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Decompound token filter factory.
 */
public class DecompoundTokenFilterFactory extends AbstractTokenFilterFactory {

    private static Map<String, List<String>> cache;

    private final Decompounder decompounder;

    private final Boolean respectKeywords;

    private final Boolean subwordsonly;

    private final Boolean usePayload;

    public DecompoundTokenFilterFactory(IndexSettings indexSettings, Environment environment,
                                        @Assisted String name, @Assisted Settings settings) {
        super(indexSettings, name, settings);
        this.decompounder = createDecompounder(settings);
        this.respectKeywords = settings.getAsBoolean("respect_keywords", false);
        this.subwordsonly = settings.getAsBoolean("subwords_only", false);
        this.usePayload = settings.getAsBoolean("use_payload", false);
        if (cache == null && settings.getAsBoolean("use_cache", false)) {
            cache = createCache(settings);
        }
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new DecompoundTokenFilter(tokenStream, decompounder, respectKeywords, subwordsonly,
                usePayload, cache);
    }

    private Decompounder createDecompounder(Settings settings) {
        try {
            String forward = settings.get("forward", "kompVVic.tree");
            String backward = settings.get("backward", "kompVHic.tree");
            String reduce = settings.get("reduce", "grfExt.tree");
            double threshold = settings.getAsDouble("threshold", 0.51d);
            return new Decompounder(getClass().getResourceAsStream(forward),
                    getClass().getResourceAsStream(backward),
                    getClass().getResourceAsStream(reduce),
                    threshold);
        } catch (Exception e) {
            throw new OpenSearchException("decompounder resources in settings not found: " + settings, e);
        }
    }

    private Map<String, List<String>> createCache(Settings settings) {
        int cachesize = settings.getAsInt("cache_size", 100000);
        float evictionfactor = settings.getAsFloat("cache_eviction_factor", 0.90f);
        return Collections.synchronizedMap(new LFUCache<>(cachesize, evictionfactor));
    }
}
