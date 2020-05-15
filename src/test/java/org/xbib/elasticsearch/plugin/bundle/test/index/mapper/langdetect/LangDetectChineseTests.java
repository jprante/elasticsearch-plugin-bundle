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
 * Language detection chinese test.
 */
public class LangDetectChineseTests extends ESSingleNodeTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(BundlePlugin.class);
    }

    public void testChineseLanguageCode() throws Exception {
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
            String source = "位于美国首都华盛顿都会圈的希望中文学校５日晚举办活动庆祝建立２０周年。" +
                    "从中国大陆留学生为子女学中文而自发建立的学习班，到学生规模在全美名列前茅的中文学校，" +
                    "这个平台的发展也折射出美国的中文教育热度逐步提升。\n" +
                    "希望中文学校是大华盛顿地区最大中文学校，现有７个校区逾４０００名学生，" +
                    "规模在美国东部数一数二。" +
                    "不过，见证了希望中文学校２０年发展的人们起初根本无法想象这个小小的中文教育平台能发展到今日之规模。";
            IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(client(), IndexAction.INSTANCE)
                    .setIndex("test").setType("someType").setId("1")
                    .setSource("content", source);
            indexRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                    .execute().actionGet();
            SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                    .setIndices("test")
                    .setQuery(QueryBuilders.termQuery("content.language", "zh-cn"))
                    .addStoredField("content.language")
                    .setTrackTotalHits(true);
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            assertEquals(1L, searchResponse.getHits().getTotalHits().value);
            assertEquals("zh-cn", searchResponse.getHits().getAt(0).field("content.language").getValue());
        } finally {
            DeleteIndexRequestBuilder deleteIndexRequestBuilder =
                    new DeleteIndexRequestBuilder(client(), DeleteIndexAction.INSTANCE, "test");
            deleteIndexRequestBuilder.execute().actionGet();
        }
    }
}
