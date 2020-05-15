package org.xbib.elasticsearch.plugin.bundle.test.index.analysis.icu;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.CharFilterFactory;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.test.ESTestCase;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizerFactory;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.IcuFoldingTokenFilterFactory;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.IcuNormalizerCharFilterFactory;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.IcuNormalizerTokenFilterFactory;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.IcuTransformTokenFilterFactory;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.instanceOf;

/**
 * ICU analysis tests
 */
public class IcuAnalysisTests extends ESTestCase {

    public void testDefaultsIcuAnalysis() throws IOException {

        TestAnalysis analysis = createTestAnalysis(new Index("test", "_na_"), Settings.EMPTY,
                new BundlePlugin(Settings.EMPTY));

        CharFilterFactory charFilterFactory = analysis.charFilter.get("icu_normalizer");
        assertThat(charFilterFactory, instanceOf(IcuNormalizerCharFilterFactory.class));

        TokenizerFactory tf = analysis.tokenizer.get("icu_tokenizer");
        assertThat(tf, instanceOf(IcuTokenizerFactory.class));

        TokenFilterFactory filterFactory = analysis.tokenFilter.get("icu_normalizer");
        assertThat(filterFactory, instanceOf(IcuNormalizerTokenFilterFactory.class));

        filterFactory = analysis.tokenFilter.get("icu_folding");
        assertThat(filterFactory, instanceOf(IcuFoldingTokenFilterFactory.class));

        filterFactory = analysis.tokenFilter.get("icu_transform");
        assertThat(filterFactory, instanceOf(IcuTransformTokenFilterFactory.class));

        Analyzer analyzer = analysis.indexAnalyzers.get( "icu_collation");
        assertThat(analyzer, instanceOf(NamedAnalyzer.class));
    }
}
