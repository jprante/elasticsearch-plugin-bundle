package org.xbib.elasticsearch.index.mapper.reference.gnd;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.plugin.analysis.bundle.BundlePlugin;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;

public class GNDReferenceMappingTests extends Assert {

    private final static ESLogger logger = ESLoggerFactory.getLogger(GNDReferenceMappingTests.class.getName());

    @Test
    public void testGND() throws IOException {
        Settings nodeSettings = Settings.settingsBuilder()
                .put("path.home", System.getProperty("path.home"))
                .put("plugin.types", BundlePlugin.class.getName())
                .put("index.number_of_shards", 1)
                .put("index.number_of_replica", 0)
                .build();
        Node node = NodeBuilder.nodeBuilder().settings(nodeSettings).local(true).build().start();
        Client client = node.client();
        try {
            client.admin().indices().prepareDelete("gnd").execute().actionGet();
        } catch (Exception e) {
            logger.warn("can not delete index 'gnd'");
        }
        String gndSettings = copyToStringFromClasspath("gnd-settings.json");
        String gndMapping = copyToStringFromClasspath("gnd-mapping.json");
        client.admin().indices().prepareCreate("gnd")
                .setSettings(gndSettings)
                .addMapping("gnd", gndMapping)
                .execute().actionGet();
        String gndDocument = copyToStringFromClasspath("gnd-document.json");
        client.prepareIndex("gnd", "gnd", "11862444X").setSource(gndDocument).setRefresh(true).execute().actionGet();

        try {
            client.admin().indices().prepareDelete("title").execute().actionGet();
        } catch (Exception e) {
            logger.warn("can not delete index 'title'");
        }
        String titleSettings = copyToStringFromClasspath("title-settings.json");
        String titleMapping = copyToStringFromClasspath("title-mapping.json");
        client.admin().indices().prepareCreate("title")
                .setSettings(titleSettings)
                .addMapping("title", titleMapping)
                .execute().actionGet();
        String titleDocument = copyToStringFromClasspath("title-document.json");
        client.prepareIndex("title", "title", "(DE-605)008427902").setSource(titleDocument).setRefresh(true).execute().actionGet();

        // search for Tucholsky
        QueryBuilder queryBuilder = matchPhraseQuery("cql.allIndexes", "Tucholsky, Kurt");
        SearchResponse searchResponse = client.prepareSearch("title")
                .setQuery(queryBuilder).execute().actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(searchResponse.getHits().getTotalHits(), 1);

        // search for Tucholsky alias "Peter Panter"
        queryBuilder = matchPhraseQuery("cql.allIndexes", "Panter, Peter");
        searchResponse = client.prepareSearch("title")
                .setQuery(queryBuilder).execute().actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("{}", hit.getSource());
        }
        assertEquals(searchResponse.getHits().getTotalHits(), 1);

        client.close();
        node.close();
    }

    public String copyToStringFromClasspath(String path) throws IOException {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), "UTF-8"));
    }
}
