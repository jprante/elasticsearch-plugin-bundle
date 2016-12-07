package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Collator;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.collation.ICUCollationAttributeFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

/**
 *
 */
public class IcuCollationTokenizerFactory extends AbstractTokenizerFactory {

    private final ICUCollationAttributeFactory factory;

    private final int bufferSize;

    public IcuCollationTokenizerFactory(IndexSettings indexSettings, Environment environment, String name,
                                        Settings settings) {
        super(indexSettings, name, settings);
        Collator collator = IcuCollationKeyAnalyzerProvider.createCollator(settings);
        this.factory = new ICUCollationAttributeFactory(collator);
        this.bufferSize = settings.getAsInt("buffer_size", KeywordTokenizer.DEFAULT_BUFFER_SIZE);
    }

    @Override
    public Tokenizer create() {
        return new KeywordTokenizer(factory, bufferSize);
    }
}
