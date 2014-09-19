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
package org.xbib.elasticsearch.index.analysis.standardnumber;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.UniqueTokenFilterFactory;
import org.elasticsearch.index.analysis.WhitespaceTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettings;

public class StandardNumberAnalyzerProvider extends AbstractIndexAnalyzerProvider<StandardNumberAnalyzer> {

    private final StandardNumberAnalyzer analyzer;

    @Inject
    public StandardNumberAnalyzerProvider(Index index, @IndexSettings Settings indexSettings,
                                          WhitespaceTokenizerFactory tokenizerFactory,
                                          StandardNumberTokenFilterFactory stdnumTokenFilterFactory,
                                          UniqueTokenFilterFactory uniqueTokenFilterFactory,
                                           @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        this.analyzer = new StandardNumberAnalyzer(tokenizerFactory, stdnumTokenFilterFactory, uniqueTokenFilterFactory);
    }

    @Override
    public StandardNumberAnalyzer get() {
        return this.analyzer;
    }
}
