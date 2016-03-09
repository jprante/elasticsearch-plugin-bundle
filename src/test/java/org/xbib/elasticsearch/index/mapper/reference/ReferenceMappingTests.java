package org.xbib.elasticsearch.index.mapper.reference;

import org.apache.lucene.index.IndexableField;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;
import org.xbib.elasticsearch.NodeTestUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;

public class ReferenceMappingTests extends Assert {

    private final static ESLogger logger = ESLoggerFactory.getLogger(ReferenceMappingTests.class.getName());

    private Node node;
    private Client client;
    private DocumentMapperParser mapperParser;

    @Before
    public void setupMapperParser() throws IOException {
        node = NodeTestUtils.createNode();
        client = node.client();
        try {
            client.admin().indices().prepareDelete("test").execute().actionGet();
        } catch (Exception e) {
            logger.warn("unable to delete test index");
        }
        BytesReference json = jsonBuilder().startObject().array("myfield", "a","b","c").endObject().bytes();
        client.prepareIndex("test", "test", "1234").setSource(json).execute().actionGet();
        try {
            client.admin().indices().prepareDelete("authorities").execute().actionGet();
        } catch (Exception e) {
            logger.warn("unable to delete test index");
        }

        json = jsonBuilder().startObject().field("author", "John Doe").endObject().bytes();
        client.prepareIndex("authorities", "persons", "1").setSource(json).execute().actionGet();

        mapperParser = MapperTestUtils.newMapperService(Settings.EMPTY, client).documentMapperParser();
    }

    @After
    public void cleanup() throws IOException {
        NodeTestUtils.releaseNode(node);
    }

    @Test
    public void testRefMappings() throws Exception {
        String mapping = copyToStringFromClasspath("ref-mapping.json");
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
        String json = copyToStringFromClasspath("ref-doc-book.json");
        String mapping = copyToStringFromClasspath("ref-mapping-books-test.json");
        try {
            client.admin().indices().prepareDelete("books").execute().actionGet();
        } catch (Exception e) {
            logger.warn("unable to delete index 'books'");
        }
        client.admin().indices().prepareCreate("books")
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

        // search in field 1, referenced value
        QueryBuilder queryBuilder = matchPhraseQuery("dc.creator", "John Doe");
        SearchResponse searchResponse = client.prepareSearch("books")
                .setQuery(queryBuilder).execute().actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        // search in field 1, unreferenced value
        queryBuilder = matchPhraseQuery("dc.creator", "A creator");
        searchResponse = client.prepareSearch("books")
                .setQuery(queryBuilder).execute().actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        // search in field 2, referenced value
        queryBuilder = matchPhraseQuery("bib.contributor", "John Doe");
        searchResponse = client.prepareSearch("books")
                .setQuery(queryBuilder).execute().actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        // search in field 2, unreferenced value
        queryBuilder = matchPhraseQuery("bib.contributor", "A contributor");
        searchResponse = client.prepareSearch("books")
                .setQuery(queryBuilder).execute().actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());
    }

    private String copyToStringFromClasspath(String path) throws IOException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(getClass().getResource(path).openStream(), "UTF-8");
            return copyToString(reader);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

}
