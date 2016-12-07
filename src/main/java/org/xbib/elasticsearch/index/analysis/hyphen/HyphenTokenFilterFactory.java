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
package org.xbib.elasticsearch.index.analysis.hyphen;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 *
 */
public class HyphenTokenFilterFactory extends AbstractTokenFilterFactory {

    private final char[] hyphenchars;

    private final boolean subwords;

    private final boolean respectKeywords;

    public HyphenTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                    Settings settings) {
        super(indexSettings, name, settings);
        this.hyphenchars = settings.get("hyphens") != null ? settings.get("hyphens").toCharArray() : HyphenTokenFilter.HYPHEN;
        this.subwords = settings.getAsBoolean("subwords", true);
        this.respectKeywords = settings.getAsBoolean("respect_keywords", false);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new HyphenTokenFilter(tokenStream, hyphenchars, subwords, respectKeywords);
    }
}
