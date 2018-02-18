package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.util.Collection;
import java.util.Collections;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Language detection binary test.
 */
public class LangDetectBinaryTests extends ESSingleNodeTestCase {

    /** The plugin classes that should be added to the node. */
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(BundlePlugin.class);
    }

    public void testLangDetectBinary() throws Exception {
        CreateIndexRequestBuilder createIndexRequestBuilder =
                new CreateIndexRequestBuilder(client(), CreateIndexAction.INSTANCE).setIndex("test");
        createIndexRequestBuilder.addMapping("someType", jsonBuilder()
                .startObject()
                .startObject("properties")
                .startObject("content")
                .field("type", "text")
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
        IndexRequestBuilder indexRequestBuilder =
                new IndexRequestBuilder(client(), IndexAction.INSTANCE)
                        .setIndex("test").setType("someType").setId("1")
                        //\"God Save the Queen\" (alternatively \"God Save the King\"
                        .setSource("content", "IkdvZCBTYXZlIHRoZSBRdWVlbiIgKGFsdGVybmF0aXZlbHkgIkdvZCBTYXZlIHRoZSBLaW5nIg==");
        indexRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).execute().actionGet();
        SearchRequestBuilder searchRequestBuilder =
                new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                        .setIndices("test")
                        .setQuery(QueryBuilders.matchAllQuery())
                        .addStoredField("content.language");
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        assertEquals(1L, searchResponse.getHits().getTotalHits());
        logger.warn("testLangDetectBinary: docs=" +  searchResponse.getHits().getAt(0).fieldsOrNull());
        assertEquals("en", searchResponse.getHits().getAt(0).field("content.language").getValue());
    }
}
