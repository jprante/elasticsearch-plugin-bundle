package org.xbib.elasticsearch.index.analysis;

import org.elasticsearch.Version;
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
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.xbib.elasticsearch.plugin.analysis.bundle.BundlePlugin;

public class AnalyzerTestUtils {

    public static AnalysisService createAnalysisService() {
        Settings settings = Settings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home")).build();
        return createAnalysisService(settings);
    }

    public static AnalysisService createAnalysisService(String resource) {
        Settings settings = Settings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .loadFromStream(resource, AnalyzerTestUtils.class.getResourceAsStream(resource))
                .build();
        return createAnalysisService(settings);
    }

    public static AnalysisService createAnalysisService(Settings settings) {
        Index index = new Index("test");
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)))
                .createInjector();
        AnalysisModule analysisModule = new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class));
        new BundlePlugin(settings).onModule(analysisModule);
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                analysisModule)
                .createChildInjector(parentInjector);
        return injector.getInstance(AnalysisService.class);
    }
}
