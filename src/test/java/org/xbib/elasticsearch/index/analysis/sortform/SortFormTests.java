package org.xbib.elasticsearch.index.analysis.sortform;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;
import org.xbib.elasticsearch.plugin.analysis.bundle.BundlePlugin;
import org.xbib.util.MultiMap;
import org.xbib.util.TreeMultiMap;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class SortFormTests extends BaseTokenStreamTest {

    @Test
    public void testBasicUsage() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, org.elasticsearch.Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .put("index.analysis.analyzer.myanalyzer.type", "sortform")
                .put("index.analysis.analyzer.myanalyzer.filter", "sortform")
                .build();
        AnalysisService analysisService = createAnalysisService(settings);
        NamedAnalyzer myanalyzer = analysisService.analyzer("myanalyzer");
        assertAnalyzesTo(myanalyzer, "<<Der>> Titel des Buches", new String[]{"Titel des Buches"});
    }

    @Test
    public void testUnicodeUsage() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, org.elasticsearch.Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .put("index.analysis.analyzer.myanalyzer.type", "sortform")
                .put("index.analysis.analyzer.myanalyzer.filter", "sortform")
                .build();
        AnalysisService analysisService = createAnalysisService(settings);
        Analyzer myanalyzer = analysisService.analyzer("myanalyzer");
        // Unicode 0098: START OF STRING
        // Unicode 009C: STRING TERMINATOR
        assertAnalyzesTo(myanalyzer, "\u0098Der\u009c Titel des Buches", new String[]{"Titel des Buches"});
    }

    @Test
    public void testFromJson() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, org.elasticsearch.Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .loadFromStream("sortform.json", getClass().getResourceAsStream("/org/xbib/elasticsearch/index/analysis/sortform/sortform.json")).build();
        AnalysisService analysisService = createAnalysisService(settings);
        Analyzer analyzer = analysisService.analyzer("german_phonebook_with_sortform").analyzer();

        String[] words = new String[]{
                "¬Frau¬ Göbel",
                "Goethe",
                "¬Dr.¬ Goldmann",
                "Göthe",
                "¬Herr¬ Götz",
                "Groß",
                "Gross"
        };

        MultiMap<BytesRef,String> map = new TreeMultiMap<>();
        for (String s : words) {
            TokenStream ts = analyzer.tokenStream(null, s);
            BytesRef sortKey = sortKeyFromTokenStream(ts);
            map.put(sortKey, s);
        }
        // strength "quaternary" orders without punctuation and ensures unique entries
        Iterator<Set<String>> it = map.values().iterator();
        assertEquals("[¬Frau¬ Göbel]",it.next().toString());
        assertEquals("[Goethe]",it.next().toString());
        assertEquals("[Göthe]",it.next().toString());
        assertEquals("[¬Herr¬ Götz]",it.next().toString());
        assertEquals("[¬Dr.¬ Goldmann]",it.next().toString());
        assertEquals("[Gross]",it.next().toString());
        assertEquals("[Groß]",it.next().toString());
    }

    private AnalysisService createAnalysisService(Settings settings) {
        Index index = new Index("test");
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)))
                .createInjector();
        AnalysisModule analysisModule = new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class));
        new BundlePlugin(settings).onModule(analysisModule);
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index), analysisModule)
                .createChildInjector(parentInjector);
        return injector.getInstance(AnalysisService.class);
    }

    private BytesRef sortKeyFromTokenStream(TokenStream stream) throws IOException {
        TermToBytesRefAttribute termAttr = stream.getAttribute(TermToBytesRefAttribute.class);
        BytesRef bytesRef = termAttr.getBytesRef();
        stream.reset();
        while (stream.incrementToken()) {
            termAttr.fillBytesRef();
        }
        stream.close();
        BytesRefBuilder b = new BytesRefBuilder();
        b.append(bytesRef);
        return b.get();
    }
}
