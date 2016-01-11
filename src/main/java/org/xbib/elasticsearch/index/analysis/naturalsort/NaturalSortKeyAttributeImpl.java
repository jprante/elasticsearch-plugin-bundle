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
package org.xbib.elasticsearch.index.analysis.naturalsort;

import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.apache.lucene.util.BytesRef;

import java.text.Collator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaturalSortKeyAttributeImpl extends CharTermAttributeImpl {

    private final static Pattern numberPattern = Pattern.compile("(\\+|\\-)?([0-9]+)");

    private final Collator collator;

    private final int digits;

    private final int maxTokens;

    public NaturalSortKeyAttributeImpl(Collator collator, int digits, int maxTokens) {
        this.collator = collator;
        this.digits = digits;
        this.maxTokens = maxTokens;
    }

    @Override
    public BytesRef getBytesRef() {
        byte[] collationKey = collator.getCollationKey(natural(toString())).toByteArray();
        final BytesRef ref = this.builder.get();
        ref.bytes = collationKey;
        ref.offset = 0;
        ref.length = collationKey.length;
        return ref;
    }

    private String natural(String s) {
        StringBuffer sb = new StringBuffer();
        Matcher m = numberPattern.matcher(s);
        int foundTokens = 0;
        while (m.find()) {
            int len = m.group(2).length();
            String repl = String.format("%0" + digits + "d", len) + m.group();
            m.appendReplacement(sb, repl);
            foundTokens++;
            if (foundTokens >= maxTokens){
                break;
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
