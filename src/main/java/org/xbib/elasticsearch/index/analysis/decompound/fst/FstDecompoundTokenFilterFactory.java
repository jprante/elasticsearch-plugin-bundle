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
package org.xbib.elasticsearch.index.analysis.decompound.fst;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettings;

import java.io.IOException;

public class FstDecompoundTokenFilterFactory extends AbstractTokenFilterFactory {

    private final FstDecompounder decompounder;

    @Inject
    public FstDecompoundTokenFilterFactory(Index index,
                                           Environment env,
                                           @IndexSettings Settings indexSettings,
                                           @Assisted String name,
                                           @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        this.decompounder = createDecompounder(env, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new FstDecompoundTokenFilter(tokenStream, decompounder);
    }

    private FstDecompounder createDecompounder(Environment env, Settings settings) {
        try {
            String words = settings.get("words", "/decompound/fst/words.fst");
            return new FstDecompounder(env.resolveConfig(words).openStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("fst decompounder resources in settings not found: " + settings, e);
        }
    }
}
