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
package org.xbib.elasticsearch.index.analysis.year;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GregorianYearTokenFilter extends TokenFilter {

    private final static Pattern pattern = Pattern.compile("(\\d{4})");
    protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final String defaultYear;

    protected GregorianYearTokenFilter(TokenStream input, String defaultYear) {
        super(input);
        this.defaultYear = defaultYear;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        } else {
            String s = termAtt.toString();
            Matcher m = pattern.matcher(s);
            termAtt.setEmpty();
            if (!m.matches()) {
                termAtt.append(defaultYear);
            } else {
                while (m.find()) {
                    termAtt.append(m.group());
                }
            }
            return true;
        }
    }
}
