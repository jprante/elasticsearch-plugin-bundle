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

import com.ibm.icu.text.MeasureFormat;
import com.ibm.icu.util.ULocale;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 *
 */
public class IcuMeasureFormatTokenFilterFactory extends AbstractTokenFilterFactory {

    private final MeasureFormat measureFormat;

    public IcuMeasureFormatTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        ULocale locale = settings.get("locale") != null ?
                new ULocale(settings.get("locale")) : ULocale.getDefault();
        String formatWidthStr = settings.get("format", "NARROW");
        MeasureFormat.FormatWidth formatWidth = MeasureFormat.FormatWidth.NARROW;
        switch (formatWidthStr) {
            case "NARROW": {
                // "3h"
                formatWidth = MeasureFormat.FormatWidth.NARROW;
                break;
            }
            case "NUMERIC": {
                // "3:17"
                formatWidth = MeasureFormat.FormatWidth.NUMERIC;
                break;
            }
            case "WIDE": {
                // "3 hours"
                formatWidth = MeasureFormat.FormatWidth.WIDE;
                break;
            }
            case "SHORT": {
                // "3 hrs"
                formatWidth = MeasureFormat.FormatWidth.SHORT;
                break;
            }
        }
        this.measureFormat = MeasureFormat.getInstance(locale, formatWidth);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new IcuMeasureFormatTokenFilter(tokenStream, measureFormat);
    }
}