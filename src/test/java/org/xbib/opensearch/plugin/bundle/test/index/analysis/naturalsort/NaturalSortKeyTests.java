package org.xbib.opensearch.plugin.bundle.test.index.analysis.naturalsort;

import org.opensearch.action.search.SearchResponse;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.plugins.Plugin;
import org.opensearch.search.sort.SortOrder;
import org.opensearch.test.OpenSearchSingleNodeTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.util.Collection;
import java.util.Collections;


/**
 * Natural sort key tests.
 */
public class NaturalSortKeyTests extends OpenSearchSingleNodeTestCase {

    /** The plugin classes that should be added to the node. */
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(BundlePlugin.class);
    }

    public void testSort() throws Exception {
        //startCluster();
        //try {
        Settings settings = Settings.builder()
                .build();

        client().admin().indices().prepareCreate("test")
                .setSettings(settings)
                .setMapping(XContentFactory.jsonBuilder().startObject()
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
                        .endObject())
                .execute().actionGet();

        client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

        String[] words = new String[]{
                "Bob: 3 points", "Bob: 10 points", "Bob: 2 points"
        };

        for (String word : words) {
            client().prepareIndex("test")
                    .setSource(XContentFactory.jsonBuilder().startObject()
                            .field("points", word)
                            .endObject()).execute().actionGet();
        }

        client().admin().indices().prepareRefresh().execute().actionGet();

        SearchResponse searchResponse = client().prepareSearch()
                .addStoredField("points")
                .addSort("points.sort", SortOrder.ASC)
                .setTrackTotalHits(true)
                .execute().actionGet();

        assertEquals(3L, searchResponse.getHits().getTotalHits().value);
        assertEquals("Bob: 2 points", searchResponse.getHits().getAt(0).getFields().get("points").getValue().toString());
        assertEquals("Bob: 3 points", searchResponse.getHits().getAt(1).getFields().get("points").getValue().toString());
        assertEquals("Bob: 10 points", searchResponse.getHits().getAt(2).getFields().get("points").getValue().toString());
        //} finally {
        //    stopCluster();
        //}
    }

    public void testComplex() throws Exception {
        //startCluster();
        //try {
        Settings settings = Settings.builder()
                .build();

        client().admin().indices().prepareCreate("test")
                .setSettings(settings)
                .setMapping(XContentFactory.jsonBuilder().startObject()
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
                        .endObject())
                .execute().actionGet();

        client().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

        String[] words = new String[]{
                "7 201 2 1", "7 25 2 1", "7 1 1 1", "7 10 1 1", "7 2 1 2", "7 20 2 1"
        };

        for (String word : words) {
            client().prepareIndex("test")
                    .setSource(XContentFactory.jsonBuilder().startObject().field("points", word).endObject()).execute().actionGet();
        }

        client().admin().indices().prepareRefresh().execute().actionGet();

        SearchResponse searchResponse = client().prepareSearch()
                .addStoredField("points")
                .addSort("points.sort", SortOrder.ASC)
                .setTrackTotalHits(true)
                .execute().actionGet();
        assertEquals(6L, searchResponse.getHits().getTotalHits().value);
        assertEquals("7 1 1 1", searchResponse.getHits().getAt(0).field("points").getValue().toString());
        assertEquals("7 2 1 2", searchResponse.getHits().getAt(1).field("points").getValue().toString());
        assertEquals("7 10 1 1", searchResponse.getHits().getAt(2).field("points").getValue().toString());
        assertEquals("7 20 2 1", searchResponse.getHits().getAt(3).field("points").getValue().toString());
        assertEquals("7 25 2 1", searchResponse.getHits().getAt(4).field("points").getValue().toString());
        assertEquals("7 201 2 1", searchResponse.getHits().getAt(5).field("points").getValue().toString());

        searchResponse = client().prepareSearch()
                .addStoredField("points")
                .addSort("points.sort", SortOrder.DESC)
                .setTrackTotalHits(true)
                .execute().actionGet();
        assertEquals(6L, searchResponse.getHits().getTotalHits().value);
        assertEquals("7 201 2 1", searchResponse.getHits().getAt(0).field("points").getValue().toString());
        assertEquals("7 25 2 1", searchResponse.getHits().getAt(1).field("points").getValue().toString());
        assertEquals("7 20 2 1", searchResponse.getHits().getAt(2).field("points").getValue().toString());
        assertEquals("7 10 1 1", searchResponse.getHits().getAt(3).field("points").getValue().toString());
        assertEquals("7 2 1 2", searchResponse.getHits().getAt(4).field("points").getValue().toString());
        assertEquals("7 1 1 1", searchResponse.getHits().getAt(5).field("points").getValue().toString());
        //} finally {
        //    stopCluster();
        //}
    }

    public void testDewey() throws Exception {
        //startCluster();
        //try {
        Settings settings = Settings.builder()
                .build();

        client().admin().indices().prepareCreate("test")
                .setSettings(settings)
                .setMapping(XContentFactory.jsonBuilder().startObject()
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
                        .endObject())
                .execute().actionGet();

        client().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

        String[] notations = new String[]{
                "10.10.1", "10.1.1", "2.11.0", "2.10.1", "2.1.1", "1.10.0", "1.0.0"
        };

        for (String notation : notations) {
            client().prepareIndex("test")
                    .setSource(XContentFactory.jsonBuilder().startObject()
                            .field("notation", notation)
                            .endObject()).execute().actionGet();
        }

        client().admin().indices().prepareRefresh().execute().actionGet();

        SearchResponse searchResponse = client().prepareSearch()
                .addStoredField("notation")
                .addSort("notation.sort", SortOrder.ASC)
                .setTrackTotalHits(true)
                .execute().actionGet();
        assertEquals(7L, searchResponse.getHits().getTotalHits().value);

        assertEquals("1.0.0", searchResponse.getHits().getAt(0).getFields().get("notation").getValue().toString());
        assertEquals("1.10.0", searchResponse.getHits().getAt(1).getFields().get("notation").getValue().toString());
        assertEquals("2.1.1", searchResponse.getHits().getAt(2).getFields().get("notation").getValue().toString());
        assertEquals("2.10.1", searchResponse.getHits().getAt(3).getFields().get("notation").getValue().toString());
        assertEquals("2.11.0", searchResponse.getHits().getAt(4).getFields().get("notation").getValue().toString());
        assertEquals("10.1.1", searchResponse.getHits().getAt(5).getFields().get("notation").getValue().toString());
        assertEquals("10.10.1", searchResponse.getHits().getAt(6).getFields().get("notation").getValue().toString());
        //} finally {
        //    stopCluster();
        //}
    }
}
