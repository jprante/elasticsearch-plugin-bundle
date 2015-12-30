package org.xbib.elasticsearch;

import org.elasticsearch.Version;
import org.elasticsearch.client.Client;
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
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.core.BinaryFieldMapper;
import org.elasticsearch.index.mapper.core.BooleanFieldMapper;
import org.elasticsearch.index.mapper.core.ByteFieldMapper;
import org.elasticsearch.index.mapper.core.CompletionFieldMapper;
import org.elasticsearch.index.mapper.core.DateFieldMapper;
import org.elasticsearch.index.mapper.core.DoubleFieldMapper;
import org.elasticsearch.index.mapper.core.FloatFieldMapper;
import org.elasticsearch.index.mapper.core.IntegerFieldMapper;
import org.elasticsearch.index.mapper.core.LongFieldMapper;
import org.elasticsearch.index.mapper.core.ShortFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.elasticsearch.index.mapper.core.TokenCountFieldMapper;
import org.elasticsearch.index.mapper.core.TypeParsers;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.internal.AllFieldMapper;
import org.elasticsearch.index.mapper.internal.IdFieldMapper;
import org.elasticsearch.index.mapper.internal.IndexFieldMapper;
import org.elasticsearch.index.mapper.internal.ParentFieldMapper;
import org.elasticsearch.index.mapper.internal.RoutingFieldMapper;
import org.elasticsearch.index.mapper.internal.SourceFieldMapper;
import org.elasticsearch.index.mapper.internal.TTLFieldMapper;
import org.elasticsearch.index.mapper.internal.TimestampFieldMapper;
import org.elasticsearch.index.mapper.internal.TypeFieldMapper;
import org.elasticsearch.index.mapper.internal.UidFieldMapper;
import org.elasticsearch.index.mapper.internal.VersionFieldMapper;
import org.elasticsearch.index.mapper.ip.IpFieldMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.index.similarity.SimilarityLookupService;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.indices.mapper.MapperRegistry;
import org.xbib.elasticsearch.index.mapper.crypt.CryptMapper;
import org.xbib.elasticsearch.index.mapper.langdetect.LangdetectMapper;
import org.xbib.elasticsearch.index.mapper.reference.ReferenceMapper;
import org.xbib.elasticsearch.index.mapper.reference.ReferenceMapperTypeParser;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberMapper;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberMapperTypeParser;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberService;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapperTestUtils {

    public static DocumentMapperParser newMapperParser() {
        return newMapperParser(Settings.EMPTY, null);
    }

    public static DocumentMapperParser newMapperParser(Client client) {
        return newMapperParser(Settings.EMPTY, client);
    }

    public static DocumentMapperParser newMapperParser(Settings settings, Client client) {
        return newMapper(settings, client).documentMapperParser();
    }

    public static MapperService newMapper(Settings settings, Client client) {
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
        Settings settings = Settings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .put("client.type", "node")
                .build();
        return newMapper(settings, null).analysisService();
    }

    public static AnalysisService analysisService(Settings settings) {
        Settings newSettings = Settings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .put("client.type", "node")
                .put(settings)
                .build();
        return newMapper(newSettings, null).analysisService();
    }

    public static AnalysisService analysisService(String resource) {
        Settings settings = Settings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .put("client.type", "node")
                .loadFromStream(resource, MapperTestUtils.class.getResourceAsStream(resource))
                .build();
        return newMapper(settings, null).analysisService();
    }

    // copy from org.elasticsearch.indices.IndicesModule
    private static Map<String, Mapper.TypeParser> registerBuiltInMappers() {
        Map<String, Mapper.TypeParser> mapperParsers = new LinkedHashMap<>();
        mapperParsers.put(ByteFieldMapper.CONTENT_TYPE, new ByteFieldMapper.TypeParser());
        mapperParsers.put(ShortFieldMapper.CONTENT_TYPE, new ShortFieldMapper.TypeParser());
        mapperParsers.put(IntegerFieldMapper.CONTENT_TYPE, new IntegerFieldMapper.TypeParser());
        mapperParsers.put(LongFieldMapper.CONTENT_TYPE, new LongFieldMapper.TypeParser());
        mapperParsers.put(FloatFieldMapper.CONTENT_TYPE, new FloatFieldMapper.TypeParser());
        mapperParsers.put(DoubleFieldMapper.CONTENT_TYPE, new DoubleFieldMapper.TypeParser());
        mapperParsers.put(BooleanFieldMapper.CONTENT_TYPE, new BooleanFieldMapper.TypeParser());
        mapperParsers.put(BinaryFieldMapper.CONTENT_TYPE, new BinaryFieldMapper.TypeParser());
        mapperParsers.put(DateFieldMapper.CONTENT_TYPE, new DateFieldMapper.TypeParser());
        mapperParsers.put(IpFieldMapper.CONTENT_TYPE, new IpFieldMapper.TypeParser());
        mapperParsers.put(StringFieldMapper.CONTENT_TYPE, new StringFieldMapper.TypeParser());
        mapperParsers.put(TokenCountFieldMapper.CONTENT_TYPE, new TokenCountFieldMapper.TypeParser());
        mapperParsers.put(ObjectMapper.CONTENT_TYPE, new ObjectMapper.TypeParser());
        mapperParsers.put(ObjectMapper.NESTED_CONTENT_TYPE, new ObjectMapper.TypeParser());
        mapperParsers.put(TypeParsers.MULTI_FIELD_CONTENT_TYPE, TypeParsers.multiFieldConverterTypeParser);
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
