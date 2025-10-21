package org.xbib.opensearch.plugin.bundle.index.analysis.icu;

import com.ibm.icu.text.Collator;
import org.apache.lucene.analysis.Tokenizer;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizer;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizerFactory;

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
