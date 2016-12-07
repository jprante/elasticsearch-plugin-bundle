package org.xbib.elasticsearch.index.analysis.hyphen;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.CharFilterFactory;
import org.elasticsearch.index.analysis.CustomAnalyzer;
import org.elasticsearch.index.analysis.CustomAnalyzerProvider;
import org.elasticsearch.index.analysis.TokenFilterFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A Hyphen analyzer provider.
 */
public class HyphenAnalyzerProvider extends CustomAnalyzerProvider {

    private final Settings analyzerSettings;

    private final HyphenTokenizerFactory tokenizerFactory;

    private CustomAnalyzer customAnalyzer;

    public HyphenAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.tokenizerFactory = new HyphenTokenizerFactory(indexSettings, environment, name, settings);
        this.analyzerSettings = settings;
    }

    @Override
    public void build(AnalysisService analysisService) {
        List<CharFilterFactory> charFilters = new ArrayList<>();
        String[] charFilterNames = analyzerSettings.getAsArray("char_filter");
        for (String charFilterName : charFilterNames) {
            CharFilterFactory charFilter = analysisService.charFilter(charFilterName);
            if (charFilter == null) {
                throw new IllegalArgumentException("hyphen analyzer [" + name() + "] failed to find char_filter under name [" + charFilterName + "]");
            }
            charFilters.add(charFilter);
        }
        List<TokenFilterFactory> tokenFilters = new ArrayList<>();
        String[] tokenFilterNames = analyzerSettings.getAsArray("filter");
        for (String tokenFilterName : tokenFilterNames) {
            TokenFilterFactory tokenFilter = analysisService.tokenFilter(tokenFilterName);
            if (tokenFilter == null) {
                throw new IllegalArgumentException("hyphen analyzer [" + name() + "] failed to find filter under name [" + tokenFilterName + "]");
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
