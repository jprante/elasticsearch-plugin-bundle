/*
 * Copyright (C) 2014 Jörg Prante
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
package org.xbib.elasticsearch.index.analysis.baseform;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.xbib.elasticsearch.common.fsa.Dictionary;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
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
            String path = "/baseform/" + lang + "-lemma-utf8.txt";
            return new Dictionary().load(new InputStreamReader(getClass().getResourceAsStream(path), "UTF-8"));
        } catch (IOException e) {
            throw new ElasticsearchException("resources in settings not found: " + settings, e);
        }
    }
}
