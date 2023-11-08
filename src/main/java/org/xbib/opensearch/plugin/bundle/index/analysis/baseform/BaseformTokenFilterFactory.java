package org.xbib.opensearch.plugin.bundle.index.analysis.baseform;

import org.apache.lucene.analysis.TokenStream;
import org.opensearch.OpenSearchException;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.common.fsa.Dictionary;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Base form token filter factory.
 */
public class BaseformTokenFilterFactory extends AbstractTokenFilterFactory {

    private final boolean respectKeywords;

    private final Dictionary dictionary;

    public BaseformTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.respectKeywords = settings.getAsBoolean("respect_keywords", false);
        this.dictionary = createDictionary(settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new BaseformTokenFilter(tokenStream, dictionary, respectKeywords);
    }

    private Dictionary createDictionary(Settings settings) {
        try {
            String lang = settings.get("language", "de");
            String path = lang + "-lemma-utf8.txt";
            return new Dictionary().loadLines(new InputStreamReader(getClass().getResourceAsStream(path), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new OpenSearchException("resources in settings not found: " + settings, e);
        }
    }
}
