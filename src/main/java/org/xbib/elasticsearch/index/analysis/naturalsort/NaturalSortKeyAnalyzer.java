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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordTokenizer;

import java.text.Collator;

public class NaturalSortKeyAnalyzer extends Analyzer {

    private final NaturalSortKeyAttributeFactory factory;

    private final int bufferSize;

    public NaturalSortKeyAnalyzer(Collator collator, int bufferSize, int digits, int maxtoken) {
        this.factory = new NaturalSortKeyAttributeFactory(collator, digits, maxtoken);
        this.bufferSize = bufferSize;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        KeywordTokenizer tokenizer = new KeywordTokenizer(factory, bufferSize);
        return new TokenStreamComponents(tokenizer, tokenizer);
    }

}
