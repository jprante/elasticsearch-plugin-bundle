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
package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettingsService;

import java.util.Locale;

public class IcuNumberFormatTokenFilterFactory extends AbstractTokenFilterFactory {

    private final NumberFormat numberFormat;

    @Inject
    public IcuNumberFormatTokenFilterFactory(Index index,
                                             IndexSettingsService indexSettingsService,
                                             @Assisted String name,
                                             @Assisted Settings settings) {
        super(index, indexSettingsService.indexSettings(), name, settings);
        Locale locale = settings.get("locale") != null ? new Locale(settings.get("locale")) : Locale.getDefault();
        String formatStr = settings.get("format", "SPELLOUT");
        int format;
        switch (formatStr.toUpperCase()) {
            case "SPELLOUT" :
                format = RuleBasedNumberFormat.SPELLOUT;
                break;
            case "DURATION" :
                format = RuleBasedNumberFormat.DURATION;
                break;
            case "NUMBERING_SYSTEM" :
                format = RuleBasedNumberFormat.NUMBERING_SYSTEM;
                break;
            case "NUMBERSTYLE" :
                format = RuleBasedNumberFormat.NUMBERSTYLE;
                break;
            case "ORDINAL" :
                format = RuleBasedNumberFormat.ORDINAL;
                break;
            default: format = RuleBasedNumberFormat.SPELLOUT;
                break;
        }
        RuleBasedNumberFormat ruleBasedNumberFormat = new RuleBasedNumberFormat(locale, format);
        // RBNF parsing is incredibly slow when lenient is enabled but the only method to parse compound number words
        ruleBasedNumberFormat.setLenientParseMode(settings.getAsBoolean("lenient", true));
        ruleBasedNumberFormat.setGroupingUsed(settings.getAsBoolean("grouping", true));
        this.numberFormat = ruleBasedNumberFormat;
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new IcuNumberFormatTokenFilter(tokenStream, numberFormat);
    }
}