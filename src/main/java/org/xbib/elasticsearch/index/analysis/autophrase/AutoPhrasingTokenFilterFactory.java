/*
 * Copyright (C) 2016 Jörg Prante
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 */
package org.xbib.elasticsearch.index.analysis.autophrase;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.WordlistLoader;
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
 *
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

    protected final CharArraySet getWordSet(ResourceLoader loader,
                                            String wordFiles, boolean ignoreCase) throws IOException {
        List<String> files = splitFileNames(wordFiles);
        CharArraySet words = null;
        if (files.size() > 0) {
            words = new CharArraySet(files.size() * 10, ignoreCase);
            for (String file : files) {
                List<String> wlist = getLines(loader, file.trim());
                words.addAll(StopFilter.makeStopSet(wlist, ignoreCase));
            }
        }
        return words;
    }

    protected final List<String> splitFileNames(String fileNames) {
        if (fileNames == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String file : fileNames.split("(?<!\\\\),")) {
            result.add(file.replaceAll("\\\\(?=,)", ""));
        }
        return result;
    }

    protected final List<String> getLines(ResourceLoader loader, String resource) throws IOException {
        return WordlistLoader.getLines(loader.openResource(resource), StandardCharsets.UTF_8);
    }

}