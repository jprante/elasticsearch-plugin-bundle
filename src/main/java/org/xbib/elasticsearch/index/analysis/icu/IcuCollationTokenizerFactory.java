package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Collator;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.xbib.elasticsearch.index.analysis.icu.segmentation.DefaultIcuTokenizerConfig;
import org.xbib.elasticsearch.index.analysis.icu.segmentation.IcuTokenizer;
import org.xbib.elasticsearch.index.analysis.icu.segmentation.IcuTokenizerConfig;
import org.xbib.elasticsearch.index.analysis.icu.tokenattributes.IcuCollationAttributeFactory;

/**
 * This {@link IcuTokenizer} uses an ICU @{@link Collator} as a char attribute factory.
 */
public class IcuCollationTokenizerFactory extends AbstractTokenizerFactory {

    private final IcuCollationAttributeFactory factory;

    private final IcuTokenizerConfig config;

    public IcuCollationTokenizerFactory(IndexSettings indexSettings, Environment environment, String name,
                                        Settings settings) {
        super(indexSettings, name, settings);
        Collator collator = IcuCollationKeyAnalyzerProvider.createCollator(settings);
        this.factory = new IcuCollationAttributeFactory(collator);
        this.config = new DefaultIcuTokenizerConfig(true, true);
    }

    @Override
    public Tokenizer create() {
        return new IcuTokenizer(factory, config);
    }
}
