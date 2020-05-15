package org.xbib.elasticsearch.plugin.bundle.test.index.mapper.langdetect;

import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.util.Collection;
import java.util.Collections;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Language detection test for german.
 */
public class LangDetectGermanTests extends ESSingleNodeTestCase {

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
