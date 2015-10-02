package org.xbib.elasticsearch.index.mapper.reference.gnd;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.plugin.analysis.bundle.BundlePlugin;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.elasticsearch.common.io.Streams.copyToString;

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
            client.admin().indices().prepareDelete("title", "gnd").execute().actionGet();
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

        String gndSettings = copyToStringFromClasspath("gnd-settings.json");
        String gndMapping = copyToStringFromClasspath("gnd-mapping.json");
        client.admin().indices().prepareCreate("gnd")
                .setSettings(gndSettings)
                .addMapping("gnd", gndMapping)
                .execute().actionGet();
        String gndDocument = copyToStringFromClasspath("gnd-document.json");
        client.prepareIndex("gnd", "gnd", "11862444X")
                .setSource(gndDocument)
                .setRefresh(true).execute().actionGet();

        String titleSettings = copyToStringFromClasspath("title-settings.json");
        String titleMapping = copyToStringFromClasspath("title-mapping.json");
        client.admin().indices().prepareCreate("title")
                .setSettings(titleSettings)
                .addMapping("title", titleMapping)
                .execute().actionGet();
        client.prepareIndex("title", "title", "(DE-605)008427902")
                .setSource(copyToStringFromClasspath("title-document-1.json"))
                .setRefresh(true).execute().actionGet();
        client.prepareIndex("title", "title", "(DE-605)017215715")
                .setSource(copyToStringFromClasspath("title-document-2.json"))
                .setRefresh(true).execute().actionGet();

        SearchResponse searchResponse = client.search(new SearchRequest()
                .indices("title")
                .types("title")
                .extraSource("{\"query\":{\"match_phrase\":{\"bib.namePersonal\":\"Tucholsky, Kurt\"}}}"))
                .actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("kurt tucholsky = {}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        searchResponse = client.search(new SearchRequest()
                .indices("title")
                .types("title")
                .extraSource("{\"query\":{\"match_phrase\":{\"bib.namePersonal\":\"Panter, Peter\"}}}"))
                .actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("peter panter = {}", hit.getSource());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        searchResponse = client.search(new SearchRequest()
                .indices("title")
                .types("title")
                .extraSource("{\"explain\":true,\"query\":{\"match\":{\"bib.namePersonal\":\"Schroeder\"}}}"))
                .actionGet();
        logger.info("hits = {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            logger.info("schroeder = {}", hit.getSource());
            logger.info(hit.getExplanation().toString());
        }
        assertEquals(1, searchResponse.getHits().getTotalHits());

        try {
            client.admin().indices().prepareDelete("title", "gnd").execute().actionGet();
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

        client.close();
        node.close();

    }

    public String copyToStringFromClasspath(String path) throws IOException {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), "UTF-8"));
    }
}
