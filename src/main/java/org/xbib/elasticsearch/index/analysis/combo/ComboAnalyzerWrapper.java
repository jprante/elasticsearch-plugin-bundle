package org.xbib.elasticsearch.index.analysis.combo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.NamedAnalyzer;

import java.io.Reader;
import java.util.ArrayList;

public final class ComboAnalyzerWrapper extends Analyzer {

    public static final String NAME = "combo";

    private final Injector injector;

    private final Settings settings;

    private final Version version;

    private final String name;

    private ComboAnalyzer analyzer;

    public ComboAnalyzerWrapper(Version version, String name, Settings settings, Injector injector) {
        this.name = name;
        // Store parameters for lazy usage
        // See ComboAnalyzerProvider comments to learn why
        this.injector = injector;
        this.settings = settings;
        this.version = version;

        this.analyzer = null; // must be lazy initialized to get free of the cyclic dependency on AnalysisService
    }

    /**
     * Read settings and load the appropriate sub-analyzers.
     */
    synchronized
    protected void init() {
        if (analyzer != null) {
            return;
        }
        AnalysisService analysisService = injector.getInstance(AnalysisService.class);

        String[] sub = settings.getAsArray("sub_analyzers");
        ArrayList<Analyzer> subAnalyzers = new ArrayList<Analyzer>();
        if (sub == null) {
            throw new ElasticsearchIllegalArgumentException("Analyzer [" + name + "] analyzer of type [" + NAME + "], must have a \"sub_analyzers\" list property");
        }

        for (String subname : sub) {
            NamedAnalyzer analyzer = analysisService.analyzer(subname);
            if (analyzer != null) {
                subAnalyzers.add(analyzer);
            }
        }

        this.analyzer = new ComboAnalyzer(version, subAnalyzers.toArray(new Analyzer[subAnalyzers.size()]));

        Boolean tokenstreamCaching = settings.getAsBoolean("tokenstream_caching", null);
        if (tokenstreamCaching != null) {
            this.analyzer.setTokenStreamCachingEnabled(tokenstreamCaching);
        }

        Boolean deduplication = settings.getAsBoolean("deduplication", null);
        if (deduplication != null) {
            this.analyzer.setDeduplicationEnabled(deduplication);
        }
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        if (analyzer == null) {
            init();
        }
        return this.analyzer.createComponents(fieldName, reader);
    }

    @Override
    public void close() {
        if (analyzer != null) {
            this.analyzer.close();
        }
        super.close();
    }

}
