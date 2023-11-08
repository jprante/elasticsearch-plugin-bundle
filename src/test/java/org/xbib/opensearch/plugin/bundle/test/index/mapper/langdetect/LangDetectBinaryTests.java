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
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.plugins.Plugin;
import org.opensearch.test.InternalSettingsPlugin;
import org.opensearch.test.OpenSearchSingleNodeTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.util.Arrays;
import java.util.Collection;

/**
 * Language detection binary test.
 */
public class LangDetectBinaryTests extends OpenSearchSingleNodeTestCase {

    /** The plugin classes that should be added to the node. */
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Arrays.asList(BundlePlugin.class, InternalSettingsPlugin.class);
    }

    public void testLangDetectBinary() throws Exception {
        try {
            CreateIndexRequestBuilder createIndexRequestBuilder =
                    new CreateIndexRequestBuilder(client(), CreateIndexAction.INSTANCE).setIndex("test");
            createIndexRequestBuilder.setMapping(XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("properties")
                    .startObject("content")
                    .field("type", "binary")
                    .startObject("fields")
                    .startObject("language")
                    .field("type", "langdetect")
                    .field("binary", true)
                    .endObject()
                    .endObject()
                    .endObject()
                    .endObject()
                    .endObject());
            createIndexRequestBuilder.execute().actionGet();
            IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(client(), IndexAction.INSTANCE)
                    .setIndex("test").setId("1")
                    //\"God Save the Queen\" (alternatively \"God Save the King\"
                    .setSource("content", "IkdvZCBTYXZlIHRoZSBRdWVlbiIgKGFsdGVybmF0aXZlbHkgIkdvZCBTYXZlIHRoZSBLaW5nIg==");
            indexRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                    .execute().actionGet();
            SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                    .setIndices("test")
                    .setQuery(QueryBuilders.termQuery("content.language", "en"))
                    .addStoredField("content.language")
                    .setTrackTotalHits(true);
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            assertEquals(1L, searchResponse.getHits().getTotalHits().value);
            assertEquals("en", searchResponse.getHits().getAt(0).field("content.language").getValue());
        } finally {
            DeleteIndexRequestBuilder deleteIndexRequestBuilder =
                    new DeleteIndexRequestBuilder(client(), DeleteIndexAction.INSTANCE, "test");
            deleteIndexRequestBuilder.execute().actionGet();
        }
    }
}
