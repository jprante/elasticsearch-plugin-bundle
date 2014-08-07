package org.xbib.elasticsearch.index.analysis.combo;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
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
import org.junit.Test;

import static org.elasticsearch.common.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleComboAnalysisTests {

    @Test
    public void testDefaultComboAnalysis() {
        Index index = new Index("test");
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(EMPTY_SETTINGS),
                new EnvironmentModule(new Environment(EMPTY_SETTINGS)),
                new IndicesAnalysisModule()).createInjector();
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, EMPTY_SETTINGS),
                new IndexNameModule(index),
                new AnalysisModule(EMPTY_SETTINGS, parentInjector.getInstance(IndicesAnalysisService.class))
                        .addProcessor(new ComboAnalysisBinderProcessor()))
                .createChildInjector(parentInjector);

        AnalysisService analysisService = injector.getInstance(AnalysisService.class);

        Analyzer analyzer = analysisService.analyzer("combo").analyzer();
        assertThat(analyzer, instanceOf(ComboAnalyzerWrapper.class));

    }
}
