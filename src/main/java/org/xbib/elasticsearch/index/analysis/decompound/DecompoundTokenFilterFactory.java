
package org.xbib.elasticsearch.index.analysis.decompound;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettings;

import java.io.IOException;

public class DecompoundTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Decompounder decompounder;

    @Inject
    public DecompoundTokenFilterFactory(Index index,
            @IndexSettings Settings indexSettings, Environment env,
            @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        this.decompounder = createDecompounder(env, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new DecompoundTokenFilter(tokenStream, decompounder);
    }

    private Decompounder createDecompounder(Environment env, Settings settings) {
        try {
            String forward = settings.get("forward", "/decompound/kompVVic.tree");
            String backward = settings.get("backward", "/decompound/kompVHic.tree");
            String reduce = settings.get("reduce", "/decompound/grfExt.tree");
            double threshold = settings.getAsDouble("threshold", 0.51);
            return new Decompounder(env.resolveConfig(forward).openStream(),
                    env.resolveConfig(backward).openStream(),
                    env.resolveConfig(reduce).openStream(),
                    threshold);
        } catch (ClassNotFoundException e) {
            throw new ElasticSearchIllegalArgumentException("decompounder resources in settings not found: " + settings, e);
        } catch (IOException e) {
            throw new ElasticSearchIllegalArgumentException("decompounder resources in settings not found: " + settings, e);
        }
    }
}
