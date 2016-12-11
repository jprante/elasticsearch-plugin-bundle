package org.xbib.elasticsearch.index.mapper.reference.simple;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class SimpleReferenceMappingTests extends NodeTestUtils {

    private static final Logger logger = LogManager.getLogger(SimpleReferenceMappingTests.class.getName());

    @Test
    public void testSimpleRef() throws IOException {
        Node node = startNode();
        Client client = node.client();

        try {
            client.admin().indices().prepareDelete("ref").execute().actionGet();
        } catch (Exception e) {
            logger.warn("can not delete index ref");
        }
        client.admin().indices().prepareCreate("ref")
                .setSettings(copyToStringFromClasspath("ref-simple-settings.json"))
                .addMapping("ref", copyToStringFromClasspath("ref-simple-mapping.json"))
                .execute().actionGet();
        client.prepareIndex("ref", "ref", "1")
                .setSource(copyToStringFromClasspath("ref-simple-document.json"))
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).execute().actionGet();

        try {
            client.admin().indices().prepareDelete("doc").execute().actionGet();
        } catch (Exception e) {
            logger.warn("can not delete index doc");
        }
        client.admin().indices().prepareCreate("doc")
                .setSettings(copyToStringFromClasspath("doc-simple-settings.json"))
                .addMapping("doc", copyToStringFromClasspath("doc-simple-mapping.json"))
                .execute().actionGet();
        client.prepareIndex("doc", "doc", "1")
                .setSource(copyToStringFromClasspath("doc-simple-document.json"))
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).execute().actionGet();

        // search for "first"
        QueryBuilder queryBuilder = matchQuery("dc.creator", "first");
        SearchResponse searchResponse = client.prepareSearch("doc")
                .setQuery(queryBuilder)
                .execute().actionGet();
        logger.info("first query, hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        // search for "second"
        queryBuilder = matchQuery("dc.creator", "second");
        searchResponse = client.prepareSearch("doc")
                .setQuery(queryBuilder)
                .execute().actionGet();
        logger.info("second query, hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        client.close();
        node.close();
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
