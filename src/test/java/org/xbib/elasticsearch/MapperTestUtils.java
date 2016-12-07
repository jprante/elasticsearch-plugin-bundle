package org.xbib.elasticsearch;

import org.elasticsearch.Version;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.mapper.AllFieldMapper;
import org.elasticsearch.index.mapper.BinaryFieldMapper;
import org.elasticsearch.index.mapper.BooleanFieldMapper;
import org.elasticsearch.index.mapper.CompletionFieldMapper;
import org.elasticsearch.index.mapper.DateFieldMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.IdFieldMapper;
import org.elasticsearch.index.mapper.IndexFieldMapper;
import org.elasticsearch.index.mapper.IpFieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.NumberFieldMapper;
import org.elasticsearch.index.mapper.ObjectMapper;
import org.elasticsearch.index.mapper.ParentFieldMapper;
import org.elasticsearch.index.mapper.RoutingFieldMapper;
import org.elasticsearch.index.mapper.SourceFieldMapper;
import org.elasticsearch.index.mapper.StringFieldMapper;
import org.elasticsearch.index.mapper.TTLFieldMapper;
import org.elasticsearch.index.mapper.TimestampFieldMapper;
import org.elasticsearch.index.mapper.TokenCountFieldMapper;
import org.elasticsearch.index.mapper.TypeFieldMapper;
import org.elasticsearch.index.mapper.UidFieldMapper;
import org.elasticsearch.index.mapper.VersionFieldMapper;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.indices.mapper.MapperRegistry;
import org.xbib.elasticsearch.index.mapper.crypt.CryptMapper;
import org.xbib.elasticsearch.index.mapper.langdetect.LangdetectMapper;
import org.xbib.elasticsearch.index.mapper.reference.ReferenceMapper;
import org.xbib.elasticsearch.index.mapper.reference.ReferenceMapperTypeParser;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberMapper;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberMapperTypeParser;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberService;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapperTestUtils {

    public static AnalysisService newAnalysisService(Settings indexSettings) {
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(indexSettings),
                new EnvironmentModule(new Environment(indexSettings))).createInjector();
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

    public static DocumentMapperParser newDocumentMapperParser() {
        return newDocumentMapperParser(Settings.builder()
                .put("path.home", System.getProperty("path.home"))
                .build());
    }

    public static DocumentMapperParser newDocumentMapperParser(Settings settings) {
        Settings forcedSettings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(settings)
                .build();
        SimilarityLookupService similarityLookupService = newSimilarityLookupService(forcedSettings);
        Map<String, Mapper.TypeParser> mappers = registerBuiltInMappers();
        mappers.put(LangdetectMapper.CONTENT_TYPE, new LangdetectMapper.TypeParser());
        mappers.put(CryptMapper.CONTENT_TYPE, new CryptMapper.TypeParser());
        Map<String, MetadataFieldMapper.TypeParser> metadataMappers = registerBuiltInMetadataMappers();
        MapperRegistry mapperRegistry = new MapperRegistry(mappers, metadataMappers);
        MapperService mapperService = new MapperService(new Index("test"),
                forcedSettings,
                newAnalysisService(forcedSettings),
                similarityLookupService,
                null,
                mapperRegistry);
        return new DocumentMapperParser(
                forcedSettings,
                mapperService,
                newAnalysisService(forcedSettings),
                similarityLookupService,
                null,
                mapperRegistry);
    }

    public static MapperService newMapperService(Settings settings, Client client) throws IOException {
        Settings indexSettings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .put("client.type", "node")
                .put(settings)
                .build();
        Index index = new Index("test");
        Injector parentInjector = new ModulesBuilder()
                .add(new SettingsModule(indexSettings),
                new EnvironmentModule(new Environment(indexSettings)))
                .createInjector();
        AnalysisModule analysisModule = new AnalysisModule(indexSettings,
                parentInjector.getInstance(IndicesAnalysisService.class));
        new BundlePlugin(settings).onModule(analysisModule);
        Injector injector = new ModulesBuilder().add(new IndexSettingsModule(index, indexSettings),
                new IndexNameModule(index),
                analysisModule)
                .createChildInjector(parentInjector);
        AnalysisService analysisService = injector.getInstance(AnalysisService.class);
        SimilarityLookupService similarityLookupService = new SimilarityLookupService(index, indexSettings);
        Map<String, Mapper.TypeParser> mappers = registerBuiltInMappers();
        mappers.put(LangdetectMapper.CONTENT_TYPE, new LangdetectMapper.TypeParser());
        mappers.put(CryptMapper.CONTENT_TYPE, new CryptMapper.TypeParser());
        StandardnumberMapperTypeParser standardnumberMapperTypeParser = new StandardnumberMapperTypeParser();
        standardnumberMapperTypeParser.setService(injector.getInstance(StandardnumberService.class));
        mappers.put(StandardnumberMapper.CONTENT_TYPE, standardnumberMapperTypeParser);
        if (client != null) {
            ReferenceMapperTypeParser referenceMapperTypeParser = new ReferenceMapperTypeParser();
            referenceMapperTypeParser.setClient(client);
            mappers.put(ReferenceMapper.CONTENT_TYPE, referenceMapperTypeParser);
        }
        Map<String, MetadataFieldMapper.TypeParser> metadataMappers = registerBuiltInMetadataMappers();
        MapperRegistry mapperRegistry = new MapperRegistry(mappers, metadataMappers);
        return new MapperService(new Index("test"),
                indexSettings,
                analysisService,
                similarityLookupService,
                null,
                mapperRegistry);
    }

    public static AnalysisService analysisService() {
        Settings settings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .put("client.type", "node")
                .build();
        return newMapperService(settings, null).analysisService();
    }

    public static AnalysisService analysisService(Settings settings) {
        Settings newSettings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .put("client.type", "node")
                .put(settings)
                .build();
        return newMapperService(newSettings, null).analysisService();
    }

    public static AnalysisService analysisService(String resource) throws IOException {
        Settings settings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .put("client.type", "node")
                .loadFromStream(resource, MapperTestUtils.class.getClassLoader().getResource(resource).openStream())
                .build();
        return newMapperService(settings, null).analysisService();
    }

    // copy from org.elasticsearch.indices.IndicesModule
    private static Map<String, Mapper.TypeParser> registerBuiltInMappers() {
        Map<String, Mapper.TypeParser> mapperParsers = new LinkedHashMap<>();
        mapperParsers.put(NumberFieldMapper.NumberType.BYTE.typeName(), new NumberFieldMapper.TypeParser(NumberFieldMapper.NumberType.BYTE));
        mapperParsers.put(NumberFieldMapper.NumberType.SHORT.typeName(), new NumberFieldMapper.TypeParser(NumberFieldMapper.NumberType.SHORT));
        mapperParsers.put(NumberFieldMapper.NumberType.INTEGER.typeName(), new NumberFieldMapper.TypeParser(NumberFieldMapper.NumberType.INTEGER));
        mapperParsers.put(NumberFieldMapper.NumberType.LONG.typeName(), new NumberFieldMapper.TypeParser(NumberFieldMapper.NumberType.LONG));
        mapperParsers.put(NumberFieldMapper.NumberType.FLOAT.typeName(), new NumberFieldMapper.TypeParser(NumberFieldMapper.NumberType.FLOAT));
        mapperParsers.put(NumberFieldMapper.NumberType.DOUBLE.typeName(), new NumberFieldMapper.TypeParser(NumberFieldMapper.NumberType.DOUBLE));
        mapperParsers.put(BooleanFieldMapper.CONTENT_TYPE, new BooleanFieldMapper.TypeParser());
        mapperParsers.put(BinaryFieldMapper.CONTENT_TYPE, new BinaryFieldMapper.TypeParser());
        mapperParsers.put(DateFieldMapper.CONTENT_TYPE, new DateFieldMapper.TypeParser());
        mapperParsers.put(IpFieldMapper.CONTENT_TYPE, new IpFieldMapper.TypeParser());
        mapperParsers.put(StringFieldMapper.CONTENT_TYPE, new StringFieldMapper.TypeParser());
        mapperParsers.put(TokenCountFieldMapper.CONTENT_TYPE, new TokenCountFieldMapper.TypeParser());
        mapperParsers.put(ObjectMapper.CONTENT_TYPE, new ObjectMapper.TypeParser());
        mapperParsers.put(ObjectMapper.NESTED_CONTENT_TYPE, new ObjectMapper.TypeParser());
        //mapperParsers.put(TypeParsers.MULTI_FIELD_CONTENT_TYPE, TypeParsers.multiFieldConverterTypeParser);
        mapperParsers.put(CompletionFieldMapper.CONTENT_TYPE, new CompletionFieldMapper.TypeParser());
        mapperParsers.put(GeoPointFieldMapper.CONTENT_TYPE, new GeoPointFieldMapper.TypeParser());
        return mapperParsers;
    }

    // copy from org.elasticsearch.indices.IndicesModule
    private static Map<String, MetadataFieldMapper.TypeParser> registerBuiltInMetadataMappers() {
        Map<String, MetadataFieldMapper.TypeParser> metadataMapperParsers = new LinkedHashMap<>();
        metadataMapperParsers.put(UidFieldMapper.NAME, new UidFieldMapper.TypeParser());
        metadataMapperParsers.put(IdFieldMapper.NAME, new IdFieldMapper.TypeParser());
        metadataMapperParsers.put(RoutingFieldMapper.NAME, new RoutingFieldMapper.TypeParser());
        metadataMapperParsers.put(IndexFieldMapper.NAME, new IndexFieldMapper.TypeParser());
        metadataMapperParsers.put(SourceFieldMapper.NAME, new SourceFieldMapper.TypeParser());
        metadataMapperParsers.put(TypeFieldMapper.NAME, new TypeFieldMapper.TypeParser());
        metadataMapperParsers.put(AllFieldMapper.NAME, new AllFieldMapper.TypeParser());
        metadataMapperParsers.put(TimestampFieldMapper.NAME, new TimestampFieldMapper.TypeParser());
        metadataMapperParsers.put(TTLFieldMapper.NAME, new TTLFieldMapper.TypeParser());
        metadataMapperParsers.put(VersionFieldMapper.NAME, new VersionFieldMapper.TypeParser());
        metadataMapperParsers.put(ParentFieldMapper.NAME, new ParentFieldMapper.TypeParser());
        return metadataMapperParsers;
    }
}
