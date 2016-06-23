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
package org.xbib.elasticsearch.index.analysis.decompound;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

import java.io.IOException;
import java.util.LinkedList;

public class DecompoundTokenFilter extends TokenFilter {

    private final LinkedList<String> tokens;

    private final Decompounder decomp;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

    private final boolean respectKeywords;

    private final boolean subwordsonly;

    private AttributeSource.State current;

    protected DecompoundTokenFilter(TokenStream input, Decompounder decomp, boolean respectKeywords, boolean subwordsonly) {
        super(input);
        this.tokens = new LinkedList<>();
        this.decomp = decomp;
        this.respectKeywords = respectKeywords;
        this.subwordsonly = subwordsonly;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!tokens.isEmpty()) {
            assert current != null;
            String token = tokens.removeFirst();
            restoreState(current);
            termAtt.setEmpty().append(token);
            if (!subwordsonly) {
                posIncAtt.setPositionIncrement(0);
            }
            return true;
        }
        if (!input.incrementToken()) {
            return false;
        }
        if (respectKeywords && keywordAtt.isKeyword()) {
            return true;
        }
        if (!decompound()) {
            current = captureState();
            if (subwordsonly) {
                String token = tokens.removeFirst();
                restoreState(current);
                termAtt.setEmpty().append(token);
                return true;
            }
        }
        return true;
    }

    protected boolean decompound() {
        String term = new String(termAtt.buffer(), 0, termAtt.length());
        for (String s : decomp.decompound(term)) {
            tokens.add(s);
        }
        return tokens.isEmpty();
    }


    @Override
    public void reset() throws IOException {
        super.reset();
        tokens.clear();
        current = null;
    }
}
