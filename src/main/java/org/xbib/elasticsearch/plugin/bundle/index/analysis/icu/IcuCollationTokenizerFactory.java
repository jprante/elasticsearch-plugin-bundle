package org.xbib.elasticsearch.plugin.bundle.index.analysis.icu;

import com.ibm.icu.text.Collator;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizer;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizerFactory;

/**
 * This {@link IcuTokenizer} uses an ICU @{@link Collator} as a char attribute factory.
 */
public class IcuCollationTokenizerFactory extends IcuTokenizerFactory {

    private final IcuCollationAttributeFactory factory;

    public IcuCollationTokenizerFactory(IndexSettings indexSettings, Environment environment, String name,
                                        Settings settings) {
        super(indexSettings, environment, name, settings);
        this.factory = new IcuCollationAttributeFactory(IcuCollationKeyAnalyzerProvider.createCollator(settings));
    }

    @Override
    public Tokenizer create() {
        return new IcuTokenizer(factory, config);
    }
}
