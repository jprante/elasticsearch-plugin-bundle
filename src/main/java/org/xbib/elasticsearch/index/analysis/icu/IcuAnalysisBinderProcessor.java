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
package org.xbib.elasticsearch.index.analysis.icu;

import org.elasticsearch.index.analysis.AnalysisModule;

public class IcuAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processCharFilters(CharFiltersBindings charFiltersBindings) {
        charFiltersBindings.processCharFilter("icu_normalizer", IcuNormalizerCharFilterFactory.class);
    }

    @Override
    public void processTokenizers(TokenizersBindings tokenizersBindings) {
        tokenizersBindings.processTokenizer("icu_tokenizer", IcuTokenizerFactory.class);
        tokenizersBindings.processTokenizer("icu_collation_tokenizer", IcuCollationTokenizerFactory.class);
    }

    @Override
    public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
        tokenFiltersBindings.processTokenFilter("icu_normalizer", IcuNormalizerTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("icu_folding", IcuFoldingTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("icu_transform", IcuTransformTokenFilterFactory.class);
    }

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
        analyzersBindings.processAnalyzer("icu_collation", IcuCollationKeyAnalyzerProvider.class);
    }
}
