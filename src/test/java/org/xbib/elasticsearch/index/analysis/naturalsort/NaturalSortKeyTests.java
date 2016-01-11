package org.xbib.elasticsearch.index.analysis.naturalsort;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.xbib.elasticsearch.util.NodeTestUtils;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.junit.Assert.assertEquals;

public class NaturalSortKeyTests extends NodeTestUtils {

    @Test
    public void testSort() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .build();

        client("1").admin().indices().prepareCreate("test")
                .setSettings(settings)
                .addMapping("type1",
                        "{ type1 : { properties : { points : { type : \"string\", fields : { sort : { type : \"string\", analyzer : \"naturalsort\" } } } } } }")
                .execute().actionGet();

        client("1").admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

        String[] words = new String[]{
                "Bob: 3 points", "Bob: 10 points", "Bob: 2 points"
        };

        for (String word : words) {
            client("1").prepareIndex("test", "type1")
                    .setSource(jsonBuilder().startObject()
                            .field("points", word)
                            .endObject()).execute().actionGet();
        }

        client("1").admin().indices().prepareRefresh().execute().actionGet();

        SearchResponse searchResponse = client("1").prepareSearch()
                .addField("points")
                .addSort("points.sort", SortOrder.ASC)
                .execute().actionGet();

        assertEquals(3L, searchResponse.getHits().totalHits());
        assertEquals("Bob: 2 points", searchResponse.getHits().getAt(0).field("points").getValue().toString());
        assertEquals("Bob: 3 points", searchResponse.getHits().getAt(1).field("points").getValue().toString());
        assertEquals("Bob: 10 points", searchResponse.getHits().getAt(2).field("points").getValue().toString());
    }


    @Test
    public void testComplex() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .build();

        client("1").admin().indices().prepareCreate("test")
                .setSettings(settings)
                .addMapping("type1", "{ type1 : { properties : { points : { type : \"string\", fields : { sort : { type : \"string\", analyzer : \"naturalsort\" } } } } } }")
                .execute().actionGet();

        client("1").admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

        String[] words = new String[] {
                "7 201 2 1", "7 25 2 1", "7 1 1 1", "7 10 1 1", "7 2 1 2", "7 20 2 1"
        };

        for (String word : words) {
            client("1").prepareIndex("test", "type1")
                  .setSource(jsonBuilder().startObject().field("points", word).endObject()).execute().actionGet();
        }

        client("1").admin().indices().prepareRefresh().execute().actionGet();

            SearchResponse searchResponse = client("1").prepareSearch()
                    .addField("points")
                    .addSort("points.sort", SortOrder.ASC)
                    .execute().actionGet();
        assertEquals(6L, searchResponse.getHits().totalHits());
        assertEquals("7 1 1 1", searchResponse.getHits().getAt(0).field("points").getValue().toString());
        assertEquals("7 2 1 2", searchResponse.getHits().getAt(1).field("points").getValue().toString());
        assertEquals("7 10 1 1", searchResponse.getHits().getAt(2).field("points").getValue().toString());
        assertEquals("7 20 2 1", searchResponse.getHits().getAt(3).field("points").getValue().toString());
        assertEquals("7 25 2 1", searchResponse.getHits().getAt(4).field("points").getValue().toString());
        assertEquals("7 201 2 1", searchResponse.getHits().getAt(5).field("points").getValue().toString());

        searchResponse = client("1").prepareSearch()
                    .addField("points")
                    .addSort("points.sort", SortOrder.DESC)
                    .execute().actionGet();
        assertEquals(6L, searchResponse.getHits().totalHits());
        assertEquals("7 201 2 1", searchResponse.getHits().getAt(0).field("points").getValue().toString());
        assertEquals("7 25 2 1", searchResponse.getHits().getAt(1).field("points").getValue().toString());
        assertEquals("7 20 2 1", searchResponse.getHits().getAt(2).field("points").getValue().toString());
        assertEquals("7 10 1 1", searchResponse.getHits().getAt(3).field("points").getValue().toString());
        assertEquals("7 2 1 2", searchResponse.getHits().getAt(4).field("points").getValue().toString());
        assertEquals("7 1 1 1", searchResponse.getHits().getAt(5).field("points").getValue().toString());
    }

    @Test
    public void testDewey() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .build();

        client("1").admin().indices().prepareCreate("test")
                .setSettings(settings)
                .addMapping("type1", "{ type1 : { properties : { notation : { type : \"string\", fields : { sort : { type : \"string\", analyzer : \"naturalsort\" } } } } } }")
                .execute().actionGet();

        client("1").admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

        String[] notations = new String[] {
                "10.10.1", "10.1.1", "2.11.0", "2.10.1", "2.1.1", "1.10.0", "1.0.0"
        };

        for (String notation : notations) {
            client("1").prepareIndex("test", "type1")
                    .setSource(jsonBuilder().startObject().field("notation", notation).endObject()).execute().actionGet();
        }

        client("1").admin().indices().prepareRefresh().execute().actionGet();

            SearchResponse searchResponse = client("1").prepareSearch()
                    .addField("notation")
                    .addSort("notation.sort", SortOrder.ASC)
                    .execute().actionGet();
            assertEquals(7L, searchResponse.getHits().totalHits());

        assertEquals("1.0.0", searchResponse.getHits().getAt(0).field("notation").getValue().toString());
        assertEquals("1.10.0",searchResponse.getHits().getAt(1).field("notation").getValue().toString());
        assertEquals("2.1.1", searchResponse.getHits().getAt(2).field("notation").getValue().toString());
        assertEquals("2.10.1", searchResponse.getHits().getAt(3).field("notation").getValue().toString());
        assertEquals("2.11.0", searchResponse.getHits().getAt(4).field("notation").getValue().toString());
        assertEquals("10.1.1", searchResponse.getHits().getAt(5).field("notation").getValue().toString());
        assertEquals("10.10.1", searchResponse.getHits().getAt(6).field("notation").getValue().toString());

    }

}
