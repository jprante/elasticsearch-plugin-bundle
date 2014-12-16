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
package org.xbib.elasticsearch.index.analysis.standardnumber;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.analysis.UniqueTokenFilterFactory;

import java.io.Reader;
import java.util.Arrays;

public class StandardnumberAnalyzer extends Analyzer {

    private final TokenizerFactory tokenizerFactory;
    private final StandardnumberTokenFilterFactory stdnumTokenFilterFactory;
    private final UniqueTokenFilterFactory uniqueTokenFilterFactory;

    public StandardnumberAnalyzer(TokenizerFactory tokenizerFactory,
                                  StandardnumberTokenFilterFactory stdnumTokenFilterFactory,
                                  UniqueTokenFilterFactory uniqueTokenFilterFactory) {
        this.tokenizerFactory = tokenizerFactory;
        this.stdnumTokenFilterFactory = stdnumTokenFilterFactory;
        this.uniqueTokenFilterFactory = uniqueTokenFilterFactory;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer tokenizer = tokenizerFactory.create(reader);
        TokenStream tokenStream = tokenizer;
        for (TokenFilterFactory tokenFilter : Arrays.asList(stdnumTokenFilterFactory, uniqueTokenFilterFactory)) {
            tokenStream = tokenFilter.create(tokenStream);
        }
        return new TokenStreamComponents(tokenizer, tokenStream);
    }

}
