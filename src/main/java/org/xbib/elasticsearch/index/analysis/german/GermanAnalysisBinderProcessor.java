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
package org.xbib.elasticsearch.index.analysis.german;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.xbib.elasticsearch.index.analysis.baseform.BaseformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.concat.ConcatTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.decompound.DecompoundTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.hyphen.HyphenTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.hyphen.HyphenTokenizerFactory;
import org.xbib.elasticsearch.index.analysis.sortform.SortformAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.sortform.SortformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.standardnumber.StandardNumberAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.standardnumber.StandardNumberTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.worddelimiter.WordDelimiterFilter2Factory;
import org.xbib.elasticsearch.index.analysis.worddelimiter.WordDelimiterFilterFactory;
import org.xbib.elasticsearch.index.analysis.year.GregorianYearTokenFilterFactory;

public class GermanAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processTokenizers(TokenizersBindings tokenizersBindings) {
        tokenizersBindings.processTokenizer("hyphen", HyphenTokenizerFactory.class);
    }

    @Override
    public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
        tokenFiltersBindings.processTokenFilter("german_normalize", GermanNormalizationFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("decompound", DecompoundTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("baseform", BaseformTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("worddelimiter", WordDelimiterFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("worddelimiter2", WordDelimiterFilter2Factory.class);
        tokenFiltersBindings.processTokenFilter("sortform", SortformTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("concat", ConcatTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("year", GregorianYearTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("hyphen", HyphenTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("standardnumber", StandardNumberTokenFilterFactory.class);
    }

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
        analyzersBindings.processAnalyzer("standardnumber", StandardNumberAnalyzerProvider.class);
        analyzersBindings.processAnalyzer("sortform", SortformAnalyzerProvider.class);
    }
}
