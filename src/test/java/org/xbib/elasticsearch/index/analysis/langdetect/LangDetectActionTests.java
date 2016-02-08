package org.xbib.elasticsearch.index.analysis.langdetect;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.junit.Assert.assertEquals;

public class LangDetectActionTests extends NodeTestUtils {

    @Test
    public void testSort() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .build();

        client("1").admin().indices().prepareCreate("test")
                .setSettings(settings)
                .addMapping("article",
                        "{ article : { properties : { content : { type : \"langdetect\" } } } }")
                .execute().actionGet();

        client("1").admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

        client("1").prepareIndex("test", "article", "1")
                    .setSource(jsonBuilder().startObject()
                            .field("title", "Some title")
                            .field("content", "Oh, say can you see by the dawn`s early light, What so proudly we hailed at the twilight`s last gleaming?")
                            .endObject()).execute().actionGet();
        client("1").prepareIndex("test", "article", "2")
                .setSource(jsonBuilder().startObject()
                        .field("title", "Ein Titel")
                        .field("content", "Einigkeit und Recht und Freiheit für das deutsche Vaterland!")
                        .endObject()).execute().actionGet();
        client("1").prepareIndex("test", "article", "3")
                .setSource(jsonBuilder().startObject()
                        .field("title", "Un titre")
                        .field("content", "Allons enfants de la Patrie, Le jour de gloire est arrivé!")
                        .endObject()).execute().actionGet();

        client("1").admin().indices().prepareRefresh().execute().actionGet();

        SearchResponse searchResponse = client("1").prepareSearch()
                .setQuery(QueryBuilders.termQuery("content", "eng"))
                .execute().actionGet();
        assertEquals(1L, searchResponse.getHits().totalHits());
        assertEquals("Oh, say can you see by the dawn`s early light, What so proudly we hailed at the twilight`s last gleaming?",
                searchResponse.getHits().getAt(0).getSource().get("content").toString());

        searchResponse = client("1").prepareSearch()
                .setQuery(QueryBuilders.termQuery("content", "ger"))
                .execute().actionGet();
        assertEquals(1L, searchResponse.getHits().totalHits());
        assertEquals("Einigkeit und Recht und Freiheit für das deutsche Vaterland!",
                searchResponse.getHits().getAt(0).getSource().get("content").toString());

        searchResponse = client("1").prepareSearch()
                .setQuery(QueryBuilders.termQuery("content", "fre"))
                .execute().actionGet();
        assertEquals(1L, searchResponse.getHits().totalHits());
        assertEquals("Allons enfants de la Patrie, Le jour de gloire est arrivé!",
                searchResponse.getHits().getAt(0).getSource().get("content").toString());
    }

}
