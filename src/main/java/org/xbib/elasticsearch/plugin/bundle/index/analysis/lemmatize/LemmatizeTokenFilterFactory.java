package org.xbib.elasticsearch.plugin.bundle.index.analysis.lemmatize;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.xbib.elasticsearch.plugin.bundle.common.fsa.Dictionary;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * Lemmatize token filter factory.
 */
public class LemmatizeTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Dictionary dictionary;

    private final boolean respectKeywords;

    private final boolean lemmaOnly;

    public LemmatizeTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.respectKeywords = settings.getAsBoolean("respect_keywords", false);
        this.lemmaOnly = settings.getAsBoolean("lemma_only", true);
        this.dictionary = createDictionary(settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new LemmatizeTokenFilter(tokenStream, dictionary, respectKeywords, lemmaOnly);
    }

    private Dictionary createDictionary(Settings settings) {
        String language = settings.get("language", "en");
        try {
            String resource = settings.get("resource", "lemmatization-" + language + ".fsa.gz");
            if (resource.endsWith(".fsa") || resource.endsWith("fsa.gz")) {
                // FSA
                InputStream inputStream = getClass().getResourceAsStream(resource);
                if (resource.endsWith(".gz")) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                Dictionary dictionary = new Dictionary().loadFSA(inputStream);
                inputStream.close();
                return dictionary;
            } else {
                // Text
                InputStream inputStream = getClass().getResourceAsStream(resource);
                if (resource.endsWith(".gz")) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                Dictionary dictionary = new Dictionary().loadLinesReverse(reader);
                reader.close();
                return dictionary;
            }
        } catch (Exception e) {
            throw new ElasticsearchException("resources for language " + language +
                    " in settings not found: " + settings, e);
        }
    }
}
