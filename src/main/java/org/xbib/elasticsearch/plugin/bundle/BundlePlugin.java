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
package org.xbib.elasticsearch.plugin.bundle;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.CharFilterFactory;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.MapperPlugin;
import org.elasticsearch.plugins.Plugin;
import org.xbib.elasticsearch.index.analysis.autophrase.AutoPhrasingTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.baseform.BaseformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.concat.ConcatTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.concat.PairTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.decompound.DecompoundTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.german.GermanNormalizationFilterFactory;
import org.xbib.elasticsearch.index.analysis.hyphen.HyphenAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.hyphen.HyphenTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.hyphen.HyphenTokenizerFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuCollationKeyAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.icu.IcuCollationTokenizerFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuFoldingCharFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuFoldingTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuNormalizerCharFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuNormalizerTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuNumberFormatTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuTokenizerFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuTransformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.naturalsort.NaturalSortKeyAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.naturalsort.NaturalSortKeyTokenizerFactory;
import org.xbib.elasticsearch.index.analysis.sortform.SortformAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.sortform.SortformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.standardnumber.StandardnumberAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.standardnumber.StandardnumberTokenFilterFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class BundlePlugin extends Plugin implements AnalysisPlugin, MapperPlugin {

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<CharFilterFactory>> getCharFilters() {
        Map<String, AnalysisModule.AnalysisProvider<CharFilterFactory>> extra = new HashMap<>();
        extra.put("icu_normalizer", IcuNormalizerCharFilterFactory::new);
        extra.put("icu_folding", IcuFoldingCharFilterFactory::new);
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> extra = new HashMap<>();
        extra.put("icu_normalizer", IcuNormalizerTokenFilterFactory::new);
        extra.put("icu_folding", IcuFoldingTokenFilterFactory::new);
        extra.put("icu_transform", IcuTransformTokenFilterFactory::new);
        extra.put("icu_numberformat", IcuNumberFormatTokenFilterFactory::new);
        extra.put("auto_phrase", AutoPhrasingTokenFilterFactory::new);
        extra.put("baseform", BaseformTokenFilterFactory::new);
        extra.put("concat", ConcatTokenFilterFactory::new);
        extra.put("pair", PairTokenFilterFactory::new);
        extra.put("decompound", DecompoundTokenFilterFactory::new);
        extra.put("german_normalize", GermanNormalizationFilterFactory::new);
        extra.put("hyphen", HyphenTokenFilterFactory::new);
        extra.put("sortform", SortformTokenFilterFactory::new);
        extra.put("standardnumber", StandardnumberTokenFilterFactory::new);
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> extra = new HashMap<>();
        extra.put("icu_collation_tokenizer", IcuCollationTokenizerFactory::new);
        extra.put("icu_tokenizer", IcuTokenizerFactory::new);
        extra.put("hyphen", HyphenTokenizerFactory::new);
        extra.put("naturalsort", NaturalSortKeyTokenizerFactory::new);
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = new HashMap<>();
        extra.put("icu_collation", IcuCollationKeyAnalyzerProvider::new);
        extra.put("hyphen", HyphenAnalyzerProvider::new);
        extra.put("naturalsort", NaturalSortKeyAnalyzerProvider::new);
        extra.put("sortform", SortformAnalyzerProvider::new);
        extra.put("standardnumber", StandardnumberAnalyzerProvider::new);
        return extra;
    }
}
