package org.xbib.opensearch.plugin.bundle.test.action.langdetect;

import org.opensearch.action.search.SearchResponse;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.plugins.Plugin;
import org.opensearch.test.OpenSearchSingleNodeTestCase;
import org.xbib.opensearch.plugin.bundle.action.langdetect.LangdetectRequestBuilder;
import org.xbib.opensearch.plugin.bundle.action.langdetect.LangdetectResponse;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.util.Collection;
import java.util.Collections;

/**
 * Language detection action test.
 */
public class LangDetectActionTests extends OpenSearchSingleNodeTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(BundlePlugin.class);
    }

    public void testLangDetectProfile() {
        LangdetectRequestBuilder langdetectRequestBuilder = new LangdetectRequestBuilder(client())
                        .setText("hello this is a test");
        LangdetectResponse response = langdetectRequestBuilder.execute().actionGet();
        assertFalse(response.getLanguages().isEmpty());
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertNull(response.getProfile());

        LangdetectRequestBuilder langdetectProfileRequestBuilder = new LangdetectRequestBuilder(client())
                        .setText("hello this is a test")
                        .setProfile("shorttext");
        response = langdetectProfileRequestBuilder.execute().actionGet();
        assertNotNull(response);
        assertFalse(response.getLanguages().isEmpty());
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertEquals("shorttext", response.getProfile());

        langdetectRequestBuilder = new LangdetectRequestBuilder(client())
                .setText("hello this is a test");
        response = langdetectRequestBuilder.execute().actionGet();
        assertNotNull(response);
        assertFalse(response.getLanguages().isEmpty());
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertNull(response.getProfile());
    }

    public void testSort() throws Exception {
        Settings settings = Settings.builder()
                .build();
        client().admin().indices().prepareCreate("test")
                .setSettings(settings)
                .addMapping("article",
                        XContentFactory.jsonBuilder().startObject()
                                .startObject("article")
                                .startObject("properties")
                                .startObject("content")
                                .field("type", "langdetect")
                                .array("languages", "de", "en", "fr")
                                .endObject()
                                .endObject()
                                .endObject()
                                .endObject())
                .execute().actionGet();

        client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

        client().prepareIndex("test", "article", "1")
                .setSource(XContentFactory.jsonBuilder().startObject()
                        .field("title", "Some title")
                        .field("content", "Oh, say can you see by the dawn`s early light, " +
                                "What so proudly we hailed at the twilight`s last gleaming?")
                        .endObject()).execute().actionGet();
        client().prepareIndex("test", "article", "2")
                .setSource(XContentFactory.jsonBuilder().startObject()
                        .field("title", "Ein Titel")
                        .field("content", "Einigkeit und Recht und Freiheit für das deutsche Vaterland!")
                        .endObject()).execute().actionGet();
        client().prepareIndex("test", "article", "3")
                .setSource(XContentFactory.jsonBuilder().startObject()
                        .field("title", "Un titre")
                        .field("content", "Allons enfants de la Patrie, Le jour de gloire est arrivé!")
                        .endObject()).execute().actionGet();

        client().admin().indices().prepareRefresh().execute().actionGet();

        SearchResponse searchResponse = client().prepareSearch()
                .setQuery(QueryBuilders.termQuery("content", "en"))
                .setTrackTotalHits(true)
                .execute().actionGet();
        assertEquals(1L, searchResponse.getHits().getTotalHits().value);
        assertEquals("Oh, say can you see by the dawn`s early light, What so proudly we hailed at the twilight`s last gleaming?",
                searchResponse.getHits().getAt(0).getSourceAsMap().get("content").toString());

        searchResponse = client().prepareSearch()
                .setQuery(QueryBuilders.termQuery("content", "de"))
                .setTrackTotalHits(true)
                .execute().actionGet();
        assertEquals(1L, searchResponse.getHits().getTotalHits().value);
        assertEquals("Einigkeit und Recht und Freiheit für das deutsche Vaterland!",
                searchResponse.getHits().getAt(0).getSourceAsMap().get("content").toString());

        searchResponse = client().prepareSearch()
                .setQuery(QueryBuilders.termQuery("content", "fr"))
                .setTrackTotalHits(true)
                .execute().actionGet();
        assertEquals(1L, searchResponse.getHits().getTotalHits().value);
        assertEquals("Allons enfants de la Patrie, Le jour de gloire est arrivé!",
                searchResponse.getHits().getAt(0).getSourceAsMap().get("content").toString());
    }
}
