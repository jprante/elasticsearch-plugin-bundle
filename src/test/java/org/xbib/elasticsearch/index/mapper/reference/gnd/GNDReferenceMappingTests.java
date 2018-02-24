package org.xbib.elasticsearch.index.mapper.reference.gnd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.util.SuppressForbidden;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;

import static org.elasticsearch.common.io.Streams.copyToString;

/**
 * GND reference mapping tests.
 */
public class GNDReferenceMappingTests extends ESSingleNodeTestCase {

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
                    .addMapping("gnd", copyToStringFromClasspath("gnd-mapping.json"), XContentType.JSON)
                    .execute().actionGet();
            client().prepareIndex("gnd", "gnd", "11862444X")
                    .setSource(copyToStringFromClasspath("gnd-document.json"), XContentType.JSON)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                    .execute().actionGet();

            client().admin().indices().prepareCreate("title")
                    .setSettings(copyToStringFromClasspath("title-settings.json"), XContentType.JSON)
                    .addMapping("title", copyToStringFromClasspath("title-mapping.json"), XContentType.JSON)
                    .execute().actionGet();
            client().prepareIndex("title", "title", "(DE-605)008427902")
                    .setSource(copyToStringFromClasspath("title-document-1.json"), XContentType.JSON)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                    .execute().actionGet();
            client().prepareIndex("title", "title", "(DE-605)017215715")
                    .setSource(copyToStringFromClasspath("title-document-2.json"), XContentType.JSON)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                    .execute().actionGet();

            SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                    .setIndices("title")
                    .setTypes("title")
                    .setQuery(QueryBuilders.matchPhraseQuery("bib.namePersonal", "Tucholsky, Kurt"));

            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

            logger.info("hits = {}", searchResponse.getHits().getTotalHits());
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                logger.info("kurt tucholsky = {}", hit.getSourceAsMap());
            }
            assertEquals(1, searchResponse.getHits().getTotalHits());

            searchRequestBuilder = new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                    .setIndices("title")
                    .setTypes("title")
                    .setQuery(QueryBuilders.matchPhraseQuery("bib.namePersonal", "Panter, Peter"));
            searchResponse = searchRequestBuilder.execute().actionGet();
            logger.info("hits = {}", searchResponse.getHits().getTotalHits());
            assertTrue(searchResponse.getHits().getTotalHits() > 0);
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                logger.info("peter panter = {}", hit.getSourceAsMap());
            }
            assertEquals(1, searchResponse.getHits().getTotalHits());

            searchRequestBuilder = new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                    .setIndices("title")
                    .setTypes("title")
                    .setQuery(QueryBuilders.matchQuery("bib.namePersonal", "Panter, Peter"))
                    .setExplain(true);
            searchResponse = searchRequestBuilder.execute().actionGet();
            logger.info("hits = {}", searchResponse.getHits().getTotalHits());
            assertTrue(searchResponse.getHits().getTotalHits() > 0);
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                logger.info("schroeder = {}", hit.getSourceAsMap());
                logger.info(hit.getExplanation().toString());
            }
            assertEquals(1, searchResponse.getHits().getTotalHits());

            try {
                client().admin().indices().prepareDelete("title", "gnd").execute().actionGet();
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
    }

    @SuppressForbidden(reason = "accessing local resources from classpath")
    private String copyToStringFromClasspath(String path) throws Exception {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), "UTF-8"));
    }
}
