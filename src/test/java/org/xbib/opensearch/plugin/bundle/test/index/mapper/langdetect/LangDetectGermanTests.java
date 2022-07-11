package org.xbib.opensearch.plugin.bundle.test.index.mapper.langdetect;

import org.opensearch.action.admin.indices.create.CreateIndexAction;
import org.opensearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.opensearch.action.admin.indices.delete.DeleteIndexAction;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.opensearch.action.index.IndexAction;
import org.opensearch.action.index.IndexRequestBuilder;
import org.opensearch.action.search.SearchAction;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.plugins.Plugin;
import org.opensearch.test.OpenSearchSingleNodeTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.util.Collection;
import java.util.Collections;

import static org.opensearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Language detection test for german.
 */
public class LangDetectGermanTests extends OpenSearchSingleNodeTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(BundlePlugin.class);
    }

    public void testGermanLanguageCode() throws Exception {
        try {
            XContentBuilder builder = jsonBuilder()
                    .startObject()
                    .startObject("properties")
                    .startObject("content")
                    .field("type", "text")
                    .startObject("fields")
                    .startObject("language")
                    .field("type", "langdetect")
                    .array("languages", "zh-cn", "en", "de")
                    .endObject()
                    .endObject()
                    .endObject()
                    .endObject()
                    .endObject();
            CreateIndexRequestBuilder createIndexRequestBuilder =
                    new CreateIndexRequestBuilder(client(), CreateIndexAction.INSTANCE);
            createIndexRequestBuilder.setIndex("test").addMapping("someType", builder).execute().actionGet();
            String source = "Einigkeit und Recht und Freiheit\n" +
                    "für das deutsche Vaterland!\n" +
                    "Danach lasst uns alle streben\n" +
                    "brüderlich mit Herz und Hand!";
            IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(client(), IndexAction.INSTANCE)
                    .setIndex("test").setType("someType").setId("1")
                    .setSource("content", source);
            indexRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                    .execute().actionGet();
            SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                    .setIndices("test")
                    .setQuery(QueryBuilders.termQuery("content.language", "de"))
                    .addStoredField("content.language")
                    .setTrackScores(true);
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            assertEquals(1L, searchResponse.getHits().getTotalHits().value);
            assertEquals("de", searchResponse.getHits().getAt(0).field("content.language").getValue());
        } finally {
            DeleteIndexRequestBuilder deleteIndexRequestBuilder =
                    new DeleteIndexRequestBuilder(client(), DeleteIndexAction.INSTANCE, "test");
            deleteIndexRequestBuilder.execute().actionGet();
        }
    }
}
