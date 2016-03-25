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
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.text.ParsePosition;

public final class IcuNumberFormatTokenFilter extends TokenFilter {

    private final NumberFormat numberFormat;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    public IcuNumberFormatTokenFilter(TokenStream input, NumberFormat numberFormat) {
        super(input);
        this.numberFormat = numberFormat;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        } else {
            String s = termAtt.toString();
            ParsePosition parsePosition = new ParsePosition(0);
            Number result = numberFormat.parse(s, parsePosition);
            if (parsePosition.getIndex() > 0) {
                // zehn-tausend -> zehntausend
                // one hundred thousand -> onehundredthousand
                s = numberFormat.format(result).replaceAll("[\u00AD\u0020]", "");
            }
            termAtt.setEmpty().append(s);
            return true;
        }
    }
}
