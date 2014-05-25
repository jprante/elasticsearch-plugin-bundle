package org.xbib.elasticsearch.index.analysis.sortform;

import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.ImmutableSettings;
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
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.testng.annotations.Test;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;

public class SimpleSortFormTests  extends BaseTokenStreamTest {

    @Test
    public void testBasicUsage() throws Exception {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("index.analysis.analyzer.myanalyzer.type", "sortform")
                //.put("index.analysis.analyzer.myanalyzer.pattern", "<<.*?>>\\s*")
                .build();
        AnalysisService analysisService = createAnalysisService(settings);
        NamedAnalyzer myanalyzer = analysisService.analyzer("myanalyzer");
        assertAnalyzesTo(myanalyzer, "<<Der>> Titel des Buches", new String[]{"Titel des Buches"});
    }

    @Test
    public void testMABUsage() throws Exception {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("index.analysis.analyzer.myanalyzer.type", "sortform")
                .build();
        AnalysisService analysisService = createAnalysisService(settings);
        NamedAnalyzer myanalyzer = analysisService.analyzer("myanalyzer");
        assertAnalyzesTo(myanalyzer, "\u0098Der\u009c Titel des Buches", new String[]{"Titel des Buches"});
    }

    @Test
    public void testDateUsage() throws Exception {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("index.analysis.analyzer.myanalyzer.type", "sortform")
                .put("index.analysis.analyzer.myanalyzer.pattern", "[^0-9]")
                .build();
        AnalysisService analysisService = createAnalysisService(settings);
        NamedAnalyzer myanalyzer = analysisService.analyzer("myanalyzer");
        assertAnalyzesTo(myanalyzer, "ersch. ca. 1988", new String[]{"1988"});
    }

    private AnalysisService createAnalysisService(Settings settings) {
        Index index = new Index("test");
        Injector parentInjector = new ModulesBuilder()
                .add(new SettingsModule(settings),
                        new EnvironmentModule(new Environment(settings)),
                        new IndicesAnalysisModule()).createInjector();
        AnalysisModule analysisModule = new AnalysisModule(settings,
                parentInjector.getInstance(IndicesAnalysisService.class));
        analysisModule.addAnalyzer("sortform", SortformAnalyzerProvider.class);
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                analysisModule)
           .createChildInjector(parentInjector);
        return injector.getInstance(AnalysisService.class);
    }
}
