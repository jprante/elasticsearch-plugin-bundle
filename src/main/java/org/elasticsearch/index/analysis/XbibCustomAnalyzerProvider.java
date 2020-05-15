package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.mapper.TextFieldMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Inherit from {@link CustomAnalyzerProvider} because of build()
 * method access privilege. Drop "AnalyzerComponents".
 *
 * Ignore reloadable search analyzers.
 *
 * A custom analyzer that is built out of a single
 * {@link org.apache.lucene.analysis.Tokenizer}
 * and a list of {@link org.apache.lucene.analysis.TokenFilter}s.
 */
public class XbibCustomAnalyzerProvider extends CustomAnalyzerProvider {

    private final Settings analyzerSettings;

    private Analyzer customAnalyzer;

    public XbibCustomAnalyzerProvider(IndexSettings indexSettings,
                                      String name,
                                      Settings settings) {
        super(indexSettings, name, settings);
        this.analyzerSettings = settings;
    }

    /**
     * Override method and extend privilege to protected.
     *
     * This oddity was introduced in ES 7.2 https://github.com/elastic/elasticsearch/pull/40609
     *
     * @param tokenizers the tokenizers
     * @param charFilters the char filters
     * @param tokenFilters the token filters
     */
    @Override
    protected void build(final Map<String, TokenizerFactory> tokenizers,
               final Map<String, CharFilterFactory> charFilters,
               final Map<String, TokenFilterFactory> tokenFilters) {
        customAnalyzer = create(name(), analyzerSettings, tokenizers, charFilters, tokenFilters);
    }

    @Override
    public Analyzer get() {
        return this.customAnalyzer;
    }

    private Analyzer create(String name,
                                   Settings analyzerSettings,
                                   Map<String, TokenizerFactory> tokenizers,
                                   Map<String, CharFilterFactory> charFilters,
                                   Map<String, TokenFilterFactory> tokenFilters) {
        int positionIncrementGap = TextFieldMapper.Defaults.POSITION_INCREMENT_GAP;
        positionIncrementGap = analyzerSettings.getAsInt("position_increment_gap", positionIncrementGap);
        int offsetGap = analyzerSettings.getAsInt("offset_gap", -1);
        String tokenizerName = analyzerSettings.get("tokenizer");
        if (tokenizerName == null) {
            throw new IllegalArgumentException("Custom Analyzer [" + name + "] must be configured with a tokenizer");
        }
        TokenizerFactory tokenizer = tokenizers.get(tokenizerName);
        if (tokenizer == null) {
            throw new IllegalArgumentException(
                    "Custom Analyzer [" + name + "] failed to find tokenizer under name " + "[" + tokenizerName + "]");
        }
        List<String> charFilterNames = analyzerSettings.getAsList("char_filter");
        List<CharFilterFactory> charFiltersList = new ArrayList<>(charFilterNames.size());
        for (String charFilterName : charFilterNames) {
            CharFilterFactory charFilter = charFilters.get(charFilterName);
            if (charFilter == null) {
                throw new IllegalArgumentException(
                        "Custom Analyzer [" + name + "] failed to find char_filter under name " + "[" + charFilterName + "]");
            }
            charFiltersList.add(charFilter);
        }
        List<String> tokenFilterNames = analyzerSettings.getAsList("filter");
        List<TokenFilterFactory> tokenFilterList = new ArrayList<>(tokenFilterNames.size());
        for (String tokenFilterName : tokenFilterNames) {
            TokenFilterFactory tokenFilter = tokenFilters.get(tokenFilterName);
            if (tokenFilter == null) {
                throw new IllegalArgumentException(
                        "Custom Analyzer [" + name + "] failed to find filter under name " + "[" + tokenFilterName + "]");
            }
            tokenFilter = tokenFilter.getChainAwareTokenFilterFactory(tokenizer, charFiltersList, tokenFilterList, tokenFilters::get);
            tokenFilterList.add(tokenFilter);
        }
        return new CustomAnalyzer(tokenizer, charFiltersList.toArray(new CharFilterFactory[charFiltersList.size()]),
                tokenFilterList.toArray(new TokenFilterFactory[tokenFilterList.size()]), positionIncrementGap, offsetGap);
    }
}
