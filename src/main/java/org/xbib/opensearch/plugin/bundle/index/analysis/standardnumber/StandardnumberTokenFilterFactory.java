package org.xbib.opensearch.plugin.bundle.index.analysis.standardnumber;

import org.apache.lucene.analysis.TokenStream;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.common.standardnumber.StandardnumberService;
import org.xbib.opensearch.plugin.bundle.index.mapper.standardnumber.StandardnumberMapper;

/**
 * Standard number token filter factory.
 */
public class StandardnumberTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Settings settings;

    private final StandardnumberService standardnumberService;

    public StandardnumberTokenFilterFactory(IndexSettings indexSettings,
                                            Environment environment,
                                            String name,
                                            Settings settings,
                                            StandardnumberMapper.TypeParser standardNumberTypeParser) {
        super(indexSettings, name, settings);
        this.settings = settings;
        this.standardnumberService = new StandardnumberService();
        this.standardnumberService.setStandardNumberTypeParser(standardNumberTypeParser);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new StandardnumberTokenFilter(tokenStream, standardnumberService, settings);
    }
}
