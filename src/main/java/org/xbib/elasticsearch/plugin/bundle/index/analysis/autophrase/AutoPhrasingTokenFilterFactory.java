package org.xbib.elasticsearch.plugin.bundle.index.analysis.autophrase;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Auto phrase token filter factory.
 */
public class AutoPhrasingTokenFilterFactory extends AbstractTokenFilterFactory implements ResourceLoaderAware {

    private final String phraseSetFiles;
    private final boolean ignoreCase;
    private final boolean emitSingleTokens;

    private CharArraySet phraseSets;
    private String replaceWhitespaceWith;

    public AutoPhrasingTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.phraseSetFiles = settings.get("phrases");
        this.ignoreCase = settings.getAsBoolean("ignoreCase", false);
        this.emitSingleTokens = settings.getAsBoolean("includeTokens", false);
        this.replaceWhitespaceWith = settings.get("replaceWhitespaceWith");
    }

    @Override
    public void inform(ResourceLoader loader) throws IOException {
        if (phraseSetFiles != null) {
            phraseSets = getWordSet(loader, phraseSetFiles, ignoreCase);
        }
    }

    @Override
    public TokenStream create(TokenStream input) {
        AutoPhrasingTokenFilter autoPhraseFilter = new AutoPhrasingTokenFilter(input, phraseSets, emitSingleTokens);
        if (replaceWhitespaceWith != null) {
            autoPhraseFilter.setReplaceWhitespaceWith(replaceWhitespaceWith.charAt(0));
        }
        return autoPhraseFilter;
    }

    private CharArraySet getWordSet(ResourceLoader loader,
                                            String wordFiles, boolean ignoreCase) throws IOException {
        List<String> files = splitFileNames(wordFiles);
        CharArraySet words = null;
        if (!files.isEmpty()) {
            words = new CharArraySet(files.size() * 10, ignoreCase);
            for (String file : files) {
                List<String> wlist = getLines(loader, file.trim());
                words.addAll(StopFilter.makeStopSet(wlist, ignoreCase));
            }
        }
        return words;
    }

    private List<String> splitFileNames(String fileNames) {
        if (fileNames == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String file : fileNames.split("(?<!\\\\),")) {
            result.add(file.replaceAll("\\\\(?=,)", ""));
        }
        return result;
    }

    private List<String> getLines(ResourceLoader loader, String resource) throws IOException {
        return WordlistLoader.getLines(loader.openResource(resource), StandardCharsets.UTF_8);
    }
}
