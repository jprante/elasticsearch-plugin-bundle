package org.xbib.opensearch.plugin.bundle.test.index.mapper.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.util.SuppressForbidden;
import org.opensearch.action.search.SearchAction;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.analysis.common.CommonAnalysisPlugin;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.plugins.Plugin;
import org.opensearch.search.SearchHit;
import org.opensearch.test.OpenSearchSingleNodeTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import static org.opensearch.common.io.Streams.copyToString;

/**
 * GND reference mapping tests.
 */
public class GNDReferenceMappingTests extends OpenSearchSingleNodeTestCase {

    private static final Logger logger = LogManager.getLogger(GNDReferenceMappingTests.class.getName());

    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Arrays.asList(BundlePlugin.class, CommonAnalysisPlugin.class);
    }

    public void testGND() throws Exception {
            try {
                client().admin().indices().prepareDelete("title", "gnd").execute().actionGet();
            } catch (Exception e) {
                logger.warn(e.getMessage() + " --> ok, ignored");
            }
            client().admin().indices().prepareCreate("gnd")
                    .setSettings(copyToStringFromClasspath("gnd-settings.json"), XContentType.JSON)
                    .setMapping(copyToStringFromClasspath("gnd-mapping.json"))
                    .execute().actionGet();
            client().prepareIndex("gnd").setId("11862444X")
                    .setSource(copyToStringFromClasspath("gnd-document.json"), XContentType.JSON)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                    .execute().actionGet();

            client().admin().indices().prepareCreate("title")
                    .setSettings(copyToStringFromClasspath("title-settings.json"), XContentType.JSON)
                    .setMapping(copyToStringFromClasspath("title-mapping.json"))
                    .execute().actionGet();
            client().prepareIndex("title").setId("(DE-605)008427902")
                    .setSource(copyToStringFromClasspath("title-document-1.json"), XContentType.JSON)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                    .execute().actionGet();
            client().prepareIndex("title").setId("(DE-605)017215715")
                    .setSource(copyToStringFromClasspath("title-document-2.json"), XContentType.JSON)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                    .execute().actionGet();

            SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                    .setIndices("title")
                    .setQuery(QueryBuilders.matchPhraseQuery("bib.namePersonal", "Tucholsky, Kurt"))
                    .setTrackTotalHits(true);
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            logger.info("hits = {}", searchResponse.getHits().getTotalHits());
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                logger.info("kurt tucholsky = {}", hit.getSourceAsMap());
            }
            assertEquals(1, searchResponse.getHits().getTotalHits().value);

            searchRequestBuilder = new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                    .setIndices("title")
                    .setQuery(QueryBuilders.matchPhraseQuery("bib.namePersonal", "Panter, Peter"))
                    .setTrackTotalHits(true);
            searchResponse = searchRequestBuilder.execute().actionGet();
            logger.info("hits = {}", searchResponse.getHits().getTotalHits().value);
            assertTrue(searchResponse.getHits().getTotalHits().value > 0);
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                logger.info("peter panter = {}", hit.getSourceAsMap());
            }
            assertEquals(1, searchResponse.getHits().getTotalHits().value);

            searchRequestBuilder = new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                    .setIndices("title")
                    .setQuery(QueryBuilders.matchQuery("bib.namePersonal", "Panter, Peter"))
                    .setExplain(true)
                    .setTrackTotalHits(true);
            searchResponse = searchRequestBuilder.execute().actionGet();
            logger.info("hits = {}", searchResponse.getHits().getTotalHits().value);
            assertTrue(searchResponse.getHits().getTotalHits().value > 0);
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                logger.info("schroeder = {}", hit.getSourceAsMap());
                logger.info(hit.getExplanation().toString());
            }
            assertEquals(1, searchResponse.getHits().getTotalHits().value);

            try {
                client().admin().indices().prepareDelete("title", "gnd").execute().actionGet();
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
    }

    @SuppressForbidden(reason = "accessing local resources from classpath")
    private String copyToStringFromClasspath(String path) throws Exception {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), StandardCharsets.UTF_8));
    }
}
