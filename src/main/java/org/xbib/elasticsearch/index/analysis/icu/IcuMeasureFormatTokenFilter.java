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
import com.ibm.icu.util.Measure;
import com.ibm.icu.util.MeasureUnit;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.text.ParsePosition;

/**
 *
 */
public final class IcuMeasureFormatTokenFilter extends TokenFilter {

    private final CharTermAttribute termAtt;

    private final MeasureFormat measureFormat;

    public IcuMeasureFormatTokenFilter(TokenStream input, MeasureFormat measureFormat) {
        super(input);
        this.termAtt = addAttribute(CharTermAttribute.class);
        this.measureFormat = measureFormat;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        } else {
            String s = termAtt.toString();
            ParsePosition parsePosition = new ParsePosition(0);
            Measure measure = measureFormat.parseObject(s, parsePosition);
            if (parsePosition.getIndex() > 0) {
                Number number = measure.getNumber();
                MeasureUnit unit = measure.getUnit();

                if (unit == MeasureUnit.KILOBYTE) {
                    number = number.doubleValue() * 1024;
                } else if (unit == MeasureUnit.MEGABYTE) {
                    number = number.doubleValue() * 1024 * 1024;
                } else if (unit == MeasureUnit.GIGABYTE) {
                    number = number.doubleValue() * 1024 * 1024 * 1024;
                } else if (unit == MeasureUnit.TERABYTE) {
                    number = number.doubleValue() * 1024 * 1024 * 1024 * 1024;
                }
                termAtt.setEmpty().append(Long.toString(number.longValue()));
            } else {
                termAtt.setEmpty().append(s);
            }
            return true;
        }
    }
}
