package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettings;

import java.io.Reader;

public class IcuCollationTokenizerFactory extends AbstractTokenizerFactory {

    private final Environment environment;

    private final Settings settings;

    @Inject
    public IcuCollationTokenizerFactory(Environment environment,
                                        Index index,
                                        @IndexSettings Settings indexSettings,
                                        @Assisted String name,
                                        @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        this.environment = environment;
        this.settings = settings;
    }

    @Override
    public Tokenizer create(Reader reader) {
        System.err.println("creating tokenizer from reader");
        return new IcuCollationTokenizer(environment, settings, reader);
    }

}
