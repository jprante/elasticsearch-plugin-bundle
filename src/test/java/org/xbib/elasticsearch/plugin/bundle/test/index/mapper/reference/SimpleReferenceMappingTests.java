package org.xbib.elasticsearch.plugin.bundle.test.index.mapper.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.util.SuppressForbidden;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.testframework.ESSingleNodeTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

/**
 * Simple reference mapping tests.
 */
public class SimpleReferenceMappingTests extends ESSingleNodeTestCase {

    private static final Logger logger = LogManager.getLogger(SimpleReferenceMappingTests.class.getName());

    /** The plugin classes that should be added to the node. */
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(BundlePlugin.class);
    }

    public void testSimpleRef() throws Exception {
        try {
            client().admin().indices().prepareDelete("ref").execute().actionGet();
        } catch (Exception e) {
            logger.warn("can not delete index ref");
        }
        client().admin().indices().prepareCreate("ref")
                .setSettings(copyToStringFromClasspath("ref-simple-settings.json"), XContentType.JSON)
                .addMapping("ref", copyToStringFromClasspath("ref-simple-mapping.json"), XContentType.JSON)
                .execute().actionGet();
        client().prepareIndex("ref", "ref", "1")
                .setSource(copyToStringFromClasspath("ref-simple-document.json"), XContentType.JSON)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).execute().actionGet();

        try {
            client().admin().indices().prepareDelete("doc").execute().actionGet();
        } catch (Exception e) {
            logger.warn("can not delete index doc");
        }
        client().admin().indices().prepareCreate("doc")
                .setSettings(copyToStringFromClasspath("doc-simple-settings.json"), XContentType.JSON)
                .addMapping("doc", copyToStringFromClasspath("doc-simple-mapping.json"), XContentType.JSON)
                .execute().actionGet();
        client().prepareIndex("doc", "doc", "1")
                .setSource(copyToStringFromClasspath("doc-simple-document.json"), XContentType.JSON)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).execute().actionGet();

        // search for "first"
        QueryBuilder queryBuilder = matchQuery("dc.creator", "first");
        SearchResponse searchResponse = client().prepareSearch("doc")
                .setQuery(queryBuilder)
                .execute().actionGet();
        logger.info("first query, hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSourceAsMap());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        // search for "second" which comes from ref
        queryBuilder = matchQuery("dc.creator", "second");
        searchResponse = client().prepareSearch("doc")
                .setQuery(queryBuilder)
                .execute().actionGet();
        logger.info("second query, hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSourceAsMap());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());
    }

    @SuppressForbidden(reason = "accessing local resources from classpath")
    private String copyToStringFromClasspath(String path) throws Exception {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), StandardCharsets.UTF_8));
    }
}
