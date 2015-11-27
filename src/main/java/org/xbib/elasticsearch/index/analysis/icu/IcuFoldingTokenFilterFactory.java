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
package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.FilteredNormalizer2;
import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.UnicodeSet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.ICUFoldingFilter;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettingsService;

/**
 * Uses the {@link org.apache.lucene.analysis.icu.ICUFoldingFilter}.
 * Applies foldings from UTR#30 Character Foldings.
 * Can be filtered to handle certain characters in a specified way (see http://icu-project.org/apiref/icu4j/com/ibm/icu/text/UnicodeSet.html)
 * E.g national chars that should be retained (filter : "[^åäöÅÄÖ]").
 * The <tt>unicodeSetFilter</tt> attribute can be used to provide the UniCodeSet for filtering.
 */
public class IcuFoldingTokenFilterFactory extends AbstractTokenFilterFactory {

    private final String unicodeSetFilter;

    @Inject
    public IcuFoldingTokenFilterFactory(Index index,
                                        IndexSettingsService indexSettingsService,
                                        @Assisted String name,
                                        @Assisted Settings settings) {
        super(index, indexSettingsService.indexSettings(), name, settings);
        this.unicodeSetFilter = settings.get("unicodeSetFilter");
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        if (unicodeSetFilter != null) {
            // The ICUFoldingFilter is in fact implemented as a ICUNormalizer2Filter.
            // ICUFoldingFilter lacks a constructor for adding filtering so we implemement it here
            Normalizer2 base = Normalizer2.getInstance(ICUFoldingFilter.class.getResourceAsStream("utr30.nrm"),
                    "utr30", Normalizer2.Mode.COMPOSE);
            UnicodeSet unicodeSet = new UnicodeSet(unicodeSetFilter);
            unicodeSet.freeze();
            Normalizer2 filtered = new FilteredNormalizer2(base, unicodeSet);
            return new org.apache.lucene.analysis.icu.ICUNormalizer2Filter(tokenStream, filtered);
        } else {
            return new ICUFoldingFilter(tokenStream);
        }
    }
}