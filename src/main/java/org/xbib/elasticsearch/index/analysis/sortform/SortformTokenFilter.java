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
package org.xbib.elasticsearch.index.analysis.sortform;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Pattern;

public class SortformTokenFilter extends TokenFilter {

    private final static Pattern[] patterns = new Pattern[]{
            Pattern.compile("\\s*<<.*?>>\\s*"),
            Pattern.compile("\\s*<.*?>\\s*"),
            Pattern.compile("\\s*\u0098.*?\u009C\\s*"),
            Pattern.compile("\\s*\u02BE.*?\u02BB\\s*"),
            Pattern.compile("\\s*\u00AC.*?\u00AC\\s*")
    };
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    protected SortformTokenFilter(TokenStream input) {
        super(input);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        } else {
            String s = termAtt.toString();
            for (Pattern pattern : patterns) {
                s = pattern.matcher(s).replaceAll("");
            }
            termAtt.setEmpty().append(s);
            return true;
        }
    }
}
