package org.xbib.opensearch.plugin.bundle.test.index.analysis.icu;

import org.apache.lucene.analysis.Analyzer;
import org.opensearch.common.settings.Settings;
import org.opensearch.index.Index;
import org.opensearch.index.analysis.CharFilterFactory;
import org.opensearch.index.analysis.NamedAnalyzer;
import org.opensearch.index.analysis.TokenFilterFactory;
import org.opensearch.index.analysis.TokenizerFactory;
import org.opensearch.test.OpenSearchTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuFoldingTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuNormalizerCharFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuNormalizerTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuTransformTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizerFactory;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.instanceOf;

/**
 * ICU analysis tests
 */
public class IcuAnalysisTests extends OpenSearchTestCase {

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
