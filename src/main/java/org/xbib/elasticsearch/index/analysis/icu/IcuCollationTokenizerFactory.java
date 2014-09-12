package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Collator;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.collation.ICUCollationAttributeFactory;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettings;

import java.io.Reader;

public class IcuCollationTokenizerFactory extends AbstractTokenizerFactory {

    private final ICUCollationAttributeFactory factory;

    private final int bufferSize;

    @Inject
    public IcuCollationTokenizerFactory(Index index,
                                        @IndexSettings Settings indexSettings,
                                        Environment environment,
                                        @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        Collator collator = IcuCollationKeyAnalyzerProvider.createCollator(environment, settings);
        this.factory = new ICUCollationAttributeFactory(collator);
        this.bufferSize = settings.getAsInt("buffer_size", KeywordTokenizer.DEFAULT_BUFFER_SIZE);
    }

    @Override
    public Tokenizer create(Reader reader) {
        return new KeywordTokenizer(factory, reader, bufferSize);
    }
}
