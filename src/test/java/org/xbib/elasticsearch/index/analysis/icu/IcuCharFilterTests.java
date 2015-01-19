package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.CharFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
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
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.plugin.analysis.bundle.BundlePlugin;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class IcuCharFilterTests extends Assert {

    @Test
    public void testFoldingCharfilter() throws IOException {
        String source = "Jörg Prante";
        AnalysisService analysisService = createAnalysisService();
        Reader charFilter = analysisService.charFilter("my_icu_folder").create(new StringReader(source));
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = charFilter.read()) != -1) {
            sb.append((char)ch);
        }
        assertEquals("jorg prante", sb.toString());
    }

    private AnalysisService createAnalysisService() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .loadFromClasspath("org/xbib/elasticsearch/index/analysis/icu/icu_folding.json").build();
        Index index = new Index("test");
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)),
                new IndicesAnalysisModule())
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
