package org.xbib.opensearch.plugin.bundle.index.analysis.sortform;

import com.ibm.icu.text.Collator;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.opensearch.common.settings.Settings;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenizerFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuCollationAttributeFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuCollationKeyAnalyzerProvider;

public class SortformTokenizerFactory extends AbstractTokenizerFactory {

    private final IcuCollationAttributeFactory factory;

    private final int bufferSize;

    public SortformTokenizerFactory(IndexSettings indexSettings, String name, Settings settings) {
        super(indexSettings, settings, name);
        Collator collator = IcuCollationKeyAnalyzerProvider.createCollator(settings);
        factory = new IcuCollationAttributeFactory(collator);
        bufferSize = settings.getAsInt("bufferSize", 256);
    }

    @Override
    public Tokenizer create() {
        return new KeywordTokenizer(factory, bufferSize);
    }
}
