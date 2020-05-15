package org.xbib.elasticsearch.plugin.bundle.test.index.mapper.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.SuppressForbidden;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.After;
import org.junit.Before;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;

/**
 * Reference mapping tests.
 */
public class ReferenceMappingTests extends ESSingleNodeTestCase {

    private static final Logger logger = LogManager.getLogger(ReferenceMappingTests.class.getName());

    /** The plugin classes that should be added to the node. */
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(BundlePlugin.class);
    }

    @Before
    public void setupReferences() throws Exception {
        try {
            client().admin().indices().prepareDelete("test").execute().actionGet();
        } catch (Exception e) {
            logger.warn("unable to delete 'test' index");
        }
        client().prepareIndex("test", "test", "1234")
                .setSource(jsonBuilder().startObject().array("myfield", "a","b","c").endObject())
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .execute().actionGet();
        try {
            client().admin().indices().prepareDelete("authorities").execute().actionGet();
        } catch (Exception e) {
            logger.warn("unable to delete 'authorities' index");
        }
        client().prepareIndex("authorities", "persons", "1")
                .setSource(jsonBuilder().startObject().field("author", "John Doe").endObject())
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .execute().actionGet();
    }

    @After
    public void destroyReferences() {
        try {
            client().admin().indices().prepareDelete("test").execute().actionGet();
        } catch (Exception e) {
            logger.warn("unable to delete 'test' index");
        }
        try {
            client().admin().indices().prepareDelete("authorities").execute().actionGet();
        } catch (Exception e) {
            logger.warn("unable to delete 'authorities' index");
        }
    }

    public void testRefMappings() throws Exception {
        IndexService indexService = createIndex("some_index", Settings.EMPTY,
                "some_type", getMapping("ref-mapping.json"));
        DocumentMapper docMapper = indexService.mapperService().documentMapper("some_type");
        BytesReference json = BytesReference.bytes(jsonBuilder().startObject()
                .field("someField", "1234")
                .endObject());
        SourceToParse sourceToParse = new SourceToParse("some_index", "some_type", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        assertNotNull(doc);
        for (IndexableField field : doc.getFields()) {
            logger.info("testRefMappings {} = {}", field.name(), field.stringValue());
        }
        assertNotNull(docMapper.mappers().getMapper("someField"));
        assertEquals("1234", doc.getFields("someField")[0].stringValue());
        assertEquals(3, doc.getFields("ref").length);
        assertEquals("a", doc.getFields("ref")[0].stringValue());
        assertEquals("b", doc.getFields("ref")[1].stringValue());
        assertEquals("c", doc.getFields("ref")[2].stringValue());
    }

    public void testRefInDoc() throws Exception {
        IndexService indexService = createIndex("docs", Settings.EMPTY,
                "docs", getMapping("ref-mapping-authorities.json"));
        DocumentMapper docMapper = indexService.mapperService().documentMapper("docs");
        BytesReference json = BytesReference.bytes(jsonBuilder().startObject()
                .field("title", "A title")
                .field("dc.creator", "A creator")
                .field("bib.contributor", "A contributor")
                .field("authorID", "1")
                .endObject());
        SourceToParse sourceToParse = new SourceToParse("docs", "docs", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        for (IndexableField field : doc.getFields()) {
            logger.info("testRefInDoc {} = {}", field.name(), field.stringValue());
        }
        assertEquals(2, doc.getFields("dc.creator").length);
        assertEquals("A creator", doc.getFields("dc.creator")[0].stringValue());
        assertEquals("John Doe", doc.getFields("dc.creator")[1].stringValue());
        assertEquals(2, doc.getFields("bib.contributor").length);
        assertEquals("A contributor", doc.getFields("bib.contributor")[0].stringValue());
        assertEquals("John Doe", doc.getFields("bib.contributor")[1].stringValue());
    }

    public void testRefFromID() throws Exception {
        IndexService indexService = createIndex("docs", Settings.EMPTY,
                "docs", getMapping("ref-mapping-from-id.json"));
        DocumentMapper docMapper = indexService.mapperService().documentMapper("docs");
        BytesReference json = BytesReference.bytes(jsonBuilder().startObject()
                .field("title", "A title")
                .field("authorID", "1")
                .endObject());
        SourceToParse sourceToParse = new SourceToParse("docs", "docs", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        assertEquals(1, doc.getFields("ref").length, 1);
        assertEquals("John Doe", doc.getFields("ref")[0].stringValue());
    }

    public void testSearch() throws Exception {
        try {
            client().admin().indices().prepareDelete("books").execute().actionGet();
        } catch (Exception e) {
            logger.warn("unable to delete index 'books'");
        }
        client().admin().indices().prepareCreate("books")
                .addMapping("test", copyToStringFromClasspath("ref-mapping-books-test.json"), XContentType.JSON)
                .execute().actionGet();
        client().prepareIndex("books", "test", "1")
                .setSource(copyToStringFromClasspath("ref-doc-book.json"), XContentType.JSON)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).execute().actionGet();

        // get mappings
        GetMappingsResponse getMappingsResponse= client().admin().indices().getMappings(new GetMappingsRequest()
                .indices("books")
                .types("test"))
                .actionGet();
        MappingMetaData md = getMappingsResponse.getMappings().get("books").get("test");
        logger.info("mappings={}", md.getSourceAsMap());

        // search in field 1, unreferenced value
        QueryBuilder queryBuilder = matchPhraseQuery("dc.creator", "A creator");
        SearchResponse searchResponse = client().prepareSearch("books")
                .setQuery(queryBuilder)
                .setTrackTotalHits(true)
                .execute().actionGet();
        logger.info("unref hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSourceAsMap());
        }
        assertEquals(1L, searchResponse.getHits().getTotalHits().value);

        // search in field 1, referenced value
        queryBuilder = matchPhraseQuery("dc.creator", "John Doe");
        searchResponse = client().prepareSearch("books")
                .setQuery(queryBuilder)
                .setTrackTotalHits(true)
                .execute().actionGet();
        logger.info("ref hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSourceAsMap());
        }
        assertEquals(1L, searchResponse.getHits().getTotalHits().value);

        // search in field 2, unreferenced value
        queryBuilder = matchPhraseQuery("bib.contributor", "A contributor");
        searchResponse = client().prepareSearch("books")
                .setQuery(queryBuilder)
                .setTrackTotalHits(true)
                .execute().actionGet();
        logger.info("field 2 unref hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSourceAsMap());
        }
        assertEquals(1L, searchResponse.getHits().getTotalHits().value);

        // search in field 2, referenced value
        queryBuilder = matchPhraseQuery("bib.contributor", "John Doe");
        searchResponse = client().prepareSearch("books")
                .setQuery(queryBuilder)
                .setTrackTotalHits(true)
                .execute().actionGet();
        logger.info("field 2 ref hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSourceAsMap());
        }
        assertEquals(1L, searchResponse.getHits().getTotalHits().value);
    }

    @SuppressForbidden(reason = "accessing local resources from classpath")
    private String copyToStringFromClasspath(String path) throws Exception {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), StandardCharsets.UTF_8));
    }

    private XContentBuilder getMapping(String path) throws Exception {
        return XContentFactory.jsonBuilder().map(XContentHelper.convertToMap(JsonXContent.jsonXContent,
                getClass().getResourceAsStream(path), true));
    }
}
