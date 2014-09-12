package org.xbib.elasticsearch.index.analysis.standardnumber;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.UniqueTokenFilterFactory;
import org.elasticsearch.index.analysis.WhitespaceTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettings;

public class StandardNumberAnalyzerProvider extends AbstractIndexAnalyzerProvider<StandardNumberAnalyzer> {

    private final StandardNumberAnalyzer analyzer;

    @Inject
    public StandardNumberAnalyzerProvider(Index index, @IndexSettings Settings indexSettings,
                                          WhitespaceTokenizerFactory tokenizerFactory,
                                          StandardNumberTokenFilterFactory stdnumTokenFilterFactory,
                                          UniqueTokenFilterFactory uniqueTokenFilterFactory,
                                           @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        this.analyzer = new StandardNumberAnalyzer(tokenizerFactory, stdnumTokenFilterFactory, uniqueTokenFilterFactory);
    }

    @Override
    public StandardNumberAnalyzer get() {
        return this.analyzer;
    }
}
