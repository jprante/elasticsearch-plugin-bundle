package org.xbib.elasticsearch.index.mapper;

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
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.index.similarity.SimilarityLookupService;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;

import java.nio.file.Path;

public class MapperTestUtils {

    public static MapperService newMapperService() {
        return newMapperService(new Index("test"), Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .build());
    }

    public static MapperService newMapperService(Index index, Settings indexSettings) {
        return new MapperService(index,
                indexSettings,
                newAnalysisService(indexSettings),
                newSimilarityLookupService(indexSettings),
                null);
    }

    public static AnalysisService newAnalysisService(Path tempDir) {
        return newAnalysisService(Settings.builder()
                .put("path.home", tempDir)
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .build());
    }

    public static AnalysisService newAnalysisService(Settings indexSettings) {
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(indexSettings), new EnvironmentModule(new Environment(indexSettings)), new IndicesAnalysisModule()).createInjector();
        Index index = new Index("test");
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, indexSettings),
                new IndexNameModule(index),
                new AnalysisModule(indexSettings, parentInjector.getInstance(IndicesAnalysisService.class))).createChildInjector(parentInjector);

        return injector.getInstance(AnalysisService.class);
    }

    public static SimilarityLookupService newSimilarityLookupService(Settings indexSettings) {
        return new SimilarityLookupService(new Index("test"), indexSettings);
    }

    public static DocumentMapperParser newMapperParser() {
        return newMapperParser(Settings.builder()
                .put("path.home", System.getProperty("path.home"))
                .build());
    }

    public static DocumentMapperParser newMapperParser(Settings settings) {
        Settings forcedSettings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(settings)
                .build();
        SimilarityLookupService similarityLookupService = newSimilarityLookupService(forcedSettings);
        MapperService mapperService = new MapperService(new Index("test"),
                forcedSettings,
                newAnalysisService(forcedSettings),
                similarityLookupService,
                null);
        return new DocumentMapperParser(
                forcedSettings,
                mapperService,
                MapperTestUtils.newAnalysisService(forcedSettings),
                similarityLookupService,
                null);
    }
}
