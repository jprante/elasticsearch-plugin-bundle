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

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.CharFilterFactory;
import org.elasticsearch.index.analysis.CustomAnalyzer;
import org.elasticsearch.index.analysis.CustomAnalyzerProvider;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettings;
import org.xbib.elasticsearch.index.analysis.icu.IcuCollationTokenizerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Like CustomAnalyzerProvider, but with IcuCollationTokenizerFactory as tokenizer
 */
public class SortformAnalyzerProvider extends CustomAnalyzerProvider {

    private final Settings analyzerSettings;

    private final IcuCollationTokenizerFactory tokenizerFactory;

    private CustomAnalyzer customAnalyzer;

    @Inject
    public SortformAnalyzerProvider(Index index, @IndexSettings Settings indexSettings,
                                    IcuCollationTokenizerFactory tokenizerFactory,
                                    @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        this.tokenizerFactory = tokenizerFactory;
        this.analyzerSettings = settings;
    }

    @Override
    public void build(AnalysisService analysisService) {
        List<CharFilterFactory> charFilters = new ArrayList<>();
        String[] charFilterNames = analyzerSettings.getAsArray("char_filter");
        for (String charFilterName : charFilterNames) {
            CharFilterFactory charFilter = analysisService.charFilter(charFilterName);
            if (charFilter == null) {
                throw new IllegalArgumentException("Sortform Analyzer [" + name() + "] failed to find char_filter under name [" + charFilterName + "]");
            }
            charFilters.add(charFilter);
        }
        List<TokenFilterFactory> tokenFilters = new ArrayList<>();
        String[] tokenFilterNames = analyzerSettings.getAsArray("filter");
        for (String tokenFilterName : tokenFilterNames) {
            TokenFilterFactory tokenFilter = analysisService.tokenFilter(tokenFilterName);
            if (tokenFilter == null) {
                throw new IllegalArgumentException("Sortform Analyzer [" + name() + "] failed to find filter under name [" + tokenFilterName + "]");
            }
            tokenFilters.add(tokenFilter);
        }

        int positionOffsetGap = analyzerSettings.getAsInt("position_offset_gap", 0);
        int offsetGap = analyzerSettings.getAsInt("offset_gap", -1);

        this.customAnalyzer = new CustomAnalyzer(tokenizerFactory,
                charFilters.toArray(new CharFilterFactory[charFilters.size()]),
                tokenFilters.toArray(new TokenFilterFactory[tokenFilters.size()]),
                positionOffsetGap,
                offsetGap
        );
    }

    @Override
    public CustomAnalyzer get() {
        return this.customAnalyzer;
    }
}
