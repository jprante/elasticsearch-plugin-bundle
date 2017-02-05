package org.xbib.elasticsearch.index.analysis.standardnumber;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberMapper;
import org.xbib.elasticsearch.common.standardnumber.StandardnumberService;

/**
 *
 */
public class StandardnumberTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Settings settings;

    private final StandardnumberService standardnumberService;

    public StandardnumberTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                            Settings settings, StandardnumberMapper.TypeParser standardNumberTypeParser) {
        super(indexSettings, name, settings);
        this.settings = settings;
        this.standardnumberService = new StandardnumberService(settings);
        this.standardnumberService.setStandardNumberTypeParser(standardNumberTypeParser);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new StandardnumberTokenFilter(tokenStream, standardnumberService, settings);
    }
}
