package org.xbib.elasticsearch.index.mapper.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexableField;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.xbib.elasticsearch.MapperTestUtils.newDocumentMapperParser;

/**
 *
 */
public class ReferenceMappingTests extends NodeTestUtils {

    private static final Logger logger = LogManager.getLogger(ReferenceMappingTests.class.getName());

    @Before
    public void setupReferences() throws IOException {
        startCluster();
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
    public void cleanup() throws IOException {
        stopCluster();
    }

    @Test
    public void testRefMappings() throws Exception {
        String mapping = copyToStringFromClasspath("ref-mapping.json");
        DocumentMapperParser mapperParser = newDocumentMapperParser("someIndex");
        DocumentMapper docMapper = mapperParser.parse("someType", new CompressedXContent(mapping));
        BytesReference json = jsonBuilder().startObject()
                .field("someField", "1234")
                .endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertNotNull(doc);
        for (IndexableField field : doc.getFields()) {
            logger.info("testRefMappings {} = {}", field.name(), field.stringValue());
        }
        assertNotNull(docMapper.mappers().smartNameFieldMapper("someField"));
        assertEquals("1234", doc.getFields("someField")[0].stringValue());
        assertEquals(3, doc.getFields("ref").length);
        assertEquals("a", doc.getFields("ref")[0].stringValue());
        assertEquals("b", doc.getFields("ref")[1].stringValue());
        assertEquals("c", doc.getFields("ref")[2].stringValue());

        // re-parse from mapping
        String builtMapping = docMapper.mappingSource().string();
        docMapper = mapperParser.parse("someType", new CompressedXContent(builtMapping));
        json = jsonBuilder().startObject()
                .field("someField", "1234")
                .endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        for (IndexableField field : doc.getFields()) {
            logger.info("reparse testRefMappings {} = {}", field.name(), field.stringValue());
        }
        assertEquals("1234", doc.getFields("someField")[0].stringValue());
        assertEquals(3, doc.getFields("ref").length);
        assertEquals("a", doc.getFields("ref")[0].stringValue());
        assertEquals("b", doc.getFields("ref")[1].stringValue());
        assertEquals("c", doc.getFields("ref")[2].stringValue());
    }

    @Test
    public void testRefInDoc() throws Exception {
        String mapping = copyToStringFromClasspath("ref-mapping-authorities.json");
        DocumentMapperParser mapperParser = newDocumentMapperParser("docs");
        DocumentMapper docMapper = mapperParser.parse("docs", new CompressedXContent(mapping));
        BytesReference json = jsonBuilder().startObject()
                .field("title", "A title")
                .field("dc.creator", "A creator")
                .field("bib.contributor", "A contributor")
                .field("authorID", "1")
                .endObject().bytes();
        ParseContext.Document doc = docMapper.parse("docs", "docs", "1", json).rootDoc();
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

    @Test
    public void testRefFromID() throws Exception {
        String mapping = copyToStringFromClasspath("ref-mapping-from-id.json");
        DocumentMapperParser mapperParser = newDocumentMapperParser("docs");
        DocumentMapper docMapper = mapperParser.parse("docs", new CompressedXContent(mapping));
        BytesReference json = jsonBuilder().startObject()
                .field("title", "A title")
                .field("authorID", "1")
                .endObject().bytes();
        ParseContext.Document doc = docMapper.parse("docs", "docs", "1", json).rootDoc();
        assertEquals(1, doc.getFields("ref").length, 1);
        assertEquals("John Doe", doc.getFields("ref")[0].stringValue());
    }

    @Test
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
                .setQuery(queryBuilder).execute().actionGet();
        logger.info("unref hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        // search in field 1, referenced value
        queryBuilder = matchPhraseQuery("dc.creator", "John Doe");
        searchResponse = client().prepareSearch("books")
                .setQuery(queryBuilder).execute().actionGet();
        logger.info("ref hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        // search in field 2, unreferenced value
        queryBuilder = matchPhraseQuery("bib.contributor", "A contributor");
        searchResponse = client().prepareSearch("books")
                .setQuery(queryBuilder).execute().actionGet();
        logger.info("field 2 unref hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        // search in field 2, referenced value
        queryBuilder = matchPhraseQuery("bib.contributor", "John Doe");
        searchResponse = client().prepareSearch("books")
                .setQuery(queryBuilder).execute().actionGet();
        logger.info("field 2 ref hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());
    }

    private String copyToStringFromClasspath(String path) throws IOException {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), StandardCharsets.UTF_8));
    }
}
