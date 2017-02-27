package org.xbib.elasticsearch.index.analysis.naturalsort;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class NaturalSortKeyTests extends NodeTestUtils {

    @Test
    public void testSort() throws Exception {
        startCluster();
        try {
            Settings settings = Settings.builder()
                    .build();

            client().admin().indices().prepareCreate("test")
                    .setSettings(settings)
                    .addMapping("type1", jsonBuilder().startObject()
                            .startObject("type1")
                              .startObject("properties")
                                .startObject("points")
                                  .field("type", "text")
                                  .field("store", true)
                                  .startObject("fields")
                                    .startObject("sort")
                                      .field("type", "text")
                                      .field("analyzer", "naturalsort")
                                      .field("fielddata", true)
                                    .endObject()
                                  .endObject()
                                .endObject()
                              .endObject()
                            .endObject()
                            .endObject())
                    .execute().actionGet();

            client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

            String[] words = new String[]{
                    "Bob: 3 points", "Bob: 10 points", "Bob: 2 points"
            };

            for (String word : words) {
                client().prepareIndex("test", "type1")
                        .setSource(jsonBuilder().startObject()
                                .field("points", word)
                                .endObject()).execute().actionGet();
            }

            client().admin().indices().prepareRefresh().execute().actionGet();

            SearchResponse searchResponse = client().prepareSearch()
                    .addStoredField("points")
                    .addSort("points.sort", SortOrder.ASC)
                    .execute().actionGet();

            assertEquals(3L, searchResponse.getHits().totalHits());
            assertEquals("Bob: 2 points", searchResponse.getHits().getAt(0).getFields().get("points").getValue().toString());
            assertEquals("Bob: 3 points", searchResponse.getHits().getAt(1).getFields().get("points").getValue().toString());
            assertEquals("Bob: 10 points", searchResponse.getHits().getAt(2).getFields().get("points").getValue().toString());
        } finally {
            stopCluster();
        }
    }

    @Test
    public void testComplex() throws Exception {
        startCluster();
        try {
            Settings settings = Settings.builder()
                    .build();

            client().admin().indices().prepareCreate("test")
                    .setSettings(settings)
                    .addMapping("type1", jsonBuilder().startObject()
                            .startObject("type1")
                              .startObject("properties")
                                .startObject("points")
                                  .field("type", "text")
                                  .field("store", true)
                                  .startObject("fields")
                                    .startObject("sort")
                                      .field("type", "text")
                                      .field("analyzer", "naturalsort")
                                      .field("fielddata", true)
                                    .endObject()
                                   .endObject()
                                  .endObject()
                                .endObject()
                              .endObject()
                            .endObject())
                    .execute().actionGet();

            client().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

            String[] words = new String[]{
                    "7 201 2 1", "7 25 2 1", "7 1 1 1", "7 10 1 1", "7 2 1 2", "7 20 2 1"
            };

            for (String word : words) {
                client().prepareIndex("test", "type1")
                        .setSource(jsonBuilder().startObject().field("points", word).endObject()).execute().actionGet();
            }

            client().admin().indices().prepareRefresh().execute().actionGet();

            SearchResponse searchResponse = client().prepareSearch()
                    .addStoredField("points")
                    .addSort("points.sort", SortOrder.ASC)
                    .execute().actionGet();
            assertEquals(6L, searchResponse.getHits().totalHits());
            assertEquals("7 1 1 1", searchResponse.getHits().getAt(0).field("points").getValue().toString());
            assertEquals("7 2 1 2", searchResponse.getHits().getAt(1).field("points").getValue().toString());
            assertEquals("7 10 1 1", searchResponse.getHits().getAt(2).field("points").getValue().toString());
            assertEquals("7 20 2 1", searchResponse.getHits().getAt(3).field("points").getValue().toString());
            assertEquals("7 25 2 1", searchResponse.getHits().getAt(4).field("points").getValue().toString());
            assertEquals("7 201 2 1", searchResponse.getHits().getAt(5).field("points").getValue().toString());

            searchResponse = client().prepareSearch()
                    .addStoredField("points")
                    .addSort("points.sort", SortOrder.DESC)
                    .execute().actionGet();
            assertEquals(6L, searchResponse.getHits().totalHits());
            assertEquals("7 201 2 1", searchResponse.getHits().getAt(0).field("points").getValue().toString());
            assertEquals("7 25 2 1", searchResponse.getHits().getAt(1).field("points").getValue().toString());
            assertEquals("7 20 2 1", searchResponse.getHits().getAt(2).field("points").getValue().toString());
            assertEquals("7 10 1 1", searchResponse.getHits().getAt(3).field("points").getValue().toString());
            assertEquals("7 2 1 2", searchResponse.getHits().getAt(4).field("points").getValue().toString());
            assertEquals("7 1 1 1", searchResponse.getHits().getAt(5).field("points").getValue().toString());
        } finally {
            stopCluster();
        }
    }

    @Test
    public void testDewey() throws Exception {
        startCluster();
        try {
            Settings settings = Settings.builder()
                    .build();

            client().admin().indices().prepareCreate("test")
                    .setSettings(settings)
                    .addMapping("type1", jsonBuilder().startObject()
                            .startObject("type1")
                                .startObject("properties")
                                    .startObject("notation")
                                        .field("type", "text")
                                        .field("store", true)
                                        .startObject("fields")
                                            .startObject("sort")
                                               .field("type", "text")
                                               .field("analyzer", "naturalsort")
                                               .field("fielddata", true)
                                             .endObject()
                                        .endObject()
                                    .endObject()
                                .endObject()
                            .endObject()
                            .endObject())
                    .execute().actionGet();

            client().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

            String[] notations = new String[]{
                    "10.10.1", "10.1.1", "2.11.0", "2.10.1", "2.1.1", "1.10.0", "1.0.0"
            };

            for (String notation : notations) {
                client().prepareIndex("test", "type1")
                        .setSource(jsonBuilder().startObject()
                                .field("notation", notation)
                                .endObject()).execute().actionGet();
            }

            client().admin().indices().prepareRefresh().execute().actionGet();

            SearchResponse searchResponse = client().prepareSearch()
                    .addStoredField("notation")
                    .addSort("notation.sort", SortOrder.ASC)
                    .execute().actionGet();
            assertEquals(7L, searchResponse.getHits().totalHits());

            assertEquals("1.0.0", searchResponse.getHits().getAt(0).getFields().get("notation").getValue().toString());
            assertEquals("1.10.0", searchResponse.getHits().getAt(1).getFields().get("notation").getValue().toString());
            assertEquals("2.1.1", searchResponse.getHits().getAt(2).getFields().get("notation").getValue().toString());
            assertEquals("2.10.1", searchResponse.getHits().getAt(3).getFields().get("notation").getValue().toString());
            assertEquals("2.11.0", searchResponse.getHits().getAt(4).getFields().get("notation").getValue().toString());
            assertEquals("10.1.1", searchResponse.getHits().getAt(5).getFields().get("notation").getValue().toString());
            assertEquals("10.10.1", searchResponse.getHits().getAt(6).getFields().get("notation").getValue().toString());
        } finally {
            stopCluster();
        }
    }
}
