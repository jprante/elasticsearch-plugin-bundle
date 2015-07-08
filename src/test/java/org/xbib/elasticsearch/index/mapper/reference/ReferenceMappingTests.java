package org.xbib.elasticsearch.index.mapper.reference;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.IndexableField;
import org.elasticsearch.Version;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.base.Charsets;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.AnalyzerProviderFactory;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.PreBuiltAnalyzerProviderFactory;
import org.elasticsearch.index.codec.docvaluesformat.DocValuesFormatService;
import org.elasticsearch.index.codec.postingsformat.PostingsFormatService;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.similarity.SimilarityLookupService;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ReferenceMappingTests extends Assert {

    private final static ESLogger logger = ESLoggerFactory.getLogger(ReferenceMappingTests.class.getName());

    private static DocumentMapperParser mapperParser;

    private static Node node;

    private static Client client;

    @BeforeClass
    public static void setupMapperParser() throws IOException {
        Settings nodeSettings = ImmutableSettings.settingsBuilder()
                .put("gateway.type", "none")
                .put("index.store.type", "memory")
                .put("index.number_of_shards", 1)
                .put("index.number_of_replica", 0)
                .put("cluster.routing.schedule", "50ms")
                .build();
        node = NodeBuilder.nodeBuilder().settings(nodeSettings).local(true).build().start();
        client = node.client();
        BytesReference json = jsonBuilder().startObject().array("myfield", "a","b","c").endObject().bytes();
        client.prepareIndex("test", "test", "1234").setSource(json).execute().actionGet();
        json = jsonBuilder().startObject().field("author", "John Doe").endObject().bytes();
        client.prepareIndex("authorities", "persons", "1").setSource(json).execute().actionGet();
        Index index = new Index("test");
        Map<String, AnalyzerProviderFactory> analyzerFactoryFactories = Maps.newHashMap();
        analyzerFactoryFactories.put("keyword",
                new PreBuiltAnalyzerProviderFactory("keyword", AnalyzerScope.INDEX, new KeywordAnalyzer()));
        Settings settings = ImmutableSettings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .build();
        AnalysisService analysisService = new AnalysisService(index, settings, null, analyzerFactoryFactories, null, null, null);
        mapperParser = new DocumentMapperParser(index, settings,
                analysisService,
                new PostingsFormatService(index),
                new DocValuesFormatService(index),
                new SimilarityLookupService(index, settings),
                null);
    }

    @AfterClass
    public static void shutdown() {
        client.close();
        node.close();
    }

    @Test
    public void testRefMappings() throws Exception {
        mapperParser.putTypeParser(ReferenceMapper.CONTENT_TYPE, new ReferenceMapper.TypeParser(client));
        String mapping = copyToStringFromClasspath("ref-mapping.json");
        DocumentMapper docMapper = mapperParser.parse(mapping);
        BytesReference json = jsonBuilder().startObject()
                .field("_id", 1)
                .field("someField", "1234")
                .endObject().bytes();
        ParseContext.Document doc = docMapper.parse(json).rootDoc();
        assertNotNull(doc);
        assertNotNull(docMapper.mappers().smartName("someField"));
        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), "1234");
        assertEquals(doc.getFields("someField.ref").length, 3);
        assertEquals(doc.getFields("someField.ref")[0].stringValue(), "a");
        assertEquals(doc.getFields("someField.ref")[1].stringValue(), "b");
        assertEquals(doc.getFields("someField.ref")[2].stringValue(), "c");

        // re-parse from mapping
        String builtMapping = docMapper.mappingSource().string();
        docMapper = mapperParser.parse(builtMapping);

        json = jsonBuilder().startObject().field("_id", 1).field("someField", "1234").endObject().bytes();
        doc = docMapper.parse(json).rootDoc();

        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), "1234");
        assertEquals(doc.getFields("someField.ref").length, 3);
        assertEquals(doc.getFields("someField.ref")[0].stringValue(), "a");
        assertEquals(doc.getFields("someField.ref")[1].stringValue(), "b");
        assertEquals(doc.getFields("someField.ref")[2].stringValue(), "c");
    }

    @Test
    public void testRefInDoc() throws Exception {
        mapperParser.putTypeParser(ReferenceMapper.CONTENT_TYPE, new ReferenceMapper.TypeParser(client));
        String mapping = copyToStringFromClasspath("ref-mapping-authorities.json");
        DocumentMapper docMapper = mapperParser.parse(mapping);
        BytesReference json = jsonBuilder().startObject()
                .field("_id", 1)
                .field("title", "A title")
                .field("dc.creator", "A creator")
                .field("authorID", "1")
                .field("bib.contributor", "A contributor")
                .endObject().bytes();
        ParseContext.Document doc = docMapper.parse(json).rootDoc();
        for (IndexableField field : doc.getFields()) {
            logger.info("{} = {}", field.name(), field.stringValue());
        }
        assertEquals(doc.getFields("dc.creator").length, 2);
        assertEquals(doc.getFields("dc.creator")[0].stringValue(), "A creator");
        assertEquals(doc.getFields("dc.creator")[1].stringValue(), "John Doe");
        assertEquals(doc.getFields("bib.contributor").length, 2);
        assertEquals(doc.getFields("bib.contributor")[0].stringValue(), "John Doe");
        assertEquals(doc.getFields("bib.contributor")[1].stringValue(), "A contributor");
    }

    @Test
    public void testRefFromID() throws Exception {
        mapperParser.putTypeParser(ReferenceMapper.CONTENT_TYPE, new ReferenceMapper.TypeParser(client));
        String mapping = copyToStringFromClasspath("ref-mapping-from-id.json");
        DocumentMapper docMapper = mapperParser.parse(mapping);
        BytesReference json = jsonBuilder().startObject()
                .field("_id", 1)
                .field("title", "A title")
                .field("authorID", "1")
                .endObject().bytes();
        ParseContext.Document doc = docMapper.parse(json).rootDoc();
        assertEquals(doc.getFields("ref").length, 1);
        assertEquals(doc.getFields("ref")[0].stringValue(), "John Doe");
    }

    @Test
    public void testSearch() throws Exception {
        String json = copyToStringFromClasspath("ref-doc-book.json");
        String mapping = copyToStringFromClasspath("ref-mapping-books-test.json");
        client.admin().indices().prepareCreate("books")
                .setIndex("books")
                .addMapping("test", mapping)
                .execute().actionGet();
        client.prepareIndex("books", "test", "1").setSource(json).setRefresh(true).execute().actionGet();

        // get mappings
        GetMappingsResponse getMappingsResponse= client.admin().indices().getMappings(new GetMappingsRequest()
                .indices("books")
                .types("test"))
                .actionGet();
        MappingMetaData md = getMappingsResponse.getMappings().get("books").get("test");
        logger.info("mappings={}", md.getSourceAsMap());

        // search in field 1
        SearchResponse searchResponse = client.search(new SearchRequest()
                .indices("books")
                .types("test")
                .extraSource("{\"query\":{\"match\":{\"dc.creator\":\"John Doe\"}}}"))
                .actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(searchResponse.getHits().getTotalHits(), 1);

        // search in field 1, unreferenced value
        searchResponse = client.search(new SearchRequest()
                .indices("books")
                .types("test")
                .extraSource("{\"query\":{\"match\":{\"dc.creator\":\"A creator\"}}}"))
                .actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(searchResponse.getHits().getTotalHits(), 1);

        // search in field 2, referenced value
        searchResponse = client.search(new SearchRequest()
                .indices("books")
                .types("test")
                .extraSource("{\"query\":{\"match\":{\"bib.contributor\":\"John Doe\"}}}"))
                .actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(searchResponse.getHits().getTotalHits(), 1);

        // search in field 2, unreferenced value
        searchResponse = client.search(new SearchRequest()
                .indices("books")
                .types("test")
                .extraSource("{\"query\":{\"match\":{\"bib.contributor\":\"A contributor\"}}}"))
                .actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(searchResponse.getHits().getTotalHits(), 1);
    }

    public String copyToStringFromClasspath(String path) throws IOException {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), Charsets.UTF_8));
    }
}
